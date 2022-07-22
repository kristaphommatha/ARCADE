package arcade.patch.vis;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import sim.engine.SimState;
import sim.util.gui.ColorMap;
import arcade.core.vis.Panel;
import arcade.patch.agent.cell.PatchCell;
import arcade.patch.env.loc.Coordinate;
import arcade.patch.env.loc.CoordinateHex;
import arcade.patch.env.loc.CoordinateTri;
import arcade.patch.env.loc.PatchLocation;
import arcade.patch.env.loc.PatchLocationContainer;
import arcade.patch.env.loc.PatchLocationFactory;
import arcade.patch.env.loc.PatchLocationFactoryHex;
import arcade.patch.sim.PatchSeries;
import arcade.patch.sim.PatchSimulation;

/**
 * Container for patch-specific {@link arcade.core.vis.Drawer} classes for
 * hexagonal patches.
 */

public abstract class PatchDrawerHex extends PatchDrawer {
    /**
     * Creates a {@link PatchDrawer} for hexagonal patch simulations.
     *
     * @param panel  the panel the drawer is attached to
     * @param name  the name of the drawer
     * @param length  the length of array (x direction)
     * @param width  the width of array (y direction)
     * @param depth  the depth of array (z direction)
     * @param map  the color map for the array
     * @param bounds  the size of the drawer within the panel
     */
    PatchDrawerHex(Panel panel, String name, int length, int width, int depth,
                   ColorMap map, Rectangle2D.Double bounds) {
        super(panel, name, length, width, depth, map, bounds);
    }
    
    /**
     * Creates a {@link PatchDrawer} for hexagonal patch simulations.
     *
     * @param panel  the panel the drawer is attached to
     * @param name  the name of the drawer
     * @param length  the length of array (x direction)
     * @param width  the width of array (y direction)
     * @param depth  the depth of array (z direction)
     * @param bounds  the size of the drawer within the panel
     */
    PatchDrawerHex(Panel panel, String name, int length, int width, int depth,
                   Rectangle2D.Double bounds) {
        super(panel, name, length, width, depth, null, bounds);
    }
    
    /**
     * Expands an array to a triangular representation.
     *
     * @param toArr  the new empty triangular array
     * @param fromArr  the original array of values
     * @param length  the length of the original array
     * @param width  the width of the original array
     */
    private static void expand(double[][] toArr, double[][] fromArr, int length, int width) {
        for (int i = 0; i < length; i++) {
            for (int j = 0; j < width; j++) {
                expand(toArr, i, j, fromArr[i][j]);
            }
        }
    }
    
    /**
     * Draws a triangle for a given location with the given value.
     *
     * @param arr  the target array
     * @param i  the coordinate of the triangle in the x direction
     * @param j  the coordinate of the triangle in the y direction
     * @param val  the value for the triangle
     */
    private static void expand(double[][] arr, int i, int j, double val) {
        int dir = ((i + j) & 1) == 0 ? 0 : 2;
        arr[i * 3 + 2][j * 3] = val;
        arr[i * 3 + 2][j * 3 + 1] = val;
        arr[i * 3 + 2][j * 3 + 2] = val;
        arr[i * 3 + 1][j * 3 + 1] = val;
        arr[i * 3 + 3][j * 3 + 1] = val;
        arr[i * 3][j * 3 + dir] = val;
        arr[i * 3 + 1][j * 3 + dir] = val;
        arr[i * 3 + 3][j * 3 + dir] = val;
        arr[i * 3 + 4][j * 3 + dir] = val;
    }
    
    /**
     * Extension of {@link PatchDrawer} for drawing hexagonal cells.
     */
    public static class PatchCells extends PatchDrawerHex {
        /** Length of the lattice (x direction). */
        private final int length;
        
        /** Width of the lattice (y direction). */
        private final int width;
        
        /** Drawing view. */
        private final View view;
        
        /**
         * Creates a {@link PatchDrawer} for drawing hexagonal cells.
         * <p>
         * Length and width of the drawer are expanded from the given length and
         * width of the simulation so each index can be drawn as a 3x3 triangle.
         *
         * @param panel  the panel the drawer is attached to
         * @param name  the name of the drawer
         * @param length  the length of array (x direction)
         * @param width  the width of array (y direction)
         * @param depth  the depth of array (z direction)
         * @param map  the color map for the array
         * @param bounds  the size of the drawer within the panel
         */
        public PatchCells(Panel panel, String name,
                          int length, int width, int depth,
                          ColorMap map, Rectangle2D.Double bounds) {
            super(panel, name, 3 * length + 2, 3 * width, depth, map, bounds);
            this.length = length;
            this.width = width;
            String[] split = name.split(":");
            view = View.valueOf(split[1]);
        }
        
        @Override
        public void step(SimState state) {
            PatchSimulation sim = (PatchSimulation) state;
            double[][] arr = array.field;
            double[][] temp = new double[length][width];
            double[][] counter = new double[length][width];
            
            PatchCell cell;
            PatchLocation location;
            
            switch (view) {
                case STATE: case AGE:
                    for (int i = 0; i < length; i++) {
                        for (int j = 0; j < width; j++) {
                            temp[i][j] = map.defaultValue();
                        }
                    }
                    break;
                case VOLUME: case HEIGHT: case COUNTS:
                    array.setTo(0);
                    break;
                default:
                    break;
            }
            
            HashMap<Integer, Integer> indices = new HashMap<>();
            
            for (Object obj : sim.getGrid().getAllObjects()) {
                cell = (PatchCell) obj;
                location = (PatchLocation) cell.getLocation();
                
                if (location.getCoordinate().z == 0) {
                    ArrayList<Coordinate> coords = location.getSubCoordinates();
                    
                    int hash = location.hashCode();
                    int index = -1;
                    if (indices.containsKey(hash)) { index = indices.get(hash); }
                    indices.put(hash, ++index);
                    
                    switch (view) {
                        case STATE:
                        case AGE:
                            CoordinateTri mainCoord = (CoordinateTri) coords.get(index);
                            switch (view) {
                                case STATE:
                                    temp[mainCoord.x][mainCoord.y] = cell.getState().ordinal();
                                    break;
                                case AGE:
                                    temp[mainCoord.x][mainCoord.y] = cell.getAge();
                                    break;
                                default:
                                    break;
                            }
                            break;
                        case COUNTS:
                        case VOLUME:
                        case HEIGHT:
                            double volume = cell.getVolume();
                            double height = cell.getHeight();
                            
                            for (Coordinate coord : coords) {
                                CoordinateTri triCoord = (CoordinateTri) coord;
                                
                                switch (view) {
                                    case COUNTS:
                                        temp[triCoord.x][triCoord.y]++;
                                        break;
                                    case VOLUME:
                                        temp[triCoord.x][triCoord.y] += volume;
                                        counter[triCoord.x][triCoord.y]++;
                                        break;
                                    case HEIGHT:
                                        temp[triCoord.x][triCoord.y] += height;
                                        counter[triCoord.x][triCoord.y]++;
                                    default:
                                        break;
                                }
                            }
                        default:
                            break;
                    }
                }
            }
    
            switch (view) {
                case VOLUME:
                case HEIGHT:
                    for (int i = 0; i < length; i++) {
                        for (int j = 0; j < width; j++) {
                            temp[i][j] /= counter[i][j];
                        }
                    }
                    break;
                default:
                    break;
            }
            
            expand(arr, temp, length, width);
        }
    }
    
    /**
     * Extension of {@link PatchDrawer} for drawing hexagonal patches.
     */
    public static class PatchGrid extends PatchDrawerHex {
        /** Offsets for hexagons. */
        private static final int[][] OFFSETS = {
                { 0, 0 },
                { 2, 0 },
                { 3, 1 },
                { 2, 2 },
                { 0, 2 },
                { -1, 1 }
        };
        
        /** Length of the lattice (x direction). */
        private final int length;
        
        /** Width of the lattice (y direction). */
        private final int width;
        
        /** List of patch locations. */
        private ArrayList<PatchLocation> locations;
        
        /**
         * Creates a {@code PatchGrid} drawer.
         * <p>
         * Length and width of the drawer are expanded from the given length and
         * width of the simulation.
         *
         * @param panel  the panel the drawer is attached to
         * @param name  the name of the drawer
         * @param length  the length of array (x direction)
         * @param width  the width of array (y direction)
         * @param depth  the depth of array (z direction)
         * @param bounds  the size of the drawer within the panel
         */
        PatchGrid(Panel panel, String name,
                int length, int width, int depth, Rectangle2D.Double bounds) {
            super(panel, name, 3 * length + 2, 3 * width, depth, bounds);
            this.length = length + 1;
            this.width = width;
            field.width = this.length;
            field.height = this.width;
        }
        
        /**
         * Steps the drawer to draw triangular grid.
         *
         * @param simstate  the MASON simulation state
         */
        public void step(SimState simstate) {
            PatchSimulation sim = (PatchSimulation) simstate;
            field.clear();
            graph.clear();
            
            if (locations == null) {
                locations = new ArrayList<>();
                PatchSeries series = (PatchSeries) sim.getSeries();
                PatchLocationFactory factory = new PatchLocationFactoryHex();
                ArrayList<Coordinate> coordinates =
                        factory.getCoordinates(series.radius, series.depth);
                
                for (Coordinate coordinate : coordinates) {
                    PatchLocationContainer container = new PatchLocationContainer(0, coordinate);
                    PatchLocation location = (PatchLocation) container.convert(factory, null);
                    locations.add(location);
                }
            }
            
            // Draw triangular grid.
            for (int i = 0; i <= width; i++) {
                add(field, graph, 1,
                    (i % 2 == 0 ? 0 : 1), i,
                    (i % 2 == 0 ? length : length - 1), i);
            }
            
            for (int i = 0; i <= length - 1; i += 2) {
                for (int j = 0; j < width; j++) {
                    add(field, graph, 1,
                        (j % 2 == 0 ? i : i + 1), j,
                        (j % 2 == 0 ? i + 1 : i), j + 1);
                }
            }
            
            for (int i = 1; i <= length; i += 2) {
                for (int j = 0; j < width; j++) {
                    add(field, graph, 1,
                        (j % 2 == 0 ? i + 1 : i), j,
                        (j % 2 == 0 ? i : i + 1), j + 1);
                }
            }
            
            // Draw hexagonal agent locations.
            for (PatchLocation loc : locations) {
                CoordinateTri tri = (CoordinateTri) loc.getSubCoordinate();
                for (int i = 0; i < 6; i++) {
                    add(field, graph, 2,
                        tri.x + OFFSETS[i][0], tri.y + OFFSETS[i][1],
                        tri.x + OFFSETS[(i + 1) % 6][0], tri.y + OFFSETS[(i + 1) % 6][1]);
                }
            }
            
            // Draw border.
            int radius = ((PatchSeries) sim.getSeries()).radius;
            int ind;
            int r;
            for (PatchLocation loc : locations) {
                CoordinateHex coord = (CoordinateHex) loc.getCoordinate();
                CoordinateTri subcoord = (CoordinateTri) loc.getSubCoordinate();
                
                r = (int) ((Math.abs(coord.u) + Math.abs(coord.v) + Math.abs(coord.w)) / 2.0) + 1;
                
                if (r == radius) {
                    if (coord.u == radius - 1) {
                        ind = 1;
                    } else if (coord.u == 1 - radius) {
                        ind = 4;
                    } else if (coord.v == radius - 1) {
                        ind = 5;
                    } else if (coord.v == 1 - radius) {
                        ind = 2;
                    } else if (coord.w == radius - 1) {
                        ind = 3;
                    } else if (coord.w == 1 - radius) {
                        ind = 0;
                    } else {
                        ind = 0;
                    }
                    
                    add(field, graph, 3,
                        subcoord.x + OFFSETS[ind][0],
                        subcoord.y + OFFSETS[ind][1],
                        subcoord.x + OFFSETS[(ind + 1) % 6][0],
                        subcoord.y + OFFSETS[(ind + 1) % 6][1]);
                    add(field, graph, 3,
                        subcoord.x + OFFSETS[(ind + 1) % 6][0],
                        subcoord.y + OFFSETS[(ind + 1) % 6][1],
                        subcoord.x + OFFSETS[(ind + 2) % 6][0],
                        subcoord.y + OFFSETS[(ind + 2) % 6][1]);
                    
                    if (coord.u == 0 || coord.v == 0 || coord.w == 0) {
                        if (coord.u == 0 && coord.v == radius - 1) {
                            ind = 1;
                        } else if (coord.u == 0 && coord.w == radius - 1) {
                            ind = 4;
                        } else if (coord.v == 0 && coord.u == radius - 1) {
                            ind = 0;
                        } else if (coord.v == 0 && coord.w == radius - 1) {
                            ind = 3;
                        } else if (coord.w == 0 && coord.u == radius - 1) {
                            ind = 3;
                        } else if (coord.w == 0 && coord.v == radius - 1) {
                            ind = 0;
                        }
                        
                        add(field, graph, 3,
                            subcoord.x + OFFSETS[ind][0],
                            subcoord.y + OFFSETS[ind][1],
                            subcoord.x + OFFSETS[(ind + 1) % 6][0],
                            subcoord.y + OFFSETS[(ind + 1) % 6][1]);
                    }
                }
            }
        }
    }
}
