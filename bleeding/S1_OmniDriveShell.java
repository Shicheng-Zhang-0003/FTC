package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import org.firstinspires.ftc.teamcode.util.HardwareNames;
import org.firstinspires.ftc.teamcode.util.MathUtils;
@Disabled
@TeleOp(name = "Omni Drive", group = "S1: Drive")
public class S1_OmniDriveShell extends LinearOpMode {
    //Declare Motors to Driver Hub
    private DcMotor leftMotor;
    private DcMotor rightMotor;
    @Override
    public void runOpMode() {
        //Initialize hardware stack
        leftMotor = hardwareMap.get(DcMotor.class, HardwareNames.LEFT_MOTOR);
        rightMotor = hardwareMap.get(DcMotor.class, HardwareNames.RIGHT_MOTOR);
        //Reverse one side so the robot drives linearly with both sticks in same direction
        leftMotor.setDirection(DcMotorSimple.Direction.REVERSE);
        rightMotor.setDirection(DcMotorSimple.Direction.FORWARD);
        telemetry.addData("Status", "Initialized");
        telemetry.update();
        waitForStart();
        while (opModeIsActive()) {
            //Omni Drive 2 motor setup:
            //Tank Drive Style with Omnidirectional movement paths available
            double leftPower = MathUtils.deadband(-gamepad1.left_stick_y, RobotConstants.DRIVE_STICK_DEADBAND);
            double rightPower = MathUtils.deadband(-gamepad1.right_stick_y, RobotConstants.DRIVE_STICK_DEADBAND);
            //Send power to motors
            leftMotor.setPower(leftPower);
            rightMotor.setPower(rightPower);
            //Telemetry, drivetrain debugging
            telemetry.addData("Left Power", leftPower);
            telemetry.addData("Right Power", rightPower);
            telemetry.update();
        }
    }
}
