#!/usr/bin/env python3
"""
fix_B1_field_centric.py

Bug  : B1 (CRITICAL) — CompetitionTeleOp declares a fieldCentric flag,
       toggles it with Options, and telemeters it, but the mecanum math
       never uses IMU yaw. The robot always drives robot-centric while
       telemetry claims "Field Centric: true".

Fix  : Rotate the stick inputs by the robot's current IMU yaw before the
       mecanum mixing (standard FTC field-centric transform). Falls back
       to robot-centric automatically when the IMU is absent.

File : bleeding/CompetitionTeleOp.java
Run  : from the folder CONTAINING bleeding/
           python3 fix_B1_field_centric.py
"""

import difflib
import sys
from pathlib import Path

TARGET_REL = Path("bleeding") / "CompetitionTeleOp.java"
BUG_ID = "B1"

# Anchor: we insert BETWEEN these two consecutive lines
# (matched whitespace-insensitively, so file indentation doesn't matter)
ANCHOR = [
    "double rx = MathUtils.deadband (gamepad1.right_stick_x, RobotConstants.DRIVE_STICK_DEADBAND);",
    "double denominator = Math.max (Math.abs (y) + Math.abs (x) + Math.abs (rx), 1);",
]

# Block to insert (indentation is applied automatically to match the file)
INSERT = [
    "//Field-centric: rotate stick inputs by the robot's current heading",
    "//Falls back to robot-centric if the IMU is not present",
    "if (fieldCentric && imu != null) {",
    "double yaw = imu.getRobotYawPitchRollAngles ().getYaw (AngleUnit.RADIANS);",
    "double rotX = x * Math.cos (-yaw) - y * Math.sin (-yaw);",
    "double rotY = x * Math.sin (-yaw) + y * Math.cos (-yaw);",
    "x = rotX;",
    "y = rotY;",
    "}",
]

MARKER = "Field-centric: rotate stick inputs"

# These must already exist in the file for the fix to be valid
REQUIRED = [
    ("fieldCentric flag",  "private boolean fieldCentric"),
    ("imu field",          "private IMU imu"),
    ("AngleUnit import",   "import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;"),
    ("yaw reset at start", "resetYaw"),
]


def find_target() -> Path:
    # Default: run from the folder containing bleeding/
    p = Path.cwd() / TARGET_REL
    if p.is_file():
        return p
    # Fallback: resolve relative to this script's own location
    p = Path(__file__).resolve().parent / TARGET_REL
    if p.is_file():
        return p
    sys.exit(f"ERROR: cannot find {TARGET_REL} — run this script from the folder containing bleeding/")


def find_anchor(lines):
    n = len(ANCHOR)
    for i in range(len(lines) - n + 1):
        if all(lines[i + j].strip() == ANCHOR[j] for j in range(n)):
            return i
    return -1


def main():
    target = find_target()
    original = target.read_text(encoding="utf-8")

    # Idempotency: don't double-apply
    if MARKER in original:
        print(f"SKIP: {target} already contains the {BUG_ID} fix. Nothing to do.")
        return

    # Sanity: prerequisites must be present
    for label, needle in REQUIRED:
        if needle not in original:
            sys.exit(f"ERROR: prerequisite missing ({label}: '{needle}'). File changed? No changes made.")

    lines = original.splitlines()
    idx = find_anchor(lines)
    if idx < 0:
        sys.exit("ERROR: anchor block not found in the drive section. File changed? No changes made.")

    # Match the file's indentation style; nest one level inside the new if-block
    base_indent = lines[idx][: len(lines[idx]) - len(lines[idx].lstrip())]
    step = "    " if base_indent else ""

    new_block = [base_indent + INSERT[0],
                 base_indent + INSERT[1],
                 base_indent + INSERT[2]]
    new_block += [base_indent + step + stmt for stmt in INSERT[3:-1]]
    new_block.append(base_indent + INSERT[-1])

    patched_lines = lines[: idx + 1] + new_block + lines[idx + 1:]
    patched = "\n".join(patched_lines)
    if original.endswith("\n"):
        patched += "\n"

    # Backup, write, verify round-trip
    backup = target.with_name(f"{target.name}.pre_{BUG_ID}.bak")
    backup.write_text(original, encoding="utf-8")
    target.write_text(patched, encoding="utf-8")

    if MARKER not in target.read_text(encoding="utf-8"):
        sys.exit("ERROR: verification failed after write — restore from backup!")

    print(f"OK: {BUG_ID} fix applied to {target}")
    print(f"    backup: {backup}")
    print()
    print("--- diff ---")
    for line in difflib.unified_diff(original.splitlines(), patched.splitlines(),
                                     fromfile="before", tofile="after", lineterm="", n=3):
        print(line)
    print()
    print("On-robot verification:")
    print("  1. Run Competition TeleOp; telemetry should show 'Field Centric: true'")
    print("  2. Rotate the robot ~90 degrees, push left stick forward")
    print("     -> robot keeps travelling the same FIELD direction")
    print("  3. Toggle with Options -> driving returns to robot-centric")
    print()
    print("NOTE: the Options toggle currently lives inside the mechanismReady block.")
    print("      That is bug B4 and gets its own script; B1 only fixes the drive math.")


if __name__ == "__main__":
    main()
