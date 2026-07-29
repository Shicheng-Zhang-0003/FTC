package org.firstinspires.ftc.teamcode;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import org.firstinspires.ftc.teamcode.util.HardwareNames;
import org.firstinspires.ftc.teamcode.util.MathUtils;
@TeleOp(name = "Mecanum Stage 1", group = "TeleOp")
public class s1_mechanum_drive_shell extends LinearOpMode {
    //Declare Motors to Driver Hub Configs (Cross Chceck and reference beforehand)
    private DcMotor frontLeft;
    private DcMotor frontRight;
    private DcMotor backLeft;
    private DcMotor backRight;
    @Override
    public void runOpMode() {
        //Initialize hardware
        frontLeft = hardwareMap.get (DcMotor.class, HardwareNames.FRONT_LEFT);
        frontRight = hardwareMap.get (DcMotor.class, HardwareNames.FRONT_RIGHT);
        backLeft = hardwareMap.get (DcMotor.class, HardwareNames.BACK_LEFT);
        backRight = hardwareMap.get (DcMotor.class, HardwareNames.BACK_RIGHT);
        //Reverse the left side so the robot drives forward
        frontLeft.setDirection (DcMotorSimple.Direction.REVERSE);
        backLeft.setDirection (DcMotorSimple.Direction.REVERSE);
        frontRight.setDirection (DcMotorSimple.Direction.FORWARD);
        backRight.setDirection (DcMotorSimple.Direction.FORWARD);
        telemetry.addData ("Status", "Initialized");
        telemetry.update ();
        waitForStart ();
        while (opModeIsActive ()) {
            //Mecanum Drive Controls
            //Left Stick = WS, Strafe. Right Stick, Rotational
            double y = MathUtils.deadband (-gamepad1.left_stick_y, RobotConstants.DRIVE_STICK_DEADBAND);
            double x = MathUtils.deadband (gamepad1.left_stick_x, RobotConstants.DRIVE_STICK_DEADBAND) * RobotConstants.DRIVE_MECANUM_STRAFE_COMPENSATION;
            double rx = MathUtils.deadband (gamepad1.right_stick_x, RobotConstants.DRIVE_STICK_DEADBAND);
            //Calculate wheel powers using standard mecanum kinematics
            double denominator = Math.max (Math.abs (y) + Math.abs (x) + Math.abs (rx), 1);
            double frontLeftPower = (y + x + rx) / denominator;
            double frontRightPower = (y - x - rx) / denominator;
            double backLeftPower = (y - x + rx) / denominator;
            double backRightPower = (y + x - rx) / denominator;
            //Send power to motors
            frontLeft.setPower (frontLeftPower);
            frontRight.setPower (frontRightPower);
            backLeft.setPower (backLeftPower);
            backRight.setPower (backRightPower);
            //Telemetry for debugging
            telemetry.addData ("Front Left", frontLeftPower);
            telemetry.addData ("Front Right", frontRightPower);
            telemetry.addData ("Back Left", backLeftPower);
            telemetry.addData ("Back Right", backRightPower);
            telemetry.update ();
        }
    }
}
