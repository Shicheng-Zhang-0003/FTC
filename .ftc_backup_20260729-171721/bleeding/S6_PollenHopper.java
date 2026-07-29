package org.firstinspires.ftc.teamcode;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Action;
import com.acmerobotics.roadrunner.Actions;
//This class represents physical mechanisms.
public class S6_PollenHopper {
    //Todo, add real hardware fields and init () before enabling hardware commands.
    public Action startIntake () {
        return new Action () {
            @Override
            public boolean run (TelemetryPacket packet) {
                //intakeMotor.setPower (1.0);
                packet.put ("Hopper", "Intake Online");
                return false;
            }
        };
    } public Action stopIntake () {
        return new Action () {
            @Override
            public boolean run (TelemetryPacket packet) {
                //intakeMotor.setPower (0);
                packet.put ("Hopper", "Intake Stasis");
                return false;
            }
        };
    } public Action waitForFill (double seconds) {
        return new Action () {
            private double startTime = -1;
            @Override
            public boolean run (TelemetryPacket packet) {
                if (startTime < 0) {startTime = Actions.now ();}
                double elapsed = Actions.now () - startTime;
                packet.put ("Fill Timer", elapsed);
                return elapsed < seconds;
            }
        };
    } public Action dumpHopper () {
        return new Action () {
            private double startTime = -1;
            @Override
            public boolean run (TelemetryPacket packet) {
                if (startTime < 0) {
                    startTime = Actions.now ();
                    //dumpServo.setPosition (RobotConstants.SERVO_DUMP_ACTIVE);
                    packet.put ("Hopper", "Dumping");
                } double elapsed = Actions.now () - startTime;
                if (elapsed < RobotConstants.TIMING_DUMP_SECONDS) {return true;} 
                else {
                    //dumpServo.setPosition (RobotConstants.SERVO_DUMP_STOWED);
                    packet.put ("Hopper", "Stowed");
                    return false;
                }
            }
        };
    }
}
