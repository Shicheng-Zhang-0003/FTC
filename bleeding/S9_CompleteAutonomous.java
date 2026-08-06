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
import org.firstinspires.ftc.robotcore.external.hardware.camera.BuiltinCameraDirection;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;
import java.util.List;
@Disabled
@Autonomous (name = "Complete Autonomous", group = "S9: Full System")
public class S9_CompleteAutonomous extends LinearOpMode {
    private AprilTagProcessor aprilTag;
    private VisionPortal visionPortal;
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
        DcMotor intake = hardwareMap.get (DcMotor.class, HardwareNames.INTAKE_MOTOR);
        Servo dump = hardwareMap.get (Servo.class, HardwareNames.DUMP_SERVO);
        hopper = new S7_StateMachineHopper ();
        hopper.init (intake, dump);
        initVision ();
        telemetry.addData ("Status", "Operational");
        telemetry.addData ("Target Tag", "Looking for ID 5");
        telemetry.update ();
        waitForStart ();
        if (isStopRequested ()) {return;}
        int targetTagId = detectTargetTag ();
        telemetry.addData ("Detected Tag", targetTagId);
        telemetry.update ();
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
    } private int detectTargetTag () {
        double startTime = System.currentTimeMillis ();
        while ((opModeIsActive ()) && ((System.currentTimeMillis () - startTime) < 3000)) {
            List<AprilTagDetection> detections = aprilTag.getDetections ();
            for (AprilTagDetection detection : detections) {if (detection.id == 5) {return detection.id;}}
            sleep (100);
        } return -1;
    } private void initVision () {
        aprilTag = new AprilTagProcessor.Builder ()
            .setDrawAxes (true)
            .setDrawCubeProjection (true)
            .build ();
        VisionPortal.Builder builder = new VisionPortal.Builder ();
        if (hardwareMap.getAll (WebcamName.class).isEmpty ()) {builder.setCamera(BuiltinCameraDirection.BACK);} 
        else {builder.setCamera (hardwareMap.get (WebcamName.class, HardwareNames.WEBCAM));}
        builder.addProcessor (aprilTag);
        visionPortal = builder.build ();
    }
}
