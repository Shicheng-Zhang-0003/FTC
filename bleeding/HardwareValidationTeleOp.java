package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.teamcode.util.HardwareNames;
import org.firstinspires.ftc.teamcode.util.MathUtils;

import java.util.ArrayList;

@TeleOp(name = "Hardware Validation", group = "Validation")
public class HardwareValidationTeleOp extends LinearOpMode {

    private DcMotorEx frontLeft;
    private DcMotorEx frontRight;
    private DcMotorEx backLeft;
    private DcMotorEx backRight;

    private DcMotorEx slideMotor;
    private DcMotor winchMotor;

    private Servo intakeServo;

    private DigitalChannel gamePieceSensor;
    private DigitalChannel bottomLimit;
    private DigitalChannel topLimit;

    private IMU imu;
    private WebcamName webcam;

    private final ArrayList<String> missing = new ArrayList<>();

    @Override
    public void runOpMode() {
        frontLeft = safeGet(DcMotorEx.class, HardwareNames.FRONT_LEFT);
        frontRight = safeGet(DcMotorEx.class, HardwareNames.FRONT_RIGHT);
        backLeft = safeGet(DcMotorEx.class, HardwareNames.BACK_LEFT);
        backRight = safeGet(DcMotorEx.class, HardwareNames.BACK_RIGHT);

        slideMotor = safeGet(DcMotorEx.class, HardwareNames.SLIDE_MOTOR);
        winchMotor = safeGet(DcMotor.class, HardwareNames.WINCH_MOTOR);

        intakeServo = safeGet(Servo.class, HardwareNames.INTAKE_SERVO);

        gamePieceSensor = safeGet(DigitalChannel.class, HardwareNames.GAME_PIECE_SENSOR);
        bottomLimit = safeGet(DigitalChannel.class, HardwareNames.BOTTOM_LIMIT);
        topLimit = safeGet(DigitalChannel.class, HardwareNames.TOP_LIMIT);

        imu = safeGet(IMU.class, HardwareNames.IMU);
        webcam = safeGet(WebcamName.class, HardwareNames.WEBCAM);

        track(frontLeft, HardwareNames.FRONT_LEFT);
        track(frontRight, HardwareNames.FRONT_RIGHT);
        track(backLeft, HardwareNames.BACK_LEFT);
        track(backRight, HardwareNames.BACK_RIGHT);

        track(slideMotor, HardwareNames.SLIDE_MOTOR);
        track(winchMotor, HardwareNames.WINCH_MOTOR);

        track(intakeServo, HardwareNames.INTAKE_SERVO);

        track(gamePieceSensor, HardwareNames.GAME_PIECE_SENSOR);
        track(bottomLimit, HardwareNames.BOTTOM_LIMIT);
        track(topLimit, HardwareNames.TOP_LIMIT);

        track(imu, HardwareNames.IMU);
        track(webcam, HardwareNames.WEBCAM);

        if (frontLeft != null) frontLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        if (frontRight != null) frontRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        if (backLeft != null) backLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        if (backRight != null) backRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        if (slideMotor != null) {
            slideMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            slideMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        }

        if (winchMotor != null) {
            winchMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        }

        telemetry.addData("Status", "Hardware Validation Ready");
        telemetry.addData("Missing Device Count", missing.size());
        telemetry.addData("Missing Devices", missing.isEmpty() ? "None" : missing.toString());
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            telemetry.addData("Missing Devices", missing.isEmpty() ? "None" : missing.toString());

            // -------------------
            // Drive test
            // -------------------
            if (frontLeft != null && frontRight != null && backLeft != null && backRight != null) {
                double y = MathUtils.deadband(-gamepad1.left_stick_y, 0.1) * 0.5;
                double x = MathUtils.deadband(gamepad1.left_stick_x, 0.1) * 0.5;
                double rx = MathUtils.deadband(gamepad1.right_stick_x, 0.1) * 0.5;

                double denominator = Math.max(Math.abs(y) + Math.abs(x) + Math.abs(rx), 1);

                frontLeft.setPower((y + x + rx) / denominator);
                frontRight.setPower((y - x - rx) / denominator);
                backLeft.setPower((y - x + rx) / denominator);
                backRight.setPower((y + x - rx) / denominator);

                telemetry.addLine("Drive: present");
            } else {
                telemetry.addLine("Drive: missing one or more motors");
            }

            // -------------------
            // Slide test
            // -------------------
            if (slideMotor != null) {
                if (gamepad2.dpad_up) {
                    slideMotor.setPower(0.2);
                } else if (gamepad2.dpad_down) {
                    slideMotor.setPower(-0.2);
                } else {
                    slideMotor.setPower(0);
                }

                telemetry.addData("Slide Present", true);
                telemetry.addData("Slide Encoder", slideMotor.getCurrentPosition());
            } else {
                telemetry.addData("Slide Present", false);
            }

            // -------------------
            // Winch test
            // -------------------
            if (winchMotor != null) {
                boolean topPressed = false;
                if (topLimit != null) {
                    boolean raw = topLimit.getState();
                    topPressed = RobotConstants.SENSOR_LIMIT_SWITCH_INVERTED ? !raw : raw;
                }

                if (gamepad2.y && !topPressed) {
                    winchMotor.setPower(0.2);
                } else if (gamepad2.x) {
                    winchMotor.setPower(-0.2);
                } else {
                    winchMotor.setPower(0);
                }

                telemetry.addData("Winch Present", true);
                telemetry.addData("Top Limit Pressed", topPressed);
            } else {
                telemetry.addData("Winch Present", false);
            }

            // -------------------
            // Servo test
            // -------------------
            if (intakeServo != null) {
                if (gamepad2.a) {
                    intakeServo.setPosition(RobotConstants.SERVO_INTAKE_OPEN);
                } else if (gamepad2.b) {
                    intakeServo.setPosition(RobotConstants.SERVO_INTAKE_CLOSED);
                }

                telemetry.addData("Intake Servo Present", true);
                telemetry.addData("Intake Servo Position", intakeServo.getPosition());
            } else {
                telemetry.addData("Intake Servo Present", false);
            }

            // -------------------
            // Sensor test
            // -------------------
            if (gamePieceSensor != null) {
                telemetry.addData("Game Piece Sensor Raw", gamePieceSensor.getState());
            }

            if (bottomLimit != null) {
                telemetry.addData("Bottom Limit Raw", bottomLimit.getState());
            }

            if (topLimit != null) {
                telemetry.addData("Top Limit Raw", topLimit.getState());
            }

            telemetry.addData("IMU Present", imu != null);
            telemetry.addData("Webcam Present", webcam != null);

            telemetry.update();
        }
    }

    private void track(Object device, String name) {
        if (device == null) {
            missing.add(name);
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
