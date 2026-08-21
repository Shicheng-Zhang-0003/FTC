package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;
import org.firstinspires.ftc.teamcode.util.HardwareNames;
@Disabled
@TeleOp (name = "Pollen Hopper", group = "Mechanisms")
public class s2_pollen_hopper extends LinearOpMode {
    private DcMotor intakeRoller;
    private Servo dumpBedTilt;
    @Override
    public void runOpMode () {
        //Servo positions, tune before deployment
        intakeRoller = hardwareMap.get (DcMotor.class, HardwareNames.INTAKE_ROLLER);
        dumpBedTilt = hardwareMap.get (Servo.class, HardwareNames.DUMP_BED_TILT);
        intakeRoller.setDirection (DcMotorSimple.Direction.FORWARD);
        dumpBedTilt.setPosition (RobotConstants.SERVO_DUMP_STOWED);
        telemetry.addData ("Status", "Initialized");
        telemetry.update ();
        waitForStart ();
        while (opModeIsActive ()) {
            //Intake
            //Right Trigger: Intake pollen to belly
            //Left Trigger: Reverse intake, clear jams
            if (gamepad2.right_trigger > RobotConstants.DRIVE_TRIGGER_DEADBAND) {intakeRoller.setPower (gamepad2.right_trigger);} 
            else if (gamepad2.left_trigger > RobotConstants.DRIVE_TRIGGER_DEADBAND) {intakeRoller.setPower (-gamepad2.left_trigger);} 
            else {intakeRoller.setPower (0);}
            //Spitout
            //A: Tilt bed to dump pollen into Hive
            //B: Pull bed back to stowed position
            if (gamepad2.a) {dumpBedTilt.setPosition (RobotConstants.SERVO_DUMP_ACTIVE);} 
            else if (gamepad2.b) {dumpBedTilt.setPosition (RobotConstants.SERVO_DUMP_STOWED);}
            telemetry.addData ("Intake Power", intakeRoller.getPower ());
            telemetry.addData ("Bed Position", dumpBedTilt.getPosition ());
            telemetry.update ();
        }
    }
}
