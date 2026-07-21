package org.firstinspires.ftc.teamcode;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;
import org.firstinspires.ftc.teamcode.util.HardwareNames;
import org.firstinspires.ftc.teamcode.util.MathUtils;
@TeleOp (name = "Branch Reach", group = "Mechanisms")
public class s2_pollenated_branches extends LinearOpMode {
    private DcMotor slideLeft;
    private DcMotor slideRight;
    private Servo wristClaw;
    @Override
    public void runOpMode () {
        slideLeft = hardwareMap.get (DcMotor.class, HardwareNames.SLIDE_LEFT);
        slideRight = hardwareMap.get (DcMotor.class, HardwareNames.SLIDE_RIGHT);
        wristClaw = hardwareMap.get (Servo.class, HardwareNames.WRIST_CLAW);
        //One side reversed to rotate in the same direction
        slideLeft.setDirection (DcMotorSimple.Direction.REVERSE);
        slideRight.setDirection (DcMotorSimple.Direction.FORWARD);
        //Reset encoders and set zero
        slideLeft.setMode (DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        slideRight.setMode (DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        // Set to run to position, holding
        slideLeft.setMode (DcMotor.RunMode.RUN_TO_POSITION);
        slideRight.setMode (DcMotor.RunMode.RUN_TO_POSITION);
        waitForStart ();
        setSlideTarget (RobotConstants.SLIDE_GROUND);
        while (opModeIsActive ()) {
            //D-Pad, Slide Height
            if (gamepad2.dpad_up) {setSlideTarget (RobotConstants.SLIDE_SCORE_HIGH);}
            else if (gamepad2.dpad_down) {setSlideTarget (RobotConstants.SLIDE_LOW);}
            else if (gamepad2.dpad_left) {setSlideTarget (RobotConstants.SLIDE_GROUND);}
            //Right Bumper: Angle wrist to score on branch
            //Left Bumper: Angle wrist down to pick up pollen
            if (gamepad2.right_bumper) {wristClaw.setPosition (RobotConstants.SERVO_WRIST_SCORE);}
            else if (gamepad2.left_bumper) {wristClaw.setPosition (RobotConstants.SERVO_WRIST_PICKUP);}
            telemetry.addData ("Slide Target", slideLeft.getTargetPosition ());
            telemetry.addData ("Slide Current", slideLeft.getCurrentPosition ());
            telemetry.addData ("Slide Busy?", slideLeft.isBusy ());
            telemetry.update ();
        }
    } //Subfunction to send both motors to target position
    private void setSlideTarget (int target) {
        target = MathUtils.clamp (target, RobotConstants.SLIDE_GROUND, RobotConstants.SLIDE_MAX_SAFE);
        slideLeft.setTargetPosition (target);
        slideRight.setTargetPosition (target);  
        // Applying power is required to actually move in RUN_TO_POSITION mode
        slideLeft.setPower (0.8); 
        slideRight.setPower (0.8);
    }
}
