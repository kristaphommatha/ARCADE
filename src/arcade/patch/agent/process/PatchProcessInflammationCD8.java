package arcade.patch.agent.process;

import ec.util.MersenneTwisterFast;
import arcade.core.agent.process.Process;
import arcade.core.sim.Simulation;
import arcade.core.util.Parameters;
import arcade.patch.agent.cell.PatchCellCART;

/**
 * Extension of {@link PatchProcessInflammation} for CD8 CAR T-cells
 *
 * <p>{@code InflammationCD8} determines granzyme amounts produced for cytotoxic effector functions
 * as a function of IL-2 bound and antigen-induced activation state.
 */
public class PatchProcessInflammationCD8 extends PatchProcessInflammation {
    /** Moles of granzyme produced per moles IL-2 [mol granzyme/mol IL-2]. */
    private static final double GRANZ_PER_IL2 = 0.005;

    /** Delay in IL-2 synthesis after antigen-induced activation. */
    private final int granzSynthesisDelay;

    /** Amount of IL-2 bound in past being used for current granzyme production calculation. */
    private double priorIL2granz;

    /**
     * Creates a CD8 {@link PatchProcessInflammation} module.
     *
     * <p>Initial amount of internal granzyme is set. Granzyme production parameters set.
     *
     * @param cell the {@link PatchCellCART} the module is associated with
     */
    public PatchProcessInflammationCD8(PatchCellCART cell) {
        super(cell);

        Parameters parameters = cell.getParameters();
        this.granzSynthesisDelay = parameters.getInt("inflammation/GRANZ_SYNTHESIS_DELAY");
        this.priorIL2granz = 0;

        amts[GRANZYME] = 1; // [molecules]
        names.add(GRANZYME, "granzyme");
    }

    @Override
    public void stepProcess(MersenneTwisterFast random, Simulation sim) {

        // Determine amount of granzyme production based on if cell is activated
        // as a function of IL-2 production.
        int granzIndex = (iL2Ticker % boundArray.length) - granzSynthesisDelay;
        if (granzIndex < 0) {
            granzIndex += boundArray.length;
        }
        priorIL2granz = boundArray[granzIndex];

        if (active && activeTicker > granzSynthesisDelay) {
            amts[GRANZYME] += GRANZ_PER_IL2 * (priorIL2granz / iL2Receptors);
        }

        // Update environment.
        // Convert units back from molecules to molecules/cm^3.
        double iL2Env = ((extIL2 - (extIL2 * fraction - amts[IL2_EXT])) * 1E12 / loc.getVolume());
        sim.getLattice("IL-2").setValue(loc, iL2Env);
    }

    @Override
    public void update(Process mod) {
        PatchProcessInflammationCD8 inflammation = (PatchProcessInflammationCD8) mod;
        double split = (this.cell.getVolume() / this.volume);

        // Update daughter cell inflammation as a fraction of parent.
        this.amts[IL2RBGA] = inflammation.amts[IL2RBGA] * split;
        this.amts[IL2_IL2RBG] = inflammation.amts[IL2_IL2RBG] * split;
        this.amts[IL2_IL2RBGA] = inflammation.amts[IL2_IL2RBGA] * split;
        this.amts[IL2RBG] =
                iL2Receptors - this.amts[IL2RBGA] - this.amts[IL2_IL2RBG] - this.amts[IL2_IL2RBGA];
        this.amts[IL2_INT_TOTAL] = this.amts[IL2_IL2RBG] + this.amts[IL2_IL2RBGA];
        this.amts[IL2R_TOTAL] = this.amts[IL2RBG] + this.amts[IL2RBGA];
        this.amts[GRANZYME] = inflammation.amts[GRANZYME] * split;
        this.boundArray = (inflammation.boundArray).clone();

        // Update parent cell with remaining fraction.
        inflammation.amts[IL2RBGA] *= (1 - split);
        inflammation.amts[IL2_IL2RBG] *= (1 - split);
        inflammation.amts[IL2_IL2RBGA] *= (1 - split);
        inflammation.amts[IL2RBG] =
                iL2Receptors
                        - inflammation.amts[IL2RBGA]
                        - inflammation.amts[IL2_IL2RBG]
                        - inflammation.amts[IL2_IL2RBGA];
        inflammation.amts[IL2_INT_TOTAL] =
                inflammation.amts[IL2_IL2RBG] + inflammation.amts[IL2_IL2RBGA];
        inflammation.amts[IL2R_TOTAL] = inflammation.amts[IL2RBG] + inflammation.amts[IL2RBGA];
        inflammation.amts[GRANZYME] *= (1 - split);
        inflammation.volume *= (1 - split);
    }
}
