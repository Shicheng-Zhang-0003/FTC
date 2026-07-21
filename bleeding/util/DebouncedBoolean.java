package org.firstinspires.ftc.teamcode.util;

public class DebouncedBoolean {
    private boolean stableValue;
    private double stableTime = 0;

    public DebouncedBoolean(boolean initialValue) {
        stableValue = initialValue;
    }

    public boolean update(boolean rawValue, double dt, double debounceTime) {
        if (debounceTime <= 0) {
            stableValue = rawValue;
            stableTime = 0;
            return stableValue;
        }

        if (rawValue == stableValue) {
            stableTime = 0;
            return stableValue;
        }

        stableTime += dt;
        if (stableTime >= debounceTime) {
            stableValue = rawValue;
            stableTime = 0;
        }

        return stableValue;
    }
}
