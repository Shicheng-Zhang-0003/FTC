package org.firstinspires.ftc.teamcode;
import org.firstinspires.ftc.teamcode.util.HardwareNames;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;
public class S7_StateMachineHopper {
    // Define the states where the fill state is present in
    public enum State {
        IDLE,
        INTAKING,
        FILLED,
        DUMPING,
        ERROR
    } private State currentState = State.IDLE;
    private DcMotor intakeMotor;
    private Servo dumpServo;
    private boolean initialised = false;
    private void requireInitialised () {
        if (!initialised) {throw new IllegalStateException ("S7_StateMachineHopper not initialised properly");}
    } //Pass hardwareMap and initialize motors and servos
    public void init (DcMotor motor, Servo servo) {
        intakeMotor = motor;
        dumpServo = servo;
        dumpServo.setPosition (RobotConstants.SERVO_DUMP_STOWED); //Stowed position
        initialised = true;
    } //Current State of being filled or not
    public State getState () {return currentState;}
    //Start intaking Pollen
    public void startIntake () {
        requireInitialised ();
        if ((currentState == State.IDLE) || (currentState == State.FILLED)) {
            currentState = State.INTAKING;
            intakeMotor.setPower (1.0);
        }
    } //Stop intake, total stow is completely filled
    public void stopIntake () {
        requireInitialised ();
        if (currentState == State.INTAKING) {
            intakeMotor.setPower (0);
            currentState = State.FILLED;
        }
    } //Start dumping
    public void startDump () {
        requireInitialised ();
        if (currentState == State.FILLED) {
            currentState = State.DUMPING;
            dumpServo.setPosition (RobotConstants.SERVO_DUMP_ACTIVE); //Dump position
        }
    } //Finish dumping, motors return to idling
    public void finishDump () {
        requireInitialised ();
        if (currentState == State.DUMPING) {
            dumpServo.setPosition (RobotConstants.SERVO_DUMP_STOWED); //Stow Pollen Dump
            currentState = State.IDLE;
        }
    } //Update method (call this in your loop)
    public void update (TelemetryPacket packet) {
        packet.put ("Hopper Initialised", initialised);
        if (!initialised) {return;}
        packet.put ("Hopper Status", currentState.toString ());
    } //TeleOp OpMode to test the state machine
    @TeleOp(name = "State Machine Hopper", group = "S7: State Machines")
    public static class TestHopper extends LinearOpMode {
        @Override
        public void runOpMode () {
            S7_StateMachineHopper hopper = new S7_StateMachineHopper ();
            DcMotor intake = hardwareMap.get(DcMotor.class, HardwareNames.INTAKE_MOTOR);
            Servo dump = hardwareMap.get(Servo.class, HardwareNames.DUMP_SERVO);
            hopper.init (intake, dump);
            telemetry = new MultipleTelemetry (telemetry, FtcDashboard.getInstance ().getTelemetry ());
            telemetry.addData ("Status", "RDY");
            telemetry.update ();
            waitForStart ();
            while (opModeIsActive ()) {
                //Controls
                if (gamepad1.a) {hopper.startIntake ();} 
                else if (gamepad1.b) {hopper.stopIntake ();} 
                else if (gamepad1.x) {hopper.startDump ();} 
                else if (gamepad1.y) {hopper.finishDump ();}
                //Update state machine
                TelemetryPacket packet = new TelemetryPacket ();
                hopper.update (packet);
                FtcDashboard.getInstance ().sendTelemetryPacket (packet);
                telemetry.addData ("Current State", hopper.getState ());
                telemetry.update ();
            }
        }
    }
}
