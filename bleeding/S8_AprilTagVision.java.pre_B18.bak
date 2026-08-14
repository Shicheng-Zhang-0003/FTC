package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import org.firstinspires.ftc.teamcode.util.HardwareNames;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.firstinspires.ftc.robotcore.external.hardware.camera.BuiltinCameraDirection;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;
import java.util.List;
@Disabled
@TeleOp(name = "AprilTag Test", group = "S8: Vision")
public class S8_AprilTagVision extends LinearOpMode {
    private AprilTagProcessor aprilTag;
    private VisionPortal visionPortal;
    @Override
    public void runOpMode () {
        initAprilTagDetection ();
        telemetry.addData ("Status", "Vision init");
        telemetry.update ();
        waitForStart ();
        if (isStopRequested ()) {return;}
        while (opModeIsActive ()) {
            //Get all detected tags for instrument
            List<AprilTagDetection> detections = aprilTag.getDetections ();
            telemetry.addData ("# Tags Found", detections.size ());
            //Process each detected tag
            for (AprilTagDetection detection : detections) {
                //Tag metadata
                telemetry.addLine (String.format ("Tag ID: %d", detection.id));
                //Robot position relative to tag (in meters)
                if (detection.metadata != null) {
                    //X, left/right (positive direction = right)
                    //Y, forward/backward (positive direction = forward)
                    //Z, up/down (positive direction = up)
                    double x = detection.robotPose.getPosition ().x;
                    double y = detection.robotPose.getPosition ().y;
                    double z = detection.robotPose.getPosition ().z;
                    telemetry.addLine (String.format ("Position: X=%.2f, Y=%.2f, Z=%.2f m", x, y, z));
                    //Convert to inches, better FTC nominal unit
                    double xInches = x * 39.3701;
                    double yInches = y * 39.3701;
                    telemetry.addLine (String.format ("Position: %.1f inches right, %.1f inches forward", xInches, yInches));
                    //Robot vector relative to tag
                    double heading = Math.toDegrees (detection.robotPose.getOrientation ().getYaw ());
                    telemetry.addLine (String.format ("Heading: %.1f degrees", heading));
                } telemetry.addLine (); //Blank line between tags
            } telemetry.update ();
        }
    } private void initAprilTagDetection () {
        //AprilTag processor
        aprilTag = new AprilTagProcessor.Builder ()
            .setDrawAxes (true)
            .setDrawCubeProjection (true)
            .setDrawTagOutline (true)
            .build ();
        //Vision portal
        VisionPortal.Builder builder = new VisionPortal.Builder ();
        //Webcam
        if (hardwareMap.getAll (WebcamName.class).isEmpty ()) {builder.setCamera (BuiltinCameraDirection.BACK);} 
        else {builder.setCamera (hardwareMap.get(WebcamName.class, HardwareNames.WEBCAM));}
        builder.addProcessor (aprilTag);
        visionPortal = builder.build ();
    }
}
