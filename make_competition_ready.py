#!/usr/bin/env python3
import re
import shutil
import sys
from datetime import datetime
from pathlib import Path

ROOT = Path.cwd()
BLEED = ROOT / "bleeding"

if not BLEED.exists() or not BLEED.is_dir():
    sys.exit("ERROR: Run this from the FTC folder, and make sure ./bleeding exists.")

BACKUP_ROOT = ROOT / f".ftc_backup_{datetime.now().strftime('%Y%m%d-%H%M%S')}"
BACKED_UP = set()

DISABLED_IMPORT = "import com.qualcomm.robotcore.eventloop.opmode.Disabled;"
HARDWARE_IMPORT = "import org.firstinspires.ftc.teamcode.util.HardwareNames;"
MATHUTILS_IMPORT = "import org.firstinspires.ftc.teamcode.util.MathUtils;"
PID_IMPORT = "import org.firstinspires.ftc.teamcode.util.PIDController;"
DEBOUNCE_IMPORT = "import org.firstinspires.ftc.teamcode.util.DebouncedBoolean;"


def backup_file(p: Path):
    p = Path(p).resolve()
    if not p.exists() or p in BACKED_UP:
        return

    try:
        rel = p.relative_to(ROOT.resolve())
    except ValueError:
        rel = Path(p.name)

    dest = BACKUP_ROOT / rel
    dest.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy2(p, dest)
    BACKED_UP.add(p)


def write_file(relpath: str, content: str):
    p = BLEED / relpath

    if p.exists():
        old = p.read_text()
        if old == content:
            print(f"UNCHANGED {relpath}")
            return
        backup_file(p)

    p.parent.mkdir(parents=True, exist_ok=True)
    p.write_text(content)
    print(f"WROTE     {relpath}")


def ensure_import_text(text: str, import_line: str) -> str:
    if import_line in text:
        return text

    return re.sub(
        r'(^package\s+[^;]+;\s*\n)',
        lambda m: m.group(1) + "\n" + import_line + "\n",
        text,
        count=1,
        flags=re.M
    )


def patch_file(relpath: str, replacements=(), regexes=(), imports=()):
    p = BLEED / relpath
    if not p.exists():
        print(f"SKIP      {relpath} (missing)")
        return

    text = p.read_text()
    orig = text

    for old, new in replacements:
        text = text.replace(old, new)

    for pattern, repl in regexes:
        text = re.sub(pattern, repl, text, count=1)

    for imp in imports:
        text = ensure_import_text(text, imp)

    if text != orig:
        backup_file(p)
        p.write_text(text)
        print(f"PATCHED   {relpath}")
    else:
        print(f"UNCHANGED {relpath}")


def normalize_util_folder():
    desired = BLEED / "util"
    alternatives = ["utils", "utility"]

    for alt in alternatives:
        alt_dir = BLEED / alt
        if not alt_dir.is_dir():
            continue

        if not desired.exists():
            alt_dir.rename(desired)
            print(f"RENAMED   {alt}/ -> util/")
        else:
            for f in [
                "HardwareNames.java",
                "MathUtils.java",
                "PIDController.java",
                "DebouncedBoolean.java",
            ]:
                fp = alt_dir / f
                if fp.exists():
                    backup_file(fp)
                    fp.unlink()
                    print(f"REMOVED   {alt}/{f}")

            try:
                alt_dir.rmdir()
                print(f"REMOVED   empty {alt}/")
            except OSError:
                pass


def apply_global_hardware_names():
    patterns = [
        # MecanumDrive quickstart names
        (r'hardwareMap\.get\s*\(\s*DcMotorEx\.class\s*,\s*"leftFront"\s*\)',
         'hardwareMap.get(DcMotorEx.class, HardwareNames.FRONT_LEFT)'),
        (r'hardwareMap\.get\s*\(\s*DcMotorEx\.class\s*,\s*"leftBack"\s*\)',
         'hardwareMap.get(DcMotorEx.class, HardwareNames.BACK_LEFT)'),
        (r'hardwareMap\.get\s*\(\s*DcMotorEx\.class\s*,\s*"rightBack"\s*\)',
         'hardwareMap.get(DcMotorEx.class, HardwareNames.BACK_RIGHT)'),
        (r'hardwareMap\.get\s*\(\s*DcMotorEx\.class\s*,\s*"rightFront"\s*\)',
         'hardwareMap.get(DcMotorEx.class, HardwareNames.FRONT_RIGHT)'),

        # Mecanum names if used with DcMotorEx
        (r'hardwareMap\.get\s*\(\s*DcMotorEx\.class\s*,\s*"front_left"\s*\)',
         'hardwareMap.get(DcMotorEx.class, HardwareNames.FRONT_LEFT)'),
        (r'hardwareMap\.get\s*\(\s*DcMotorEx\.class\s*,\s*"front_right"\s*\)',
         'hardwareMap.get(DcMotorEx.class, HardwareNames.FRONT_RIGHT)'),
        (r'hardwareMap\.get\s*\(\s*DcMotorEx\.class\s*,\s*"back_left"\s*\)',
         'hardwareMap.get(DcMotorEx.class, HardwareNames.BACK_LEFT)'),
        (r'hardwareMap\.get\s*\(\s*DcMotorEx\.class\s*,\s*"back_right"\s*\)',
         'hardwareMap.get(DcMotorEx.class, HardwareNames.BACK_RIGHT)'),

        # Mecanum / tank names with DcMotor
        (r'hardwareMap\.get\s*\(\s*DcMotor\.class\s*,\s*"front_left"\s*\)',
         'hardwareMap.get(DcMotor.class, HardwareNames.FRONT_LEFT)'),
        (r'hardwareMap\.get\s*\(\s*DcMotor\.class\s*,\s*"front_right"\s*\)',
         'hardwareMap.get(DcMotor.class, HardwareNames.FRONT_RIGHT)'),
        (r'hardwareMap\.get\s*\(\s*DcMotor\.class\s*,\s*"back_left"\s*\)',
         'hardwareMap.get(DcMotor.class, HardwareNames.BACK_LEFT)'),
        (r'hardwareMap\.get\s*\(\s*DcMotor\.class\s*,\s*"back_right"\s*\)',
         'hardwareMap.get(DcMotor.class, HardwareNames.BACK_RIGHT)'),

        # Tank names
        (r'hardwareMap\.get\s*\(\s*DcMotorEx\.class\s*,\s*"left"\s*\)',
         'hardwareMap.get(DcMotorEx.class, HardwareNames.LEFT_MOTOR)'),
        (r'hardwareMap\.get\s*\(\s*DcMotorEx\.class\s*,\s*"right"\s*\)',
         'hardwareMap.get(DcMotorEx.class, HardwareNames.RIGHT_MOTOR)'),
        (r'hardwareMap\.get\s*\(\s*DcMotor\.class\s*,\s*"left_motor"\s*\)',
         'hardwareMap.get(DcMotor.class, HardwareNames.LEFT_MOTOR)'),
        (r'hardwareMap\.get\s*\(\s*DcMotor\.class\s*,\s*"right_motor"\s*\)',
         'hardwareMap.get(DcMotor.class, HardwareNames.RIGHT_MOTOR)'),

        # Slide / lift / winch motors
        (r'hardwareMap\.get\s*\(\s*DcMotorEx\.class\s*,\s*"slide_motor"\s*\)',
         'hardwareMap.get(DcMotorEx.class, HardwareNames.SLIDE_MOTOR)'),
        (r'hardwareMap\.get\s*\(\s*DcMotor\.class\s*,\s*"slide_motor"\s*\)',
         'hardwareMap.get(DcMotor.class, HardwareNames.SLIDE_MOTOR)'),
        (r'hardwareMap\.get\s*\(\s*DcMotor\.class\s*,\s*"slide_left"\s*\)',
         'hardwareMap.get(DcMotor.class, HardwareNames.SLIDE_LEFT)'),
        (r'hardwareMap\.get\s*\(\s*DcMotor\.class\s*,\s*"slide_right"\s*\)',
         'hardwareMap.get(DcMotor.class, HardwareNames.SLIDE_RIGHT)'),
        (r'hardwareMap\.get\s*\(\s*DcMotor\.class\s*,\s*"winch_motor"\s*\)',
         'hardwareMap.get(DcMotor.class, HardwareNames.WINCH_MOTOR)'),

        # Intake / hopper motors
        (r'hardwareMap\.get\s*\(\s*DcMotor\.class\s*,\s*"intake_motor"\s*\)',
         'hardwareMap.get(DcMotor.class, HardwareNames.INTAKE_MOTOR)'),
        (r'hardwareMap\.get\s*\(\s*DcMotor\.class\s*,\s*"intake_roller"\s*\)',
         'hardwareMap.get(DcMotor.class, HardwareNames.INTAKE_ROLLER)'),

        # Servos
        (r'hardwareMap\.get\s*\(\s*Servo\.class\s*,\s*"dump_servo"\s*\)',
         'hardwareMap.get(Servo.class, HardwareNames.DUMP_SERVO)'),
        (r'hardwareMap\.get\s*\(\s*Servo\.class\s*,\s*"intake_servo"\s*\)',
         'hardwareMap.get(Servo.class, HardwareNames.INTAKE_SERVO)'),
        (r'hardwareMap\.get\s*\(\s*Servo\.class\s*,\s*"wrist_claw"\s*\)',
         'hardwareMap.get(Servo.class, HardwareNames.WRIST_CLAW)'),
        (r'hardwareMap\.get\s*\(\s*Servo\.class\s*,\s*"dump_bed_tilt"\s*\)',
         'hardwareMap.get(Servo.class, HardwareNames.DUMP_BED_TILT)'),
        (r'hardwareMap\.get\s*\(\s*Servo\.class\s*,\s*"test_claw"\s*\)',
         'hardwareMap.get(Servo.class, HardwareNames.TEST_CLAW)'),

        # Digital channels
        (r'hardwareMap\.get\s*\(\s*DigitalChannel\.class\s*,\s*"game_piece_sensor"\s*\)',
         'hardwareMap.get(DigitalChannel.class, HardwareNames.GAME_PIECE_SENSOR)'),
        (r'hardwareMap\.get\s*\(\s*DigitalChannel\.class\s*,\s*"top_limit"\s*\)',
         'hardwareMap.get(DigitalChannel.class, HardwareNames.TOP_LIMIT)'),
        (r'hardwareMap\.get\s*\(\s*DigitalChannel\.class\s*,\s*"bottom_limit"\s*\)',
         'hardwareMap.get(DigitalChannel.class, HardwareNames.BOTTOM_LIMIT)'),

        # Localization devices
        (r'hardwareMap\.get\s*\(\s*SparkFunOTOS\.class\s*,\s*"sensor_otos"\s*\)',
         'hardwareMap.get(SparkFunOTOS.class, HardwareNames.OTOS)'),
        (r'hardwareMap\.get\s*\(\s*GoBildaPinpointDriver\.class\s*,\s*"pinpoint"\s*\)',
         'hardwareMap.get(GoBildaPinpointDriver.class, HardwareNames.PINPOINT)'),

        # Dead wheel encoders
        (r'hardwareMap\.get\s*\(\s*DcMotorEx\.class\s*,\s*"par0"\s*\)',
         'hardwareMap.get(DcMotorEx.class, HardwareNames.PAR0)'),
        (r'hardwareMap\.get\s*\(\s*DcMotorEx\.class\s*,\s*"par1"\s*\)',
         'hardwareMap.get(DcMotorEx.class, HardwareNames.PAR1)'),
        (r'hardwareMap\.get\s*\(\s*DcMotorEx\.class\s*,\s*"perp"\s*\)',
         'hardwareMap.get(DcMotorEx.class, HardwareNames.PERP)'),
        (r'hardwareMap\.get\s*\(\s*DcMotorEx\.class\s*,\s*"par"\s*\)',
         'hardwareMap.get(DcMotorEx.class, HardwareNames.PAR)'),

        # Vision
        (r'hardwareMap\.get\s*\(\s*WebcamName\.class\s*,\s*"Main Cam"\s*\)',
         'hardwareMap.get(WebcamName.class, HardwareNames.WEBCAM)'),
    ]

    for p in BLEED.rglob("*.java"):
        rel = p.relative_to(BLEED)
        if rel.parts and rel.parts[0] == "util":
            continue

        text = p.read_text()
        orig = text

        for pattern, repl in patterns:
            text = re.sub(pattern, repl, text)

        # IMU used by Road Runner drive classes
        text = re.sub(
            r'new LazyHardwareMapImu\s*\(\s*hardwareMap\s*,\s*"imu"',
            'new LazyHardwareMapImu(hardwareMap, HardwareNames.IMU',
            text
        )

        # If you previously used utility/ instead of util/
        text = text.replace(
            "import org.firstinspires.ftc.teamcode.utility.",
            "import org.firstinspires.ftc.teamcode.util."
        )

        if text != orig:
            if "HardwareNames." in text and HARDWARE_IMPORT not in text:
                text = ensure_import_text(text, HARDWARE_IMPORT)

            backup_file(p)
            p.write_text(text)
            print(f"PATCHED   {rel}")


def ensure_util_imports_where_used():
    for p in BLEED.rglob("*.java"):
        rel = p.relative_to(BLEED)
        if rel.parts and rel.parts[0] == "util":
            continue

        text = p.read_text()
        orig = text

        if "HardwareNames." in text and HARDWARE_IMPORT not in text:
            text = ensure_import_text(text, HARDWARE_IMPORT)

        if "MathUtils." in text and MATHUTILS_IMPORT not in text:
            text = ensure_import_text(text, MATHUTILS_IMPORT)

        if "PIDController" in text and PID_IMPORT not in text and "class PIDController" not in text:
            text = ensure_import_text(text, PID_IMPORT)

        if "DebouncedBoolean" in text and DEBOUNCE_IMPORT not in text and "class DebouncedBoolean" not in text:
            text = ensure_import_text(text, DEBOUNCE_IMPORT)

        if text != orig:
            backup_file(p)
            p.write_text(text)
            print(f"PATCHED   {rel}")


def add_disabled_to_opmode(relpath: str):
    p = BLEED / relpath
    if not p.exists():
        print(f"SKIP      {relpath} (missing)")
        return

    text = p.read_text()
    orig = text

    if "@Disabled" in text:
        text = ensure_import_text(text, DISABLED_IMPORT)
        if text != orig:
            backup_file(p)
            p.write_text(text)
            print(f"PATCHED   {relpath}")
        else:
            print(f"UNCHANGED {relpath}")
        return

    text = ensure_import_text(text, DISABLED_IMPORT)

    if re.search(r'(?m)^[ \t]*@TeleOp\b', text):
        text = re.sub(r'(?m)^([ \t]*)(@TeleOp\b)', r'\1@Disabled\n\1\2', text, count=1)
    elif re.search(r'(?m)^[ \t]*@Autonomous\b', text):
        text = re.sub(r'(?m)^([ \t]*)(@Autonomous\b)', r'\1@Disabled\n\1\2', text, count=1)
    else:
        print(f"SKIP      {relpath} (no @TeleOp or @Autonomous found)")
        return

    if text != orig:
        backup_file(p)
        p.write_text(text)
        print(f"PATCHED   {relpath}")
    else:
        print(f"UNCHANGED {relpath}")


def fix_mecanum_turn_action():
    patch_file(
        "MecanumDrive.java",
        regexes=[
            (
                r'leftFront\.setPower\(feedforward\.compute\(wheelVels\.leftFront\) / voltage\);\s*'
                r'leftBack\.setPower\(feedforward\.compute\(wheelVels\.leftBack\) / voltage\);\s*'
                r'rightBack\.setPower\(feedforward\.compute\(wheelVels\.rightBack\) / voltage\);\s*'
                r'rightFront\.setPower\(feedforward\.compute\(wheelVels\.rightFront\) / voltage\);',
                'leftFront.setPower(leftFrontPower);\n'
                '                leftBack.setPower(leftBackPower);\n'
                '                rightBack.setPower(rightBackPower);\n'
                '                rightFront.setPower(rightFrontPower);'
            )
        ]
    )


def fix_s9():
    rel = "S9_CompleteAutonomous.java"
    p = BLEED / rel
    if not p.exists():
        print(f"SKIP      {rel} (missing)")
        return

    text = p.read_text()
    orig = text

    # Remove bad preselect if present
    text = re.sub(r',\s*preselectTeleOp\s*=\s*"TeleOp: Default"', '', text)

    # Fix wrong Actions.now() usage if present
    if re.search(r'(?<!\.)Actions\.now\s*\(\s*\)', text) and "private static double rrNow" not in text:
        text = re.sub(r'(?<!\.)Actions\.now\s*\(\s*\)', 'rrNow()', text)

        helper = (
            "\n\n"
            "    private static double rrNow() {\n"
            "        return com.acmerobotics.roadrunner.Actions.now();\n"
            "    }\n"
        )

        stripped = text.rstrip()
        if stripped.endswith("}"):
            text = stripped[:-1].rstrip() + helper + "}\n"

    if text != orig:
        backup_file(p)
        p.write_text(text)
        print(f"PATCHED   {rel}")
    else:
        print(f"UNCHANGED {rel}")


# -----------------------------
# Full-file contents
# -----------------------------

HARDWARE_NAMES_JAVA = r'''package org.firstinspires.ftc.teamcode.util;

public final class HardwareNames {
    private HardwareNames() {}

    // Drivetrain
    public static final String FRONT_LEFT = "front_left";
    public static final String FRONT_RIGHT = "front_right";
    public static final String BACK_LEFT = "back_left";
    public static final String BACK_RIGHT = "back_right";

    public static final String LEFT_MOTOR = "left_motor";
    public static final String RIGHT_MOTOR = "right_motor";

    // Sensors / localization
    public static final String IMU = "imu";
    public static final String OTOS = "sensor_otos";
    public static final String PINPOINT = "pinpoint";
    public static final String PAR = "par";
    public static final String PERP = "perp";
    public static final String PAR0 = "par0";
    public static final String PAR1 = "par1";

    // Mechanisms
    public static final String SLIDE_MOTOR = "slide_motor";
    public static final String SLIDE_LEFT = "slide_left";
    public static final String SLIDE_RIGHT = "slide_right";
    public static final String WRIST_CLAW = "wrist_claw";

    public static final String INTAKE_MOTOR = "intake_motor";
    public static final String DUMP_SERVO = "dump_servo";
    public static final String INTAKE_SERVO = "intake_servo";

    public static final String INTAKE_ROLLER = "intake_roller";
    public static final String DUMP_BED_TILT = "dump_bed_tilt";

    public static final String WINCH_MOTOR = "winch_motor";

    public static final String TOP_LIMIT = "top_limit";
    public static final String BOTTOM_LIMIT = "bottom_limit";
    public static final String GAME_PIECE_SENSOR = "game_piece_sensor";

    // Testing and vision
    public static final String TEST_CLAW = "test_claw";
    public static final String WEBCAM = "Main Cam";
}
'''

MATH_UTILS_JAVA = r'''package org.firstinspires.ftc.teamcode.util;

public final class MathUtils {
    private MathUtils() {}

    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public static double deadband(double value, double threshold) {
        return Math.abs(value) < threshold ? 0.0 : value;
    }

    public static boolean within(double value, double target, double tolerance) {
        return Math.abs(value - target) <= tolerance;
    }
}
'''

PID_CONTROLLER_JAVA = r'''package org.firstinspires.ftc.teamcode.util;

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
'''

DEBOUNCED_BOOLEAN_JAVA = r'''package org.firstinspires.ftc.teamcode.util;

public class DebouncedBoolean {
    private boolean stableValue;
    private double stableTime = 0;

    public DebouncedBoolean(boolean initialValue) {
        stableValue = initialValue;
    }

    public boolean update(boolean rawValue, double dt, double debounceTime) {
        if (debounceTime <= 0) {
            stableValue = rawValue;
            stableTime = 0;
            return stableValue;
        }

        if (rawValue == stableValue) {
            stableTime = 0;
            return stableValue;
        }

        stableTime += dt;
        if (stableTime >= debounceTime) {
            stableValue = rawValue;
            stableTime = 0;
        }

        return stableValue;
    }
}
'''

ROBOT_CONSTANTS_JAVA = r'''package org.firstinspires.ftc.teamcode;

import com.acmerobotics.dashboard.config.Config;

@Config
public class RobotConstants {
    // Drive
    public static double DRIVE_MECANUM_STRAFE_COMPENSATION = 1.1;
    public static double DRIVE_STICK_DEADBAND = 0.05;
    public static double DRIVE_TRIGGER_DEADBAND = 0.1;
    public static double DRIVE_TICKS_PER_INCH = 38.2;

    // Servos
    public static double SERVO_CLAW_OPEN = 0.8;
    public static double SERVO_CLAW_CLOSED = 0.2;

    public static double SERVO_INTAKE_OPEN = 0.82;
    public static double SERVO_INTAKE_CLOSED = 0.35;

    public static double SERVO_DUMP_STOWED = 0.10;
    public static double SERVO_DUMP_ACTIVE = 0.90;

    public static double SERVO_WRIST_PICKUP = 0.8;
    public static double SERVO_WRIST_SCORE = 0.2;

    // Slide PID
    public static double SLIDE_P = 10.0;
    public static double SLIDE_I = 0.5;
    public static double SLIDE_D = 1.0;
    public static double SLIDE_INTEGRAL_CLAMP = 1000.0;
    public static double SLIDE_OUTPUT_CLAMP = 1.0;

    // Slide positions
    public static int SLIDE_GROUND = 0;
    public static int SLIDE_LOW = 500;
    public static int SLIDE_INTAKE = 500;
    public static int SLIDE_SCORE_LOW = 1000;
    public static int SLIDE_SCORE_HIGH = 1500;
    public static int SLIDE_MAX_SAFE = 1900;
    public static int SLIDE_POSITION_TOLERANCE = 50;

    // Sensors
    public static double SENSOR_GAME_PIECE_DISTANCE_CM = 5.0;
    public static boolean SENSOR_LIMIT_SWITCH_INVERTED = true;
    public static boolean SENSOR_GAME_PIECE_INVERTED = false;
    public static double SENSOR_LIMIT_SWITCH_DEBOUNCE_SECONDS = 0.05;

    // Timing
    public static double TIMING_SCORE_HOLD_SECONDS = 1.0;
    public static double TIMING_DUMP_SECONDS = 1.0;
    public static double TIMING_INTAKE_FILL_SECONDS = 1.5;
}
'''

HANG_SUBSYSTEM_JAVA = r'''package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.DigitalChannel;

import org.firstinspires.ftc.teamcode.util.DebouncedBoolean;

public class HangSubsystem {
    private DcMotor winchMotor;
    private DigitalChannel topLimitSwitch;

    private final DebouncedBoolean topLimitDebounced = new DebouncedBoolean(false);
    private boolean topPressed = false;

    public void init(DcMotor winch, DigitalChannel topLimit) {
        winchMotor = winch;
        topLimitSwitch = topLimit;

        topLimitSwitch.setMode(DigitalChannel.Mode.INPUT);
        winchMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        winchMotor.setDirection(DcMotorSimple.Direction.FORWARD);
    }

    public void update(double dt, boolean raiseRequested, boolean lowerRequested) {
        topPressed = topLimitDebounced.update(
                limitPressed(topLimitSwitch.getState(), RobotConstants.SENSOR_LIMIT_SWITCH_INVERTED),
                dt,
                RobotConstants.SENSOR_LIMIT_SWITCH_DEBOUNCE_SECONDS
        );

        if (raiseRequested && !topPressed) {
            winchMotor.setPower(1.0);
        } else if (lowerRequested) {
            winchMotor.setPower(-0.5);
        } else {
            winchMotor.setPower(0);
        }
    }

    public boolean isTopPressed() {
        return topPressed;
    }

    public double getPower() {
        return winchMotor.getPower();
    }

    private boolean limitPressed(boolean raw, boolean inverted) {
        return inverted ? !raw : raw;
    }
}
'''

ROBOT_READINESS_JAVA = r'''package org.firstinspires.ftc.teamcode;

public final class RobotReadiness {
    private RobotReadiness() {}

    public static final boolean DRIVE_READY = true;
    public static final boolean MECHANISM_READY = false;
    public static final boolean HANG_READY = false;
    public static final boolean VISION_READY = false;
    public static final boolean ROAD_RUNNER_TUNED = false;
}
'''

COMPETITION_TELEOP_JAVA = r'''package org.firstinspires.ftc.teamcode;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.util.HardwareNames;
import org.firstinspires.ftc.teamcode.util.MathUtils;

@TeleOp(name = "Competition TeleOp", group = "Competition")
public class CompetitionTeleOp extends LinearOpMode {

    private DcMotor frontLeft;
    private DcMotor frontRight;
    private DcMotor backLeft;
    private DcMotor backRight;

    private S12_IntegratedMechanism mechanism;
    private HangSubsystem hang;

    private final ElapsedTime loopTimer = new ElapsedTime();

    @Override
    public void runOpMode() {
        frontLeft = hardwareMap.get(DcMotor.class, HardwareNames.FRONT_LEFT);
        frontRight = hardwareMap.get(DcMotor.class, HardwareNames.FRONT_RIGHT);
        backLeft = hardwareMap.get(DcMotor.class, HardwareNames.BACK_LEFT);
        backRight = hardwareMap.get(DcMotor.class, HardwareNames.BACK_RIGHT);

        frontLeft.setDirection(DcMotorSimple.Direction.REVERSE);
        backLeft.setDirection(DcMotorSimple.Direction.REVERSE);
        frontRight.setDirection(DcMotorSimple.Direction.FORWARD);
        backRight.setDirection(DcMotorSimple.Direction.FORWARD);

        DcMotorEx slide = hardwareMap.get(DcMotorEx.class, HardwareNames.SLIDE_MOTOR);
        Servo intakeServo = hardwareMap.get(Servo.class, HardwareNames.INTAKE_SERVO);
        DigitalChannel gamePieceSensor = hardwareMap.get(DigitalChannel.class, HardwareNames.GAME_PIECE_SENSOR);
        DigitalChannel bottomLimit = hardwareMap.get(DigitalChannel.class, HardwareNames.BOTTOM_LIMIT);

        mechanism = new S12_IntegratedMechanism();
        mechanism.init(slide, intakeServo, gamePieceSensor, bottomLimit);

        hang = new HangSubsystem();
        hang.init(
            hardwareMap.get(DcMotor.class, HardwareNames.WINCH_MOTOR),
            hardwareMap.get(DigitalChannel.class, HardwareNames.TOP_LIMIT)
        );

        telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());
        telemetry.addData("Status", "Competition TeleOp Ready");
        telemetry.addData("Drive Ready", RobotReadiness.DRIVE_READY);
        telemetry.addData("Mechanism Ready", RobotReadiness.MECHANISM_READY);
        telemetry.addData("Hang Ready", RobotReadiness.HANG_READY);
        telemetry.addData("Vision Ready", RobotReadiness.VISION_READY);
        telemetry.addData("Road Runner Tuned", RobotReadiness.ROAD_RUNNER_TUNED);
        telemetry.update();

        waitForStart();

        mechanism.resetControllers();
        loopTimer.reset();

        while (opModeIsActive()) {
            double dt = loopTimer.seconds();
            if (dt < 0.001) dt = 0.001;

            // -------------------
            // Drive
            // -------------------
            double y = MathUtils.deadband(-gamepad1.left_stick_y, RobotConstants.DRIVE_STICK_DEADBAND);
            double x = MathUtils.deadband(gamepad1.left_stick_x, RobotConstants.DRIVE_STICK_DEADBAND)
                    * RobotConstants.DRIVE_MECANUM_STRAFE_COMPENSATION;
            double rx = MathUtils.deadband(gamepad1.right_stick_x, RobotConstants.DRIVE_STICK_DEADBAND);

            double denominator = Math.max(Math.abs(y) + Math.abs(x) + Math.abs(rx), 1);

            double frontLeftPower = (y + x + rx) / denominator;
            double frontRightPower = (y - x - rx) / denominator;
            double backLeftPower = (y - x + rx) / denominator;
            double backRightPower = (y + x - rx) / denominator;

            frontLeft.setPower(frontLeftPower);
            frontRight.setPower(frontRightPower);
            backLeft.setPower(backLeftPower);
            backRight.setPower(backRightPower);

            // -------------------
            // Mechanism controls
            // -------------------
            if (gamepad2.a) {
                mechanism.startIntakeSequence();
            } else if (gamepad2.b) {
                mechanism.startScoreSequence();
            } else if (gamepad2.x) {
                mechanism.emergencyStop();
            }

            // -------------------
            // Hang controls
            // -------------------
            hang.update(dt, gamepad2.y, gamepad2.left_bumper);

            // -------------------
            // Mechanism update
            // -------------------
            TelemetryPacket packet = new TelemetryPacket();
            mechanism.update(packet);
            FtcDashboard.getInstance().sendTelemetryPacket(packet);

            // -------------------
            // Driver telemetry
            // -------------------
            telemetry.addData("Mechanism State", mechanism.getState());
            telemetry.addData("Hang Top Limit", hang.isTopPressed());
            telemetry.addData("Winch Power", hang.getPower());
            telemetry.update();

            loopTimer.reset();
        }
    }
}
'''

COMPETITION_AUTO_JAVA = r'''package org.firstinspires.ftc.teamcode;

import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.ftc.Actions;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

@Disabled
@Autonomous(name = "Competition Auto", group = "Competition")
public class CompetitionAuto extends LinearOpMode {

    @Override
    public void runOpMode() throws InterruptedException {
        Pose2d startPose = new Pose2d(0, 0, 0);
        MecanumDrive drive = new MecanumDrive(hardwareMap, startPose);

        // TODO: initialize real mechanism here when hardware is finalized

        telemetry.addData("Status", "Competition Auto Ready");
        telemetry.addData("Road Runner Tuned", RobotReadiness.ROAD_RUNNER_TUNED);
        telemetry.update();

        waitForStart();

        if (isStopRequested()) return;

        Actions.runBlocking(
            drive.actionBuilder(startPose)
                .lineToX(24)
                .turn(Math.toRadians(90))
                .lineToY(24)
                .build()
        );

        // TODO:
        // - intake
        // - score
        // - park

        telemetry.addData("Status", "Auto complete");
        telemetry.update();
    }
}
'''

S2_HANG_JAVA = r'''package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.util.DebouncedBoolean;
import org.firstinspires.ftc.teamcode.util.HardwareNames;

@Disabled
@TeleOp(name = "Canopy Hanging Parking", group = "Mechanisms")
public class s2_hang_branch_endgame extends LinearOpMode {
    private DcMotor winchMotor;
    private DigitalChannel topLimitSwitch;

    private final ElapsedTime loopTimer = new ElapsedTime();
    private final DebouncedBoolean topLimitDebounced = new DebouncedBoolean(false);

    @Override
    public void runOpMode() {
        winchMotor = hardwareMap.get(DcMotor.class, HardwareNames.WINCH_MOTOR);
        topLimitSwitch = hardwareMap.get(DigitalChannel.class, HardwareNames.TOP_LIMIT);

        topLimitSwitch.setMode(DigitalChannel.Mode.INPUT);

        winchMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        winchMotor.setDirection(DcMotorSimple.Direction.FORWARD);

        telemetry.addData("Status", "Operational-RDY");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            double dt = loopTimer.seconds();
            if (dt < 0.001) dt = 0.001;

            boolean topPressed = topLimitDebounced.update(
                limitPressed(topLimitSwitch.getState(), RobotConstants.SENSOR_LIMIT_SWITCH_INVERTED),
                dt,
                RobotConstants.SENSOR_LIMIT_SWITCH_DEBOUNCE_SECONDS
            );

            loopTimer.reset();

            if ((gamepad2.y) && (!topPressed)) {
                winchMotor.setPower(1.0);
            } else if (gamepad2.x) {
                winchMotor.setPower(-0.5);
            } else {
                winchMotor.setPower(0);
            }

            telemetry.addData("Limit Switch Pressed", topPressed);
            telemetry.addData("Winch Power", winchMotor.getPower());
            telemetry.update();
        }
    }

    private boolean limitPressed(boolean raw, boolean inverted) {
        return inverted ? !raw : raw;
    }
}
'''

S6_POLLEN_HOPPER_JAVA = r'''package org.firstinspires.ftc.teamcode;

import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Action;
import com.acmerobotics.roadrunner.Actions;

// This class represents physical mechanisms.
public class S6_PollenHopper {
    // TODO: add real hardware fields and init() before enabling hardware commands.

    public Action startIntake() {
        return new Action() {
            @Override
            public boolean run(TelemetryPacket packet) {
                // intakeMotor.setPower(1.0);
                packet.put("Hopper", "Intake Online");
                return false;
            }
        };
    }

    public Action stopIntake() {
        return new Action() {
            @Override
            public boolean run(TelemetryPacket packet) {
                // intakeMotor.setPower(0);
                packet.put("Hopper", "Intake Stasis");
                return false;
            }
        };
    }

    public Action waitForFill(double seconds) {
        return new Action() {
            private double startTime = -1;

            @Override
            public boolean run(TelemetryPacket packet) {
                if (startTime < 0) {
                    startTime = Actions.now();
                }

                double elapsed = Actions.now() - startTime;
                packet.put("Fill Timer", elapsed);

                return elapsed < seconds;
            }
        };
    }

    public Action dumpHopper() {
        return new Action() {
            private double startTime = -1;

            @Override
            public boolean run(TelemetryPacket packet) {
                if (startTime < 0) {
                    startTime = Actions.now();
                    // dumpServo.setPosition(RobotConstants.SERVO_DUMP_ACTIVE);
                    packet.put("Hopper", "Dumping");
                }

                double elapsed = Actions.now() - startTime;

                if (elapsed < RobotConstants.TIMING_DUMP_SECONDS) {
                    return true;
                } else {
                    // dumpServo.setPosition(RobotConstants.SERVO_DUMP_STOWED);
                    packet.put("Hopper", "Stowed");
                    return false;
                }
            }
        };
    }
}
'''

S12_INTEGRATED_MECHANISM_JAVA = r'''package org.firstinspires.ftc.teamcode;

import org.firstinspires.ftc.teamcode.util.HardwareNames;
import org.firstinspires.ftc.teamcode.util.MathUtils;
import org.firstinspires.ftc.teamcode.util.PIDController;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

public class S12_IntegratedMechanism {
    public enum State {
        IDLE,
        MOVING_TO_INTAKE,
        INTAKING,
        MOVING_TO_SCORE,
        SCORING,
        ERROR
    }

    private State currentState = State.IDLE;

    private DcMotorEx slideMotor;
    private Servo intakeServo;
    private DigitalChannel gamePieceSensor;
    private DigitalChannel bottomLimit;

    private int targetPosition = 0;
    private PIDController pid;
    private final ElapsedTime pidTimer = new ElapsedTime();
    private final ElapsedTime stateTimer = new ElapsedTime();

    public void init(DcMotorEx slide, Servo intake, DigitalChannel sensor, DigitalChannel limit) {
        slideMotor = slide;
        intakeServo = intake;
        gamePieceSensor = sensor;
        bottomLimit = limit;

        slideMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        slideMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        slideMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        gamePieceSensor.setMode(DigitalChannel.Mode.INPUT);
        bottomLimit.setMode(DigitalChannel.Mode.INPUT);

        intakeServo.setPosition(RobotConstants.SERVO_INTAKE_CLOSED);

        pid = new PIDController(
            RobotConstants.SLIDE_P,
            RobotConstants.SLIDE_I,
            RobotConstants.SLIDE_D
        );
        pid.setIntegralClamp(RobotConstants.SLIDE_INTEGRAL_CLAMP);
        pid.setOutputClamp(RobotConstants.SLIDE_OUTPUT_CLAMP);

        pidTimer.reset();
        pid.reset();
        stateTimer.reset();

        currentState = State.IDLE;
    }

    public State getState() {
        return currentState;
    }

    public void resetControllers() {
        pidTimer.reset();
        pid.reset();
    }

    private void setTargetPosition(int target) {
        targetPosition = MathUtils.clamp(
            target,
            RobotConstants.SLIDE_GROUND,
            RobotConstants.SLIDE_MAX_SAFE
        );
    }

    private boolean limitPressed(boolean raw, boolean inverted) {
        return inverted ? !raw : raw;
    }

    private boolean hasGamePiece() {
        boolean raw = gamePieceSensor.getState();
        return RobotConstants.SENSOR_GAME_PIECE_INVERTED ? !raw : raw;
    }

    private boolean atBottom() {
        return limitPressed(bottomLimit.getState(), RobotConstants.SENSOR_LIMIT_SWITCH_INVERTED);
    }

    private boolean atTargetPosition() {
        int error = Math.abs(targetPosition - slideMotor.getCurrentPosition());
        return error < RobotConstants.SLIDE_POSITION_TOLERANCE;
    }

    private void updatePID() {
        int currentPosition = slideMotor.getCurrentPosition();
        double error = targetPosition - currentPosition;

        double dt = pidTimer.seconds();
        if (dt < 0.001) dt = 0.001;

        pid.setGains(
            RobotConstants.SLIDE_P,
            RobotConstants.SLIDE_I,
            RobotConstants.SLIDE_D
        );

        double power = pid.update(error, dt);
        slideMotor.setPower(power);

        pidTimer.reset();
    }

    public void startIntakeSequence() {
        if (currentState == State.IDLE) {
            currentState = State.MOVING_TO_INTAKE;
            setTargetPosition(RobotConstants.SLIDE_INTAKE);
            intakeServo.setPosition(RobotConstants.SERVO_INTAKE_OPEN);
        }
    }

    public void startScoreSequence() {
        if ((currentState == State.IDLE) || (currentState == State.INTAKING)) {
            currentState = State.MOVING_TO_SCORE;
            setTargetPosition(RobotConstants.SLIDE_SCORE_HIGH);
        }
    }

    public void emergencyStop() {
        slideMotor.setPower(0);
        intakeServo.setPosition(RobotConstants.SERVO_INTAKE_CLOSED);
        currentState = State.IDLE;
    }

    public void update(TelemetryPacket packet) {
        updatePID();

        switch (currentState) {
            case MOVING_TO_INTAKE:
                if (atTargetPosition()) {
                    currentState = State.INTAKING;
                }
                break;

            case INTAKING:
                if (hasGamePiece()) {
                    intakeServo.setPosition(RobotConstants.SERVO_INTAKE_CLOSED);
                    currentState = State.IDLE;
                }
                break;

            case MOVING_TO_SCORE:
                if (atTargetPosition()) {
                    currentState = State.SCORING;
                    intakeServo.setPosition(RobotConstants.SERVO_INTAKE_OPEN);
                    stateTimer.reset();
                }
                break;

            case SCORING:
                if (stateTimer.seconds() >= RobotConstants.TIMING_SCORE_HOLD_SECONDS) {
                    intakeServo.setPosition(RobotConstants.SERVO_INTAKE_CLOSED);
                    setTargetPosition(RobotConstants.SLIDE_GROUND);
                    currentState = State.IDLE;
                }
                break;

            default:
                break;
        }

        if (atBottom() && targetPosition < RobotConstants.SLIDE_GROUND) {
            setTargetPosition(RobotConstants.SLIDE_GROUND);
            packet.put("Safety", "Bottom Extension Limit");
        }

        packet.put("State", currentState.toString());
        packet.put("Target Pos", targetPosition);
        packet.put("Current Pos", slideMotor.getCurrentPosition());
        packet.put("Has Piece", hasGamePiece());
    }

    @Disabled
    @TeleOp(name = "Test: Integrated Mechanism", group = "S12: Full System")
    public static class TestIntegrated extends LinearOpMode {
        @Override
        public void runOpMode() {
            S12_IntegratedMechanism mech = new S12_IntegratedMechanism();

            DcMotorEx slide = hardwareMap.get(DcMotorEx.class, HardwareNames.SLIDE_MOTOR);
            Servo intake = hardwareMap.get(Servo.class, HardwareNames.INTAKE_SERVO);
            DigitalChannel sensor = hardwareMap.get(DigitalChannel.class, HardwareNames.GAME_PIECE_SENSOR);
            DigitalChannel limit = hardwareMap.get(DigitalChannel.class, HardwareNames.BOTTOM_LIMIT);

            mech.init(slide, intake, sensor, limit);

            telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());
            telemetry.addData("Status", "RDY");
            telemetry.update();

            waitForStart();

            mech.resetControllers();

            while (opModeIsActive()) {
                if (gamepad1.a) {
                    mech.startIntakeSequence();
                } else if (gamepad1.b) {
                    mech.startScoreSequence();
                } else if (gamepad1.x) {
                    mech.emergencyStop();
                }

                TelemetryPacket packet = new TelemetryPacket();
                mech.update(packet);
                FtcDashboard.getInstance().sendTelemetryPacket(packet);

                telemetry.addData("State", mech.getState());
                telemetry.update();
            }
        }
    }
}
'''

TUNING_CHECKLIST_MD = r'''# Road Runner Tuning Checklist

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
'''


def main():
    print("Starting all-in-one FTC codebase converter...")
    print(f"Backup folder: {BACKUP_ROOT}")

    # 1. Normalize util folder and write shared utility files
    normalize_util_folder()

    write_file("util/HardwareNames.java", HARDWARE_NAMES_JAVA)
    write_file("util/MathUtils.java", MATH_UTILS_JAVA)
    write_file("util/PIDController.java", PID_CONTROLLER_JAVA)
    write_file("util/DebouncedBoolean.java", DEBOUNCED_BOOLEAN_JAVA)

    # 2. Write centralized constants and readiness flags
    write_file("RobotConstants.java", ROBOT_CONSTANTS_JAVA)
    write_file("RobotReadiness.java", ROBOT_READINESS_JAVA)

    # 3. Write competition layer
    write_file("HangSubsystem.java", HANG_SUBSYSTEM_JAVA)
    write_file("CompetitionTeleOp.java", COMPETITION_TELEOP_JAVA)
    write_file("CompetitionAuto.java", COMPETITION_AUTO_JAVA)

    # 4. Write critical mechanism/example fixes
    write_file("S12_IntegratedMechanism.java", S12_INTEGRATED_MECHANISM_JAVA)
    write_file("s2_hang_branch_endgame.java", S2_HANG_JAVA)
    write_file("S6_PollenHopper.java", S6_POLLEN_HOPPER_JAVA)

    # 5. Write documentation
    write_file("TUNING_CHECKLIST.md", TUNING_CHECKLIST_MD)

    # 6. Convert hardware strings and add missing imports globally
    apply_global_hardware_names()
    ensure_util_imports_where_used()

    # 7. Fix known remaining bugs
    fix_mecanum_turn_action()
    fix_s9()

    # 8. Disable example/test OpModes
    example_opmodes = [
        "s1_tank_drive_shell.java",
        "s1_omnidrive_shell.java",
        "s1_mechanum_drive_shell.java",
        "s2_pollen_hopper.java",
        "s2_pollenated_branches.java",
        "s3_test_dashboard.java",
        "S4_SimpleAuto.java",
        "S5_RoadRunnerAutoExample.java",
        "S7_StateMachineHopper.java",
        "S8_AprilTagVision.java",
        "S10_SensorHopper.java",
        "S11_PIDLinearSlide.java",
    ]

    for rel in example_opmodes:
        add_disabled_to_opmode(rel)

    print("\nDone.")
    print("Next, build with:")
    print("  cd FtcRobotController")
    print("  ./gradlew clean assembleDebug")


if __name__ == "__main__":
    main()
