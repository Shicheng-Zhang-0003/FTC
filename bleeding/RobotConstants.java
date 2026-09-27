package org.firstinspires.ftc.teamcode;

import com.acmerobotics.dashboard.config.Config;

@Config
public class RobotConstants {
    // BIOBUZZ 26-27 SEASON (under-one-roof upgrade)
    // Generic naming is kept: game piece == pollen, SLIDE_* == branch heights,
    // SERVO_INTAKE == pollen intake, SERVO_DUMP == hive scoring.
    // PURGE.PY archives the old pollen-specific S6/S7/S9 guesswork; S12 is the
    // promoted generic mechanism — duplicate it and re-bind servos/sensors via
    // HardwareNames for the real field.
    // Drive
    public static double DRIVE_MECANUM_STRAFE_COMPENSATION = 1.1;
    public static double DRIVE_STICK_DEADBAND = 0.05;
    public static double DRIVE_TRIGGER_DEADBAND = 0.1;
    public static double DRIVE_TICKS_PER_INCH = 38.2;

    // Servos
    public static double SERVO_CLAW_OPEN = 0.8;
    public static double SERVO_CLAW_CLOSED = 0.2;

    public static double SERVO_INTAKE_OPEN = 0.82;
    public static double SERVO_INTAKE_CLOSED = 0.35;

    public static double SERVO_DUMP_STOWED = 0.10;
    public static double SERVO_DUMP_ACTIVE = 0.90;

    public static double SERVO_WRIST_PICKUP = 0.8;
    public static double SERVO_WRIST_SCORE = 0.2;

    // Slide PID (scaled for encoder ticks, max travel ~1900 ticks)
    public static double SLIDE_P = 0.005;
    public static double SLIDE_I = 0.0001;
    public static double SLIDE_D = 0.0002;
    public static double SLIDE_INTEGRAL_CLAMP = 2.0;
    public static double SLIDE_OUTPUT_CLAMP = 1.0;
    public static double SLIDE_KG = 0.10; // B8 fix: gravity feedforward
    // B8 fix: feedforward power needed to hold the slide against gravity.
    // Tune on-robot: minimum power that stops the slide drooping at mid-height.

    // Slide positions
    public static int SLIDE_GROUND = 0;
    public static int SLIDE_LOW = 500;
    public static int SLIDE_INTAKE = 500;
    public static int SLIDE_SCORE_HIGH = 1500;
    public static int SLIDE_MAX_SAFE = 1900;
    public static int SLIDE_POSITION_TOLERANCE = 50;

    // Sensors
    public static boolean SENSOR_LIMIT_SWITCH_INVERTED = true;
    public static boolean SENSOR_GAME_PIECE_INVERTED = false;
    public static double SENSOR_LIMIT_SWITCH_DEBOUNCE_SECONDS = 0.05;

    // Timing
    public static double TIMING_SCORE_HOLD_SECONDS = 1.0;
    public static double TIMING_DUMP_SECONDS = 1.0;
    public static double TIMING_INTAKE_FILL_SECONDS = 1.5;

    // ── BIOBUZZ Shooter (old-school) ─────────────────────────────────────
    // Flywheel powers tuned on-robot for 2.8 in pollen @ ~48 in to HIVE (30.6 in high)
    public static double SHOOTER_POLLEN_POWER = 0.85;
    public static double SHOOTER_NECTAR_POWER = 0.92; // heavier 3.6 in needs more
    public static double SHOOTER_SPINUP_SECONDS = 1.2;
    public static double SHOOTER_FEED_STROKE_SECONDS = 0.35; // servo push + retract
    public static double SHOOTER_RECOVERY_SECONDS = 0.30; // flywheel recovers between shots
    public static double SERVO_FEEDER_STOWED = 0.20;
    public static double SERVO_FEEDER_PUSH = 0.75;
    // Encoder shooter: redundant PID not needed; old-school is plain power
    // Autonomous shooter sequence: spinUp -> feed 4x -> stop
    public static int SHOOTER_POLLEN_PER_AUTO = 4; // preload
    public static double SHOOTER_POLLEN_INTERVAL_SECONDS = 0.8;
}
