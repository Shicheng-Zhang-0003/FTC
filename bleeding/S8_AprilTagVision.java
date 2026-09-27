package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.hardware.camera.BuiltinCameraDirection;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.teamcode.util.HardwareNames;
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
    public void runOpMode() {
        initAprilTagDetection();
        telemetry.addData("Status", "Vision init");
        telemetry.update();
        waitForStart();
        if (isStopRequested()) {
            if (visionPortal != null) visionPortal.close();
            return;
        }
        try {
            while (opModeIsActive()) {
                List<AprilTagDetection> detections = aprilTag.getDetections();
                telemetry.addData("# Tags Found", detections.size());
                for (AprilTagDetection detection : detections) {
                    telemetry.addLine(String.format("Tag ID: %d", detection.id));
                    // B18 fix: ftcPose is the tag's pose relative to the CAMERA, in inches,
                    // and needs no metadata. robotPose is the robot's FIELD position estimate -
                    // the old block mislabeled it as 'robot position relative to tag'.
                    // FTC convention: x + = tag right of camera, y + = downrange, z + = up.
                    if (detection.ftcPose != null) {
                        double x = detection.ftcPose.x;
                        double y = detection.ftcPose.y;
                        double z = detection.ftcPose.z;
                        telemetry.addLine(String.format("Tag offset: X=%.2f, Y=%.2f, Z=%.2f in", x, y, z));
                        telemetry.addLine(String.format("Range %.1f in, Bearing %.1f deg, Elevation %.1f deg", detection.ftcPose.range, detection.ftcPose.bearing, detection.ftcPose.elevation));
                        double yaw = detection.ftcPose.yaw;
                        telemetry.addLine(String.format("Tag yaw vs camera: %.1f degrees", yaw));
                    }
                    telemetry.addLine();
                }
                telemetry.update();
            }
        } finally {
            if (visionPortal != null) visionPortal.close();
        }
    }

    private void initAprilTagDetection() {
        aprilTag = new AprilTagProcessor.Builder()
                .setDrawAxes(true)
                .setDrawCubeProjection(true)
                .setDrawTagOutline(true)
                .build();
        VisionPortal.Builder builder = new VisionPortal.Builder();
        if (hardwareMap.getAll(WebcamName.class).isEmpty()) {
            builder.setCamera(BuiltinCameraDirection.BACK);
        } else {
            builder.setCamera(hardwareMap.get(WebcamName.class, HardwareNames.WEBCAM));
        }
        builder.addProcessor(aprilTag);
        visionPortal = builder.build();
    }
}
