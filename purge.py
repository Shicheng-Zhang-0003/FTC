#!/usr/bin/env python3
"""
kickoff_purge.py - Run on Kickoff Day to archive the BIOBUZZ guesswork.
Moves pollen-specific files to an archive folder, preserving the core 
architecture (PID, State Machines, Vision, Road Runner) for the real game.
"""
import shutil
from pathlib import Path

SCRIPT_DIR = Path(__file__).resolve().parent
BLEEDING = SCRIPT_DIR / "bleeding"
ARCHIVE = BLEEDING / "archive_biobuzz_guesswork"

# Files that are strictly tied to the "Pollen/Hive/Branches" guess
GUESSWORK_FILES = [
    "s2_pollenated_branches.java",
    "s2_pollen_hopper.java",
    "S6_PollenHopper.java",
    "S6_ParallelAuto.java",
    "S7_StateMachineHopper.java",
    "S9_CompleteAutonomous.java"
]

def main():
    print("=== BIOBUZZ Kickoff Purge ===")
    ARCHIVE.mkdir(exist_ok=True)

    for fname in GUESSWORK_FILES:
        src = BLEEDING / fname
        dst = ARCHIVE / fname
        if src.exists():
            shutil.move(str(src), str(dst))
            print(f"  ✅ Archived: {fname}")
        else:
            print(f"  ⚠️  Skipped (not found): {fname}")

    print("\n✅ Guesswork safely archived.")
    print("Your core architecture (S11 PID, S12 State Machine, S8 Vision, RR) is intact.")
    print("\nNext steps:")
    print("  1. Read the new game manual.")
    print("  2. Duplicate S12_IntegratedMechanism.java and adapt it for the real game piece.")
    print("  3. Update RobotConstants.java with the new servo/motor names.")
    print("  4. Wire the new states into CompetitionAuto.java.")

if __name__ == "__main__":
    main()
