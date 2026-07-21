package org.firstinspires.ftc.teamcode;

import org.firstinspires.ftc.teamcode.util.HardwareNames;
import org.firstinspires.ftc.teamcode.util.MathUtils;
import org.firstinspires.ftc.teamcode.util.PIDController;
import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;
public class S12_IntegratedMechanism {
    //State Machine Definition
    public enum State {
        IDLE,
        MOVING_TO_INTAKE,
        INTAKING,
        MOVING_TO_SCORE,
        SCORING,
        ERROR
    } private State currentState = State.IDLE;
    //Hardware Init
    private DcMotorEx slideMotor;
    private Servo intakeServo;
    private DigitalChannel gamePieceSensor;
    private DigitalChannel bottomLimit;
    //Control state
    private int targetPosition = 0;
    private PIDController pid;
    private final ElapsedTime pidTimer = new ElapsedTime ();
    private final ElapsedTime stateTimer = new ElapsedTime ();
    //Init Everything
    public void init (DcMotorEx slide, Servo intake, DigitalChannel sensor, DigitalChannel limit) {
        slideMotor = slide;
        intakeServo = intake;
        gamePieceSensor = sensor;
        bottomLimit = limit;
        //Configure Hardware Options
        slideMotor.setMode (DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        slideMotor.setMode (DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        slideMotor.setZeroPowerBehavior (DcMotor.ZeroPowerBehavior.BRAKE);
        gamePieceSensor.setMode (DigitalChannel.Mode.INPUT);
        bottomLimit.setMode (DigitalChannel.Mode.INPUT);
        intakeServo.setPosition (RobotConstants.SERVO_INTAKE_CLOSED);
        pid = new PIDController (RobotConstants.SLIDE_P, RobotConstants.SLIDE_I, RobotConstants.SLIDE_D);
        pid.setIntegralClamp (RobotConstants.SLIDE_INTEGRAL_CLAMP);
        pid.setOutputClamp (RobotConstants.SLIDE_OUTPUT_CLAMP);
        pidTimer.reset ();
        pid.reset ();
        stateTimer.reset ();
        currentState = State.IDLE;
    } //Check Sensor States
    public State getState () {return currentState;}
    public void resetControllers () {
        pidTimer.reset ();
        pid.reset ();
    } private void setTargetPosition (int target) {targetPosition = MathUtils.clamp (target, RobotConstants.SLIDE_GROUND, RobotConstants.SLIDE_MAX_SAFE);}
    private boolean limitPressed (boolean raw, boolean inverted) {return inverted ? !raw : raw;}
    //Check Sensor States
    private boolean hasGamePiece () {
        boolean raw = gamePieceSensor.getState ();
        return RobotConstants.SENSOR_GAME_PIECE_INVERTED ? !raw : raw;
    } private boolean atBottom () {return limitPressed (bottomLimit.getState (), RobotConstants.SENSOR_LIMIT_SWITCH_INVERTED);}
    private boolean atTargetPosition () {
        int error = Math.abs (targetPosition - slideMotor.getCurrentPosition ());
        return error < RobotConstants.SLIDE_POSITION_TOLERANCE;
    } private void updatePID () {
        int currentPosition = slideMotor.getCurrentPosition ();
        double error = targetPosition - currentPosition;
        double dt = pidTimer.seconds ();
        if (dt < 0.001) {dt = 0.001;}
        pid.setGains (RobotConstants.SLIDE_P, RobotConstants.SLIDE_I,RobotConstants.SLIDE_D);
        double power = pid.update (error, dt);
        slideMotor.setPower (power);
        pidTimer.reset ();
    } //State Machine Specific Commands
    public void startIntakeSequence () {
        if (currentState == State.IDLE) {
            currentState = State.MOVING_TO_INTAKE;
            setTargetPosition (RobotConstants.SLIDE_INTAKE);
            intakeServo.setPosition (RobotConstants.SERVO_INTAKE_OPEN);
        }
    } public void startScoreSequence () {
        if ((currentState == State.IDLE) || (currentState == State.INTAKING)) {
            currentState = State.MOVING_TO_SCORE;
            setTargetPosition (RobotConstants.SLIDE_SCORE_HIGH);
        }
    } public void emergencyStop () {
        slideMotor.setPower (0);
        intakeServo.setPosition (RobotConstants.SERVO_INTAKE_CLOSED);
        currentState = State.IDLE;
    } //Master Loop
    public void update (TelemetryPacket packet) {
        //Always PID to maintain position
        updatePID ();
        //State machine logic
        switch (currentState) {
            case MOVING_TO_INTAKE:
                if (atTargetPosition ()) {currentState = State.INTAKING;}
                break;
            case INTAKING:
                if (hasGamePiece ()) {
                    intakeServo.setPosition (RobotConstants.SERVO_INTAKE_CLOSED);
                    currentState = State.IDLE;
                } break;
            case MOVING_TO_SCORE:
                if (atTargetPosition ()) {
                 currentState = State.SCORING;
                    intakeServo.setPosition (RobotConstants.SERVO_INTAKE_OPEN);
                    stateTimer.reset ();
                } break;
            case SCORING:
                if (stateTimer.seconds () >= RobotConstants.TIMING_SCORE_HOLD_SECONDS) {
                    intakeServo.setPosition (RobotConstants.SERVO_INTAKE_CLOSED);
                    setTargetPosition (RobotConstants.SLIDE_GROUND);
                    currentState = State.IDLE;
                } break;
            default:
                break;
        } //Safety check
        //Safety check
        if ((atBottom ()) && (targetPosition < RobotConstants.SLIDE_GROUND)) {
            setTargetPosition (RobotConstants.SLIDE_GROUND);
            packet.put ("Safety", "Bottom Extension Limit");
        } //Telemetry
        packet.put ("State", currentState.toString ());
        packet.put ("Target Pos", targetPosition);
        packet.put ("Current Pos", slideMotor.getCurrentPosition ());
        packet.put ("Has Piece", hasGamePiece ());
    } //Test Teleop Runtime
    @TeleOp (name = "Test: Integrated Mechanism", group = "S12: Full System")
    public static class TestIntegrated extends LinearOpMode {
        @Override
        public void runOpMode () {
            S12_IntegratedMechanism mech = new S12_IntegratedMechanism ();
            DcMotorEx slide = hardwareMap.get(DcMotorEx.class, HardwareNames.SLIDE_MOTOR);
            Servo intake = hardwareMap.get(Servo.class, HardwareNames.INTAKE_SERVO);
            DigitalChannel sensor = hardwareMap.get(DigitalChannel.class, HardwareNames.GAME_PIECE_SENSOR);
            DigitalChannel limit = hardwareMap.get(DigitalChannel.class, HardwareNames.BOTTOM_LIMIT);
            mech.init (slide, intake, sensor, limit);
            telemetry = new MultipleTelemetry (telemetry, FtcDashboard.getInstance ().getTelemetry ());
            telemetry.addData ("Status", "RDY");
            telemetry.update ();
            waitForStart ();
            mech.resetControllers ();
            while (opModeIsActive ()) {
                //Controls
                if (gamepad1.a) {mech.startIntakeSequence ();} 
                else if (gamepad1.b) {mech.startScoreSequence ();} 
                else if (gamepad1.x) {mech.emergencyStop ();}
                //Update Runtime Mechanism
                TelemetryPacket packet = new TelemetryPacket ();
                
            mech.update(packet);
            FtcDashboard.getInstance().sendTelemetryPacket(packet);
                telemetry.addData ("State", mech.getState ());
                telemetry.update ();
            }
        }
    }
}
