package org.firstinspires.ftc.teamcode.util;
import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.Vector2d;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
@Config
public final class AprilTagFieldTransform {
private AprilTagFieldTransform () {}
// Camera mounting position on robot (inches from robot center)
// MEASURE THESE on your physical robot and update via FTC Dashboard
public static double CAMERA_X_OFFSET = 0.0;   // forward(+)/backward(-) from center
public static double CAMERA_Y_OFFSET = 0.0;   // left(+)/right(-) from center
public static double CAMERA_Z_OFFSET = 5.0;   // height above ground (inches)
public static double CAMERA_PITCH_DEG = 0.0;  // tilt angle in degrees
public static double CAMERA_YAW_DEG = 0.0;    // rotation from robot forward axis
// Meters to inches conversion factor
private static final double M_TO_IN = 39.3701;
/**
* Converts camera-relative AprilTag detection into robot-relative offset.
* Applies camera mounting yaw to rotate from camera frame to robot frame,
* then adds the physical mounting offset.
*
* @param detection The AprilTag detection (must have valid metadata)
* @return Robot-relative Vector2d (x=forward, y=left) in inches, or null if invalid
*/
public static Vector2d getRobotRelativeOffset (AprilTagDetection detection) {
if ((detection == null) || (detection.metadata == null)) {return null;}
// Get camera-relative position in meters, convert to inches
double camX = detection.robotPose.getPosition ().x * M_TO_IN;
double camY = detection.robotPose.getPosition ().y * M_TO_IN;
// Apply camera yaw to rotate into robot frame
double yawRad = Math.toRadians (CAMERA_YAW_DEG);
double robotX = camX * Math.cos (yawRad) - camY * Math.sin (yawRad) + CAMERA_X_OFFSET;
double robotY = camX * Math.sin (yawRad) + camY * Math.cos (yawRad) + CAMERA_Y_OFFSET;
return new Vector2d (robotX, robotY);
}
/**
* Returns the heading error between the robot and the tag, in degrees.
* Positive means the robot needs to rotate counterclockwise.
*
* @param detection The AprilTag detection (must have valid metadata)
* @return Heading error in degrees, or 0 if detection is invalid
*/
public static double getHeadingError (AprilTagDetection detection) {
if ((detection == null) || (detection.metadata == null)) {return 0.0;}
double camYawDeg = Math.toDegrees (detection.robotPose.getOrientation ().getYaw ());
return camYawDeg + CAMERA_YAW_DEG;
}
/**
* Simple alignment vector: returns the drive correction needed to center on the tag.
* Use this for quick alignment without full field localization.
* The returned vector tells you how far to drive (forward, strafe) to align.
*
* @param detection The AprilTag detection (must have valid metadata)
* @return Correction vector in inches (x=forward, y=strafe), or zero vector if invalid
*/
public static Vector2d getAlignmentVector (AprilTagDetection detection) {
Vector2d offset = getRobotRelativeOffset (detection);
if (offset == null) {return new Vector2d (0, 0);}
// Invert: if tag is 5 inches to the right, robot must move 5 inches right
return new Vector2d (-offset.x, -offset.y);
}
/**
* Full field-relative correction: computes the target robot pose given
* the tag's known field position and the camera-relative detection.
*
* @param detection    The AprilTag detection (must have valid metadata)
* @param tagFieldPose Known field position of the tag (from field layout)
* @return Target Pose2d for the robot, or null if detection is invalid
*/
public static Pose2d getFieldCorrection (AprilTagDetection detection, Pose2d tagFieldPose) {
if ((detection == null) || (detection.metadata == null)) {return null;}
Vector2d robotOffset = getRobotRelativeOffset (detection);
double headingError = getHeadingError (detection);
// Target position: tag position minus the robot's offset from tag
double targetX = tagFieldPose.position.x - robotOffset.x;
double targetY = tagFieldPose.position.y - robotOffset.y;
double targetHeading = tagFieldPose.heading.toDouble () - Math.toRadians (headingError);
return new Pose2d (targetX, targetY, targetHeading);
}
}
