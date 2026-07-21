package org.firstinspires.ftc.teamcode.util;

public final class MathUtils {
    private MathUtils() {}

    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public static double deadband(double value, double threshold) {
        return Math.abs(value) < threshold ? 0.0 : value;
    }

    public static boolean within(double value, double target, double tolerance) {
        return Math.abs(value - target) <= tolerance;
    }
}
