package org.firstinspires.ftc.teamcode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import org.firstinspires.ftc.teamcode.util.DebouncedBoolean;
public class HangSubsystem {
private DcMotor winchMotor;
private DigitalChannel topLimitSwitch;
private DigitalChannel bottomLimitSwitch;
private final DebouncedBoolean topLimitDebounced = new DebouncedBoolean (false);
private final DebouncedBoolean bottomLimitDebounced = new DebouncedBoolean (false);
private boolean topPressed = false;
private boolean bottomPressed = false;
// B25 fix: soft-start ramp state for the winch raise
private double raisePower = 0.0;
private static final double RAISE_RAMP_PER_SECOND = 2.0; // full power in ~0.5s
private static final double RAISE_MAX_POWER = 1.0;
public void init (DcMotor winch, DigitalChannel topLimit, DigitalChannel bottomLimit) {
winchMotor = winch;
topLimitSwitch = topLimit;
bottomLimitSwitch = bottomLimit;
topLimitSwitch.setMode (DigitalChannel.Mode.INPUT);
if (bottomLimitSwitch != null) {bottomLimitSwitch.setMode (DigitalChannel.Mode.INPUT);}
winchMotor.setZeroPowerBehavior (DcMotor.ZeroPowerBehavior.BRAKE);
winchMotor.setDirection (DcMotorSimple.Direction.FORWARD);
}
public void init (DcMotor winch, DigitalChannel topLimit) {
init (winch, topLimit, null);
}
public void update (double dt, boolean raiseRequested, boolean lowerRequested) {
topPressed = topLimitDebounced.update (
limitPressed(topLimitSwitch.getState (), RobotConstants.SENSOR_LIMIT_SWITCH_INVERTED),
dt,
RobotConstants.SENSOR_LIMIT_SWITCH_DEBOUNCE_SECONDS
);
if (bottomLimitSwitch != null) {
bottomPressed = bottomLimitDebounced.update (
limitPressed(bottomLimitSwitch.getState (), RobotConstants.SENSOR_LIMIT_SWITCH_INVERTED),
dt,
RobotConstants.SENSOR_LIMIT_SWITCH_DEBOUNCE_SECONDS
);
} else {
bottomPressed = false;
}
if ((raiseRequested) && (!topPressed)) {
    // B25 fix: soft-start ramp instead of an instant 1.0 step. Full power
    // from rest shock-loads the winch and can brown out the hub when the
    // whole robot hangs on it. Ramp up over ~0.5s instead.
    raisePower = Math.min(RAISE_MAX_POWER, raisePower + RAISE_RAMP_PER_SECOND * dt);
    winchMotor.setPower(raisePower);
}
else if ((lowerRequested) && (!bottomPressed)) {
    raisePower = 0.0; // B25 fix: reset the ramp when direction changes
    winchMotor.setPower(-0.5);
}
else {
    raisePower = 0.0; // B25 fix: reset the ramp when idle
    winchMotor.setPower(0);
}
}
public boolean isTopPressed () {return topPressed;}
public boolean isBottomPressed () {return bottomPressed;}
public double getPower () {return winchMotor.getPower ();}
private boolean limitPressed (boolean raw, boolean inverted) {return inverted ? !raw : raw;}
}
