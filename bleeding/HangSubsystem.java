package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.DigitalChannel;

import org.firstinspires.ftc.teamcode.util.DebouncedBoolean;

public class HangSubsystem {
    private DcMotor winchMotor;
    private DigitalChannel topLimitSwitch;

    private final DebouncedBoolean topLimitDebounced = new DebouncedBoolean(false);
    private boolean topPressed = false;

    public void init(DcMotor winch, DigitalChannel topLimit) {
        winchMotor = winch;
        topLimitSwitch = topLimit;

        topLimitSwitch.setMode(DigitalChannel.Mode.INPUT);
        winchMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        winchMotor.setDirection(DcMotorSimple.Direction.FORWARD);
    }

    public void update(double dt, boolean raiseRequested, boolean lowerRequested) {
        topPressed = topLimitDebounced.update(
                limitPressed(topLimitSwitch.getState(), RobotConstants.SENSOR_LIMIT_SWITCH_INVERTED),
                dt,
                RobotConstants.SENSOR_LIMIT_SWITCH_DEBOUNCE_SECONDS
        );

        if (raiseRequested && !topPressed) {
            winchMotor.setPower(1.0);
        } else if (lowerRequested) {
            winchMotor.setPower(-0.5);
        } else {
            winchMotor.setPower(0);
        }
    }

    public boolean isTopPressed() {
        return topPressed;
    }

    public double getPower() {
        return winchMotor.getPower();
    }

    private boolean limitPressed(boolean raw, boolean inverted) {
        return inverted ? !raw : raw;
    }
}
