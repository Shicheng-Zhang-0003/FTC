# Road Runner Tuning Checklist

## Before tuning
- [ ] Confirm motor names in HardwareNames.java match the active configuration
- [ ] Confirm IMU orientation in MecanumDrive.Params
- [ ] Confirm localizer choice
- [ ] Confirm wheel direction
- [ ] Confirm encoder direction
- [ ] Confirm RobotReadiness.ROAD_RUNNER_TUNED is still false until complete

## Tuning steps
- [ ] AngularRampLogger
- [ ] ForwardRampLogger
- [ ] LateralRampLogger
- [ ] ManualFeedforwardTuner
- [ ] ManualFeedbackTuner
- [ ] LocalizationTest
- [ ] SplineTest

## Values to fill in
- [ ] inPerTick
- [ ] lateralInPerTick
- [ ] trackWidthTicks
- [ ] kS
- [ ] kV
- [ ] kA
- [ ] axialGain
- [ ] lateralGain
- [ ] headingGain
- [ ] axialVelGain
- [ ] lateralVelGain
- [ ] headingVelGain

## After tuning
- [ ] Set RobotReadiness.ROAD_RUNNER_TUNED = true
- [ ] Re-run LocalizationTest
- [ ] Re-run SplineTest
- [ ] Verify CompetitionAuto paths
