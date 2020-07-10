package arcade.agent.module;

import ec.util.MersenneTwisterFast;
import arcade.sim.Simulation;
import arcade.agent.cell.PottsCell;

public class AutosisModule implements Module  {
	final PottsCell cell;
	
	public AutosisModule(PottsCell cell) { this.cell = cell; }
	
	public int getPhase() { return 0; }
	
	public void step(MersenneTwisterFast random, Simulation sim) { }
}
