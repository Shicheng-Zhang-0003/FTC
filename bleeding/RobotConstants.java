package org.firstinspires.ftc.teamcode;

import com.acmerobotics.dashboard.config.Config;

@Config
public class RobotConstants {
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

    // Slide PID
    public static double SLIDE_P = 10.0;
    public static double SLIDE_I = 0.5;
    public static double SLIDE_D = 1.0;
    public static double SLIDE_INTEGRAL_CLAMP = 2.0;
    public static double SLIDE_OUTPUT_CLAMP = 1.0;

    // Slide positions
    public static int SLIDE_GROUND = 0;
    public static int SLIDE_LOW = 500;
    public static int SLIDE_INTAKE = 500;
    public static int SLIDE_SCORE_LOW = 1000;
    public static int SLIDE_SCORE_HIGH = 1500;
    public static int SLIDE_MAX_SAFE = 1900;
    public static int SLIDE_POSITION_TOLERANCE = 50;

    // Sensors
    public static double SENSOR_GAME_PIECE_DISTANCE_CM = 5.0;
    public static boolean SENSOR_LIMIT_SWITCH_INVERTED = true;
    public static boolean SENSOR_GAME_PIECE_INVERTED = false;
    public static double SENSOR_LIMIT_SWITCH_DEBOUNCE_SECONDS = 0.05;

    // Timing
    public static double TIMING_SCORE_HOLD_SECONDS = 1.0;
    public static double TIMING_DUMP_SECONDS = 1.0;
    public static double TIMING_INTAKE_FILL_SECONDS = 1.5;
}
