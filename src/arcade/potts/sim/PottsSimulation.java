package arcade.potts.sim;

import java.util.*;
import sim.engine.*;
import arcade.core.sim.Simulation;
import arcade.core.sim.Series;
import arcade.core.env.grid.Grid;
import arcade.core.env.lat.Lattice;
import arcade.core.env.loc.Location;
import arcade.core.util.MiniBox;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.agent.cell.PottsCellFactory;
import arcade.potts.env.grid.PottsGrid;
import arcade.potts.env.loc.PottsLocationFactory;
import static arcade.core.agent.cell.CellFactory.CellContainer;
import static arcade.core.env.loc.LocationFactory.LocationContainer;

public abstract class PottsSimulation extends SimState implements Simulation {
	/** Stepping order for simulation */
	public enum Ordering {
		/** Stepping order for potts */
		POTTS,
		
		/** Stepping order for cells */
		CELLS
	}
	
	/** {@link arcade.core.sim.Series} object containing this simulation */
	final PottsSeries series;
	
	/** Random number generator seed for this simulation */
	final int seed;
	
	/** {@link arcade.potts.sim.Potts} object for the simulation */
	Potts potts;
	
	/** {@link arcade.core.env.grid.Grid} containing agents in the simulation */
	Grid agents;
	
	/** Cell ID tracker */
	int id;
	
	/**
	 * Simulation instance for a {@link arcade.core.sim.Series} for given random seed.
	 * 
	 * @param seed  the random seed for random number generator
	 * @param series  the simulation series
	 */
	public PottsSimulation(long seed, Series series) {
		super(seed);
		this.series = (PottsSeries)series;
		this.seed = (int)seed - Series.SEED_OFFSET;
	}
	
	public Series getSeries() { return series; }
	public Schedule getSchedule() { return schedule; }
	public int getSeed() { return seed; }
	public int getID() { return ++id; }
	public Potts getPotts() { return potts; }
	public Grid getAgents() { return agents; }
	public Lattice getEnvironment(String key) { return null; }
	
	/**
	 * Called at the start of the simulation to set up agents and environment
	 * and schedule components and helpers as needed.
	 */
	public void start() {
		super.start();
		
		// Reset id.
		id = 0;
		
		// Equip simulation to loader.
		if (series.loader != null) {
			series.loader.equip(this);
		}
		
		setupPotts();
		setupAgents();
		setupEnvironment();
		
		scheduleHelpers();
		scheduleComponents();
		
		// Equip simulation to saver and schedule.
		if (!series.isVis) {
			series.saver.equip(this);
			doOutput(true);
		}
	}
	
	/**
	 * Called at the end of the simulation.
	 */
	public void finish() {
		super.finish();
		
		// Finalize saver.
		if (!series.isVis) {
			doOutput(false);
		}
	}
	
	/**
	 * Creates the {@link arcade.potts.sim.Potts} object for the simulation.
	 * 
	 * @return  a {@link arcade.potts.sim.Potts} object
	 */
	abstract Potts makePotts();
	
	public void setupPotts() {
		potts = makePotts();
		schedule.scheduleRepeating(1, Ordering.POTTS.ordinal(), potts);
	}
	
	/**
	 * Creates a factory for locations.
	 *
	 * @return  a {@link arcade.core.env.loc.Location} factory
	 */
	abstract PottsLocationFactory makeLocationFactory();
	
	/**
	 * Creates a factory for cells.
	 * 
	 * @return  a {@link arcade.core.agent.cell.Cell} factory
	 */
	abstract PottsCellFactory makeCellFactory();
	
	public void setupAgents() {
		// Initialize grid for agents.
		agents = new PottsGrid();
		potts.grid = agents;
		
		// Create factory for locations.
		PottsLocationFactory locationFactory = makeLocationFactory();
		PottsCellFactory cellFactory = makeCellFactory();
		
		// Initialize factories.
		locationFactory.initialize(series, random);
		cellFactory.initialize(series);
		
		// Iterate through each population to create agents.
		for (MiniBox population : series._populations.values()) {
			int pop = population.getInt("CODE");
			HashSet<Integer> ids = cellFactory.popToIDs.get(pop);
			
			for (int i : ids) {
				// Get location and cell containers.
				LocationContainer locationContainer = locationFactory.locations.get(i);
				CellContainer cellContainer = cellFactory.cells.get(i);
				
				// Check that we have enough containers.
				if (locationContainer == null || cellContainer == null) { break; }
				
				// Make the location and cell.
				Location location = locationFactory.make(locationContainer, cellContainer, random);
				PottsCell cell = (PottsCell)cellFactory.make(cellContainer, location);
				
				// Add, initialize, and schedule the cell.
				agents.addObject(i, cell);
				cell.initialize(potts.IDS, potts.REGIONS);
				cell.schedule(schedule);
				
				// Update id tracking.
				id = Math.max(i, id);
			}
		}
	}
	
	public void setupEnvironment() {
		// TODO add environment setup (currently not needed)
	}
	
	public void scheduleHelpers() {
		// TODO add helper scheduling
	}
	
	public void scheduleComponents() {
		// TODO add component scheduling
	}
	
	/**
	 * Runs output methods.
	 * 
	 * @param isScheduled  {@code true} if the output should be scheduled, {@code false} otherwise
	 */
	public void doOutput(boolean isScheduled) {
		if (isScheduled) { series.saver.schedule(schedule, series.getInterval()); }
		else { series.saver.save(schedule.getTime() + 1); }
	}
}