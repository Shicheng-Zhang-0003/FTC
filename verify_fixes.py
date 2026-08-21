#!/usr/bin/env python3
"""
verify_fixes.py — READ-ONLY audit of which B-series fixes are present.
Writes nothing; safe to run any time.
Run from the folder containing bleeding/:
    python3 verify_fixes.py
Or point it at the folder that actually compiles (TeamCode) to double-check:
    python3 verify_fixes.py FtcRobotController/TeamCode/src/main/java/org/firstinspires/ftc/teamcode
"""
import sys
from pathlib import Path

SCRIPT_DIR = Path(__file__).resolve().parent

# (bug, file, marker)
CHECKS = [
    ("B1",  "CompetitionTeleOp.java",           "fieldCentric && imu"),  # B17: guard is now imuReady
    ("B4",  "CompetitionTeleOp.java",           "B4 fix"),
    ("B11", "CompetitionTeleOp.java",           "B11 fix"),
    ("B2",  "CompetitionAuto.java",             "B2 fix"),
    ("B10", "CompetitionAuto.java",             "B10 fix"),
    ("B11", "CompetitionAuto.java",             "B11 fix"),
    ("B3",  "S9_CompleteAutonomous.java",       "B3 fix"),
    ("B6",  "MecanumDrive.java",                "B6 fix"),
    ("B7",  "MecanumDrive.java",                "B7 fix"),
    ("B8",  "RobotConstants.java",              "SLIDE_KG"),
    ("B8",  "S12_IntegratedMechanism.java",     "SLIDE_KG"),
    ("B8",  "S11_PIDLinearSlide.java",          "SLIDE_KG"),
    ("B9",  "S8_AprilTagAuto.java",             "B9 fix"),
    ("B12", "S12_IntegratedMechanism.java",     "B12 fix"),
    ("B13", "S12_IntegratedMechanism.java",     "B13 fix"),
    ("B14", "S10_SensorHopper.java",            "B14 fix"),
    ("B15", "util/AprilTagFieldTransform.java", "ALIGNMENT_SIGN"),
    ("B17", "CompetitionTeleOp.java",           "B17 fix"),
    ("B18", "util/AprilTagFieldTransform.java", "B18 fix"),
    ("B18", "S8_AprilTagAuto.java",             "B18 fix"),
    ("B18", "S8_AprilTagVision.java",           "B18 fix"),
    ("B19", "S12_IntegratedMechanism.java",     "B19 fix"),
    ("B20", "S9_CompleteAutonomous.java",       "B20 fix"),
    # B23-B26: vacation sprint (pure code fixes, no hardware needed)
    ("B23", "S9_CompleteAutonomous.java",       "B23 fix"),
    ("B24", "CompetitionAuto.java",             "B24 fix"),
    ("B24", "MechanismActions.java",            "B24 fix"),
    ("B25", "HangSubsystem.java",               "B25 fix"),
    ("B26", "S4_SimpleAuto.java",               "B26 fix"),
]

# Duplicate-prone spots from the earlier collisions (these break compile if >1)
DUP_CHECKS = [
    ("util/AprilTagFieldTransform.java", "double camYawDeg =",   1, "duplicate var breaks compile"),
    ("S10_SensorHopper.java",            "public void clearError", 1, "duplicate method breaks compile"),
    ("RobotConstants.java",              "SLIDE_KG =",           1, "duplicate constant breaks compile"),
    ("CompetitionTeleOp.java",           "boolean imuReady",       1, "duplicate local breaks compile"),
    ("util/AprilTagFieldTransform.java", "detection.robotPose", 0, "robotPose is field position; alignment must use ftcPose"),
    ("S8_AprilTagAuto.java",             "targetTag.robotPose", 0, "robotPose is field position; alignment must use ftcPose"),
    ("S8_AprilTagVision.java",           "detection.robotPose", 0, "robotPose is field position; diagnostics must use ftcPose"),
    ("S12_IntegratedMechanism.java",     "private static final double INTAKE_TIMEOUT_SECONDS", 1, "duplicate constant breaks compile"),
    ("util/AprilTagFieldTransform.java", "getFieldCorrection", 0, "dead method removed in B22"),
    ("util/AprilTagFieldTransform.java", "import com.acmerobotics.roadrunner.Pose2d;", 0, "Pose2d import removed in B22"),
    ("RobotConstants.java",              "SENSOR_GAME_PIECE_DISTANCE_CM", 0, "dead constant removed in B22"),
    ("RobotConstants.java",              "SLIDE_SCORE_LOW", 0, "dead constant removed in B22"),
    ("CompetitionAuto.java",             "scorePose", 0, "dead variable removed in B22"),
    ("S9_CompleteAutonomous.java",       "AprilTagProcessor", 0, "vision stripped in B20"),
    ("S9_CompleteAutonomous.java",       "detectTargetTag", 0, "vision stripped in B20"),
]

def read(root, rel):
    p = root / rel
    if p.exists():
        return p.read_text(encoding="utf-8")
    # B28 fix: fall back to the Kickoff archive. purge.py moves the BIOBUZZ
    # guesswork into archive_biobuzz_guesswork/, and the audit should still
    # verify those files instead of reporting NOFILE.
    archive_p = root / "archive_biobuzz_guesswork" / rel
    if archive_p.exists():
        return archive_p.read_text(encoding="utf-8")
    return None

# B27 fix: detect live code swallowed into a // comment line.
# A swallowed line reads as a comment but contains what looks like a complete
# method-call statement ending in ');'. This is the class of bug that hid B23.
def find_swallowed_code(root):
    import re
    call = re.compile(r"\w+\.\w+\s*\(")
    hits = []
    for java in sorted(root.rglob("*.java")):
        if ".bak" in java.name:
            continue
        try:
            lines = java.read_text(encoding="utf-8").splitlines()
        except Exception:
            continue
        for i, line in enumerate(lines, 1):
            stripped = line.strip()
            if not stripped.startswith("//"):
                continue
            if stripped.endswith(");") and call.search(stripped):
                # B27 patch: ignore Road Runner Quickstart "TODO: reverse if needed" templates
                if ".setDirection(" in stripped:
                    continue
                hits.append((str(java.relative_to(root)), i, stripped[:90]))
    return hits

def main():
    root = Path(sys.argv[1]).resolve() if len(sys.argv) > 1 else SCRIPT_DIR / "bleeding"
    if not root.is_dir():
        sys.exit(f"ERROR: {root} is not a directory")

    print(f"Auditing: {root}\n")
    print("=== Fix presence ===")
    present = missing = 0
    for bug, rel, marker in CHECKS:
        src = read(root, rel)
        if src is None:
            status, mark = "NOFILE", "⚠️ "
        elif marker in src:
            status, mark = "OK", "✅"; present += 1
        else:
            status, mark = "MISSING", "❌"; missing += 1
        print(f"  {mark} {bug:<4} {rel:<34} [{marker}]")

    print("\n=== Duplicate / compile-safety checks ===")
    dup_problems = 0
    for rel, needle, expected, why in DUP_CHECKS:
        src = read(root, rel)
        if src is None:
            print(f"  ⚠️  {rel}: file not found"); continue
        count = src.count(needle)
        ok = (count == expected)
        if not ok: dup_problems += 1
        print(f"  {'✅' if ok else '❌'} {rel}: '{needle}' appears {count}x (expected {expected}) — {why}")

    s10 = read(root, "S10_SensorHopper.java")
    if s10:
        lb = sum(1 for l in s10.splitlines() if l.strip().startswith("else if") and "left_bumper" in l and "clearError" in l)
        if lb != 1: dup_problems += 1
        print(f"  {'✅' if lb == 1 else '❌'} S10_SensorHopper.java: left_bumper+clearError bindings = {lb} (expected 1)")

    print("\n=== Swallowed-code checks (B27) ===")
    swallowed = find_swallowed_code(root)
    if swallowed:
        dup_problems += len(swallowed)
        for rel, lineno, preview in swallowed:
            print(f"  ❌ {rel}:{lineno} possible swallowed code: {preview}")
    else:
        print("  ✅ no swallowed code lines detected")

    print("\n=== Summary ===")
    print(f"  Fixes present: {present} / {present + missing}")
    if missing:
        print("  Missing fixes are marked ❌ above - run the matching apply_BXX.py script.")
    print(f"  {'⚠️  ' + str(dup_problems) + ' duplicate/safety issue(s) — fix before trusting build.' if dup_problems else 'No duplicate/safety issues detected.'}")

if __name__ == "__main__":
    main()
