package org.firstinspires.ftc.teamcode;

/**
 * BioBuzzField — nominal dimensions from 2026-27 BIOBUZZ Competition Manual v1 + TU01
 * and Event Field Setup Guide (Sep 12 2026). All imperial; metric in cm in manual.
 * Tolerance +/-1 in per manual 9.1. Tiles are 24x24 nominal.
 *
 * Coordinate origin: inside of field perimeter, (0,0)=corner at tile A1 (Red Garden side),
 * +X toward F (east), +Y toward 6 (north). Center of 144x144 field = (72,72) = HIVE frame center.
 * This is OLD-SCHOOL ENCODER FIELD — no AprilTag, no Road Runner needed for distance math.
 */
public final class BioBuzzField {
    private BioBuzzField() {}

    // ── Field ────────────────────────────────────────────────────────────────
    public static final double FIELD_SIZE_IN = 144.0;
    public static final double TILE_SIZE_IN = 24.0;
    public static final double TILE_COUNT = 6.0;
    public static final double WALL_HEIGHT_IN = 12.0; // perimeter wall nominal 1 ft

    // Center
    public static final double CENTER_X_IN = 72.0;
    public static final double CENTER_Y_IN = 72.0;

    // ── HIVE Structure (center) ────────────────────────────────────────────
    public static final double HIVE_FRAME_WIDTH_IN = 49.46;
    public static final double HIVE_FRAME_DEPTH_IN = 38.95;
    public static final double HIVE_PIVOT_HEIGHT_IN = 43.95; // axis above tiles
    public static final double HIVE_CELL_BOTTOM_HEIGHT_IN = 30.6; // TU01 corrected, was 30.6 not earlier  ~?
    public static final double HIVE_CELL_SPACING_IN = 18.8; // between the two cells of one hive
    public static final double HIVE_CELL_OPENING_WIDTH_IN = 20.0;
    public static final double HIVE_CELL_OPENING_HEIGHT_IN = 14.0;
    public static final double HIVE_CELL_OPENING_DEPTH_IN = 12.0;

    // ── FLOWERs (4, at midpoints of each wall, attached to perimeter) ─────
    // Nominal: attached to wall, opening 4 in diam, 21.5 above tiles
    public static final double FLOWER_OPENING_DIAM_IN = 4.0;
    public static final double FLOWER_HEIGHT_IN = 21.5;
    public static final double FLOWER_RETRIEVAL_HEIGHT_IN = 3.55;
    public static final double FLOWER_RETRIEVAL_DEPTH_IN = 3.57;
    public static final double FLOWER_BOTTOM_RING_DIAM_IN = 2.79;
    public static final double FLOWER_BOTTOM_RING_HEIGHT_IN = 0.4;
    // Approx wall-center positions (nominal, per Setup Guide “4 locations around perimeter”)
    public static final double FLOWER_NORTH_X_IN = 72.0, FLOWER_NORTH_Y_IN = 144.0;
    public static final double FLOWER_SOUTH_X_IN = 72.0, FLOWER_SOUTH_Y_IN = 0.0;
    public static final double FLOWER_EAST_X_IN = 144.0, FLOWER_EAST_Y_IN = 72.0;
    public static final double FLOWER_WEST_X_IN = 0.0, FLOWER_WEST_Y_IN = 72.0;

    // ── LOADING ZONE (Alliance specific, 23 x 11, on A5 red and F2 blue) ──
    public static final double LOADING_ZONE_WIDTH_IN = 23.0;
    public static final double LOADING_ZONE_DEPTH_IN = 11.0;
    // Tile bounds: A5 = x[0,24], y[96,120]; F2 = x[120,144], y[24,48] (A1 origin)
    public static final double LOADING_ZONE_RED_X_IN = 12.0;  // center of A5 tile
    public static final double LOADING_ZONE_RED_Y_IN = 108.0;
    public static final double LOADING_ZONE_BLUE_X_IN = 132.0;
    public static final double LOADING_ZONE_BLUE_Y_IN = 36.0;

    // ── GARDEN (23 x 2, opposite corners A1 red, F6 blue) ──────────────────
    public static final double GARDEN_WIDTH_IN = 23.0;
    public static final double GARDEN_DEPTH_IN = 2.0;
    public static final double GARDEN_RED_X_IN = 12.0;   // A1 center
    public static final double GARDEN_RED_Y_IN = 12.0;
    public static final double GARDEN_BLUE_X_IN = 132.0; // F6 center
    public static final double GARDEN_BLUE_Y_IN = 132.0;

    // ── ALLIANCE AREA (outside field, 97 x 54) ─────────────────────────────
    public static final double ALLIANCE_AREA_WIDTH_IN = 97.0;
    public static final double ALLIANCE_AREA_DEPTH_IN = 54.0;

    // ── Scoring Elements ───────────────────────────────────────────────────
    public static final double POLLEN_DIAMETER_IN = 2.8;
    public static final double NECTAR_DIAMETER_IN = 3.6;
    public static final int TOTAL_POLLEN = 40;
    public static final int TOTAL_NECTAR_RED = 8;
    public static final int TOTAL_NECTAR_BLUE = 8;
    public static final int POLLEN_TO_TIP_HIVE = 8; // approx
    public static final int NECTAR_TO_TIP_HIVE = 5;

    // ── Autonomous distances (old-school encoder, robot radius 9 in) ───────
    // Start: touching alliance wall (y=0 south wall for testing). Distance to HIVE center:
    // 72 - 9 (robot radius) - HIVE frame half-depth (~19.5) = ~43.5 to frame, ~60 to center cell.
    // Use conservative 48 in shooting distance for ~30.6 in high cell opening from 21.5 flower height logic.
    public static final double START_TO_HIVE_SHOOT_IN = 48.0; // wall to hive shooting spot
    public static final double START_TO_HIVE_CENTER_IN = 60.0; // wall to hive pivot
    public static final double HIVE_TO_FLOWER_IN = 72.0; // center to wall flower
    public static final double HIVE_TO_LOADING_ZONE_IN = 48.0; // center to loading zone
    public static final double PARK_IN_LOADING_ZONE_IN = 12.0; // small PARK move
    // Tile to inches helper
    public static double tilesToInches(double tiles) {return tiles * TILE_SIZE_IN;}
    // Inches to encoder ticks (uses RobotConstants.DRIVE_TICKS_PER_INCH, 38.2 nominal)
    public static int inchesToTicks(double inches) {
        return (int) Math.round(inches * RobotConstants.DRIVE_TICKS_PER_INCH);
    }
    // Common autonomous legs
    public static final double AUTO_LEAVE_START_IN = 12.0; // earn SWARM: leave starting location
    public static final double AUTO_PARK_LOADING_ZONE_IN = 8.0; // park in loading zone
}
