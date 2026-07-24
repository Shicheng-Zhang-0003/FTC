package org.firstinspires.ftc.teamcode;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.acmerobotics.roadrunner.ftc.Actions;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
@Disabled
@Autonomous (name = "Competition Auto", group = "Competition")
public class CompetitionAuto extends LinearOpMode {
    @Override
    public void runOpMode () throws InterruptedException {
        Pose2d startPose = new Pose2d (0, 0, 0);
        MecanumDrive drive = new MecanumDrive (hardwareMap, startPose);
        //Todo, initialize real mechanism here
        telemetry.addData ("Status", "Competition Auto Ready");
        telemetry.update ();
        waitForStart ();
        if (isStopRequested ()) {return;}
        Actions.runBlocking (
            drive.actionBuilder (startPose)
                .lineToX (24)
                .turn (Math.toRadians (90))
                .lineToY (24)
                .build ()
        ); // Todo, Intake, Scoring, Parking
    }
}
