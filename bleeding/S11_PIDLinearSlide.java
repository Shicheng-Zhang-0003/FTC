package org.firstinspires.ftc.teamcode;

import org.firstinspires.ftc.teamcode.util.HardwareNames;
import org.firstinspires.ftc.teamcode.util.MathUtils;
import org.firstinspires.ftc.teamcode.util.PIDController;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.util.ElapsedTime;

@TeleOp(name = "Test: PID Linear Slide", group = "S11: PID Control")
public class S11_PIDLinearSlide extends LinearOpMode {
    private DcMotorEx slideMotor;

    private PIDController pid;
    private final ElapsedTime loopTimer = new ElapsedTime();

    @Override
    public void runOpMode() {
        slideMotor = hardwareMap.get(DcMotorEx.class, HardwareNames.SLIDE_MOTOR);

        slideMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        slideMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        slideMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        slideMotor.setDirection(DcMotor.Direction.FORWARD);

        pid = new PIDController(
            RobotConstants.SLIDE_P,
            RobotConstants.SLIDE_I,
            RobotConstants.SLIDE_D
        );
        pid.setIntegralClamp(RobotConstants.SLIDE_INTEGRAL_CLAMP);
        pid.setOutputClamp(RobotConstants.SLIDE_OUTPUT_CLAMP);

        telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());
        telemetry.addData("Status", "PID Slide Ready");
        telemetry.addData("Instructions", "Use dpad up/down/left/right to set position");
        telemetry.update();

        int targetPosition = 0;

        waitForStart();

        loopTimer.reset();
        pid.reset();

        while (opModeIsActive()) {
            if (gamepad1.dpad_up) {
                targetPosition = RobotConstants.SLIDE_SCORE_HIGH;
            } else if (gamepad1.dpad_down) {
                targetPosition = RobotConstants.SLIDE_GROUND;
            } else if (gamepad1.dpad_left) {
                targetPosition = RobotConstants.SLIDE_LOW;
            } else if (gamepad1.dpad_right) {
                targetPosition = RobotConstants.SLIDE_MAX_SAFE;
            }

            targetPosition = MathUtils.clamp(
                targetPosition,
                RobotConstants.SLIDE_GROUND,
                RobotConstants.SLIDE_MAX_SAFE
            );

            int currentPosition = slideMotor.getCurrentPosition();
            double error = targetPosition - currentPosition;

            double dt = loopTimer.seconds();
            if (dt < 0.001) dt = 0.001;

            pid.setGains(
                RobotConstants.SLIDE_P,
                RobotConstants.SLIDE_I,
                RobotConstants.SLIDE_D
            );

            double power = pid.update(error, dt);
            slideMotor.setPower(power);

            loopTimer.reset();

            double pTerm = pid.pTerm;
            double iTerm = pid.iTerm;
            double dTerm = pid.dTerm;

            telemetry.addData("Target Position", targetPosition);
            telemetry.addData("Current Position", currentPosition);
            telemetry.addData("Error", error);
            telemetry.addData("Motor Power", "%.2f", power);
            telemetry.addLine();
            telemetry.addData("P Term", "%.2f", pTerm);
            telemetry.addData("I Term", "%.2f", iTerm);
            telemetry.addData("D Term", "%.2f", dTerm);
            telemetry.update();
        }
    }
}
