package org.firstinspires.ftc.teamcode.util;

/**
 * Single source of truth for hardwareMap names.
 * Keep ACTIVE vs LEGACY vs ARCHIVED (BIOBUZZ guesswork) separated so new members
 * know what to wire for 2026 DECODE/BIOBUZZ.
 */
public final class HardwareNames {
    private HardwareNames() {}

    // ── ACTIVE: Competition (DECODE/BIOBUZZ 26-27) ──────────────────────
    // Drivetrain (Mecanum) — validated by HardwareValidationTeleOp & TuningOpModes
    public static final String FRONT_LEFT = "front_left";
    public static final String FRONT_RIGHT = "front_right";
    public static final String BACK_LEFT = "back_left";
    public static final String BACK_RIGHT = "back_right";

    // Core mechanism (S12) — slide + intake servo + sensors
    public static final String SLIDE_MOTOR = "slide_motor";
    public static final String INTAKE_SERVO = "intake_servo";
    public static final String GAME_PIECE_SENSOR = "game_piece_sensor";
    public static final String BOTTOM_LIMIT = "bottom_limit";

    // Shooter (old-school BIOBUZZ) — flywheel + feeder for pollen/nectar launch
    public static final String SHOOTER_MOTOR = "shooter_motor";
    public static final String FEEDER_SERVO = "feeder_servo";

    // Endgame
    public static final String WINCH_MOTOR = "winch_motor";
    public static final String TOP_LIMIT = "top_limit";

    // Localization / vision (ACTIVE)
    public static final String IMU = "imu";
    public static final String WEBCAM = "Main Cam";

    // ── OPTIONAL LOCALIZERS (enable when hardware present) ──────────────
    public static final String OTOS = "sensor_otos";
    public static final String PINPOINT = "pinpoint";
    public static final String PAR = "par";
    public static final String PERP = "perp";
    public static final String PAR0 = "par0";
    public static final String PAR1 = "par1";

    // ── LEGACY / TEACHING SHELLS (S1) ───────────────────────────────────
    public static final String LEFT_MOTOR = "left_motor";
    public static final String RIGHT_MOTOR = "right_motor";
    public static final String TEST_CLAW = "test_claw";

    // ── ARCHIVED BIOBUZZ GUESSWORK — kept for purge.py / teaching, not wired in Competition* ─
    // Do not add to active config unless re-validated for real 26-27 game.
    public static final String INTAKE_MOTOR = "intake_motor";
    public static final String DUMP_SERVO = "dump_servo";
    public static final String INTAKE_ROLLER = "intake_roller";
    public static final String DUMP_BED_TILT = "dump_bed_tilt";
    public static final String SLIDE_LEFT = "slide_left";
    public static final String SLIDE_RIGHT = "slide_right";
    public static final String WRIST_CLAW = "wrist_claw";
}
