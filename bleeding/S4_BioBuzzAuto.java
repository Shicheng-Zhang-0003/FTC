package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.util.HardwareNames;

/**
 * S4_BioBuzzAuto — OLD SCHOOL encoder autonomous demo for 144x144 BIOBUZZ field.
 * No Road Runner, no AprilTag, pure RUN_TO_POSITION ticks + shooter.
 * Distances from BioBuzzField (tiles 24in, 6x6, HIVE at 72,72, Flower at walls).
 */
@Disabled
@Autonomous(name = "Auto: BioBuzz Old School", group = "S4: Auto")
public class S4_BioBuzzAuto extends LinearOpMode {
    private DcMotor frontLeft, frontRight, backLeft, backRight;
    private ShooterSubsystem shooter;
    private boolean shooterReady = false;
    private final ElapsedTime runtime = new ElapsedTime();

    @Override
    public void runOpMode() {
        frontLeft = hardwareMap.get(DcMotor.class, HardwareNames.FRONT_LEFT);
        frontRight = hardwareMap.get(DcMotor.class, HardwareNames.FRONT_RIGHT);
        backLeft = hardwareMap.get(DcMotor.class, HardwareNames.BACK_LEFT);
        backRight = hardwareMap.get(DcMotor.class, HardwareNames.BACK_RIGHT);

        frontLeft.setDirection(DcMotorSimple.Direction.REVERSE);
        backLeft.setDirection(DcMotorSimple.Direction.REVERSE);
        frontRight.setDirection(DcMotorSimple.Direction.FORWARD);
        backRight.setDirection(DcMotorSimple.Direction.FORWARD);

        // B26 fix: re-zero at init
        frontLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        frontRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        backLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        backRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        frontLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        frontRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        backLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        backRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        DcMotorEx shooterMotor = null;
        Servo feeder = null;
        try {shooterMotor = hardwareMap.get(DcMotorEx.class, HardwareNames.SHOOTER_MOTOR);} catch (Exception ignored) {}
        try {feeder = hardwareMap.get(Servo.class, HardwareNames.FEEDER_SERVO);} catch (Exception ignored) {}

        shooter = new ShooterSubsystem();
        if (shooterMotor != null && feeder != null) {
            shooter.init(shooterMotor, feeder);
            shooterReady = true;
        }

        telemetry.addData("Status", "BIOBUZZ Old School RDY");
        telemetry.addData("Field", "144x144 HIVE center 72,72 shooter 48in");
        telemetry.addData("Shooter Ready", shooterReady);
        telemetry.addData("Ticks/In", RobotConstants.DRIVE_TICKS_PER_INCH);
        telemetry.update();

        waitForStart();
        if (isStopRequested()) return;

        // Demo sequence mirroring CompetitionAuto but single tile-optimized
        // 1) Leave start 1 tile (24in) — SWARM
        driveForward(BioBuzzField.TILE_SIZE_IN, 0.5);
        // 2) Drive to hive shoot (48in total, so +24)
        driveForward(BioBuzzField.START_TO_HIVE_SHOOT_IN - BioBuzzField.TILE_SIZE_IN, 0.5);
        // 3) Shoot 2 pollen old-school (if shooter present)
        if (shooterReady) {
            shooter.shootShots(2, true);
            ElapsedTime t = new ElapsedTime(); t.reset();
            while (opModeIsActive() && t.seconds() < 4.0 && shooter.getState() != ShooterSubsystem.State.IDLE) {
                shooter.update(new com.acmerobotics.dashboard.telemetry.TelemetryPacket());
                telemetry.addData("Shooter", shooter.getState());
                telemetry.update();
                sleep(20);
            }
            shooter.stop();
        } else {
            sleep(1000); // simulate shot time
        }
        // 4) Strafe to flower wall (72 to wall = 72, we are at 48, need +24 to wall flower)
        strafeRight(BioBuzzField.TILE_SIZE_IN, 0.5);
        // 5) Park small move into loading zone
        driveForward(12, 0.4);

        telemetry.addData("Status", "BioBuzz Demo Done");
        telemetry.update();
        sleep(2000);
    }

    private void driveForward(double inches, double power) {
        int ticks = BioBuzzField.inchesToTicks(inches);
        driveWithTicks(ticks, ticks, ticks, ticks, power, 5.0, "Fwd " + inches);
    }

    private void strafeRight(double inches, double power) {
        int ticks = BioBuzzField.inchesToTicks(inches);
        driveWithTicks(ticks, -ticks, -ticks, ticks, power, 5.0, "Strafe " + inches);
    }

    private void driveWithTicks(int fl, int fr, int bl, int br, double p, double timeout, String label) {
        int flT = frontLeft.getCurrentPosition() + fl;
        int frT = frontRight.getCurrentPosition() + fr;
        int blT = backLeft.getCurrentPosition() + bl;
        int brT = backRight.getCurrentPosition() + br;
        frontLeft.setTargetPosition(flT);
        frontRight.setTargetPosition(frT);
        backLeft.setTargetPosition(blT);
        backRight.setTargetPosition(brT);
        frontLeft.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        frontRight.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        backLeft.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        backRight.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        frontLeft.setPower(p); frontRight.setPower(p); backLeft.setPower(p); backRight.setPower(p);
        runtime.reset();
        while (opModeIsActive() && runtime.seconds() < timeout && (frontLeft.isBusy() || frontRight.isBusy() || backLeft.isBusy() || backRight.isBusy())) {
            telemetry.addData("Driving", label);
            telemetry.update();
        }
        frontLeft.setPower(0); frontRight.setPower(0); backLeft.setPower(0); backRight.setPower(0);
        frontLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        frontRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        backLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        backRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }
}
