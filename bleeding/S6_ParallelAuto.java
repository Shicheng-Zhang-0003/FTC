package org.firstinspires.ftc.teamcode;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.acmerobotics.roadrunner.SequentialAction;
import com.acmerobotics.roadrunner.ParallelAction;
import com.acmerobotics.roadrunner.ftc.Actions;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;
import org.firstinspires.ftc.teamcode.util.HardwareNames;
@Disabled
@Autonomous (name = "Parallel Operands", group = "S6: Advanced")
public class S6_ParallelAuto extends LinearOpMode {
    @Override
    public void runOpMode () throws InterruptedException {
        //Init
        Pose2d startPose = new Pose2d (0, 0, 0);
        MecanumDrive drive = new MecanumDrive (hardwareMap, startPose);
        S6_PollenHopper hopper = new S6_PollenHopper ();
DcMotor intakeMotor = hardwareMap.get (DcMotor.class, HardwareNames.INTAKE_MOTOR);
Servo dumpServo = hardwareMap.get (Servo.class, HardwareNames.DUMP_SERVO);
hopper.init (intakeMotor, dumpServo);
        telemetry.addData ("Status", "Ready for Parallel Magic!");
        telemetry.update ();
        waitForStart ();
        if (isStopRequested ()) {return;}
        //Actual Parallel Runtime
        Actions.runBlocking (
            new SequentialAction (
                //Drive to pollen AND turn on the intake SIMULTANEOUSLY
                new ParallelAction (
                    hopper.startIntake (),
                    drive.actionBuilder (startPose)
                        .splineTo (new Vector2d (24, 0), Math.PI / 2)
                        .build ()
                ), //Wait for the hopper to fill completely
                //The intake is still spinning from the previous command because no termination command is sent yet
                hopper.waitForFill (RobotConstants.TIMING_INTAKE_FILL_SECONDS),
                //Stop the intake AND turn around SIMULTANEOUSLY
                new ParallelAction (
                    hopper.stopIntake (),
                    drive.actionBuilder (new Pose2d (24, 0, Math.PI / 2))
                        .turn (Math.PI) // Turn 180 degrees to face the scoring area
                        .build ()
                ), //Drive to the scoring area
                drive.actionBuilder (new Pose2d (24, 0, -Math.PI / 2))
                    .lineToY (48)
                    .build (),
                //Dump Collected Pollen
                hopper.dumpHopper ()
            )
        );
    }
}
