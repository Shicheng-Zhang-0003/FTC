package org.firstinspires.ftc.teamcode;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.util.HardwareNames;

import java.util.ArrayList;

@TeleOp(name = "Subsystem Test", group = "Validation")
public class SubsystemTestTeleOp extends LinearOpMode {

    private S12_IntegratedMechanism mechanism;
    private HangSubsystem hang;

    private boolean mechanismReady = false;
    private boolean hangReady = false;

    private final ArrayList<String> missing = new ArrayList<>();
    private final ElapsedTime loopTimer = new ElapsedTime();

    @Override
    public void runOpMode() {
        DcMotorEx slide = safeGet(DcMotorEx.class, HardwareNames.SLIDE_MOTOR);
        Servo intakeServo = safeGet(Servo.class, HardwareNames.INTAKE_SERVO);
        DigitalChannel gamePieceSensor = safeGet(DigitalChannel.class, HardwareNames.GAME_PIECE_SENSOR);
        DigitalChannel bottomLimit = safeGet(DigitalChannel.class, HardwareNames.BOTTOM_LIMIT);

        DcMotor winch = safeGet(DcMotor.class, HardwareNames.WINCH_MOTOR);
        DigitalChannel topLimit = safeGet(DigitalChannel.class, HardwareNames.TOP_LIMIT);

        if (slide == null) missing.add(HardwareNames.SLIDE_MOTOR);
        if (intakeServo == null) missing.add(HardwareNames.INTAKE_SERVO);
        if (gamePieceSensor == null) missing.add(HardwareNames.GAME_PIECE_SENSOR);
        if (bottomLimit == null) missing.add(HardwareNames.BOTTOM_LIMIT);
        if (winch == null) missing.add(HardwareNames.WINCH_MOTOR);
        if (topLimit == null) missing.add(HardwareNames.TOP_LIMIT);

        if (slide != null && intakeServo != null && gamePieceSensor != null && bottomLimit != null) {
            mechanism = new S12_IntegratedMechanism();
            mechanism.init(slide, intakeServo, gamePieceSensor, bottomLimit);
            mechanismReady = true;
        }

        if (winch != null && topLimit != null) {
            hang = new HangSubsystem();
            hang.init(winch, topLimit, bottomLimit);
            hangReady = true;
        }

        telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());
        telemetry.addData("Status", "Subsystem Test Ready");
        telemetry.addData("Mechanism Ready", mechanismReady);
        telemetry.addData("Hang Ready", hangReady);
        telemetry.addData("Missing Devices", missing.isEmpty() ? "None" : missing.toString());
        telemetry.update();

        waitForStart();

        if (mechanismReady) {
            mechanism.resetControllers();
        }

        loopTimer.reset();

        while (opModeIsActive()) {
            double dt = loopTimer.seconds();
            if (dt < 0.001) dt = 0.001;

            if (mechanismReady) {
                if (gamepad2.a) {
                    mechanism.startIntakeSequence();
                } else if (gamepad2.b) {
                    mechanism.startScoreSequence();
                } else if (gamepad2.x) {
                    mechanism.emergencyStop();
                }

                TelemetryPacket packet = new TelemetryPacket();
                mechanism.update(packet);
                FtcDashboard.getInstance().sendTelemetryPacket(packet);

                telemetry.addData("Mechanism State", mechanism.getState());
            } else {
                telemetry.addData("Mechanism State", "Not initialized");
            }

            if (hangReady) {
                hang.update(dt, gamepad2.y, gamepad2.left_bumper);
                telemetry.addData("Hang Top Limit", hang.isTopPressed());
                telemetry.addData("Winch Power", hang.getPower());
            } else {
                telemetry.addData("Hang State", "Not initialized");
            }

            telemetry.addData("Missing Devices", missing.isEmpty() ? "None" : missing.toString());
            telemetry.update();

            loopTimer.reset();
        }
    }

    private <T> T safeGet(Class<? extends T> type, String name) {
        try {
            return hardwareMap.get(type, name);
        } catch (RuntimeException e) {
            return null;
        }
    }
}
