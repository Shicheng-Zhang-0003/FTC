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
                DcMotorEx slide = safeGet (DcMotorEx.class, HardwareNames.SLIDE_MOTOR);
        Servo intakeServo = safeGet (Servo.class, HardwareNames.INTAKE_SERVO);
        DigitalChannel gamePieceSensor = safeGet (DigitalChannel.class, HardwareNames.GAME_PIECE_SENSOR);
        DigitalChannel bottomLimit = safeGet (DigitalChannel.class, HardwareNames.BOTTOM_LIMIT);

        if (slide == null) {missing.add (HardwareNames.SLIDE_MOTOR);}
        if (intakeServo == null) {missing.add (HardwareNames.INTAKE_SERVO);}
        if (gamePieceSensor == null) {missing.add (HardwareNames.GAME_PIECE_SENSOR);}
        if (bottomLimit == null) {missing.add (HardwareNames.BOTTOM_LIMIT);}

        boolean mechanismReady = false;
        S12_IntegratedMechanism mechanism = new S12_IntegratedMechanism ();
        if (slide != null && intakeServo != null && gamePieceSensor != null && bottomLimit != null) {
            mechanism.init (slide, intakeServo, gamePieceSensor, bottomLimit);
            mechanismReady = true;
        }

        telemetry.addData ("Status", mechanismReady ? "Competition Auto Ready" : "MISSING MECHANISM HARDWARE");
        telemetry.addData ("Road Runner Tuned", RobotReadiness.ROAD_RUNNER_TUNED);
        telemetry.addData ("Auto Ready", RobotReadiness.AUTO_READY);
telemetry.addData ("Missing Devices", missing.isEmpty () ? "None" : missing.toString ());
        telemetry.addData ("Sequence", "Drive -> Intake -> Drive -> Score");
        telemetry.update ();
        waitForStart ();
        if (isStopRequested ()) {return;}
        //B2 fix: do not run an auto whose mechanism failed to initialize
        if (!mechanismReady) {
            while (opModeIsActive ()) {
                telemetry.addData ("AUTO ABORTED", "Missing mechanism hardware");
                telemetry.addData ("Missing Devices", missing.toString ());
                telemetry.update ();
                sleep (100);
            }
            return;
        }
                // B11 fix: readiness gate - AUTO_READY must be true to run the auto
        if (!RobotReadiness.AUTO_READY) {
            while (opModeIsActive ()) {
                telemetry.addData ("AUTO BLOCKED", "RobotReadiness.AUTO_READY is false");
                telemetry.addData ("Required", "RR tuning + mechanism validation + verified paths");
                telemetry.update ();
                sleep (100);
            }
            return;
        }
        mechanism.resetControllers ();
        Pose2d intakePose = new Pose2d (24, 0, 0);
        Actions.runBlocking (
            new SequentialAction (
                MechanismActions.startIntake (mechanism),
                MechanismActions.driveWithUpdates (
                    drive.actionBuilder (startPose)
                        .lineToX (24)
                        .build (),
                    mechanism
                ),                 MechanismActions.waitForAnyState (
                mechanism,
                // B10 fix: 2.0s was too tight - the wait must cover slide travel to intake
                // height, game-piece detection, and claw close. ERROR is accepted too so a
                // failed mechanism fails fast instead of burning the whole timeout.
                4.0,
                S12_IntegratedMechanism.State.FILLED,
                // B24 fix: CLOSING_CLAW removed - startScoreSequence() only acts on FILLED,
                // so accepting it let the wait return before the piece was confirmed and
                // scoring silently became a no-op. Wait for FILLED (hasPiece guaranteed) instead.
                S12_IntegratedMechanism.State.ERROR
                ), MechanismActions.driveWithUpdates (
                    drive.actionBuilder (intakePose)
                        .lineToY (48)
                        .build (),
                    mechanism
                ), MechanismActions.startScore (mechanism),
                                MechanismActions.waitForAnyState (
                mechanism,
                // B10 fix: scoring worst case is move (<=3s) + hold (1s) + retract;
                // 3.0s could expire mid-sequence. ERROR accepted for fail-fast.
                5.0,
                S12_IntegratedMechanism.State.IDLE,
                S12_IntegratedMechanism.State.ERROR
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
