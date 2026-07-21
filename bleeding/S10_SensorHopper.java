package org.firstinspires.ftc.teamcode;

import org.firstinspires.ftc.teamcode.util.HardwareNames;
import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.Servo;
public class S10_SensorHopper {
    public enum State {
        IDLE,
        INTAKING,
        FILLED,
        DUMPING,
        ERROR
    } private State currentState = State.IDLE; 
    //Hardware config
    private DcMotor intakeMotor;
    private Servo dumpServo;
    private DigitalChannel gamePieceSensor;  //Beam or color sensor
    private DigitalChannel topLimitSwitch;   //Prevents over-extension
    private boolean initialised = false;
    private void requireInitialised () {if (!initialised) {throw new IllegalStateException ("S10_SensorHopper not initialized");}}
    //Sensor config
    public void init (DcMotor motor, Servo servo, DigitalChannel sensor, DigitalChannel limitSwitch) {
        intakeMotor = motor;
        dumpServo = servo;
        gamePieceSensor = sensor;
        topLimitSwitch = limitSwitch;
        //Configure sensors as input
        gamePieceSensor.setMode (DigitalChannel.Mode.INPUT);
        topLimitSwitch.setMode (DigitalChannel.Mode.INPUT);
        //Stow dump servo
        dumpServo.setPosition (RobotConstants.SERVO_DUMP_STOWED);
        initialised = true;
        currentState = State.IDLE;
    } public State getState () {return currentState;}
    //Check if sensor detects a game piece
    private boolean hasGamePiece () {
        boolean raw = gamePieceSensor.getState ();
        return RobotConstants.SENSOR_GAME_PIECE_INVERTED ? !raw : raw;
    } //Check if system extended to top limit
    private boolean atTopLimit () {
        boolean raw = topLimitSwitch.getState ();
        return RobotConstants.SENSOR_LIMIT_SWITCH_INVERTED ? !raw : raw;
    } // Most limit switches are inverted}
    //Command: Start intaking
    public void startIntake () {
        requireInitialised ();
        if (currentState == State.IDLE) {
            currentState = State.INTAKING;
            intakeMotor.setPower (1.0);
        }
    } //Command to stop intake (manual override)
    public void stopIntake () {
        requireInitialised ();
        if (currentState == State.INTAKING) {
            intakeMotor.setPower (0);
            currentState = State.IDLE;
        }
    } //Command to start dumping sequence
    public void startDump () {
        requireInitialised ();
        if (currentState == State.FILLED) {
            currentState = State.DUMPING;
            dumpServo.setPosition (RobotConstants.SERVO_DUMP_ACTIVE);
        }
    } //Command to finish dumping sequence
    public void finishDump () {
        requireInitialised ();
        if (currentState == State.DUMPING) {
            dumpServo.setPosition (RobotConstants.SERVO_DUMP_STOWED);
            currentState = State.IDLE;
        }
    } //Updates, call this every loop iteration
    public void update (TelemetryPacket packet) {
        packet.put ("Hopper Initialised", initialised);
        if (!initialised) {return;}
        packet.put ("Hopper State", currentState.toString ());
        packet.put ("Game Piece Detected", hasGamePiece ());
        packet.put ("At Top Limit", atTopLimit ()); 
        //Automatic state transitions based on sensors
        switch (currentState) {
            case INTAKING:
                //If sensor detects game piece, automatically transition to filled
                if (hasGamePiece ()) {
                    intakeMotor.setPower (0);
                    currentState = State.FILLED;
                    packet.put ("Transition", "Sensor detected game piece");
                } break;
            case DUMPING:
                // Safety check - if we're dumping and hit the top limit, stop
                if (atTopLimit ()) {packet.put ("Warning", "Hit top limit switch");}
                break;
            default:
                break;
        }
    } //TeleOp test mode
    @TeleOp(name = "Test: Sensor Hopper", group = "S10: Sensors")
    public static class TestSensorHopper extends LinearOpMode {
        @Override
        public void runOpMode () {
            S10_SensorHopper hopper = new S10_SensorHopper ();
            //Init all hardware
            DcMotor intake = hardwareMap.get (DcMotor.class, HardwareNames.INTAKE_MOTOR);
            Servo dump = hardwareMap.get (Servo.class, HardwareNames.DUMP_SERVO);
            DigitalChannel sensor = hardwareMap.get (DigitalChannel.class, HardwareNames.GAME_PIECE_SENSOR);
            DigitalChannel limit = hardwareMap.get (DigitalChannel.class, HardwareNames.TOP_LIMIT);
            hopper.init (intake, dump, sensor, limit);
            //Setup dashboard telemetry
            telemetry = new MultipleTelemetry (telemetry, FtcDashboard.getInstance ().getTelemetry ());
            telemetry.addData ("Status", "Ready");
            telemetry.update ();
            waitForStart ();
            while (opModeIsActive ()) {
                //Manual hopper controls
                if (gamepad1.a) {hopper.startIntake ();} 
                else if (gamepad1.b) {hopper.stopIntake ();} 
                else if (gamepad1.x) {hopper.startDump ();} 
                else if (gamepad1.y) {hopper.finishDump ();}
                //Update state machine, checks sensors automatically
                TelemetryPacket packet = new TelemetryPacket ();
                hopper.update (packet);
                FtcDashboard.getInstance ().sendTelemetryPacket (packet);
                //Send telemetry to dashboard
                telemetry.addData ("Current State", hopper.getState ());
                telemetry.update ();
            }
        }
    }
}
