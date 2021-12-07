package org.firstinspires.ftc.teamcode.Autonomous;

import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.hardware.bosch.JustLoggingAccelerationIntegrator;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcontroller.external.samples.HardwarePushbot;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import org.firstinspires.ftc.teamcode.Hardware.HWDriveTrain;

import java.util.Locale;


@Autonomous(name = "autonomousDrive")
public class AutonomousDrive extends LinearOpMode {
    HWDriveTrain hwDriveTrain;

    BNO055IMU imu;

    Orientation angles;

    public ElapsedTime runtime = new ElapsedTime();

    static final double COUNTS_PER_MOTOR_REV = 1440;    // eg: TETRIX Motor Encoder
    static final double DRIVE_GEAR_REDUCTION = 2.0;     // This is < 1.0 if geared UP
    static final double WHEEL_DIAMETER_INCHES = 4.0;     // For figuring circumference
    static final double COUNTS_PER_INCH = (COUNTS_PER_MOTOR_REV * DRIVE_GEAR_REDUCTION) /
            (WHEEL_DIAMETER_INCHES * 3.1415);
    static final double DRIVE_SPEED = 0.6;
    static final double TURN_SPEED = 0.5;


    static final double TURNING_P = 0.015;

    @Override
    public void runOpMode() {

        hwDriveTrain = new HWDriveTrain();

        hwDriveTrain.init(this.hardwareMap);

        telemetry.addData("Status", "Resetting Encoders");    //
        telemetry.update();

        hwDriveTrain.leftFront.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        hwDriveTrain.rightFront.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        hwDriveTrain.leftBack.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        hwDriveTrain.rightBack.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);


        hwDriveTrain.leftFront.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        hwDriveTrain.rightFront.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        hwDriveTrain.leftBack.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        hwDriveTrain.rightBack.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        BNO055IMU.Parameters parameters = new BNO055IMU.Parameters();
        parameters.angleUnit           = BNO055IMU.AngleUnit.DEGREES;
        parameters.accelUnit           = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC;
        parameters.calibrationDataFile = "BNO055IMUCalibration.json"; // see the calibration sample opmode
        parameters.loggingEnabled      = true;
        parameters.loggingTag          = "IMU";
        parameters.accelerationIntegrationAlgorithm = new JustLoggingAccelerationIntegrator();

        imu = hardwareMap.get(BNO055IMU.class, "imu");
        imu.initialize(parameters);

        telemetry.addData("Path0", "Starting at %7d :%7d",
                hwDriveTrain.leftFront.getCurrentPosition(),
                hwDriveTrain.rightFront.getCurrentPosition(),
                hwDriveTrain.leftBack.getCurrentPosition(),
                hwDriveTrain.rightBack.getCurrentPosition());

        telemetry.update();

        waitForStart();

        turnToPosition(90);




    }

    public void turnToPosition(double target) {

        double current_angle = getHeading();

        target = current_angle + target;

        double error = target - current_angle;

        int valid_cycles = 0;

        while( error > 1 && valid_cycles > 10) {
            current_angle = getHeading();
            error = target - current_angle;
            double power = error * TURNING_P;

            hwDriveTrain.leftBack.setPower(power);
            hwDriveTrain.leftFront.setPower(power);
            hwDriveTrain.rightBack.setPower(-power);
            hwDriveTrain.rightFront.setPower(-power);

            telemetry.addData("Heading: ", current_angle);
            telemetry.addData("Error: ", error);
            telemetry.addData("Power: ", power);
            telemetry.update();

            if (error < 1) {
                valid_cycles++;
            }
            else {
                valid_cycles = 0;
            }
        }

        hwDriveTrain.leftBack.setPower(0);
        hwDriveTrain.leftFront.setPower(0);
        hwDriveTrain.rightBack.setPower(0);
        hwDriveTrain.rightFront.setPower(0);

        telemetry.addData("Finished Turning", "Printing final values");
        telemetry.addData("Heading: ", current_angle);
        telemetry.addData("Error: ", error);
        telemetry.update();
    }


    public void encoderDrive(double speed,
                             double leftInches, double rightInches,
                             double timeoutS) {
        int newLeftFrontTarget;
        int newRightFrontTarget;
        int newLeftBackTarget;
        int newRightBackTarget;

        // Ensure that the opmode is still active
        if (opModeIsActive()) {
/*
            // Determine new target position, and pass to motor controller
            newLeftFrontTarget = hwDriveTrain.leftFront.getCurrentPosition() + (int)(leftInches * COUNTS_PER_INCH);
            newRightFrontTarget = hwDriveTrain.rightFront.getCurrentPosition() + (int)(rightInches * COUNTS_PER_INCH);
            newLeftBackTarget = hwDriveTrain.leftBack.getCurrentPosition() + (int)(rightInches * COUNTS_PER_INCH);
            newRightBackTarget = hwDriveTrain.rightBack.getCurrentPosition() + (int)(rightInches * COUNTS_PER_INCH);

            hwDriveTrain.leftFront.setTargetPosition(newLeftFrontTarget);
            hwDriveTrain.rightFront.setTargetPosition(newRightFrontTarget);
            hwDriveTrain.leftBack.setTargetPosition(newLeftBackTarget);
            hwDriveTrain.rightBack.setTargetPosition(newRightBackTarget);

            // Turn On RUN_TO_POSITION
            hwDriveTrain.leftDrive.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            hwDriveTrain.rightDrive.setMode(DcMotor.RunMode.RUN_TO_POSITION);

            // reset the timeout time and start motion.
            runtime.reset();
            hwDriveTrain.leftDrive.setPower(Math.abs(speed));
            hwDriveTrain.rightDrive.setPower(Math.abs(speed));

            // keep looping while we are still active, and there is time left, and both motors are running.
            // Note: We use (isBusy() && isBusy()) in the loop test, which means that when EITHER motor hits
            // its target position, the motion will stop.  This is "safer" in the event that the robot will
            // always end the motion as soon as possible.
            // However, if you require that BOTH motors have finished their moves before the robot continues
            // onto the next step, use (isBusy() || isBusy()) in the loop test.
            while (opModeIsActive() &&
                    (runtime.seconds() < timeoutS) &&
                    (hwDriveTrain.leftDrive.isBusy() && hwDriveTrain.rightDrive.isBusy())) {

                // Display it for the driver.
                telemetry.addData("Path1",  "Running to %7d :%7d", newLeftTarget,  newRightTarget);
                telemetry.addData("Path2",  "Running at %7d :%7d",
                        hwDriveTrain.leftDrive.getCurrentPosition(),
                        hwDriveTrain.rightDrive.getCurrentPosition());
                telemetry.update();
            }

            // Stop all motion;
            hwDriveTrain.leftDrive.setPower(0);
            hwDriveTrain.rightDrive.setPower(0);

            // Turn off RUN_TO_POSITION
            hwDriveTrain.leftDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            hwDriveTrain.rightDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
*/
            //  sleep(250);   // optional pause after each move
        }
    }

  public double getHeading() {
      angles   = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);
      return AngleUnit.DEGREES.fromUnit(angles.angleUnit, angles.firstAngle);
  }


}
