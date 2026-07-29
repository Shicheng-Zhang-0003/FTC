package org.firstinspires.ftc.teamcode;
import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.acmerobotics.roadrunner.ftc.Actions;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
@Autonomous (name = "Auto, RoadRunner Example", group = "S5: RoadRunner")
public class S5_RoadRunnerAutoExample extends LinearOpMode {
    @Override
    public void runOpMode () throws InterruptedException {
        //Initialize MecanumDrive from Quickstart
        Pose2d beginPose = new Pose2d (0, 0, 0);
        MecanumDrive drive = new MecanumDrive (hardwareMap, beginPose);
        //Setup dashboard telemetry
        FtcDashboard dashboard = FtcDashboard.getInstance ();
        telemetry = new MultipleTelemetry (telemetry, dashboard.getTelemetry ());
        telemetry.addData ("Status", "Ready!");
        telemetry.update ();
        waitForStart ();
        if (isStopRequested ()) {return;}
        //RoadRunner 1.0 --> ActionBuilder with chaining
        //Drive forward 24 inches, strafe right 24 inches, turns 90 degrees
        Actions.runBlocking (
            drive.actionBuilder (beginPose)
                .lineToX (24)
                .strafeTo (new Vector2d (24, 24))
                .turn (Math.toRadians (90))
                .build ()
        ); telemetry.addData ("Status", "Done!");
        telemetry.update ();
        sleep (2000);
    }
}
