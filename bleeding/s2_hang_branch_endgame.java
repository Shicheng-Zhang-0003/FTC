package org.firstinspires.ftc.teamcode;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.util.ElapsedTime;
import org.firstinspires.ftc.teamcode.util.DebouncedBoolean;
import org.firstinspires.ftc.teamcode.util.HardwareNames;
@TeleOp (name = "Canopy Hanging Parking", group = "Mechanisms")
public class s2_hang_branch_endgame extends LinearOpMode {
    private DcMotor winchMotor;
    private DigitalChannel topLimitSwitch;
    private final ElapsedTime loopTimer = new ElapsedTime ();
    private final DebouncedBoolean topLimitDebounced = new DebouncedBoolean (false);
    @Override
    public void runOpMode () {
        winchMotor = hardwareMap.get (DcMotor.class, HardwareNames.WINCH_MOTOR);
        topLimitSwitch = hardwareMap.get (DigitalChannel.class, HardwareNames.TOP_LIMIT);
        topLimitSwitch.setMode (DigitalChannel.Mode.INPUT);
        winchMotor.setZeroPowerBehavior (DcMotor.ZeroPowerBehavior.BRAKE);
        winchMotor.setDirection (DcMotorSimple.Direction.FORWARD);
        telemetry.addData ("Status", "Operational-RDY");
        telemetry.update ();
        waitForStart ();
        while (opModeIsActive ()) {
            double dt = loopTimer.seconds ();
            if (dt < 0.001) {dt = 0.001;}
            boolean topPressed = topLimitDebounced.update (limitPressed (topLimitSwitch.getState (), RobotConstants.SENSOR_LIMIT_SWITCH_INVERTED), dt,RobotConstants.SENSOR_LIMIT_SWITCH_DEBOUNCE_SECONDS);
            loopTimer.reset ();
            if ((gamepad2.y) && (!topPressed)) {winchMotor.setPower (1.0);} 
            else if (gamepad2.x) {winchMotor.setPower (-0.5);} 
            else {winchMotor.setPower (0);}
            telemetry.addData ("Limit Switch Pressed", topPressed);
            telemetry.addData ("Winch Power", winchMotor.getPower ());
            telemetry.update ();
        }
    } private boolean limitPressed (boolean raw, boolean inverted) {return inverted ? !raw : raw;}
}
