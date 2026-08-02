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
import org.firstinspires.ftc.teamcode.util.HardwareNames;
import org.firstinspires.ftc.teamcode.util.MathUtils;
@TeleOp (name = "Competition TeleOp", group = "Competition")
public class CompetitionTeleOp extends LinearOpMode {
    private DcMotor frontLeft;
    private DcMotor frontRight;
    private DcMotor backLeft;
    private DcMotor backRight;
    private S12_IntegratedMechanism mechanism;
    private HangSubsystem hang;
    private final ElapsedTime loopTimer = new ElapsedTime ();
    @Override
    public void runOpMode () {
        frontLeft = hardwareMap.get (DcMotor.class, HardwareNames.FRONT_LEFT);
        frontRight = hardwareMap.get (DcMotor.class, HardwareNames.FRONT_RIGHT);
        backLeft = hardwareMap.get (DcMotor.class, HardwareNames.BACK_LEFT);
        backRight = hardwareMap.get (DcMotor.class, HardwareNames.BACK_RIGHT);
        frontLeft.setDirection (DcMotorSimple.Direction.REVERSE);
        backLeft.setDirection (DcMotorSimple.Direction.REVERSE);
        frontRight.setDirection (DcMotorSimple.Direction.FORWARD);
        backRight.setDirection (DcMotorSimple.Direction.FORWARD);
        DcMotorEx slide = hardwareMap.get (DcMotorEx.class, HardwareNames.SLIDE_MOTOR);
        Servo intakeServo = hardwareMap.get (Servo.class, HardwareNames.INTAKE_SERVO);
        DigitalChannel gamePieceSensor = hardwareMap.get (DigitalChannel.class, HardwareNames.GAME_PIECE_SENSOR);
        DigitalChannel bottomLimit = hardwareMap.get (DigitalChannel.class, HardwareNames.BOTTOM_LIMIT);
        mechanism = new S12_IntegratedMechanism ();
        mechanism.init (slide, intakeServo, gamePieceSensor, bottomLimit);
        hang = new HangSubsystem ();
        hang.init (hardwareMap.get (DcMotor.class, HardwareNames.WINCH_MOTOR), hardwareMap.get (DigitalChannel.class, HardwareNames.TOP_LIMIT));
        telemetry = new MultipleTelemetry (telemetry, FtcDashboard.getInstance ().getTelemetry ());
        telemetry.addData ("Status", "Competition TeleOp Ready");
        telemetry.addData ("Drive Ready", RobotReadiness.DRIVE_READY);
        telemetry.addData ("Mechanism Ready", RobotReadiness.MECHANISM_READY);
        telemetry.addData ("Hang Ready", RobotReadiness.HANG_READY);
        telemetry.addData ("Vision Ready", RobotReadiness.VISION_READY);
        telemetry.addData ("Road Runner Tuned", RobotReadiness.ROAD_RUNNER_TUNED);
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
            hang.update (dt, gamepad2.y, gamepad2.left_bumper);
            //Mechanism update
            TelemetryPacket packet = new TelemetryPacket ();
            mechanism.update (packet);
            FtcDashboard.getInstance ().sendTelemetryPacket (packet);
            //Driver telemetry
            telemetry.addData ("Mechanism State", mechanism.getState ());
            telemetry.addData ("Hang Top Limit", hang.isTopPressed ());
            telemetry.addData ("Winch Power", hang.getPower ());
            telemetry.update ();
            loopTimer.reset ();
        }
    }
}
