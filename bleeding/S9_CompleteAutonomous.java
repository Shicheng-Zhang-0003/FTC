package org.firstinspires.ftc.teamcode;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.acmerobotics.roadrunner.SequentialAction;
import com.acmerobotics.roadrunner.ParallelAction;
import com.acmerobotics.roadrunner.Action;
import com.acmerobotics.roadrunner.ftc.Actions;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;
import org.firstinspires.ftc.teamcode.util.HardwareNames;
import java.util.ArrayList;
@Disabled
@Autonomous (name = "Complete Autonomous", group = "S9: Full System")
public class S9_CompleteAutonomous extends LinearOpMode {
    private S7_StateMachineHopper hopper;
    private static final double INTAKE_X = 24;
    private static final double INTAKE_Y = 0;
    private static final double SCORING_X = 48;
    private static final double SCORING_Y = 24;
    private static double rrNow () {return com.acmerobotics.roadrunner.Actions.now ();}
    @Override
    public void runOpMode () throws InterruptedException {
        Pose2d startPose = new Pose2d (0, 0, 0);
        MecanumDrive drive = new MecanumDrive (hardwareMap, startPose);
ArrayList<String> missing = new ArrayList<> ();
                DcMotor intake = safeGet (DcMotor.class, HardwareNames.INTAKE_MOTOR);
        Servo dump = safeGet (Servo.class, HardwareNames.DUMP_SERVO);

        if (intake == null) {missing.add (HardwareNames.INTAKE_MOTOR);}
        if (dump == null) {missing.add (HardwareNames.DUMP_SERVO);}

        boolean hopperReady = false;
        hopper = new S7_StateMachineHopper ();
        if (intake != null && dump != null) {
            hopper.init (intake, dump);
            hopperReady = true;
        }
        // B20 fix: vision removed - S9 is a dead-reckoning training auto.
        // Tag-guided behavior belongs in S8 / CompetitionAuto, not a disabled legacy opmode.
        telemetry.addData("Status", hopperReady ? "Operational" : "MISSING MECHANISM HARDWARE"); // B23 fix: un-swallow from B20 comment
telemetry.addData ("Missing Devices", missing.isEmpty () ? "None" : missing.toString ());
        telemetry.update ();
        waitForStart ();
        if (isStopRequested ()) {return;}
        //B3 fix: do not run an auto whose hopper failed to initialize
        if (!hopperReady) {
            while (opModeIsActive ()) {
                telemetry.addData ("AUTO ABORTED", "Missing mechanism hardware");
                telemetry.addData ("Missing Devices", missing.isEmpty () ? "None" : missing.toString ());
                telemetry.update ();
                sleep (100);
            }
            return;
        }
        Actions.runBlocking (
            new SequentialAction (
                new ParallelAction (
                    drive.actionBuilder (startPose)
                        .splineTo (new Vector2d (INTAKE_X, INTAKE_Y), Math.PI / 4)
                        .build (),
                    new Action () {
                        @Override
                        public boolean run (TelemetryPacket packet) {
                            hopper.startIntake ();
                            packet.put ("Phase", "Dual Intake-Drivetrain");
                            return false;
                        }
                    }
                ), new Action () {
                    private double startTime = -1;
                    @Override
                    public boolean run (TelemetryPacket packet) {
                        if (startTime < 0) {startTime = rrNow ();}
                        double elapsed = rrNow () - startTime;
                        packet.put ("Phase", "Waiting for completed fill");
                        packet.put ("Filling Time Taken", elapsed);
                        if (elapsed > RobotConstants.TIMING_INTAKE_FILL_SECONDS) {
                            hopper.stopIntake ();
                            return false;
                        } return true;
                    }
                }, drive.actionBuilder (new Pose2d(INTAKE_X, INTAKE_Y, 0))
                    .splineTo (new Vector2d (SCORING_X, SCORING_Y), Math.PI / 2)
                    .build (),
                new Action() {
                    private double startTime = -1;
                    @Override
                    public boolean run (TelemetryPacket packet) {
                        if (startTime < 0) {
                            startTime = rrNow ();
                            hopper.startDump ();
                            packet.put ("Phase", "Dumping Pollen");
                        } double elapsed = rrNow () - startTime;
                        if (elapsed > RobotConstants.TIMING_DUMP_SECONDS) {
                            hopper.finishDump ();
                            packet.put ("Phase", "Dumping Completed");
                            return false;
                        } return true;
                    }
                }
            )
        ); telemetry.addData ("Status", "Autonomous runtime completed");
        telemetry.update ();
        sleep (2000);
    } private <T> T safeGet (Class<? extends T> type, String name) {
try {
return hardwareMap.get (type, name);
} catch (RuntimeException e) {
return null;
}
}
}
