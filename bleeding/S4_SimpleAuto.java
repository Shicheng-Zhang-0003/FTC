package org.firstinspires.ftc.teamcode;
import org.firstinspires.ftc.teamcode.util.HardwareNames;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.util.ElapsedTime;
@Autonomous (name = "Auto: Simple Square", group = "S4: Auto")
public class S4_SimpleAuto extends LinearOpMode {
    private DcMotor frontLeft, frontRight, backLeft, backRight;
    private ElapsedTime runtime = new ElapsedTime ();
    @Override
    public void runOpMode () {
        //Initialize motors
        frontLeft = hardwareMap.get (DcMotor.class, HardwareNames.FRONT_LEFT);
        frontRight = hardwareMap.get (DcMotor.class, HardwareNames.FRONT_RIGHT);
        backLeft = hardwareMap.get (DcMotor.class, HardwareNames.BACK_LEFT);
        backRight = hardwareMap.get (DcMotor.class, HardwareNames.BACK_RIGHT); 
        //Set motor directions
        frontLeft.setDirection (DcMotorSimple.Direction.REVERSE);
        backLeft.setDirection (DcMotorSimple.Direction.REVERSE);
        frontRight.setDirection (DcMotorSimple.Direction.FORWARD);
        backRight.setDirection (DcMotorSimple.Direction.FORWARD);
        //Use encoders
        frontLeft.setMode (DcMotor.RunMode.RUN_USING_ENCODER);
        frontRight.setMode (DcMotor.RunMode.RUN_USING_ENCODER);
        backLeft.setMode (DcMotor.RunMode.RUN_USING_ENCODER);
        backRight.setMode (DcMotor.RunMode.RUN_USING_ENCODER);
        telemetry.addData("Status", "RDY");
        telemetry.update ();
        waitForStart ();
        if (isStopRequested ()) return;
        //Drive forward 24 inches
        driveForward (24, 0.5);
        //Strafe right 24 inches
        strafeRight (24, 0.5);
        //Drive backward 24 inches
        driveForward (-24, 0.5);
        //Strafe left 24 inches
        strafeRight (-24, 0.5);
        telemetry.addData ("Status", "Done!");
        telemetry.update ();
        sleep (3000);
    } private void driveForward (double inches, double power) {
        int targetTicks = (int) (inches * RobotConstants.DRIVE_TICKS_PER_INCH);
        frontLeft.setTargetPosition (frontLeft.getCurrentPosition () + targetTicks);
        frontRight.setTargetPosition (frontRight.getCurrentPosition () + targetTicks);
        backLeft.setTargetPosition (backLeft.getCurrentPosition () + targetTicks);
        backRight.setTargetPosition (backRight.getCurrentPosition () + targetTicks);
        frontLeft.setMode (DcMotor.RunMode.RUN_TO_POSITION);
        frontRight.setMode (DcMotor.RunMode.RUN_TO_POSITION);
        backLeft.setMode (DcMotor.RunMode.RUN_TO_POSITION);
        backRight.setMode (DcMotor.RunMode.RUN_TO_POSITION);
        frontLeft.setPower (power);
        frontRight.setPower (power);
        backLeft.setPower (power);
        backRight.setPower (power);
        runtime.reset();
        while ((opModeIsActive ()) && (runtime.seconds () < 5.0) && ((frontLeft.isBusy ()) || (frontRight.isBusy ()) || (backLeft.isBusy ()) || (backRight.isBusy ()))) {
            telemetry.addData ("Driving", "Forward %.1f inches", inches);
            telemetry.update ();
        } stopMotors ();
    } private void strafeRight(double inches, double power) {
        int targetTicks = (int) (inches * RobotConstants.DRIVE_TICKS_PER_INCH);
        frontLeft.setTargetPosition (frontLeft.getCurrentPosition () + targetTicks);
        frontRight.setTargetPosition (frontRight.getCurrentPosition () - targetTicks);
        backLeft.setTargetPosition (backLeft.getCurrentPosition () - targetTicks);
        backRight.setTargetPosition (backRight.getCurrentPosition () + targetTicks); 
        frontLeft.setMode (DcMotor.RunMode.RUN_TO_POSITION);
        frontRight.setMode (DcMotor.RunMode.RUN_TO_POSITION);
        backLeft.setMode (DcMotor.RunMode.RUN_TO_POSITION);
        backRight.setMode (DcMotor.RunMode.RUN_TO_POSITION);
        frontLeft.setPower (power);
        frontRight.setPower (power);
        backLeft.setPower (power);
        backRight.setPower (power);
        runtime.reset ();
        while ((opModeIsActive ()) && (runtime.seconds () < 5.0) && ((frontLeft.isBusy ()) || (frontRight.isBusy ()) || (backLeft.isBusy ()) || (backRight.isBusy ()))) {
            telemetry.addData ("Strafing", "Right %.1f inches", inches);
            telemetry.update ();
        } stopMotors ();
    } private void stopMotors () {
        frontLeft.setPower (0);
        frontRight.setPower (0);
        backLeft.setPower (0);
        backRight.setPower (0);
        frontLeft.setMode (DcMotor.RunMode.RUN_USING_ENCODER);
        frontRight.setMode (DcMotor.RunMode.RUN_USING_ENCODER);
        backLeft.setMode (DcMotor.RunMode.RUN_USING_ENCODER);
        backRight.setMode (DcMotor.RunMode.RUN_USING_ENCODER);
    }
}
