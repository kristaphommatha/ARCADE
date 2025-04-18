package arcade.potts.vis;

import sim.engine.SimState;
import arcade.core.sim.Series;
import arcade.core.sim.Simulation;
import arcade.core.vis.*;
import static arcade.potts.vis.PottsColorMaps.*;

/** Extension of {@link Visualization} for potts models. */
public final class PottsVisualization extends Visualization {
    /** Maximum horizontal size of panel. */
    static final int MAX_HORIZONTAL = 600;

    /** Maximum vertical size of panel. */
    static final int MAX_VERTICAL = 900;

    /** Length of the array (x direction). */
    final int length;

    /** Width of the array (y direction). */
    final int width;

    /** Height of the array (z direction). */
    final int height;

    /** Color maps for the simulation. */
    final PottsColorMaps maps;

    /** Horizontal size of the panels. */
    final int horizontal;

    /** Vertical size of the panels. */
    final int vertical;

    /**
     * Creates a {@link Visualization} for potts simulations.
     *
     * @param sim the simulation instance
     */
    public PottsVisualization(Simulation sim) {
        super((SimState) sim);

        // Update sizes.
        Series series = sim.getSeries();
        length = series.length;
        width = series.width;
        height = series.height;

        // Get color maps.
        maps = new PottsColorMaps(series);

        // Calculate sizing of panels.
        int horz;
        int vert;

        if (length != width) {
            horz = MAX_HORIZONTAL;
            vert = (int) Math.round((width + .0) / length * MAX_HORIZONTAL);

            if (vert > MAX_VERTICAL) {
                double frac = (MAX_VERTICAL + .0) / vert;
                horz = (int) Math.round(horz * frac);
                vert = (int) Math.round(vert * frac);
            }
        } else {
            horz = MAX_HORIZONTAL;
            vert = MAX_HORIZONTAL;
        }

        horizontal = horz;
        vertical = vert;
    }

    @Override
    public Drawer[] createDrawers() {
        if (height == 1) {
            return create2DDrawers();
        } else {
            return create3DDrawers();
        }
    }

    /**
     * Creates drawers for visualizing 2D simulations.
     *
     * @return a list of {@link Drawer} instances
     */
    Drawer[] create2DDrawers() {
        int h = horizontal / 2;
        int v = vertical / 2;

        return new Drawer[] {
            new PottsDrawer.PottsCells(
                    panels[0], "agents:CYTOPLASM", length, width, height, MAP_CYTOPLASM, null),
            new PottsDrawer.PottsCells(
                    panels[0], "agents:NUCLEUS", length, width, height, MAP_NUCLEUS, null),
            new PottsDrawer.PottsGrid(panels[0], "grid", length, width, height, null),
            new PottsDrawer.PottsCells(
                    panels[1],
                    "agents:STATE",
                    length,
                    width,
                    height,
                    MAP_STATE,
                    getBox(0, 0, h, v)),
            new PottsDrawer.PottsCells(
                    panels[1],
                    "agents:POPULATION",
                    length,
                    width,
                    height,
                    MAP_POPULATION,
                    getBox(h, 0, h, v)),
            new PottsDrawer.PottsCells(
                    panels[1],
                    "agents:VOLUME",
                    length,
                    width,
                    height,
                    maps.mapVolume,
                    getBox(0, v, h, v)),
            new PottsDrawer.PottsCells(
                    panels[1],
                    "agents:HEIGHT",
                    length,
                    width,
                    height,
                    maps.mapHeight,
                    getBox(h, v, h, v)),
            new PottsDrawer.PottsCells(
                    panels[1],
                    "agents:OVERLAY",
                    length,
                    width,
                    height,
                    MAP_OVERLAY,
                    getBox(0, 0, h, v)),
            new PottsDrawer.PottsCells(
                    panels[1],
                    "agents:OVERLAY",
                    length,
                    width,
                    height,
                    MAP_OVERLAY,
                    getBox(h, 0, h, v)),
            new PottsDrawer.PottsCells(
                    panels[1],
                    "agents:OVERLAY",
                    length,
                    width,
                    height,
                    MAP_OVERLAY,
                    getBox(0, v, h, v)),
            new PottsDrawer.PottsCells(
                    panels[1],
                    "agents:OVERLAY",
                    length,
                    width,
                    height,
                    MAP_OVERLAY,
                    getBox(h, v, h, v)),
            new PottsDrawer.PottsGrid(panels[1], "grid", length, width, height, getBox(0, 0, h, v)),
            new PottsDrawer.PottsGrid(panels[1], "grid", length, width, height, getBox(h, 0, h, v)),
            new PottsDrawer.PottsGrid(panels[1], "grid", length, width, height, getBox(0, v, h, v)),
            new PottsDrawer.PottsGrid(panels[1], "grid", length, width, height, getBox(h, v, h, v)),
        };
    }

    /**
     * Creates drawers for visualizing 3D simulations.
     *
     * @return a list of {@link Drawer} instances
     */
    Drawer[] create3DDrawers() {
        int h = horizontal / 2;
        int v = vertical / 2;

        int hh = (int) Math.round((length + .0) / (length + height) * h);
        int vv = (int) Math.round((width + .0) / (width + height) * v);

        int hx = (int) Math.round((height + .0) / (length + height) * h);
        int vx = (int) Math.round((height + .0) / (width + height) * v);

        return new Drawer[] {
            new PottsDrawer.PottsCells(
                    panels[0],
                    "agents:CYTOPLASM:Z",
                    length,
                    width,
                    height,
                    MAP_CYTOPLASM,
                    getBox(0, 0, hh * 2, vv * 2)),
            new PottsDrawer.PottsCells(
                    panels[0],
                    "agents:CYTOPLASM:X",
                    length,
                    width,
                    height,
                    MAP_CYTOPLASM,
                    getBox(hh * 2, 0, hx * 2, vv * 2)),
            new PottsDrawer.PottsCells(
                    panels[0],
                    "agents:CYTOPLASM:Y",
                    length,
                    width,
                    height,
                    MAP_CYTOPLASM,
                    getBox(0, vv * 2, hh * 2, vx * 2)),
            new PottsDrawer.PottsCells(
                    panels[0],
                    "agents:NUCLEUS:Z",
                    length,
                    width,
                    height,
                    MAP_NUCLEUS,
                    getBox(0, 0, hh * 2, vv * 2)),
            new PottsDrawer.PottsCells(
                    panels[0],
                    "agents:NUCLEUS:X",
                    length,
                    width,
                    height,
                    MAP_NUCLEUS,
                    getBox(hh * 2, 0, hx * 2, vv * 2)),
            new PottsDrawer.PottsCells(
                    panels[0],
                    "agents:NUCLEUS:Y",
                    length,
                    width,
                    height,
                    MAP_NUCLEUS,
                    getBox(0, vv * 2, hh * 2, vx * 2)),
            new PottsDrawer.PottsCells(
                    panels[1],
                    "agents:STATE:Z",
                    length,
                    width,
                    height,
                    MAP_STATE,
                    getBox(0, 0, hh, vv)),
            new PottsDrawer.PottsCells(
                    panels[1],
                    "agents:POPULATION:Z",
                    length,
                    width,
                    height,
                    MAP_POPULATION,
                    getBox(h, 0, hh, vv)),
            new PottsDrawer.PottsCells(
                    panels[1],
                    "agents:VOLUME:Z",
                    length,
                    width,
                    height,
                    maps.mapVolume,
                    getBox(0, v, hh, vv)),
            new PottsDrawer.PottsCells(
                    panels[1],
                    "agents:HEIGHT:Z",
                    length,
                    width,
                    height,
                    maps.mapHeight,
                    getBox(h, v, hh, vv)),
            new PottsDrawer.PottsCells(
                    panels[1],
                    "agents:OVERLAY:Z",
                    length,
                    width,
                    height,
                    MAP_OVERLAY,
                    getBox(0, 0, hh, vv)),
            new PottsDrawer.PottsCells(
                    panels[1],
                    "agents:OVERLAY:Z",
                    length,
                    width,
                    height,
                    MAP_OVERLAY,
                    getBox(h, 0, hh, vv)),
            new PottsDrawer.PottsCells(
                    panels[1],
                    "agents:OVERLAY:Z",
                    length,
                    width,
                    height,
                    MAP_OVERLAY,
                    getBox(0, v, hh, vv)),
            new PottsDrawer.PottsCells(
                    panels[1],
                    "agents:OVERLAY:Z",
                    length,
                    width,
                    height,
                    MAP_OVERLAY,
                    getBox(h, v, hh, vv)),
            new PottsDrawer.PottsGrid(
                    panels[1], "grid:Z", length, width, height, getBox(0, 0, hh, vv)),
            new PottsDrawer.PottsGrid(
                    panels[1], "grid:Z", length, width, height, getBox(h, 0, hh, vv)),
            new PottsDrawer.PottsGrid(
                    panels[1], "grid:Z", length, width, height, getBox(0, v, hh, vv)),
            new PottsDrawer.PottsGrid(
                    panels[1], "grid:Z", length, width, height, getBox(h, v, hh, vv)),
            new PottsDrawer.PottsCells(
                    panels[1],
                    "agents:STATE:X",
                    length,
                    width,
                    height,
                    MAP_STATE,
                    getBox(hh, 0, hx, vv)),
            new PottsDrawer.PottsCells(
                    panels[1],
                    "agents:POPULATION:X",
                    length,
                    width,
                    height,
                    MAP_POPULATION,
                    getBox(h + hh, 0, hx, vv)),
            new PottsDrawer.PottsCells(
                    panels[1],
                    "agents:VOLUME:X",
                    length,
                    width,
                    height,
                    maps.mapVolume,
                    getBox(hh, v, hx, vv)),
            new PottsDrawer.PottsCells(
                    panels[1],
                    "agents:HEIGHT:X",
                    length,
                    width,
                    height,
                    maps.mapHeight,
                    getBox(h + hh, v, hx, vv)),
            new PottsDrawer.PottsCells(
                    panels[1],
                    "agents:OVERLAY:X",
                    length,
                    width,
                    height,
                    MAP_OVERLAY,
                    getBox(hh, 0, hx, vv)),
            new PottsDrawer.PottsCells(
                    panels[1],
                    "agents:OVERLAY:X",
                    length,
                    width,
                    height,
                    MAP_OVERLAY,
                    getBox(h + hh, 0, hx, vv)),
            new PottsDrawer.PottsCells(
                    panels[1],
                    "agents:OVERLAY:X",
                    length,
                    width,
                    height,
                    MAP_OVERLAY,
                    getBox(hh, v, hx, vv)),
            new PottsDrawer.PottsCells(
                    panels[1],
                    "agents:OVERLAY:X",
                    length,
                    width,
                    height,
                    MAP_OVERLAY,
                    getBox(h + hh, v, hx, vv)),
            new PottsDrawer.PottsGrid(
                    panels[1], "grid:X", length, width, height, getBox(hh, 0, hx, vv)),
            new PottsDrawer.PottsGrid(
                    panels[1], "grid:X", length, width, height, getBox(h + hh, 0, hx, vv)),
            new PottsDrawer.PottsGrid(
                    panels[1], "grid:X", length, width, height, getBox(hh, v, hx, vv)),
            new PottsDrawer.PottsGrid(
                    panels[1], "grid:X", length, width, height, getBox(h + hh, v, hx, vv)),
            new PottsDrawer.PottsCells(
                    panels[1],
                    "agents:STATE:Y",
                    length,
                    width,
                    height,
                    MAP_STATE,
                    getBox(0, vv, hh, vx)),
            new PottsDrawer.PottsCells(
                    panels[1],
                    "agents:POPULATION:Y",
                    length,
                    width,
                    height,
                    MAP_POPULATION,
                    getBox(h, vv, hh, vx)),
            new PottsDrawer.PottsCells(
                    panels[1],
                    "agents:VOLUME:Y",
                    length,
                    width,
                    height,
                    maps.mapVolume,
                    getBox(0, v + vv, hh, vx)),
            new PottsDrawer.PottsCells(
                    panels[1],
                    "agents:HEIGHT:Y",
                    length,
                    width,
                    height,
                    maps.mapHeight,
                    getBox(h, v + vv, hh, vx)),
            new PottsDrawer.PottsCells(
                    panels[1],
                    "agents:OVERLAY:Y",
                    length,
                    width,
                    height,
                    MAP_OVERLAY,
                    getBox(0, vv, hh, vx)),
            new PottsDrawer.PottsCells(
                    panels[1],
                    "agents:OVERLAY:Y",
                    length,
                    width,
                    height,
                    MAP_OVERLAY,
                    getBox(h, vv, hh, vx)),
            new PottsDrawer.PottsCells(
                    panels[1],
                    "agents:OVERLAY:Y",
                    length,
                    width,
                    height,
                    MAP_OVERLAY,
                    getBox(0, v + vv, hh, vx)),
            new PottsDrawer.PottsCells(
                    panels[1],
                    "agents:OVERLAY:Y",
                    length,
                    width,
                    height,
                    MAP_OVERLAY,
                    getBox(h, v + vv, hh, vx)),
            new PottsDrawer.PottsGrid(
                    panels[1], "grid:Y", length, width, height, getBox(0, vv, hh, vx)),
            new PottsDrawer.PottsGrid(
                    panels[1], "grid:Y", length, width, height, getBox(h, vv, hh, vx)),
            new PottsDrawer.PottsGrid(
                    panels[1], "grid:Y", length, width, height, getBox(0, v + vv, hh, vx)),
            new PottsDrawer.PottsGrid(
                    panels[1], "grid:Y", length, width, height, getBox(h, v + vv, hh, vx)),
        };
    }

    @Override
    public Panel[] createPanels() {
        return new Panel[] {
            new Panel("[POTTS] Agents", 100, 50, horizontal, vertical, this),
            new Panel("[POTTS] Auxiliary", horizontal + 120, 50, horizontal, vertical, this),
        };
    }
}
