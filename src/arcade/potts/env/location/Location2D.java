package arcade.potts.env.location;

import java.util.ArrayList;
import java.util.HashMap;
import static arcade.potts.sim.Potts2D.*;
import static arcade.potts.util.PottsEnums.Direction;

/**
 * Static location methods for 2D.
 *
 * <p>Interface defines generalized 2D voxel methods that can be applied for both {@link
 * PottsLocation} objects (without regions) and {@link PottsLocations} objects (with regions).
 */
public interface Location2D {
    /** Equation power for surface area conversion. */
    double EQUATION_PARAMETER_N = 0.53312428;

    /** Equation coefficient for surface area conversion. */
    double EQUATION_PARAMETER_A = 0.7137145;

    /** Equation offset for surface area conversion. */
    double EQUATION_PARAMETER_B = -0.95549169;

    /** List of valid 2D directions. */
    Direction[] DIRECTIONS =
            new Direction[] {
                Direction.YZ_PLANE,
                Direction.ZX_PLANE,
                Direction.POSITIVE_XY,
                Direction.NEGATIVE_XY,
            };

    /**
     * Calculate correction factor for surface area conversion.
     *
     * @param volume the volume
     * @return the correction factor
     */
    static double getCorrection(double volume) {
        return EQUATION_PARAMETER_A * Math.pow(volume, EQUATION_PARAMETER_N) + EQUATION_PARAMETER_B;
    }

    /**
     * Gets list of neighbors of a given voxel.
     *
     * @param focus the focus voxel
     * @return the list of neighbor voxels
     */
    static ArrayList<Voxel> getNeighbors(Voxel focus) {
        ArrayList<Voxel> neighbors = new ArrayList<>();
        for (int i = 0; i < NUMBER_NEIGHBORS; i++) {
            Voxel v = new Voxel(focus.x + MOVES_X[i], focus.y + MOVES_Y[i], focus.z);
            neighbors.add(v);
        }
        return neighbors;
    }

    /**
     * Converts volume and height to surface area.
     *
     * @param volume the volume
     * @param height the height
     * @return the surface area
     */
    static double convertSurface(double volume, double height) {
        double surface = 2 * Math.sqrt(Math.PI) * Math.sqrt(volume);
        double correction = getCorrection(volume);
        return Math.ceil(surface + correction);
    }

    /**
     * Calculates surface of location.
     *
     * @param voxels the list of voxels
     * @return the surface
     */
    static int calculateSurface(ArrayList<Voxel> voxels) {
        int surface = 0;

        for (Voxel v : voxels) {
            for (int i = 0; i < NUMBER_NEIGHBORS; i++) {
                Voxel voxel = new Voxel(v.x + MOVES_X[i], v.y + MOVES_Y[i], v.z);
                if (!voxels.contains(voxel)) {
                    surface++;
                }
            }
        }

        return surface;
    }

    /**
     * Calculates height of location (z axis).
     *
     * <p>Height is always one if there is as least one voxel in the list of voxels. Otherwise,
     * height is zero.
     *
     * @param voxels the list of voxels
     * @return the height
     */
    static int calculateHeight(ArrayList<Voxel> voxels) {
        return (voxels.size() > 0 ? 1 : 0);
    }

    /**
     * Calculates the local change in surface of the location.
     *
     * @param voxels the list of voxels
     * @param voxel the voxel the update is centered in
     * @return the change in surface
     */
    static int updateSurface(ArrayList<Voxel> voxels, Voxel voxel) {
        int change = 0;

        for (int i = 0; i < NUMBER_NEIGHBORS; i++) {
            Voxel v = new Voxel(voxel.x + MOVES_X[i], voxel.y + MOVES_Y[i], voxel.z);
            if (!voxels.contains(v)) {
                change++;
            } else {
                change--;
            }
        }

        return change;
    }

    /**
     * Calculates the local change in height of the location.
     *
     * @param voxels the list of voxels
     * @param voxel the voxel the update is centered in
     * @return the change in height
     */
    static int updateHeight(ArrayList<Voxel> voxels, Voxel voxel) {
        boolean addToEmpty = voxels.size() == 0;
        boolean removeToEmpty = voxels.size() == 1 && voxels.contains(voxel);
        return (addToEmpty || removeToEmpty ? 1 : 0);
    }

    /**
     * Calculates diameters in each direction.
     *
     * @param voxels the list of voxels
     * @param focus the focus voxel
     * @return the map of direction to diameter
     */
    static HashMap<Direction, Integer> getDiameters(ArrayList<Voxel> voxels, Voxel focus) {
        HashMap<Direction, Integer> minValueMap = new HashMap<>();
        HashMap<Direction, Integer> maxValueMap = new HashMap<>();
        HashMap<Direction, Boolean> existsMap = new HashMap<>();

        // Initialized entries into direction maps.
        for (Direction direction : DIRECTIONS) {
            minValueMap.put(direction, Integer.MAX_VALUE);
            maxValueMap.put(direction, Integer.MIN_VALUE);
            existsMap.put(direction, false);
        }

        Direction dir;
        int v;

        // Iterate through all the voxels for the location to update minimum and
        // maximum values in each direction.
        for (Voxel voxel : voxels) {
            int i = voxel.x - focus.x;
            int j = voxel.y - focus.y;

            // Need to update all directions if at the center.
            if (i == 0 && j == 0) {
                v = 0;

                for (Direction direction : DIRECTIONS) {
                    existsMap.put(direction, true);
                    if (v > maxValueMap.get(direction)) {
                        maxValueMap.put(direction, v);
                    }
                    if (v < minValueMap.get(direction)) {
                        minValueMap.put(direction, v);
                    }
                }

                continue;
            } else if (j == 0) {
                dir = Direction.YZ_PLANE;
                v = i;
            } else if (i == 0) {
                dir = Direction.ZX_PLANE;
                v = j;
            } else if (i == j) {
                dir = Direction.POSITIVE_XY;
                v = i;
            } else if (i == -j) {
                dir = Direction.NEGATIVE_XY;
                v = i;
            } else {
                continue;
            }

            existsMap.put(dir, true);
            if (v > maxValueMap.get(dir)) {
                maxValueMap.put(dir, v);
            }
            if (v < minValueMap.get(dir)) {
                minValueMap.put(dir, v);
            }
        }

        HashMap<Direction, Integer> diameterMap = new HashMap<>();

        // Calculate diameter in each direction.
        for (Direction direction : DIRECTIONS) {
            int diameter = maxValueMap.get(direction) - minValueMap.get(direction) + 1;
            diameterMap.put(direction, existsMap.get(direction) ? diameter : 0);
        }

        return diameterMap;
    }

    /**
     * Selects the slice direction for a given minimum diameter direction.
     *
     * @param direction the direction of the minimum diameter
     * @param diameters the list of diameters
     * @return the slice direction
     */
    static Direction getSlice(Direction direction, HashMap<Direction, Integer> diameters) {
        switch (direction) {
            case YZ_PLANE:
                return Direction.YZ_PLANE;
            case ZX_PLANE:
                return Direction.ZX_PLANE;
            case POSITIVE_XY:
                return Direction.NEGATIVE_XY;
            case NEGATIVE_XY:
                return Direction.POSITIVE_XY;
            default:
                return null;
        }
    }

    /**
     * Selects specified number of voxels from a focus voxel.
     *
     * @param voxels the list of voxels
     * @param focus the focus voxel
     * @param n the number of voxels to select
     * @return the list of selected voxels
     */
    static ArrayList<Voxel> getSelected(ArrayList<Voxel> voxels, Voxel focus, double n) {
        ArrayList<Voxel> selected = new ArrayList<>();
        double r = Math.sqrt(n / Math.PI);

        // Select voxels within given radius.
        for (Voxel voxel : voxels) {
            double d = Math.sqrt(Math.pow(focus.x - voxel.x, 2) + Math.pow(focus.y - voxel.y, 2));
            if (d < r) {
                selected.add(voxel);
            }
        }

        return selected;
    }
}
