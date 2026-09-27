package org.firstinspires.ftc.teamcode;

import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

/**
 * ShooterSubsystem — old-school BIOBUZZ flywheel + feeder.
 * No AprilTag, no Road Runner, no PID needed. Spin flywheel by power,
 * push feeder servo to feed one pollen/nectar per stroke.
 *
 * Under-one-roof: matches 144x144 field, 30.6 in HIVE cell height, 48 in shooting distance.
 */
public class ShooterSubsystem {
    public enum State { IDLE, SPINNING_UP, READY, FEEDING, RECOVERING }

    private DcMotorEx shooterMotor;
    private Servo feederServo;
    private State state = State.IDLE;
    private final ElapsedTime stateTimer = new ElapsedTime();
    private double targetPower = 0.0;
    private boolean isPollen = true; // pollen vs nectar power
    private int shotsFired = 0;
    private int shotsToFire = 0;

    public void init(DcMotorEx shooter, Servo feeder) {
        shooterMotor = shooter;
        feederServo = feeder;
        shooterMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        shooterMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        feederServo.setPosition(RobotConstants.SERVO_FEEDER_STOWED);
        state = State.IDLE;
        targetPower = 0;
        shotsFired = 0;
        stateTimer.reset();
    }

    public State getState() {return state;}
    public int getShotsFired() {return shotsFired;}
    public boolean isReady() {return state == State.READY;}
    public boolean isIdle() {return state == State.IDLE;}

    /** Start spinning flywheel for pollen (true) or nectar (false). */
    public void spinUp(boolean pollen) {
        if (state == State.IDLE) {
            isPollen = pollen;
            targetPower = pollen ? RobotConstants.SHOOTER_POLLEN_POWER : RobotConstants.SHOOTER_NECTAR_POWER;
            shooterMotor.setPower(targetPower);
            state = State.SPINNING_UP;
            stateTimer.reset();
        }
    }

    /** Queue N shots — will auto-spinup if idle. Call update() in loop. */
    public void shootShots(int count, boolean pollen) {
        if (state == State.IDLE) {
            isPollen = pollen;
            targetPower = pollen ? RobotConstants.SHOOTER_POLLEN_POWER : RobotConstants.SHOOTER_NECTAR_POWER;
            shooterMotor.setPower(targetPower);
            state = State.SPINNING_UP;
            stateTimer.reset();
        }
        // if already spinning/ready, just queue
        shotsToFire = count;
        shotsFired = 0;
    }

    public void shootOne(boolean pollen) {shootShots(1, pollen);}

    public void stop() {
        shooterMotor.setPower(0);
        feederServo.setPosition(RobotConstants.SERVO_FEEDER_STOWED);
        state = State.IDLE;
        targetPower = 0;
        shotsToFire = 0;
        shotsFired = 0;
        stateTimer.reset();
    }

    public void eStop() {stop();}

    /** Call every loop with dt not needed — uses internal timer. Fills TelemetryPacket. */
    public void update(TelemetryPacket packet) {
        packet.put("Shooter State", state.toString());
        packet.put("Shooter Power", shooterMotor != null ? shooterMotor.getPower() : 0);
        packet.put("Shots Fired", shotsFired + "/" + shotsToFire);
        packet.put("Feeder Pos", feederServo != null ? feederServo.getPosition() : 0);
        if (shooterMotor != null) {
            packet.put("Shooter Vel", shooterMotor.getVelocity());
        }

        switch (state) {
            case SPINNING_UP:
                if (stateTimer.seconds() >= RobotConstants.SHOOTER_SPINUP_SECONDS) {
                    if (shotsToFire > 0) {
                        state = State.FEEDING;
                        feederServo.setPosition(RobotConstants.SERVO_FEEDER_PUSH);
                        stateTimer.reset();
                    } else {
                        state = State.READY;
                        stateTimer.reset();
                    }
                }
                break;
            case READY:
                if (shotsToFire > 0 && shotsFired < shotsToFire) {
                    state = State.FEEDING;
                    feederServo.setPosition(RobotConstants.SERVO_FEEDER_PUSH);
                    stateTimer.reset();
                }
                break;
            case FEEDING:
                if (stateTimer.seconds() >= RobotConstants.SHOOTER_FEED_STROKE_SECONDS) {
                    feederServo.setPosition(RobotConstants.SERVO_FEEDER_STOWED);
                    shotsFired++;
                    if (shotsFired >= shotsToFire) {
                        shotsToFire = 0;
                        state = State.IDLE;
                        shooterMotor.setPower(0);
                        packet.put("Shooter", "Sequence complete");
                    } else {
                        state = State.RECOVERING;
                    }
                    stateTimer.reset();
                }
                break;
            case RECOVERING:
                if (stateTimer.seconds() >= RobotConstants.SHOOTER_RECOVERY_SECONDS) {
                    state = State.FEEDING;
                    feederServo.setPosition(RobotConstants.SERVO_FEEDER_PUSH);
                    stateTimer.reset();
                }
                break;
            case IDLE:
            default:
                break;
        }
    }

    public boolean isShootingComplete() {
        return state == State.IDLE && shotsFired >= shotsToFire && shotsToFire == 0;
    }
}
