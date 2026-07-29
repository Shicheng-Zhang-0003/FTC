package org.firstinspires.ftc.teamcode;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import org.firstinspires.ftc.teamcode.util.HardwareNames;
import org.firstinspires.ftc.teamcode.util.MathUtils;
@TeleOp(name = "Tank Drive", group = "TeleOp")
public class s1_tank_drive_shell extends LinearOpMode {
    // Declare Motors (Ensure these match your DS Configuration names)
    private DcMotor leftMotor;
    private DcMotor rightMotor;
    @Override
    public void runOpMode () {
        //Initialize hardware stack
        leftMotor = hardwareMap.get (DcMotor.class, HardwareNames.LEFT_MOTOR);
        rightMotor = hardwareMap.get (DcMotor.class, HardwareNames.RIGHT_MOTOR);
        //Reverse one side so the robot drives forward when both sticks are pushed up
        leftMotor.setDirection (DcMotorSimple.Direction.REVERSE);
        rightMotor.setDirection (DcMotorSimple.Direction.FORWARD);
        telemetry.addData ("Status", "Initialized");
        telemetry.update ();
        waitForStart ();
        while (opModeIsActive ()) {
            //Tank Drive Controls: stick controls the drive wheel on the respective side
            double leftPower = MathUtils.deadband (-gamepad1.left_stick_y, RobotConstants.DRIVE_STICK_DEADBAND);
            double rightPower = MathUtils.deadband (-gamepad1.right_stick_y, RobotConstants.DRIVE_STICK_DEADBAND);
            //Send power to motors
            leftMotor.setPower (leftPower);
            rightMotor.setPower (rightPower);
            //Telemetry for debugging
            telemetry.addData ("Left Power", leftPower);
            telemetry.addData ("Right Power", rightPower);
            telemetry.update ();
        }
    }
}
