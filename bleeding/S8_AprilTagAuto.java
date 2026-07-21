package org.firstinspires.ftc.teamcode;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.ftc.Actions;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import org.firstinspires.ftc.teamcode.util.HardwareNames;
import org.firstinspires.ftc.robotcore.external.hardware.camera.BuiltinCameraDirection;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;
import java.util.List;
@Disabled
@Autonomous(name = "AprilTag Alignment", group = "S8: Vision")
public class S8_AprilTagAuto extends LinearOpMode {
    private AprilTagProcessor aprilTag;
    private VisionPortal visionPortal;
    private int targetTagId = 5; //The tag required
    @Override
    public void runOpMode () throws InterruptedException {
        //Init
        Pose2d startPose = new Pose2d (0, 0, 0);
        MecanumDrive drive = new MecanumDrive (hardwareMap, startPose);
        initAprilTagDetection ();
        telemetry.addData ("Status", "Looking for Tag ID %d", targetTagId);
        telemetry.update ();
        waitForStart ();
        if (isStopRequested ()) {return;}
        //Drive to approximated position
        Actions.runBlocking (
            drive.actionBuilder (startPose)
                .lineToX (24)
                .build ()
        ); //Use AprilTag to align vectors
        AprilTagDetection targetTag = findTargetTag ();
        if ((targetTag != null) && (targetTag.metadata != null)) {
            telemetry.addLine ("Alignment in operation");
            telemetry.update ();
            //Get robot position relative to tag
            double xOffset = targetTag.robotPose.getPosition ().x * 39.3701; //inches
            double yOffset = targetTag.robotPose.getPosition ().y * 39.3701; //inches
            double headingError = Math.toDegrees (targetTag.robotPose.getOrientation ().getYaw ());
            //Small corrections for miniature alignments
            //Ex: If 5 inches to the right, strafe left 5 inches
            telemetry.addData ("Alignment xOffset", xOffset);
            telemetry.addData ("Alignment yOffset", yOffset);
            telemetry.addData ("Alignment headingError", headingError);
            telemetry.addLine ("Todo, convert camera-relative offsets to field-relative correction before moving.");
            telemetry.update ();
        } else {
            telemetry.addLine ("Tag Missing. Stasis");
            telemetry.update ();
        } //Continue autonomous runtime
        Actions.runBlocking (
            drive.actionBuilder (drive.localizer.getPose ())
                .lineToY (48)
                .build ()
        );
    } private AprilTagDetection findTargetTag () {
        //Try for 2 seconds to find the tag
        double startTime = System.currentTimeMillis ();
        while ((opModeIsActive ()) && ((System.currentTimeMillis () - startTime) < 2000)) {
            List<AprilTagDetection> detections = aprilTag.getDetections ();
            for (AprilTagDetection detection : detections) {if (detection.id == targetTagId) {return detection;}}
            telemetry.addData ("Searching", "Retrieving Tag");
            telemetry.update ();
            sleep (100);
        } return null;
    } private void initAprilTagDetection () {
        aprilTag = new AprilTagProcessor.Builder ()
            .setDrawAxes (true)
            .setDrawCubeProjection (true)
            .build ();
        VisionPortal.Builder builder = new VisionPortal.Builder ();
        if (hardwareMap.getAll (WebcamName.class).isEmpty ()) {builder.setCamera (BuiltinCameraDirection.BACK);} 
        else {builder.setCamera (hardwareMap.get (WebcamName.class, HardwareNames.WEBCAM));}
        builder.addProcessor (aprilTag);
        visionPortal = builder.build ();
    }
}
