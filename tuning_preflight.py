#!/usr/bin/env python3
"""
tuning_preflight.py — READ-ONLY pre-tuning sanity check. Run from FTC/.
Automates the 'before tuning' section of TUNING_CHECKLIST.md against the code,
so the physical Road Runner tuning session starts from a known-good state.
Writes nothing; safe to run any time.
Usage:  python3 tuning_preflight.py [<teamcode-dir>]
"""
import re
import sys
from pathlib import Path

SCRIPT_DIR = Path(__file__).resolve().parent

def read(root, rel):
    p = root / rel
    return p.read_text(encoding="utf-8") if p.exists() else None

def grab(src, pattern):
    m = re.search(pattern, src)
    return m.group(1) if m else None

def main():
    root = Path(sys.argv[1]).resolve() if len(sys.argv) > 1 else SCRIPT_DIR / "bleeding"
    if not root.is_dir():
        sys.exit(f"ERROR: {root} is not a directory")

    print(f"Pre-tuning preflight: {root}\n")

    md = read(root, "MecanumDrive.java")
    hn = read(root, "util/HardwareNames.java")
    rr = read(root, "RobotReadiness.java")
    tuning = read(root, "tuning/TuningOpModes.java")

    if md is None or hn is None or rr is None:
        sys.exit("ERROR: MecanumDrive.java / HardwareNames.java / RobotReadiness.java not all found")

    def row(ok, label, detail):
        mark = "✅" if ok is True else ("❌" if ok is False else "⚠️ ")
        print(f"  {mark} {label:<38} {detail}")

    # 1. Drive class used by the tuning OpModes
    drive_class = grab(tuning, r"DRIVE_CLASS\s*=\s*([\w.]+)\.class") if tuning else None
    row(drive_class == "MecanumDrive", "TuningOpModes.DRIVE_CLASS",
        f"{drive_class} (expect MecanumDrive)" if drive_class else "tuning/TuningOpModes.java not found")

    # 2. Motor names referenced by MecanumDrive exist in HardwareNames
    names = ["FRONT_LEFT", "FRONT_RIGHT", "BACK_LEFT", "BACK_RIGHT"]
    missing = [n for n in names if f"HardwareNames.{n}" not in md or not re.search(rf"\b{n}\s*=", hn)]
    row(not missing, "Drive motor names wired", "all 4 present" if not missing else f"missing: {missing}")

    # 3. Wheel direction (B6) - left side reversed
    b6 = ("leftFront.setDirection(DcMotorSimple.Direction.REVERSE)" in md
          and "leftBack.setDirection(DcMotorSimple.Direction.REVERSE)" in md)
    row(b6, "Wheel direction (B6)", "left side reversed" if b6 else "B6 reversal not found")

    # 4. Encoder direction note (B7)
    row("B7 fix" in md, "Encoder direction (B7)", "B7 note present" if "B7 fix" in md else "B7 note missing")

    # 5. IMU orientation - report for PHYSICAL confirmation (can't validate from code)
    logo = grab(md, r"logoFacingDirection\s*=\s*[\s\S]*?LogoFacingDirection\.(\w+)")
    usb = grab(md, r"usbFacingDirection\s*=\s*[\s\S]*?UsbFacingDirection\.(\w+)")
    row(None, "IMU orientation (verify on robot)", f"logo={logo}, usb={usb}")

    # 6. ROAD_RUNNER_TUNED must still be false until tuning is done
    tuned = grab(rr, r"ROAD_RUNNER_TUNED\s*=\s*(true|false)")
    row(tuned == "false", "ROAD_RUNNER_TUNED still false",
        f"{tuned}" + (" - flip back until tuning is done!" if tuned == "true" else ""))

    # 7. Are the drive PARAMS still quickstart defaults?
    defaults = {"inPerTick": "0.3757", "kS": "0.5", "kV": "0.02", "kA": "0.005"}
    still_default = []
    for k, v in defaults.items():
        val = grab(md, rf"public double {k} = ([\d.]+);")
        if val == v:
            still_default.append(k)
    row(not still_default, "Drive PARAMS tuned",
        "all changed from defaults" if not still_default else f"still default: {still_default} -> tuning needed")

    print("\nPreflight complete. Fix any ❌ before running the ramp loggers.")
    print("The ⚠️  IMU orientation row needs eyeballs on the actual hub mounting.")

if __name__ == "__main__":
    main()
