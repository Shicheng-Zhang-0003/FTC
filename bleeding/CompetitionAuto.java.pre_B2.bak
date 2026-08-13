package org.firstinspires.ftc.teamcode;
import com.acmerobotics.roadrunner.SequentialAction;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.ftc.Actions;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.Servo;
import org.firstinspires.ftc.teamcode.util.HardwareNames;
import java.util.ArrayList;
@Disabled
@Autonomous (name = "Competition Auto", group = "Competition")
public class CompetitionAuto extends LinearOpMode {
    @Override
    public void runOpMode () throws InterruptedException {
        Pose2d startPose = new Pose2d (0, 0, 0);
        MecanumDrive drive = new MecanumDrive (hardwareMap, startPose);
ArrayList<String> missing = new ArrayList<> ();
        S12_IntegratedMechanism mechanism = new S12_IntegratedMechanism ();
        mechanism.init (
            safeGet (DcMotorEx.class, HardwareNames.SLIDE_MOTOR),
            safeGet (Servo.class, HardwareNames.INTAKE_SERVO),
            safeGet (DigitalChannel.class, HardwareNames.GAME_PIECE_SENSOR),
            safeGet (DigitalChannel.class, HardwareNames.BOTTOM_LIMIT)
        ); telemetry.addData ("Status", "Competition Auto Ready");
        telemetry.addData ("Road Runner Tuned", RobotReadiness.ROAD_RUNNER_TUNED);
        telemetry.addData ("Auto Ready", RobotReadiness.AUTO_READY);
telemetry.addData ("Missing Devices", missing.isEmpty () ? "None" : missing.toString ());
        telemetry.addData ("Sequence", "Drive -> Intake -> Drive -> Score");
        telemetry.update ();
        waitForStart ();
        if (isStopRequested ()) {return;}
        mechanism.resetControllers ();
        Pose2d intakePose = new Pose2d (24, 0, 0);
        Pose2d scorePose = new Pose2d (24, 48, 0);
        Actions.runBlocking (
            new SequentialAction (
                MechanismActions.startIntake (mechanism),
                MechanismActions.driveWithUpdates (
                    drive.actionBuilder (startPose)
                        .lineToX (24)
                        .build (),
                    mechanism
                ), MechanismActions.waitForAnyState (
                    mechanism,
                    2.0,
                    S12_IntegratedMechanism.State.FILLED,
                    S12_IntegratedMechanism.State.CLOSING_CLAW
                ), MechanismActions.driveWithUpdates (
                    drive.actionBuilder (intakePose)
                        .lineToY (48)
                        .build (),
                    mechanism
                ), MechanismActions.startScore (mechanism),
                MechanismActions.waitForAnyState (
                    mechanism,
                    3.0,
                    S12_IntegratedMechanism.State.IDLE
                ), MechanismActions.emergencyStop (mechanism)
            )
        ); telemetry.addData ("Status", "Auto complete");
        telemetry.update ();
    }

private <T> T safeGet (Class<? extends T> type, String name) {
try {
return hardwareMap.get (type, name);
} catch (RuntimeException e) {
return null;
}
}
}
