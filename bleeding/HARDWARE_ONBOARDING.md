# Hardware Onboarding Checklist

## Purpose
Use this when the robot hardware exists and you are ready to validate the electrical/mechanical configuration.

## Step 1: Configuration names
Confirm that the active configuration on the Robot Controller contains these exact device names:

### Drive
- front_left
- front_right
- back_left
- back_right

### Mechanism
- slide_motor
- intake_servo
- game_piece_sensor
- bottom_limit

### Hang
- winch_motor
- top_limit

### Localization / vision
- imu
- Main Cam

Optional / future:
- sensor_otos
- pinpoint
- par
- perp
- par0
- par1

## Step 2: Run Hardware Validation
Use:

- `Hardware Validation` TeleOp

Check:
- [ ] all devices are detected
- [ ] missing device list is empty
- [ ] drive motors spin in the expected direction
- [ ] slide motor moves safely with dpad
- [ ] winch moves safely with y/x
- [ ] intake servo open/close works
- [ ] digital sensors report expected raw values

## Step 3: Run Subsystem Test
Use:

- `Subsystem Test` TeleOp

Check:
- [ ] mechanism initializes
- [ ] intake sequence works
- [ ] score sequence works
- [ ] emergency stop works
- [ ] hang subsystem initializes
- [ ] hang limit switch works

## Step 4: Update readiness flags
In `RobotReadiness.java`, set flags to true only after validation:

- [ ] DRIVE_READY
- [ ] MECHANISM_READY
- [ ] HANG_READY
- [ ] VISION_READY
- [ ] ROAD_RUNNER_TUNED
- [ ] AUTO_READY

## Step 5: Begin Road Runner tuning
Only after drive hardware is validated:

- [ ] AngularRampLogger
- [ ] ForwardRampLogger
- [ ] LateralRampLogger
- [ ] ManualFeedforwardTuner
- [ ] ManualFeedbackTuner
- [ ] LocalizationTest
- [ ] SplineTest

## Step 6: Verify Competition Auto
Use:

- `Competition Auto`

Current auto is still a skeleton. It should only be enabled after:
- [ ] Road Runner is tuned
- [ ] mechanism is validated
- [ ] auto paths are physically verified
