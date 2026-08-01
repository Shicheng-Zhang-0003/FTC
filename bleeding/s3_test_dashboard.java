package org.firstinspires.ftc.teamcode;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.util.HardwareNames;

@Disabled
@TeleOp(name = "Dashboard Testing", group = "Tests")
public class s3_test_dashboard extends LinearOpMode {
    @Override
    public void runOpMode() {
        FtcDashboard dashboard = FtcDashboard.getInstance();
        telemetry = new MultipleTelemetry(telemetry, dashboard.getTelemetry());

        Servo testClaw = hardwareMap.get(Servo.class, HardwareNames.TEST_CLAW);

        telemetry.addData("Status", "Init Finished. Connect Dashboard to tune.");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            if (gamepad1.a) {
                testClaw.setPosition(RobotConstants.SERVO_CLAW_OPEN);
            } else if (gamepad1.b) {
                testClaw.setPosition(RobotConstants.SERVO_CLAW_CLOSED);
            }

            telemetry.addData("Claw Target Open", RobotConstants.SERVO_CLAW_OPEN);
            telemetry.addData("Claw Target Closed", RobotConstants.SERVO_CLAW_CLOSED);
            telemetry.addData("Actual Servo Pos", testClaw.getPosition());
            telemetry.update();
        }
    }
}
