package org.firstinspires.ftc.teamcode;

import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.acmerobotics.roadrunner.ftc.Actions;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.robotcore.external.hardware.camera.BuiltinCameraDirection;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.teamcode.util.AprilTagFieldTransform;
import org.firstinspires.ftc.teamcode.util.HardwareNames;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;

import java.util.List;

@Disabled
@Autonomous(name = "AprilTag Alignment", group = "S8: Vision")
public class S8_AprilTagAuto extends LinearOpMode {
    private AprilTagProcessor aprilTag;
    private VisionPortal visionPortal;
    private int targetTagId = 5;

    @Override
    public void runOpMode() throws InterruptedException {
        Pose2d startPose = new Pose2d(0, 0, 0);
        MecanumDrive drive = new MecanumDrive(hardwareMap, startPose);
        initAprilTagDetection();
        telemetry.addData("Status", "Looking for Tag ID %d", targetTagId);
        telemetry.update();
        waitForStart();
        if (isStopRequested()) {
            if (visionPortal != null) visionPortal.close();
            return;
        }
        try {
            Actions.runBlocking(
                    drive.actionBuilder(startPose)
                            .lineToX(24)
                            .build()
            );
            AprilTagDetection targetTag = findTargetTag();
            // B18 fix: gate on ftcPose, not metadata. Alignment only needs the
            // tag-relative pose; ftcPose is valid without a tag library.
            if (targetTag != null && targetTag.ftcPose != null) {
                telemetry.addLine("Alignment in operation");
                telemetry.update();
                // B18 fix: diagnostics now come from ftcPose (tag relative to camera,
                // INCHES). robotPose is the robot's FIELD position in meters - these
                // readouts used to show ~24in of field coordinate, which is also why
                // the B9 >18in sanity clamp below always skipped the correction.
                double xOffset = targetTag.ftcPose.x; // inches, + = tag right of camera
                double yOffset = targetTag.ftcPose.y; // inches, + = tag downrange
                double headingError = targetTag.ftcPose.yaw; // degrees, tag yaw vs camera
                telemetry.addData("Alignment xOffset", xOffset);
                telemetry.addData("Alignment yOffset", yOffset);
                telemetry.addData("Alignment headingError", headingError);
                Vector2d alignVec = AprilTagFieldTransform.getAlignmentVector(targetTag);
                double headingErr = AprilTagFieldTransform.getHeadingError(targetTag);
                telemetry.addData("Align Forward(in)", alignVec.x);
                telemetry.addData("Align Strafe(in)", alignVec.y);
                telemetry.addData("Heading Error(deg)", headingErr);
                telemetry.update();
                // B9 fix: actually apply the correction (previously computed and telemetered only).
                // alignVec is robot-relative (x=forward, y=strafe-left); rotate it into the
                // field frame with the current heading, translate, then turn to square up.
                // Sanity clamp: corrections beyond ~18 inches are almost certainly misdetections.
                // If the robot drives AWAY from the tag on the real field, the sign convention
                // in AprilTagFieldTransform is inverted for your camera mount (bug B15):
                // flip fieldDx/fieldDy and/or the turn sign, and fix it at the source.
                double offsetMag = Math.hypot(alignVec.x, alignVec.y);
                if (offsetMag > 18.0) {
                    telemetry.addLine("Correction too large - likely misdetection, skipping");
                    telemetry.update();
                } else {
                    Pose2d pose = drive.localizer.getPose();
                    double heading = pose.heading.toDouble();
                    double fieldDx = alignVec.x * Math.cos(heading) - alignVec.y * Math.sin(heading);
                    double fieldDy = alignVec.x * Math.sin(heading) + alignVec.y * Math.cos(heading);
                    Actions.runBlocking(
                            drive.actionBuilder(pose)
                                    .strafeTo(new Vector2d(pose.position.x + fieldDx, pose.position.y + fieldDy))
                                    .turn(Math.toRadians(headingErr))
                                    .build()
                    );
                    telemetry.addLine("Correction applied");
                    telemetry.update();
                }
            } else {
                telemetry.addLine("Tag Missing. Stasis");
                telemetry.update();
            }
            Actions.runBlocking(
                    drive.actionBuilder(drive.localizer.getPose())
                            .lineToY(48)
                            .build()
            );
        } finally {
            if (visionPortal != null) visionPortal.close();
        }
    }

    private AprilTagDetection findTargetTag() {
        double startTime = System.currentTimeMillis();
        while (opModeIsActive() && (System.currentTimeMillis() - startTime) < 2000) {
            List<AprilTagDetection> detections = aprilTag.getDetections();
            for (AprilTagDetection detection : detections) {
                if (detection.id == targetTagId) return detection;
            }
            telemetry.addData("Searching", "Retrieving Tag");
            telemetry.update();
            sleep(100);
        }
        return null;
    }

    private void initAprilTagDetection() {
        aprilTag = new AprilTagProcessor.Builder()
                .setDrawAxes(true)
                .setDrawCubeProjection(true)
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
