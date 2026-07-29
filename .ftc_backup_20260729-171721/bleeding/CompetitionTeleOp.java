package org.firstinspires.ftc.teamcode;
import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;
import org.firstinspires.ftc.teamcode.util.DebouncedBoolean;
import org.firstinspires.ftc.teamcode.util.HardwareNames;
import org.firstinspires.ftc.teamcode.util.MathUtils;
@TeleOp (name = "Competition TeleOp", group = "Competition")
public class CompetitionTeleOp extends LinearOpMode {
    //Drivetrain
    private DcMotor frontLeft;
    private DcMotor frontRight;
    private DcMotor backLeft;
    private DcMotor backRight;
    //Mechanisms
    private S12_IntegratedMechanism mechanism;
    //Hanging
    private DcMotor winchMotor;
    private DigitalChannel topLimitSwitch;
    private final ElapsedTime loopTimer = new ElapsedTime ();
    private final DebouncedBoolean topLimitDebounced = new DebouncedBoolean (false);
    @Override
    public void runOpMode () {
        //Drivetrain hardware
        frontLeft = hardwareMap.get (DcMotor.class, HardwareNames.FRONT_LEFT);
        frontRight = hardwareMap.get (DcMotor.class, HardwareNames.FRONT_RIGHT);
        backLeft = hardwareMap.get (DcMotor.class, HardwareNames.BACK_LEFT);
        backRight = hardwareMap.get (DcMotor.class, HardwareNames.BACK_RIGHT);
        frontLeft.setDirection (DcMotorSimple.Direction.REVERSE);
        backLeft.setDirection (DcMotorSimple.Direction.REVERSE);
        frontRight.setDirection (DcMotorSimple.Direction.FORWARD);
        backRight.setDirection (DcMotorSimple.Direction.FORWARD);
        //Mechanism Main hardware
        DcMotorEx slide = hardwareMap.get (DcMotorEx.class, HardwareNames.SLIDE_MOTOR);
        Servo intakeServo = hardwareMap.get (Servo.class, HardwareNames.INTAKE_SERVO);
        DigitalChannel gamePieceSensor = hardwareMap.get (DigitalChannel.class, HardwareNames.GAME_PIECE_SENSOR);
        DigitalChannel bottomLimit = hardwareMap.get (DigitalChannel.class, HardwareNames.BOTTOM_LIMIT);
        mechanism = new S12_IntegratedMechanism ();
        mechanism.init (slide, intakeServo, gamePieceSensor, bottomLimit);
        //Hang hardware
        winchMotor = hardwareMap.get (DcMotor.class, HardwareNames.WINCH_MOTOR);
        topLimitSwitch = hardwareMap.get (DigitalChannel.class, HardwareNames.TOP_LIMIT);
        topLimitSwitch.setMode (DigitalChannel.Mode.INPUT);
        winchMotor.setZeroPowerBehavior (DcMotor.ZeroPowerBehavior.BRAKE);
        winchMotor.setDirection (DcMotorSimple.Direction.FORWARD);
        //Telemetry Outputs
        telemetry = new MultipleTelemetry (telemetry, FtcDashboard.getInstance ().getTelemetry ());
        telemetry.addData ("Status", "Competition TeleOp Ready");
        telemetry.update ();
        waitForStart ();
        mechanism.resetControllers ();
        loopTimer.reset ();
        while (opModeIsActive ()) {
            double dt = loopTimer.seconds ();
            if (dt < 0.001) {dt = 0.001;}
            //Drivetrain
            double y = MathUtils.deadband (-gamepad1.left_stick_y, RobotConstants.DRIVE_STICK_DEADBAND);
            double x = MathUtils.deadband (gamepad1.left_stick_x, RobotConstants.DRIVE_STICK_DEADBAND) * RobotConstants.DRIVE_MECANUM_STRAFE_COMPENSATION;
            double rx = MathUtils.deadband (gamepad1.right_stick_x, RobotConstants.DRIVE_STICK_DEADBAND);
            double denominator = Math.max (Math.abs (y) + Math.abs (x) + Math.abs (rx), 1);
            double frontLeftPower = (y + x + rx) / denominator;
            double frontRightPower = (y - x - rx) / denominator;
            double backLeftPower = (y - x + rx) / denominator;
            double backRightPower = (y + x - rx) / denominator;
            frontLeft.setPower (frontLeftPower);
            frontRight.setPower (frontRightPower);
            backLeft.setPower (backLeftPower);
            backRight.setPower (backRightPower);
            //Mechanism controls
            if (gamepad2.a) {mechanism.startIntakeSequence ();} 
            else if (gamepad2.b) {mechanism.startScoreSequence ();} 
            else if (gamepad2.x) {mechanism.emergencyStop ();}
            //Hang controls
            boolean topPressed = topLimitDebounced.update (limitPressed(topLimitSwitch.getState (), RobotConstants.SENSOR_LIMIT_SWITCH_INVERTED), dt, RobotConstants.SENSOR_LIMIT_SWITCH_DEBOUNCE_SECONDS);
            if ((gamepad2.y) && (!topPressed)) {winchMotor.setPower(1.0);} 
            else if (gamepad2.x) {winchMotor.setPower (-0.5);} 
            else {winchMotor.setPower (0);}
            //Mechanism update
            TelemetryPacket packet = new TelemetryPacket ();
            mechanism.update (packet);
            FtcDashboard.getInstance ().sendTelemetryPacket (packet);
            //Driver telemetry
            telemetry.addData ("Mechanism State", mechanism.getState ());
            telemetry.addData ("Top Limit Pressed", topPressed);
            telemetry.addData ("Winch Power", winchMotor.getPower ());
            telemetry.update ();
            loopTimer.reset ();
        }
    } private boolean limitPressed (boolean raw, boolean inverted) {return inverted ? !raw : raw;}
}
