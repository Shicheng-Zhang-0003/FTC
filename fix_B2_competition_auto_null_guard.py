#!/usr/bin/env python3
"""
fix_B2_competition_auto_null_guard.py

Bug   : B2 (CRITICAL) - CompetitionAuto passes safeGet() results directly into
        S12_IntegratedMechanism.init() with no null checks. init() immediately
        calls slideMotor.setMode(...) and gamePieceSensor.setMode(...), so any
        missing device crashes the opmode during INIT with a NullPointerException.
        This script also fixes the CompetitionAuto half of B5: the `missing`
        list was declared but never populated, so telemetry always said "None".
        (The S9_CompleteAutonomous half of B5 belongs to the B3 script.)

Fix   : 1) fetch each device into a named variable, record nulls in `missing`
        2) only init the mechanism when all four devices are present
        3) report honest status at init
        4) after waitForStart, abort with persistent telemetry instead of
           running a half-initialized auto

File  : bleeding/CompetitionAuto.java
Run   : from the folder CONTAINING bleeding/
            python3 fix_B2_competition_auto_null_guard.py
"""

import difflib
import re
import sys
from pathlib import Path

TARGET_REL = Path("bleeding") / "CompetitionAuto.java"
BUG_ID = "B2"
MARKER = "AUTO ABORTED"

REQUIRED = [
    ("safeGet helper",   "private <T> T safeGet"),
    ("ArrayList import", "import java.util.ArrayList;"),
    ("missing list",     "ArrayList<String> missing = new ArrayList<> ();"),
    ("stop check",       "if (isStopRequested ()) {return;}"),
]

# ---------------------------------------------------------------------------
# Edit A: replace the unguarded init block + status line with the guarded
# version. Anchors are matched with flexible whitespace BETWEEN lines, so it
# works whether or not the file merges statements like "); telemetry.addData".
# ---------------------------------------------------------------------------
EDIT_A_ANCHOR = [
    "S12_IntegratedMechanism mechanism = new S12_IntegratedMechanism ();",
    "mechanism.init (",
    "safeGet (DcMotorEx.class, HardwareNames.SLIDE_MOTOR),",
    "safeGet (Servo.class, HardwareNames.INTAKE_SERVO),",
    "safeGet (DigitalChannel.class, HardwareNames.GAME_PIECE_SENSOR),",
    "safeGet (DigitalChannel.class, HardwareNames.BOTTOM_LIMIT)",
    ");",
    'telemetry.addData ("Status", "Competition Auto Ready");',
]

EDIT_A_NEW = [
    (0, "DcMotorEx slide = safeGet (DcMotorEx.class, HardwareNames.SLIDE_MOTOR);"),
    (0, "Servo intakeServo = safeGet (Servo.class, HardwareNames.INTAKE_SERVO);"),
    (0, "DigitalChannel gamePieceSensor = safeGet (DigitalChannel.class, HardwareNames.GAME_PIECE_SENSOR);"),
    (0, "DigitalChannel bottomLimit = safeGet (DigitalChannel.class, HardwareNames.BOTTOM_LIMIT);"),
    (0, ""),
    (0, "if (slide == null) {missing.add (HardwareNames.SLIDE_MOTOR);}"),
    (0, "if (intakeServo == null) {missing.add (HardwareNames.INTAKE_SERVO);}"),
    (0, "if (gamePieceSensor == null) {missing.add (HardwareNames.GAME_PIECE_SENSOR);}"),
    (0, "if (bottomLimit == null) {missing.add (HardwareNames.BOTTOM_LIMIT);}"),
    (0, ""),
    (0, "boolean mechanismReady = false;"),
    (0, "S12_IntegratedMechanism mechanism = new S12_IntegratedMechanism ();"),
    (0, "if (slide != null && intakeServo != null && gamePieceSensor != null && bottomLimit != null) {"),
    (1, "mechanism.init (slide, intakeServo, gamePieceSensor, bottomLimit);"),
    (1, "mechanismReady = true;"),
    (0, "}"),
    (0, ""),
    (0, 'telemetry.addData ("Status", mechanismReady ? "Competition Auto Ready" : "MISSING MECHANISM HARDWARE");'),
]

# ---------------------------------------------------------------------------
# Edit B: insert the abort block right after the isStopRequested check.
# ---------------------------------------------------------------------------
EDIT_B_ANCHOR = [
    "if (isStopRequested ()) {return;}",
]

EDIT_B_NEW = [
    (0, "//B2 fix: do not run an auto whose mechanism failed to initialize"),
    (0, "if (!mechanismReady) {"),
    (1, "while (opModeIsActive ()) {"),
    (2, 'telemetry.addData ("AUTO ABORTED", "Missing mechanism hardware");'),
    (2, 'telemetry.addData ("Missing Devices", missing.toString ());'),
    (2, "telemetry.update ();"),
    (2, "sleep (100);"),
    (1, "}"),
    (1, "return;"),
    (0, "}"),
]


def find_target():
    # Default: run from the folder containing bleeding/
    p = Path.cwd() / TARGET_REL
    if p.is_file():
        return p
    # Fallback: resolve relative to this script's own location
    p = Path(__file__).resolve().parent / TARGET_REL
    if p.is_file():
        return p
    sys.exit(f"ERROR: cannot find {TARGET_REL} - run this script from the folder containing bleeding/")


def anchor_pattern(anchor_lines):
    # Exact within-line match, any whitespace (incl. newlines) between lines
    return re.compile(r"\s*".join(re.escape(line) for line in anchor_lines))


def line_indent(text, pos):
    line_start = text.rfind("\n", 0, pos) + 1
    indent = ""
    for ch in text[line_start:pos]:
        if ch in " \t":
            indent += ch
        else:
            break
    return indent


def build_block(spec, base_indent, nl):
    step = "    " if base_indent else ""
    return nl.join("" if content == "" else base_indent + step * level + content
                   for level, content in spec)


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

    nl = "\r\n" if "\r\n" in original else "\n"
    patched = original

    # ---- Edit A: guarded init + honest status
    pat_a = anchor_pattern(EDIT_A_ANCHOR)
    if len(pat_a.findall(patched)) != 1:
        sys.exit(f"ERROR: edit-A anchor found {len(pat_a.findall(patched))} times (expected 1). No changes made.")
    m = pat_a.search(patched)
    repl_a = build_block(EDIT_A_NEW, line_indent(patched, m.start()), nl)
    patched = pat_a.sub(lambda _m: repl_a, patched, count=1)

    # ---- Edit B: abort block after start
    pat_b = anchor_pattern(EDIT_B_ANCHOR)
    if len(pat_b.findall(patched)) != 1:
        sys.exit(f"ERROR: edit-B anchor found {len(pat_b.findall(patched))} times (expected 1). No changes made.")
    m = pat_b.search(patched)
    repl_b = build_block(EDIT_B_NEW, line_indent(patched, m.start()), nl)
    patched = patched[:m.end()] + nl + repl_b + patched[m.end():]

    # ---- Verify before writing
    if MARKER not in patched or "mechanism.init (slide, intakeServo, gamePieceSensor, bottomLimit);" not in patched:
        sys.exit("ERROR: internal verification failed before write. No changes made.")

    backup = target.with_name(f"{target.name}.pre_{BUG_ID}.bak")
    backup.write_text(original, encoding="utf-8")
    target.write_text(patched, encoding="utf-8")

    if MARKER not in target.read_text(encoding="utf-8"):
        sys.exit("ERROR: verification failed after write - restore from backup!")

    print(f"OK: {BUG_ID} fix applied to {target}")
    print(f"    backup: {backup}")
    print()
    print("--- diff ---")
    for line in difflib.unified_diff(original.splitlines(), patched.splitlines(),
                                     fromfile="before", tofile="after", lineterm="", n=3):
        print(line)
    print()
    print("What changed:")
    print("  - devices fetched into named variables; nulls recorded in `missing` (B5, this file)")
    print("  - mechanism.init() only runs when all four devices are present")
    print("  - init telemetry reports honest status")
    print("  - missing hardware after start -> persistent AUTO ABORTED loop instead of NPE")
    print()
    print("Notes:")
    print("  - S9_CompleteAutonomous has the same hole; that is the B3 script.")
    print("  - MecanumDrive still throws if drive motors are missing (deliberate: drive is mandatory).")
    print("  - Spotted while patching: `scorePose` is declared but never used (nitpick, left alone).")
    print()
    print("Verification:")
    print("  1. Remove one mechanism device from the config, init Competition Auto")
    print("     -> expect 'MISSING MECHANISM HARDWARE' at init and AUTO ABORTED after start, no crash")
    print("  2. Restore the config, init again -> expect normal 'Competition Auto Ready'")


if __name__ == "__main__":
    main()
