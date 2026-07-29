package org.firstinspires.ftc.teamcode;

import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.ftc.Actions;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

@Disabled
@Autonomous(name = "Competition Auto", group = "Competition")
public class CompetitionAuto extends LinearOpMode {

    @Override
    public void runOpMode() throws InterruptedException {
        Pose2d startPose = new Pose2d(0, 0, 0);
        MecanumDrive drive = new MecanumDrive(hardwareMap, startPose);

        // TODO: initialize real mechanism here when hardware is finalized

        telemetry.addData("Status", "Competition Auto Ready");
        telemetry.addData("Road Runner Tuned", RobotReadiness.ROAD_RUNNER_TUNED);
        telemetry.update();

        waitForStart();

        if (isStopRequested()) return;

        Actions.runBlocking(
            drive.actionBuilder(startPose)
                .lineToX(24)
                .turn(Math.toRadians(90))
                .lineToY(24)
                .build()
        );

        // TODO:
        // - intake
        // - score
        // - park

        telemetry.addData("Status", "Auto complete");
        telemetry.update();
    }
}
