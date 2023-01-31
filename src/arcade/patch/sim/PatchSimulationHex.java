package arcade.patch.sim;

import arcade.core.agent.action.Action;
import arcade.core.env.comp.Component;
import arcade.core.sim.Series;
import arcade.core.util.MiniBox;
import arcade.patch.agent.action.PatchActionConvert;
import arcade.patch.agent.action.PatchActionInsert;
import arcade.patch.agent.action.PatchActionRemove;
import arcade.patch.agent.cell.PatchCellFactory;
import arcade.patch.env.comp.PatchComponentCycle;
import arcade.patch.env.comp.PatchComponentPulse;
import arcade.patch.env.comp.PatchComponentSitesGraphTri;
import arcade.patch.env.comp.PatchComponentSitesPatternTri;
import arcade.patch.env.comp.PatchComponentSitesSource;
import arcade.patch.env.lat.PatchLatticeFactory;
import arcade.patch.env.lat.PatchLatticeFactoryTri;
import arcade.patch.env.loc.PatchLocationFactory;
import arcade.patch.env.loc.PatchLocationFactoryHex;
import arcade.patch.env.loc.PatchLocationHex;

/**
 * Extension of {@link PatchSimulation} for hexagonal geometry.
 */

public final class PatchSimulationHex extends PatchSimulation {
    /**
     * Hexagonal simulation for a {@link Series} for given random seed.
     *
     * @param seed  the random seed for random number generator
     * @param series  the simulation series
     */
    public PatchSimulationHex(long seed, Series series) {
        super(seed, series);
        PatchLocationHex.updateConfigs((PatchSeries) series);
    }
    
    @Override
    public PatchLocationFactory makeLocationFactory() {
        return new PatchLocationFactoryHex();
    }
    
    @Override
    public PatchCellFactory makeCellFactory() {
        return new PatchCellFactory();
    }
    
    @Override
    public PatchLatticeFactory makeLatticeFactory() {
        return new PatchLatticeFactoryTri();
    }
    
    @Override
    public Action makeAction(String actionClass, MiniBox parameters) {
        switch (actionClass) {
            case "insert":
                return new PatchActionInsert(series, parameters);
            case "remove":
                return new PatchActionRemove(series, parameters);
            case "convert":
                return new PatchActionConvert(series, parameters);
            default:
                return null;
        }
    }
    
    @Override
    public Component makeComponent(String componentClass, MiniBox parameters) {
        switch (componentClass) {
            case "source_sites":
                return new PatchComponentSitesSource(series, parameters);
            case "pattern_sites":
                return new PatchComponentSitesPatternTri(series, parameters);
            case "graph_sites_simple":
                return new PatchComponentSitesGraphTri.Simple(series, parameters, random);
            case "graph_sites_complex":
                return new PatchComponentSitesGraphTri.Complex(series, parameters, random);
            case "pulse":
                return new PatchComponentPulse(series, parameters);
            case "cycle":
                return new PatchComponentCycle(series, parameters);
            default:
                return null;
        }
    }
}
