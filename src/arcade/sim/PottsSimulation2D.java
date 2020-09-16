package arcade.sim;

import java.util.ArrayList;
import java.util.HashSet;
import ec.util.MersenneTwisterFast;
import arcade.agent.cell.*;
import arcade.env.loc.*;
import arcade.util.MiniBox;
import static arcade.sim.Potts.*;
import static arcade.agent.cell.Cell.*;
import static arcade.env.loc.Location.*;

public class PottsSimulation2D extends PottsSimulation {
	public PottsSimulation2D(long seed, Series series) { super(seed, series); }
	
	/**
	 * Converts volume to voxels per square side.
	 * 
	 * @param volume  the target volume
	 * @return  the voxels per side
	 */
	static int convert(double volume) {
		int sqrt = (int)(Math.sqrt(volume/DS));
		return sqrt + (sqrt%2 == 0 ? 1 : 0);
	}
	
	/**
	 * Increases the number of voxels by adding from a given list of voxels.
	 * 
	 * @param random  the seeded random number generator
	 * @param allVoxels  the list of all possible voxels
	 * @param voxels  the list of selected voxels
	 * @param target  the target number of voxels
	 */
	static void increase(MersenneTwisterFast random, ArrayList<Voxel> allVoxels, ArrayList<Voxel> voxels, int target) {
		int size = voxels.size();
		HashSet<Voxel> neighbors = new HashSet<>();
		
		// Get neighbors.
		for (Voxel voxel : voxels) {
			ArrayList<Voxel> allNeighbors = Location2D.getNeighbors(voxel);
			for (Voxel neighbor : allNeighbors) {
				if (allVoxels.contains(neighbor) && !voxels.contains(neighbor)) { neighbors.add(neighbor); }
			}
		}
		
		// Add in random neighbors until target size is reached.
		ArrayList<Voxel> neighborsShuffled = new ArrayList<>(neighbors);
		Simulation.shuffle(neighborsShuffled, random);
		for (int i = 0; i < target - size; i++) {
			voxels.add(neighborsShuffled.get(i));
		}
	}
	
	/**
	 * Decreases the number of voxels by removing.
	 *
	 * @param random  the seeded random number generator
	 * @param voxels  the list of selected voxels
	 * @param target  the target number of voxels
	 */
	static void decrease(MersenneTwisterFast random, ArrayList<Voxel> voxels, int target) {
		int size = voxels.size();
		ArrayList<Voxel> neighbors = new ArrayList<>();
		
		// Get neighbors.
		for (Voxel voxel : voxels) {
			ArrayList<Voxel> allNeighbors = Location2D.getNeighbors(voxel);
			for (Voxel neighbor : allNeighbors) {
				if (voxels.contains(neighbor)) { continue; }
				neighbors.add(voxel);
				break;
			}
		}
		
		// Remove random neighbors until target size is reached.
		ArrayList<Voxel> neighborsShuffled = new ArrayList<>(neighbors);
		Simulation.shuffle(neighborsShuffled, random);
		for (int i = 0; i < size - target; i++) {
			voxels.remove(neighborsShuffled.get(i));
		}
	}
	
	Potts makePotts() { return new Potts2D(series, agents); }
	
	ArrayList<int[]> makeCenters() {
		ArrayList<int[]> centers = new ArrayList<>();
		int n = 0;
		
		for (String key : series._keys) {
			MiniBox population = series._populations.get(key);
			int pop = population.getInt("pop");
			
			double criticalVolume = series.getParam(pop, "CRITICAL_VOLUME");
			int voxelsPerSide = convert(criticalVolume) + 2;
			
			if (voxelsPerSide > n) { n = voxelsPerSide; }
		}
		
		for (int i = 0; i < (series._length - 2)/n; i++) {
			for (int j = 0; j < (series._width - 2)/n; j++) {
				int cx = i*n + (n + 1)/2;
				int cy = j*n + (n + 1)/2;
				centers.add(new int[] { cx, cy });
			}
		}
		
		return centers;
	}
	
	Location makeLocation(MiniBox population, int[] center) {
		// All voxel options.
		ArrayList<Voxel> allVoxels = new ArrayList<>();
		
		// Get population code and any tags.
		int pop = population.getInt("pop");
		MiniBox tags = population.filter("TAG");
		
		// Parse sizing.
		double criticalVolume = series.getParam(pop, "CRITICAL_VOLUME");
		int target = (int)Math.round(criticalVolume/DS);
		
		// Select all possible voxels.
		int n = convert(criticalVolume) + 2;
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				allVoxels.add(new Voxel(center[0] + i - (n - 1)/2, center[1] + j - (n - 1)/2, 0));
			}
		}
		
		// Select voxels.
		Voxel centerVoxel = new Voxel(center[0], center[1], 0);
		ArrayList<Voxel> voxels = Location2D.getSelected(allVoxels, centerVoxel, target);
		
		// Add or remove voxels to reach target number.
		int size = voxels.size();
		if (size < target) { increase(random, allVoxels, voxels, target); }
		else if (size > target) { decrease(random, voxels, target); }
		
		// Make location.
		Location location;
		
		// Add tags.
		if (tags.getKeys().size() > 0) {
			location = new PottsLocations2D(voxels);
			
			for (String key : tags.getKeys()) {
				if (key.equals("CYTOPLASM")) { continue; }
				
				int tag = (key.equals("NUCLEUS") ? TAG_NUCLEUS : TAG_CYTOPLASM);
				
				// Select tag voxels.
				int tagTarget = (int)Math.round(criticalVolume*tags.getDouble(key)/DS);
				ArrayList<Voxel> tagVoxels = Location2D.getSelected(allVoxels, centerVoxel, tagTarget);
				
				// Add or remove tag voxels to reach target number.
				int tagSize = tagVoxels.size();
				if (tagSize < tagTarget) { increase(random, voxels, tagVoxels, tagTarget); }
				else if (tagSize > tagTarget) { decrease(random, tagVoxels, tagTarget); }
				
				// Assign tags.
				for (Voxel voxel : tagVoxels) { location.assign(tag, voxel); }
			}
		} else { location = new PottsLocation2D(voxels); }
		
		return location;
	}
	
	public Cell makeCell(int id, MiniBox population, int[] center) {
		int pop = population.getInt("pop");
		
		// Get critical values.
		double[] criticals = new double[] {
				series.getParam(pop, "CRITICAL_VOLUME"),
				series.getParam(pop, "CRITICAL_SURFACE")
		};
		
		// Get lambda values.
		double[] lambdas = new double[] {
				series.getParam(pop, "LAMBDA_VOLUME"),
				series.getParam(pop, "LAMBDA_SURFACE")
		};
		
		// Get adhesion values.
		double[] adhesion = new double[series._keys.length + 1];
		adhesion[0] = population.getDouble("adhesion:*");
		for (int i = 0; i < series._keys.length; i++) {
			adhesion[i + 1] = population.getDouble("adhesion:" + series._keys[i]);
		}
		
		// Create location.
		Location location = makeLocation(population, center);
		
		// Get tags if there are any.
		MiniBox tag = population.filter("TAG");
		if (tag.getKeys().size() > 0) {
			int tags = tag.getKeys().size();
			
			double[][] criticalsTag = new double[NUMBER_TERMS][tags];
			double[][] lambdasTag = new double[NUMBER_TERMS][tags];
			double[][] adhesionsTag = new double[tags][tags];
			
			for (int i = 0; i < tags; i++) {
				String key = tag.getKeys().get(i);
				
				// Load ta critical values.
				criticalsTag[TERM_VOLUME][i] = series.getParam(pop, "CRITICAL_VOLUME_" + key);
				criticalsTag[TERM_SURFACE][i] = series.getParam(pop, "CRITICAL_SURFACE_" + key);
				
				// Load tag lambda values.
				lambdasTag[TERM_VOLUME][i] = series.getParam(pop, "LAMBDA_VOLUME_" + key);
				lambdasTag[TERM_SURFACE][i] = series.getParam(pop, "LAMBDA_SURFACE_" + key);
				
				// Load tag adhesion values.
				for (int j = 0; j < tags; j++) {
					adhesionsTag[i][j] = population.getDouble("adhesion:" + key + "-" + tag.getKeys().get(j));
				}
			}
			
			return new PottsCell2D(id, pop, STATE_PROLIFERATIVE, 0, location,
				criticals, lambdas, adhesion, tags,
				criticalsTag, lambdasTag, adhesionsTag);
		} else {
			return new PottsCell2D(id, pop, location, criticals, lambdas, adhesion);
		}
	}
}