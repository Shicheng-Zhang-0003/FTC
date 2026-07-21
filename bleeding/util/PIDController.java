package org.firstinspires.ftc.teamcode.util;

public class PIDController {
    private double kP, kI, kD;
    private double integralSum = 0;
    private double lastError = 0;
    private double integralClamp = 1000;
    private double outputClamp = 1.0;
    private boolean firstUpdate = true;

    public double pTerm = 0;
    public double iTerm = 0;
    public double dTerm = 0;

    public PIDController(double kP, double kI, double kD) {
        setGains(kP, kI, kD);
    }

    public void setGains(double kP, double kI, double kD) {
        this.kP = kP;
        this.kI = kI;
        this.kD = kD;
    }

    public void setIntegralClamp(double integralClamp) {
        this.integralClamp = Math.abs(integralClamp);
    }

    public void setOutputClamp(double outputClamp) {
        this.outputClamp = Math.abs(outputClamp);
    }

    public void reset() {
        integralSum = 0;
        lastError = 0;
        firstUpdate = true;
        pTerm = 0;
        iTerm = 0;
        dTerm = 0;
    }

    public double update(double error, double dt) {
        if (dt <= 0) dt = 0.001;

        pTerm = kP * error;

        integralSum += error * dt;
        integralSum = Math.max(-integralClamp, Math.min(integralClamp, integralSum));
        iTerm = kI * integralSum;

        if (firstUpdate) {
            dTerm = 0;
            firstUpdate = false;
        } else {
            dTerm = kD * (error - lastError) / dt;
        }

        lastError = error;

        double output = pTerm + iTerm + dTerm;
        return Math.max(-outputClamp, Math.min(outputClamp, output));
    }
}
