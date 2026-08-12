package org.firstinspires.ftc.teamcode;

import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Action;
import com.acmerobotics.roadrunner.Actions;
import com.acmerobotics.roadrunner.SequentialAction;

public final class MechanismActions {
    private MechanismActions() {}

    public static Action update(S12_IntegratedMechanism mechanism) {
        return new Action() {
            @Override
            public boolean run(TelemetryPacket packet) {
                mechanism.update(packet);
                return false;
            }
        };
    }

    public static Action startIntake(S12_IntegratedMechanism mechanism) {
        return new Action() {
            @Override
            public boolean run(TelemetryPacket packet) {
                mechanism.startIntakeSequence();
                packet.put("Mechanism Command", "startIntakeSequence");
                return false;
            }
        };
    }

    public static Action startScore(S12_IntegratedMechanism mechanism) {
        return new Action() {
            @Override
            public boolean run(TelemetryPacket packet) {
                mechanism.startScoreSequence();
                packet.put("Mechanism Command", "startScoreSequence");
                return false;
            }
        };
    }

    public static Action emergencyStop(S12_IntegratedMechanism mechanism) {
        return new Action() {
            @Override
            public boolean run(TelemetryPacket packet) {
                mechanism.emergencyStop();
                packet.put("Mechanism Command", "emergencyStop");
                return false;
            }
        };
    }

    public static Action intakeAndWait(S12_IntegratedMechanism mechanism, double timeoutSeconds) {
        return new SequentialAction(
            startIntake(mechanism),
            waitForAnyState(
                mechanism,
                timeoutSeconds,
                S12_IntegratedMechanism.State.FILLED,
                S12_IntegratedMechanism.State.CLOSING_CLAW,
                S12_IntegratedMechanism.State.ERROR
            )
        );
    }

    public static Action scoreAndWait(S12_IntegratedMechanism mechanism, double timeoutSeconds) {
        return new SequentialAction(
            startScore(mechanism),
            waitForAnyState(
                mechanism,
                timeoutSeconds,
                S12_IntegratedMechanism.State.IDLE,
                S12_IntegratedMechanism.State.ERROR
            )
        );
    }

    public static Action driveWithUpdates(Action driveAction, S12_IntegratedMechanism mechanism) {
        return new Action() {
            @Override
            public boolean run(TelemetryPacket packet) {
                mechanism.update(packet);
                return driveAction.run(packet);
            }
        };
    }

    public static Action waitForAnyState(
        S12_IntegratedMechanism mechanism,
        double timeoutSeconds,
        S12_IntegratedMechanism.State... states
    ) {
        return new Action() {
            private double startTime = -1;

            @Override
            public boolean run(TelemetryPacket packet) {
                mechanism.update(packet);

                S12_IntegratedMechanism.State current = mechanism.getState();
                for (S12_IntegratedMechanism.State candidate : states) {
                    if (current == candidate) {
                        return false;
                    }
                }

                if (startTime < 0) {
                    startTime = Actions.now();
                }

                double elapsed = Actions.now() - startTime;
                packet.put("Mechanism Wait Time", elapsed);

                if (elapsed > timeoutSeconds) {
                    packet.put("Mechanism Wait Timeout", true);
                    return false;
                }

                return true;
            }
        };
    }
}
