package arcade.potts.agent.module;

import sim.util.distribution.Poisson;
import ec.util.MersenneTwisterFast;
import arcade.core.sim.Simulation;
import arcade.core.util.MiniBox;
import arcade.potts.agent.cell.PottsCell;
import static arcade.core.util.Enums.State;
import static arcade.potts.util.PottsEnums.Phase;

/**
 * Extension of {@link PottsModule} for proliferation.
 * <p>
 * During proliferation, cells grow and, once they reach a critical threshold,
 * divide to create a new daughter cell.
 */

public class PottsModuleProliferationSimple extends PottsModuleProliferation {
    /** Ratio of critical volume for size checkpoint. */
    static final double SIZE_CHECKPOINT = 2 * 0.95;
    
    /** Event rate for G1 phase (steps/tick). */
    final double rateG1;
    
    /** Event rate for S phase (steps/tick). */
    final double rateS;
    
    /** Event rate for G2 phase (steps/tick). */
    final double rateG2;
    
    /** Event rate for M phase (steps/tick). */
    final double rateM;
    
    /** Steps for G1 phase (steps). */
    final int stepsG1;
    
    /** Steps for S phase (steps). */
    final int stepsS;
    
    /** Steps for G2 phase (steps). */
    final int stepsG2;
    
    /** Steps for M phase (steps). */
    final int stepsM;
    
    /** Overall growth rate for cell (voxels/tick). */
    final double cellGrowthRate;
    
    /** Basal rate of apoptosis (ticks^-1). */
    final double basalApoptosisRate;
    
    /**
     * Creates a proliferation {@code Module} for the given {@link PottsCell}.
     *
     * @param cell  the {@link PottsCell} the module is associated with
     */
    public PottsModuleProliferationSimple(PottsCell cell) {
        super(cell);
        
        MiniBox parameters = cell.getParameters();
        rateG1 = parameters.getDouble("proliferation/RATE_G1");
        rateS = parameters.getDouble("proliferation/RATE_S");
        rateG2 = parameters.getDouble("proliferation/RATE_G2");
        rateM = parameters.getDouble("proliferation/RATE_M");
        stepsG1 = parameters.getInt("proliferation/STEPS_G1");
        stepsS = parameters.getInt("proliferation/STEPS_S");
        stepsG2 = parameters.getInt("proliferation/STEPS_G2");
        stepsM = parameters.getInt("proliferation/STEPS_M");
        cellGrowthRate = parameters.getDouble("proliferation/CELL_GROWTH_RATE");
        basalApoptosisRate = parameters.getDouble("proliferation/BASAL_APOPTOSIS_RATE");
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * Cell increases in size toward a target of twice its critical size at a
     * rate of {@code CELL_GROWTH_RATE}.
     * Cell will transition to S phase after completing {@code STEPS_G1} steps
     * at an average rate of {@code RATE_G1}.
     * At each tick, cell may randomly apoptosis at a basal rate of
     * {@code BASAL_APOPTOSIS_RATE}.
     */
    void stepG1(MersenneTwisterFast random) {
        // Random chance of apoptosis.
        if (random.nextDouble() < basalApoptosisRate) {
            cell.setState(State.APOPTOTIC);
            return;
        }
        
        // Increase size of cell.
        cell.updateTarget(cellGrowthRate, 2);
        
        // Check for phase transition.
        Poisson poisson = poissonFactory.createPoisson(rateG1, random);
        currentSteps += poisson.nextInt();
        if (currentSteps >= stepsG1) { setPhase(Phase.PROLIFERATIVE_S); }
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * Cell increases in size toward a target of twice its critical size at a
     * rate of {@code CELL_GROWTH_RATE}.
     * Cell will transition to G2 phase after completing {@code STEPS_S} steps
     * at an average rate of {@code RATE_S}.
     */
    @Override
    void stepS(MersenneTwisterFast random) {
        // Increase size of cell.
        cell.updateTarget(cellGrowthRate, 2);
        
        // Check for phase transition.
        Poisson poisson = poissonFactory.createPoisson(rateS, random);
        currentSteps += poisson.nextInt();
        if (currentSteps >= stepsS) { setPhase(Phase.PROLIFERATIVE_G2); }
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * Cell increases in size toward a target of twice its critical size at a
     * rate of {@code CELL_GROWTH_RATE}.
     * Cell will transition to M phase after completing {@code STEPS_G2} steps
     * at an average rate of {@code RATE_G2}.
     * At each tick, cell may randomly apoptosis at a basal rate of
     * {@code BASAL_APOPTOSIS_RATE}.
     */
    void stepG2(MersenneTwisterFast random) {
        // Random chance of apoptosis.
        if (random.nextDouble() < basalApoptosisRate) {
            cell.setState(State.APOPTOTIC);
            return;
        }
        
        // Increase size of cell.
        cell.updateTarget(cellGrowthRate, 2);
        
        // Check for phase transition.
        Poisson poisson = poissonFactory.createPoisson(rateG2, random);
        currentSteps += poisson.nextInt();
        if (currentSteps >= stepsG2) { setPhase(Phase.PROLIFERATIVE_M); }
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * Cell increases in size toward a target of twice its critical size at a
     * rate of {@code CELL_GROWTH_RATE}.
     * Cell will complete cell division after completing {@code STEPS_M} steps
     * at an average rate of {@code RATE_M}.
     * Cell must be greater than {@code SIZE_CHECKPOINT} times the critical size.
     */
    void stepM(MersenneTwisterFast random, Simulation sim) {
        // Increase size of cell.
        cell.updateTarget(cellGrowthRate, 2);
        
        // Check for phase transition.
        Poisson poisson = poissonFactory.createPoisson(rateM, random);
        currentSteps += poisson.nextInt();
        if (cell.getVolume() >= SIZE_CHECKPOINT * cell.getCriticalVolume()
                && currentSteps >= stepsM) {
            addCell(random, sim);
            setPhase(Phase.PROLIFERATIVE_G1);
        }
    }
}
