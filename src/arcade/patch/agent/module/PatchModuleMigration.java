package arcade.patch.agent.module;

import ec.util.MersenneTwisterFast;
import arcade.core.sim.Simulation;
import arcade.core.util.Parameters;
import arcade.patch.agent.cell.PatchCell;
import arcade.patch.agent.cell.PatchCellCART;
import arcade.patch.env.location.PatchLocation;
import static arcade.patch.util.PatchEnums.State;

/**
 * Extension of {@link PatchModule} for migration.
 *
 * <p>During migration, the module is stepped once after the number of ticks corresponding to
 * (distance to move) / {@code MIGRATION_RATE} has passed. The module will move the cell from one
 * location to the best valid location in the neighborhood.
 */
public class PatchModuleMigration extends PatchModule {
    /** Tracker for duration of cell movement. */
    private int ticker;

    /** Cell migration rate [um/min]. */
    private final double migrationRate;

    /** Time required for cell migration [min]. */
    private final double movementDuration;

    /**
     * Creates a migration {@link PatchModule} for the given cell.
     *
     * <p>Loaded parameters include:
     *
     * <ul>
     *   <li>{@code MIGRATION_RATE} = cell migration rate
     * </ul>
     *
     * @param cell the {@link PatchCell} the module is associated with
     */
    public PatchModuleMigration(PatchCell cell) {
        super(cell);

        // Set loaded parameters.
        Parameters parameters = cell.getParameters();
        migrationRate = parameters.getDouble("migration/MIGRATION_RATE");
        movementDuration = Math.round(location.getCoordinateSize() / migrationRate);
    }

    @Override
    public void step(MersenneTwisterFast random, Simulation sim) {
        if (ticker > movementDuration) {
            PatchLocation newLocation = cell.selectBestLocation(sim, random);
            if (newLocation == null) {
                if (cell instanceof PatchCellCART) {
                    cell.setState(State.PAUSED);
                } else {
                    cell.setState(State.QUIESCENT);
                }
            } else {
                if (!location.equals(newLocation)) {
                    sim.getGrid().moveObject(cell, location, newLocation);

                    // TODO: Update environment generator sites.
                }
                cell.setState(State.UNDEFINED);
            }
        } else {
            ticker++;
        }
    }
}
