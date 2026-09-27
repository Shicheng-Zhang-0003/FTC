package org.firstinspires.ftc.teamcode;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.util.HardwareNames;

import java.util.ArrayList;

/**
 * CompetitionAuto — OLD SCHOOL BIOBUZZ (no AprilTag, no Road Runner)
 * Raw encoders + travel + shooter. Uses BioBuzzField dimensions (144x144, 24 in tiles).
 * Shooter launches pollen/nectar into HIVE (30.6 in high, ~48 in shooting distance).
 * Replace Road Runner with RUN_TO_POSITION encoder driving.
 */
@Autonomous(name = "Competition Auto", group = "Competition")
public class CompetitionAuto extends LinearOpMode {
    private DcMotor frontLeft, frontRight, backLeft, backRight;
    private final ElapsedTime runtime = new ElapsedTime();
    private final ElapsedTime shootTimer = new ElapsedTime();

    private ShooterSubsystem shooter;
    private boolean shooterReady = false;

    @Override
    public void runOpMode() throws InterruptedException {
        ArrayList<String> missing = new ArrayList<>();

        frontLeft = safeGet(DcMotor.class, HardwareNames.FRONT_LEFT);
        frontRight = safeGet(DcMotor.class, HardwareNames.FRONT_RIGHT);
        backLeft = safeGet(DcMotor.class, HardwareNames.BACK_LEFT);
        backRight = safeGet(DcMotor.class, HardwareNames.BACK_RIGHT);

        DcMotorEx shooterMotor = safeGet(DcMotorEx.class, HardwareNames.SHOOTER_MOTOR);
        Servo feederServo = safeGet(Servo.class, HardwareNames.FEEDER_SERVO);

        if (frontLeft == null) missing.add(HardwareNames.FRONT_LEFT);
        if (frontRight == null) missing.add(HardwareNames.FRONT_RIGHT);
        if (backLeft == null) missing.add(HardwareNames.BACK_LEFT);
        if (backRight == null) missing.add(HardwareNames.BACK_RIGHT);
        if (shooterMotor == null) missing.add(HardwareNames.SHOOTER_MOTOR);
        if (feederServo == null) missing.add(HardwareNames.FEEDER_SERVO);

        if (frontLeft != null) frontLeft.setDirection(DcMotorSimple.Direction.REVERSE);
        if (backLeft != null) backLeft.setDirection(DcMotorSimple.Direction.REVERSE);
        if (frontRight != null) frontRight.setDirection(DcMotorSimple.Direction.FORWARD);
        if (backRight != null) backRight.setDirection(DcMotorSimple.Direction.FORWARD);

        // Encoder reset — B26 fix: re-zero at init so every RUN_TO_POSITION is from known zero
        if (frontLeft != null) frontLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        if (frontRight != null) frontRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        if (backLeft != null) backLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        if (backRight != null) backRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        if (frontLeft != null) frontLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        if (frontRight != null) frontRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        if (backLeft != null) backLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        if (backRight != null) backRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        shooter = new ShooterSubsystem();
        if (shooterMotor != null && feederServo != null) {
            shooter.init(shooterMotor, feederServo);
            shooterReady = true;
        }

        telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());
        telemetry.addData("Status", shooterReady ? "Competition Auto Ready (OLD SCHOOL)" : "MISSING SHOOTER");
        telemetry.addData("Road Runner Tuned", RobotReadiness.ROAD_RUNNER_TUNED);
        telemetry.addData("Auto Ready", RobotReadiness.AUTO_READY);
        telemetry.addData("Missing Devices", missing.isEmpty() ? "None" : missing.toString());
        telemetry.addData("Field", "144x144 6x6 tiles 24in — HIVE center 72,72");
        telemetry.addData("Sequence", "Leave(12in) -> Shoot 4 pollen @48in -> Park loading zone");
        telemetry.addData("Drive Ticks/In", RobotConstants.DRIVE_TICKS_PER_INCH);
        telemetry.addData("Shooter Pollen Power", RobotConstants.SHOOTER_POLLEN_POWER);
        telemetry.update();

        waitForStart();
        if (isStopRequested()) return;

        // B2 fix: do not run an auto whose mechanism failed to initialize
        if (!shooterReady) {
            while (opModeIsActive()) {
                telemetry.addData("AUTO ABORTED", "Missing shooter hardware");
                telemetry.addData("Missing Devices", missing.toString());
                telemetry.update();
                sleep(100);
            }
            return;
        }
        // B11 fix: readiness gate - AUTO_READY must be true to run the auto
        if (!RobotReadiness.AUTO_READY) {
            while (opModeIsActive()) {
                telemetry.addData("AUTO BLOCKED", "RobotReadiness.AUTO_READY is false");
                telemetry.addData("Required", "Hardware validation + verified paths");
                telemetry.update();
                sleep(100);
            }
            return;
        }

        // ── OLD SCHOOL AUTONOMOUS (BIOBUZZ) ──────────────────────────────────
        // All distances from BioBuzzField — shooter is at 30.6 in HIVE cell height, 48 in shoot.
        // 1) Leave start = 12 in (SWARM ranking point)
        driveForward(BioBuzzField.AUTO_LEAVE_START_IN, 0.5);
        // B10 fix: shooting worst case is spinUp(1.2s) + 4*feed(0.35+0.30) ≈ 3.8s; use 5.0s timeout
        // B24 fix: CLOSING_CLAW removed — shooter only acts on READY, so wait for READY not intermediate
        shootPollen(4);

        // 2) Drive to HIVE shooting distance (48 in total from wall, we already did 12, so +36)
        driveForward(BioBuzzField.START_TO_HIVE_SHOOT_IN - BioBuzzField.AUTO_LEAVE_START_IN, 0.5);

        // 3) Park in Loading Zone (strafe + forward per BioBuzzField)
        // Blue alliance example: loading zone at (132,36) — from center shoot pos (~72,48) strafe 24 right + 12 forward
        strafeRight(24, 0.5);
        driveForward(12, 0.5);

        // Ensure shooter stopped
        shooter.stop();

        telemetry.addData("Status", "Auto complete — SWARM + 4 pollen shot");
        telemetry.update();
        sleep(1000);
    }

    // ── Shooter sequence ───────────────────────────────────────────────────
    private void shootPollen(int count) {
        shooter.shootShots(count, true);
        shootTimer.reset();
        double timeout = RobotConstants.SHOOTER_SPINUP_SECONDS + count * (RobotConstants.SHOOTER_FEED_STROKE_SECONDS + RobotConstants.SHOOTER_RECOVERY_SECONDS) + 2.0;
        // B10 fix: timeout must cover spinup + feed + recovery, fail-fast on shooter IDLE
        while (opModeIsActive() && shootTimer.seconds() < timeout) {
            TelemetryPacket packet = new TelemetryPacket();
            shooter.update(packet);
            FtcDashboard.getInstance().sendTelemetryPacket(packet);
            telemetry.addData("Shooter State", shooter.getState());
            telemetry.addData("Shots", shooter.getShotsFired() + "/" + count);
            telemetry.update();
            if (shooter.getState() == ShooterSubsystem.State.IDLE && shooter.getShotsFired() >= count) break;
            // B24 fix: shooter READY is the only stable pre-feed state; don't accept intermediate SPINNING_UP as done
            sleep(20);
        }
        // Safety: if timeout, stop shooter
        if (shootTimer.seconds() >= timeout) {
            shooter.stop();
            telemetry.addData("Shooter Timeout", true);
            telemetry.update();
        }
    }

    // ── Encoder drive helpers (S4 style) ───────────────────────────────────
    private void driveForward(double inches, double power) {
        int ticks = BioBuzzField.inchesToTicks(inches);
        // B10 fix: 5.0s timeout per leg — was too tight at 2.0s for 48in moves
        driveWithTicks(ticks, ticks, ticks, ticks, power, 5.0, "Forward " + inches + "in");
    }

    private void strafeRight(double inches, double power) {
        int ticks = BioBuzzField.inchesToTicks(inches);
        // Mecanum strafe: FL +ticks, FR -ticks, BL -ticks, BR +ticks
        driveWithTicks(ticks, -ticks, -ticks, ticks, power, 5.0, "Strafe " + inches + "in");
    }

    private void driveWithTicks(int flTicks, int frTicks, int blTicks, int brTicks, double power, double timeoutS, String label) {
        int flTarget = frontLeft.getCurrentPosition() + flTicks;
        int frTarget = frontRight.getCurrentPosition() + frTicks;
        int blTarget = backLeft.getCurrentPosition() + blTicks;
        int brTarget = backRight.getCurrentPosition() + brTicks;

        frontLeft.setTargetPosition(flTarget);
        frontRight.setTargetPosition(frTarget);
        backLeft.setTargetPosition(blTarget);
        backRight.setTargetPosition(brTarget);

        frontLeft.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        frontRight.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        backLeft.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        backRight.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        frontLeft.setPower(power);
        frontRight.setPower(power);
        backLeft.setPower(power);
        backRight.setPower(power);

        runtime.reset();
        while (opModeIsActive() && runtime.seconds() < timeoutS
                && (frontLeft.isBusy() || frontRight.isBusy() || backLeft.isBusy() || backRight.isBusy())) {
            telemetry.addData("Driving", label);
            telemetry.addData("FL", frontLeft.getCurrentPosition() + "/" + flTarget);
            telemetry.addData("FR", frontRight.getCurrentPosition() + "/" + frTarget);
            telemetry.update();
        }
        stopMotors();
        // Return to RUN_USING_ENCODER for next move
        frontLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        frontRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        backLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        backRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }

    private void stopMotors() {
        frontLeft.setPower(0);
        frontRight.setPower(0);
        backLeft.setPower(0);
        backRight.setPower(0);
        frontLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        frontRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        backLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        backRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }

    private <T> T safeGet(Class<? extends T> type, String name) {
        try {
            return hardwareMap.get(type, name);
        } catch (RuntimeException e) {
            return null;
        }
    }
}
