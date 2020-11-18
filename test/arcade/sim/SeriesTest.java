package arcade.sim;

import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import sim.engine.Schedule;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.display.GUIState;
import arcade.util.Box;
import arcade.util.MiniBox;
import static arcade.sim.Series.SEED_OFFSET;
import static arcade.sim.Series.TARGET_SEPARATOR;
import static arcade.util.MiniBox.TAG_SEPARATOR;

public class SeriesTest {
	private static final double EPSILON = 1E-4;
	private static final double DS = (Math.random()*10) + 1;
	private static final double DT = Math.random() + 0.5;
	private static Box PARAMETERS;
	private static final String TEST_NAME = "DEFAULT_NAME";
	private static final String TEST_PATH = "/default/path/";
	private static final int DEFAULT_START_SEED = randomInt();
	private static final int DEFAULT_END_SEED = randomInt();
	private static final int DEFAULT_TICKS = randomInt();
	private static final int DEFAULT_INTERVAL = randomInt();
	private static final int DEFAULT_LENGTH = randomInt();
	private static final int DEFAULT_WIDTH = randomInt();
	private static final int DEFAULT_HEIGHT = randomOdd();
	
	private static final String REGION_ID_1 = randomString().toUpperCase();
	private static final String REGION_ID_2 = randomString().toUpperCase();
	
	private static final String MODULE_ID_1 = randomString().toLowerCase();
	private static final String MODULE_ID_2 = randomString().toLowerCase();
	
	private static final String[] POTTS_PARAMETER_NAMES = new String[] {
			"POTTS_PARAMETER_1",
			"POTTS_PARAMETER_2",
			"POTTS_PARAMETER_3"
	};
	
	private static final double[] POTTS_PARAMETER_VALUES = new double[] {
			randomInt(),
			randomInt(),
			randomInt()
	};
	
	private static final int POTTS_PARAMETER_COUNT = POTTS_PARAMETER_NAMES.length;
	
	private static final String[] POPULATION_PARAMETER_NAMES = new String[] {
			"ADHESION",
			"POPULATION_PARAMETER_1",
			"POPULATION_PARAMETER_2",
			MODULE_ID_1 + TAG_SEPARATOR + "MODULE_PARAMETER_11",
			MODULE_ID_1 + TAG_SEPARATOR + "MODULE_PARAMETER_12",
			MODULE_ID_2 + TAG_SEPARATOR + "MODULE_PARAMETER_21",
			REGION_ID_1 + TAG_SEPARATOR + "POPULATION_PARAMETER_1",
			REGION_ID_1 + TAG_SEPARATOR + "POPULATION_PARAMETER_2",
			REGION_ID_2 + TAG_SEPARATOR + "POPULATION_PARAMETER_1",
			REGION_ID_2 + TAG_SEPARATOR + "POPULATION_PARAMETER_2",
			REGION_ID_1 + TAG_SEPARATOR + "ADHESION" + TARGET_SEPARATOR + REGION_ID_1,
			REGION_ID_1 + TAG_SEPARATOR + "ADHESION" + TARGET_SEPARATOR + REGION_ID_2,
			REGION_ID_2 + TAG_SEPARATOR + "ADHESION" + TARGET_SEPARATOR + REGION_ID_1,
			REGION_ID_2 + TAG_SEPARATOR + "ADHESION" + TARGET_SEPARATOR + REGION_ID_2,
	};
	
	private static final double[] POPULATION_PARAMETER_VALUES = new double[] {
			randomDouble(),
			randomDouble(),
			randomDouble(),
			randomDouble(),
			randomDouble(),
			randomDouble(),
			randomDouble(),
			randomDouble(),
			randomDouble(),
			randomDouble(),
			randomDouble(),
			randomDouble(),
			randomDouble(),
			randomDouble(),
			randomDouble(),
	};
	
	private static final String POPULATION_ID_1 = randomString();
	private static final String POPULATION_ID_2 = randomString();
	private static final String POPULATION_ID_3 = randomString();
	
	private static MiniBox POTTS, POPULATION;
	
	private static final HashMap<String, ArrayList<Box>> setupListsMock = mock(HashMap.class);
	
	static int randomInt() { return (int)(Math.random()*100) + 1; }
	
	static double randomDouble() { return Math.random()*100; }
	
	static int randomEven() { return (int)(Math.random()*100)*2; }
	
	static int randomOdd() { return (int)(Math.random()*100)*2 + 1; }
	
	static String randomString() {
		return new Random().ints(65, 91)
				.limit(5)
				.collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
				.toString();
	}
	
	@BeforeClass
	public static void setupParameters() {
		PARAMETERS = new Box();
		
		// DEFAULTS
		PARAMETERS.addTag("START_SEED", "DEFAULT");
		PARAMETERS.addTag("END_SEED", "DEFAULT");
		PARAMETERS.addTag("TICKS", "DEFAULT");
		PARAMETERS.addTag("INTERVAL", "DEFAULT");
		PARAMETERS.addTag("LENGTH", "DEFAULT");
		PARAMETERS.addTag("WIDTH", "DEFAULT");
		PARAMETERS.addTag("HEIGHT", "DEFAULT");
		PARAMETERS.addTag("DS", "DEFAULT");
		PARAMETERS.addTag("DT", "DEFAULT");
		PARAMETERS.addAtt("START_SEED", "value", "" + DEFAULT_START_SEED);
		PARAMETERS.addAtt("END_SEED", "value", "" + DEFAULT_END_SEED);
		PARAMETERS.addAtt("TICKS", "value", "" + DEFAULT_TICKS);
		PARAMETERS.addAtt("INTERVAL", "value", "" + DEFAULT_INTERVAL);
		PARAMETERS.addAtt("LENGTH", "value", "" + DEFAULT_LENGTH);
		PARAMETERS.addAtt("WIDTH", "value", "" + DEFAULT_WIDTH);
		PARAMETERS.addAtt("HEIGHT", "value", "" + DEFAULT_HEIGHT);
		PARAMETERS.addAtt("DS", "value", "" + DS);
		PARAMETERS.addAtt("DT", "value", "" + DT);
		
		// POTTS
		for (int i = 0; i < POTTS_PARAMETER_COUNT; i++) {
			PARAMETERS.addTag(POTTS_PARAMETER_NAMES[i], "POTTS");
			PARAMETERS.addAtt(POTTS_PARAMETER_NAMES[i], "value", "" + POTTS_PARAMETER_VALUES[i]);
		}
		POTTS = PARAMETERS.getIdValForTag("POTTS");
		
		// POPULATION
		for (int i = 0; i < POPULATION_PARAMETER_NAMES.length; i++) {
			PARAMETERS.addTag(POPULATION_PARAMETER_NAMES[i], "POPULATION");
			PARAMETERS.addAtt(POPULATION_PARAMETER_NAMES[i], "value", "" + POPULATION_PARAMETER_VALUES[i]);
		}
		POPULATION = PARAMETERS.getIdValForTag("POPULATION");
	}
	
	private HashMap<String, MiniBox> makeDicts() {
		HashMap<String, MiniBox> setupDicts = new HashMap<>();
		
		MiniBox set = new MiniBox();
		set.put("path", TEST_PATH);
		setupDicts.put("set", set);
		
		MiniBox series = new MiniBox();
		series.put("name", TEST_NAME);
		setupDicts.put("series", series);
		
		return setupDicts;
	}
	
	private HashMap<String, ArrayList<Box>> makeLists() {
		HashMap<String, ArrayList<Box>> setupLists = new HashMap<>();
		
		ArrayList<Box> potts = new ArrayList<>();
		setupLists.put("potts", potts);
		
		ArrayList<Box> populations = new ArrayList<>();
		setupLists.put("populations", populations);
		
		return setupLists;
	}
	
	@Test
	public void constructor_noPrefix_updatesNames() {
		HashMap<String, MiniBox> setupDicts = makeDicts();
		Series series = new Series(setupDicts, setupListsMock, PARAMETERS, false);
		
		assertEquals(TEST_NAME, series.getName());
		assertEquals(TEST_PATH + TEST_NAME, series.getPrefix());
	}
	
	@Test
	public void constructor_givenPrefix_updatesNames() {
		HashMap<String, MiniBox> setupDicts = makeDicts();
		setupDicts.get("set").put("prefix", "PREFIX_");
		Series series = new Series(setupDicts, setupListsMock, PARAMETERS, false);
		
		assertEquals(TEST_NAME, series.getName());
		assertEquals(TEST_PATH + "PREFIX_" + TEST_NAME, series.getPrefix());
	}
	
	@Test
	public void constructor_seedsNotGiven_usesDefaults() {
		HashMap<String, MiniBox> setupDicts = makeDicts();
		Series series = new Series(setupDicts, setupListsMock, PARAMETERS, false);
		
		assertEquals(DEFAULT_START_SEED, series.getStartSeed());
		assertEquals(DEFAULT_END_SEED, series.getEndSeed());
	}
	
	@Test
	public void constructor_seedGiven_usesGiven() {
		int startSeed = randomInt();
		int endSeed = startSeed + randomInt();
		HashMap<String, MiniBox> setupDicts = makeDicts();
		setupDicts.get("series").put("start", startSeed);
		setupDicts.get("series").put("end", endSeed);
		Series series = new Series(setupDicts, setupListsMock, PARAMETERS, false);
		
		assertEquals(startSeed, series.getStartSeed());
		assertEquals(endSeed, series.getEndSeed());
	}
	
	@Test
	public void constructor_ticksNotGiven_usesDefault() {
		HashMap<String, MiniBox> setupDicts = makeDicts();
		Series series = new Series(setupDicts, setupListsMock, PARAMETERS, false);
		
		assertEquals(DEFAULT_TICKS, series.getTicks());
	}
	
	@Test
	public void constructor_ticksGiven_usesGiven() {
		int ticks = randomInt();
		HashMap<String, MiniBox> setupDicts = makeDicts();
		setupDicts.get("series").put("ticks", ticks);
		Series series = new Series(setupDicts, setupListsMock, PARAMETERS, false);
		
		assertEquals(ticks, series.getTicks());
	}
	
	@Test
	public void constructor_intervalNotGiven_usesDefault() {
		HashMap<String, MiniBox> setupDicts = makeDicts();
		Series series = new Series(setupDicts, setupListsMock, PARAMETERS, false);
		
		assertEquals(DEFAULT_INTERVAL, series.getInterval());
	}
	
	@Test
	public void constructor_intervalGiven_usesGiven() {
		int interval = randomInt();
		HashMap<String, MiniBox> setupDicts = makeDicts();
		setupDicts.get("series").put("interval", interval);
		Series series = new Series(setupDicts, setupListsMock, PARAMETERS, false);
		
		assertEquals(interval, series.getInterval());
	}
	
	@Test
	public void constructor_withoutVis_updatesField() {
		HashMap<String, MiniBox> setupDicts = makeDicts();
		Series series = new Series(setupDicts, setupListsMock, PARAMETERS, false);
		assertFalse(series.isVis);
	}
	
	@Test
	public void constructor_withVis_updatesField() {
		HashMap<String, MiniBox> setupDicts = makeDicts();
		Series series = new Series(setupDicts, setupListsMock, PARAMETERS, true);
		assertTrue(series.isVis);
	}
	
	@Test
	public void constructor_sizesNotGiven_usesDefaults() {
		HashMap<String, MiniBox> setupDicts = makeDicts();
		Series series = spy(new Series(setupDicts, setupListsMock, PARAMETERS, false));
		
		assertEquals(DEFAULT_LENGTH, series._length);
		assertEquals(DEFAULT_WIDTH, series._width);
		assertEquals(DEFAULT_HEIGHT, series._height);
	}
	
	@Test
	public void constructor_oneSizeGivenOdd_updatesSizes() {
		int length = randomOdd();
		int width = randomOdd();
		int height = randomOdd();
		
		HashMap<String, MiniBox> setupDicts;
		Series series;
		
		setupDicts = makeDicts();
		setupDicts.get("series").put("length", length);
		series = new Series(setupDicts, setupListsMock, PARAMETERS, false);
		assertEquals(length, series._length);
		assertEquals(DEFAULT_WIDTH, series._width);
		assertEquals(DEFAULT_HEIGHT, series._height);
		
		setupDicts = makeDicts();
		setupDicts.get("series").put("width", width);
		series = new Series(setupDicts, setupListsMock, PARAMETERS, false);
		assertEquals(DEFAULT_LENGTH, series._length);
		assertEquals(width, series._width);
		assertEquals(DEFAULT_HEIGHT, series._height);
		
		setupDicts = makeDicts();
		setupDicts.get("series").put("height", height);
		series = new Series(setupDicts, setupListsMock, PARAMETERS, false);
		assertEquals(DEFAULT_LENGTH, series._length);
		assertEquals(DEFAULT_WIDTH, series._width);
		assertEquals(height, series._height);
	}
	
	@Test
	public void constructor_twoSizesGivenOdd_updatesSizes() {
		int length = randomOdd();
		int width = randomOdd();
		int height = randomOdd();
		
		HashMap<String, MiniBox> setupDicts;
		Series series;
		
		setupDicts = makeDicts();
		setupDicts.get("series").put("length", length);
		setupDicts.get("series").put("width", width);
		series = new Series(setupDicts, setupListsMock, PARAMETERS, false);
		assertEquals(length, series._length);
		assertEquals(width, series._width);
		assertEquals(DEFAULT_HEIGHT, series._height);
		
		setupDicts = makeDicts();
		setupDicts.get("series").put("width", width);
		setupDicts.get("series").put("height", height);
		series = new Series(setupDicts, setupListsMock, PARAMETERS, false);
		assertEquals(DEFAULT_LENGTH, series._length);
		assertEquals(width, series._width);
		assertEquals(height, series._height);
		
		setupDicts = makeDicts();
		setupDicts.get("series").put("length", length);
		setupDicts.get("series").put("height", height);
		series = new Series(setupDicts, setupListsMock, PARAMETERS, false);
		assertEquals(length, series._length);
		assertEquals(DEFAULT_WIDTH, series._width);
		assertEquals(height, series._height);
	}
	
	@Test
	public void constructor_allSizesGivenOdd_updatesSizes() {
		int length = randomOdd();
		int width = randomOdd();
		int height = randomOdd();
		
		HashMap<String, MiniBox> setupDicts = makeDicts();
		setupDicts.get("series").put("length", length);
		setupDicts.get("series").put("width", width);
		setupDicts.get("series").put("height", height);
		Series series = new Series(setupDicts, setupListsMock, PARAMETERS, false);
		
		assertEquals(length, series._length);
		assertEquals(width, series._width);
		assertEquals(height, series._height);
	}
	
	@Test
	public void constructor_oneSizeGivenEven_updatesSizes() {
		int length = randomEven();
		int width = randomEven();
		int height = randomEven();
		
		HashMap<String, MiniBox> setupDicts;
		Series series;
		
		setupDicts = makeDicts();
		setupDicts.get("series").put("length", length);
		series = new Series(setupDicts, setupListsMock, PARAMETERS, false);
		assertEquals(length, series._length);
		assertEquals(DEFAULT_WIDTH, series._width);
		assertEquals(DEFAULT_HEIGHT, series._height);
		
		setupDicts = makeDicts();
		setupDicts.get("series").put("width", width);
		series = new Series(setupDicts, setupListsMock, PARAMETERS, false);
		assertEquals(DEFAULT_LENGTH, series._length);
		assertEquals(width, series._width);
		assertEquals(DEFAULT_HEIGHT, series._height);
		
		setupDicts = makeDicts();
		setupDicts.get("series").put("height", height);
		series = new Series(setupDicts, setupListsMock, PARAMETERS, false);
		assertEquals(DEFAULT_LENGTH, series._length);
		assertEquals(DEFAULT_WIDTH, series._width);
		assertEquals(height + 1, series._height);
	}
	
	@Test
	public void constructor_twoSizesGivenEven_updatesSizes() {
		int length = randomEven();
		int width = randomEven();
		int height = randomEven();
		
		HashMap<String, MiniBox> setupDicts;
		Series series;
		
		setupDicts = makeDicts();
		setupDicts.get("series").put("length", length);
		setupDicts.get("series").put("width", width);
		series = new Series(setupDicts, setupListsMock, PARAMETERS, false);
		assertEquals(length, series._length);
		assertEquals(width, series._width);
		assertEquals(DEFAULT_HEIGHT, series._height);
		
		setupDicts = makeDicts();
		setupDicts.get("series").put("width", width);
		setupDicts.get("series").put("height", height);
		series = new Series(setupDicts, setupListsMock, PARAMETERS, false);
		assertEquals(DEFAULT_LENGTH, series._length);
		assertEquals(width, series._width);
		assertEquals(height + 1, series._height);
		
		setupDicts = makeDicts();
		setupDicts.get("series").put("length", length);
		setupDicts.get("series").put("height", height);
		series = new Series(setupDicts, setupListsMock, PARAMETERS, false);
		assertEquals(length, series._length);
		assertEquals(DEFAULT_WIDTH, series._width);
		assertEquals(height + 1, series._height);
	}
	
	@Test
	public void constructor_allSizesGivenEven_updatesSizes() {
		int length = randomEven();
		int width = randomEven();
		int height = randomEven();
		
		HashMap<String, MiniBox> setupDicts = makeDicts();
		setupDicts.get("series").put("length", length);
		setupDicts.get("series").put("width", width);
		setupDicts.get("series").put("height", height);
		Series series = new Series(setupDicts, setupListsMock, PARAMETERS, false);
		
		assertEquals(length, series._length);
		assertEquals(width, series._width);
		assertEquals(height + 1, series._height);
	}
	
	@Test
	public void constructor_dsNotGiven_usesDefault() {
		HashMap<String, MiniBox> setupDicts = makeDicts();
		Series series = new Series(setupDicts, setupListsMock, PARAMETERS, false);
		
		assertEquals(DS, series.DS, EPSILON);
	}
	
	@Test
	public void constructor_dsGiven_usesGiven() {
		double ds = randomDouble();
		HashMap<String, MiniBox> setupDicts = makeDicts();
		setupDicts.get("series").put("ds", ds);
		Series series = new Series(setupDicts, setupListsMock, PARAMETERS, false);
		
		assertEquals(ds, series.DS, EPSILON);
	}
	
	@Test
	public void constructor_dtNotGiven_usesDefault() {
		HashMap<String, MiniBox> setupDicts = makeDicts();
		Series series = new Series(setupDicts, setupListsMock, PARAMETERS, false);
		
		assertEquals(DT, series.DT, EPSILON);
	}
	
	@Test
	public void constructor_dtGiven_usesGiven() {
		double dt = randomDouble();
		HashMap<String, MiniBox> setupDicts = makeDicts();
		setupDicts.get("series").put("dt", dt);
		Series series = new Series(setupDicts, setupListsMock, PARAMETERS, false);
		
		assertEquals(dt, series.DT, EPSILON);
	}
	
	@Test
	public void initialize_default_callsMethods() {
		HashMap<String, ArrayList<Box>> setupLists = makeLists();
		Series series = mock(Series.class, CALLS_REAL_METHODS);
		series.initialize(setupLists, PARAMETERS);
		
		ArrayList<Box> potts = setupLists.get("potts");
		verify(series).updatePotts(eq(potts), any(MiniBox.class));
		
		ArrayList<Box> populations = setupLists.get("populations");
		verify(series).updatePopulations(eq(populations), any(MiniBox.class), any(MiniBox.class));
		
		ArrayList<Box> molecules = setupLists.get("molecules");
		verify(series).updateMolecules(eq(molecules), any(MiniBox.class));
		
		ArrayList<Box> helpers = setupLists.get("helpers");
		verify(series).updateHelpers(eq(helpers), any(MiniBox.class));
		
		ArrayList<Box> components = setupLists.get("components");
		verify(series).updateComponents(eq(components), any(MiniBox.class));
		
		ArrayList<Box> profilers = setupLists.get("profilers");
		verify(series).updateProfilers(eq(profilers), any(MiniBox.class));
		
		ArrayList<Box> checkpoints = setupLists.get("checkpoints");
		verify(series).updateCheckpoints(eq(checkpoints), any(MiniBox.class));
	}
	
	@Test
	public void isValidNumber_noValue_returnsFalse() {
		Box box = new Box();
		assertFalse(Series.isValidNumber(box, "number"));
	}
	
	@Test
	public void isValidNumber_invalidValues_returnsFalse() {
		String[] values = new String[] { "-1", "1.", "-1.", "1.5", "-1.5", "1E-2" };
		for (String value : values) {
			Box box = new Box();
			box.add("number", value);
			assertFalse(Series.isValidNumber(box, "number"));
		}
	}
	
	@Test
	public void isValidNumber_validValues_returnsTrue() {
		String[] values = new String[] { "0", "1", "1E2" };
		for (String value : values) {
			Box box = new Box();
			box.add("number", value);
			assertTrue(Series.isValidNumber(box, "number"));
		}
	}
	
	@Test
	public void isValidFraction_noValue_returnsFalse() {
		Box box = new Box();
		assertFalse(Series.isValidFraction(box, "number"));
	}
	
	@Test
	public void isValidFraction_invalidValues_returnsFalse() {
		String[] values = new String[] { "-0", "-0.0", "-1", "-1.0", "2.0" };
		for (String value : values) {
			Box box = new Box();
			box.add("number", value);
			assertFalse(Series.isValidFraction(box, "number"));
		}
	}
	
	@Test
	public void isValidFraction_validValues_returnsTrue() {
		String[] values = new String[] { "0", "0.0", "1", "1.0", "0.5" };
		for (String value : values) {
			Box box = new Box();
			box.add("number", value);
			assertTrue(Series.isValidFraction(box, "number"));
		}
	}
	
	@Test
	public void parseParameter_noValueNoScaling_usesDefault() {
		MiniBox box = new MiniBox();
		String parameter = randomString();
		double defaultParameter = randomDouble();
		MiniBox values = new MiniBox();
		MiniBox scales = new MiniBox();
		Series.parseParameter(box, parameter, "" + defaultParameter, values, scales);
		assertEquals(defaultParameter, box.getDouble(parameter), EPSILON);
	}
	
	@Test
	public void parseParameter_withValueOnly_usesDefault() {
		MiniBox box = new MiniBox();
		String parameter = randomString();
		double defaultParameter = randomDouble();
		MiniBox values = new MiniBox();
		MiniBox scales = new MiniBox();
		
		double parameterValue = randomDouble();
		values.put(parameter, parameterValue);
		
		Series.parseParameter(box, parameter, "" + defaultParameter, values, scales);
		assertEquals(parameterValue, box.getDouble(parameter), EPSILON);
	}
	
	@Test
	public void parseParameter_withScaleOnly_usesDefault() {
		MiniBox box = new MiniBox();
		String parameter = randomString();
		double defaultParameter = randomDouble();
		MiniBox values = new MiniBox();
		MiniBox scales = new MiniBox();
		
		double parameterScale = randomDouble();
		scales.put(parameter, parameterScale);
		
		Series.parseParameter(box, parameter, "" + defaultParameter, values, scales);
		assertEquals(defaultParameter*parameterScale, box.getDouble(parameter), EPSILON);
	}
	
	@Test
	public void parseParameter_valueAndScale_usesDefault() {
		MiniBox box = new MiniBox();
		String parameter = randomString();
		double defaultParameter = randomDouble();
		MiniBox values = new MiniBox();
		MiniBox scales = new MiniBox();
		
		double parameterValue = randomDouble();
		values.put(parameter, parameterValue);
		
		double parameterScale = randomDouble();
		scales.put(parameter, parameterScale);
		
		Series.parseParameter(box, parameter, "" + defaultParameter, values, scales);
		assertEquals(parameterValue*parameterScale, box.getDouble(parameter), EPSILON);
	}
	
	@Test
	public void parseConversion_invalidConversion_returnsOne() {
		assertEquals(1, Series.parseConversion(randomString(), DS, DT), EPSILON);
	}
	
	@Test
	public void parseConversion_noExponent_returnsValue() {
		assertEquals(DS, Series.parseConversion("DS", DS, DT), EPSILON);
		assertEquals(DT, Series.parseConversion("DT", DS, DT), EPSILON);
	}
	
	@Test
	public void parseConversion_positiveExponent_returnsValue() {
		int n = randomInt();
		assertEquals(Math.pow(DS, n), Series.parseConversion("DS^" + n, DS, DT), EPSILON);
		assertEquals(Math.pow(DT, n), Series.parseConversion("DT^" + n, DS, DT), EPSILON);
	}
	
	@Test
	public void parseConversion_negativeExponent_returnsValue() {
		int n = randomInt();
		assertEquals(Math.pow(DS, -n), Series.parseConversion("DS^-" + n, DS, DT), EPSILON);
		assertEquals(Math.pow(DT, -n), Series.parseConversion("DT^-" + n, DS, DT), EPSILON);
	}
	
	private Series makeSeriesForPotts(Box box) {
		HashMap<String, ArrayList<Box>> setupLists = makeLists();
		Series series = mock(Series.class, CALLS_REAL_METHODS);
		ArrayList<Box> potts = setupLists.get("potts");
		potts.add(box);
		series.updatePotts(potts, POTTS);
		return series;
	}
	
	@Test
	public void updatePotts_noSetting_createsBox() {
		Series series = mock(Series.class, CALLS_REAL_METHODS);
		series.updatePotts(null, POTTS);
		assertNotNull(series._potts);
	}
	
	@Test
	public void updatePotts_noParameters_usesDefaults() {
		Series series = makeSeriesForPotts(null);
		MiniBox box = series._potts;
		
		for (String parameter : POTTS_PARAMETER_NAMES) {
			assertEquals(POTTS.get(parameter), box.get(parameter));
		}
	}
	
	@Test
	public void updatePotts_givenParameters_updatesValues() {
		for (String pottsParameter1 : POTTS_PARAMETER_NAMES) {
			for (String pottsParameter2 : POTTS_PARAMETER_NAMES) {
				Box potts = new Box();
				
				double value = randomDouble();
				double scale = randomDouble();
				potts.addAtt(pottsParameter1, "value", "" + value);
				potts.addTag(pottsParameter1, "PARAMETER");
				potts.addAtt(pottsParameter2, "scale", "" + scale);
				potts.addTag(pottsParameter2, "PARAMETER");
				
				Series series = makeSeriesForPotts(potts);
				MiniBox box = series._potts;
				
				for (String parameter : POTTS_PARAMETER_NAMES) {
					double expected = POTTS.getDouble(parameter);
					if (parameter.equals(pottsParameter1)) { expected = value; }
					if (parameter.equals(pottsParameter2)) { expected *= scale; }
					assertEquals(expected, box.getDouble(parameter), EPSILON);
				}
			}
		}
	}
	
	private Series makeSeriesForPopulation(Box[] boxes) {
		return makeSeriesForPopulation(boxes, new MiniBox());
	}
	
	private Series makeSeriesForPopulation(Box[] boxes, MiniBox conversion) {
		HashMap<String, ArrayList<Box>> setupLists = makeLists();
		Series series = mock(Series.class, CALLS_REAL_METHODS);
		ArrayList<Box> populations = setupLists.get("populations");
		populations.addAll(Arrays.asList(boxes));
		
		try {
			Field dsField = Series.class.getDeclaredField("DS");
			dsField.setAccessible(true);
			dsField.setDouble(series, DS);
			
			Field dtField = Series.class.getDeclaredField("DT");
			dtField.setAccessible(true);
			dtField.setDouble(series, DT);
		} catch (Exception ignored) { }
		
		series.updatePopulations(populations, POPULATION, conversion);
		return series;
	}
	
	@Test
	public void updatePopulation_noPopulations_createsMap() {
		Series series = mock(Series.class, CALLS_REAL_METHODS);
		series.updatePopulations(null, POPULATION, new MiniBox());
		assertEquals(0, series._populations.size());
	}
	
	@Test
	public void updatePopulation_onePopulation_createsMap() {
		Box[] boxes = new Box[] { new Box() };
		boxes[0].add("id", POPULATION_ID_1);
		Series series = makeSeriesForPopulation(boxes);
		
		assertEquals(1, series._populations.size());
		assertNotNull(series._populations.get(POPULATION_ID_1));
		assertEquals(1, series._populations.get(POPULATION_ID_1).getInt("CODE"));
	}
	
	@Test
	public void updatePopulation_multiplePopulations_createsMap() {
		Box[] boxes = new Box[] { new Box(), new Box(), new Box() };
		boxes[0].add("id", POPULATION_ID_1);
		boxes[1].add("id", POPULATION_ID_2);
		boxes[2].add("id", POPULATION_ID_3);
		Series series = makeSeriesForPopulation(boxes);
		
		assertEquals(3, series._populations.size());
		assertNotNull(series._populations.get(POPULATION_ID_1));
		assertNotNull(series._populations.get(POPULATION_ID_2));
		assertNotNull(series._populations.get(POPULATION_ID_3));
		assertEquals(1, series._populations.get(POPULATION_ID_1).getInt("CODE"));
		assertEquals(2, series._populations.get(POPULATION_ID_2).getInt("CODE"));
		assertEquals(3, series._populations.get(POPULATION_ID_3).getInt("CODE"));
	}
	
	@Test
	public void updatePopulation_noInit_setsZero() {
		Box[] boxes = new Box[] { new Box() };
		boxes[0].add("id", POPULATION_ID_1);
		Series series = makeSeriesForPopulation(boxes);
		
		MiniBox box = series._populations.get(POPULATION_ID_1);
		assertEquals(0, box.getDouble("INIT"), EPSILON);
	}
	
	@Test
	public void updatePopulation_givenValidInit_setsValue() {
		String[] fractions = new String[] { "0", "10", "1E2" };
		int[] values = new int[] { 0, 10, 100 };
		
		for (int i = 0; i < fractions.length; i++) {
			Box[] boxes = new Box[] { new Box() };
			boxes[0].add("id", POPULATION_ID_1);
			boxes[0].add("init", fractions[i]);
			Series series = makeSeriesForPopulation(boxes);
			
			MiniBox box = series._populations.get(POPULATION_ID_1);
			assertEquals(values[i], box.getInt("INIT"));
		}
	}
	
	@Test
	public void updatePopulation_givenInvalidInit_setsZero() {
		String[] fractions = new String[] { "1.1", "-1" };
		
		for (String fraction : fractions) {
			Box[] boxes = new Box[]{new Box()};
			boxes[0].add("id", POPULATION_ID_1);
			boxes[0].add("init", fraction);
			Series series = makeSeriesForPopulation(boxes);
			
			MiniBox box = series._populations.get(POPULATION_ID_1);
			assertEquals(0, box.getDouble("INIT"), EPSILON);
		}
	}
	
	@Test
	public void updatePopulation_withRegionsValidFraction_setsTags() {
		String[] fractions = new String[] { "0", "0.", "0.0", ".0", "0.5", "0.67", "1", "1.", "1.0" };
		double[] values = new double[] { 0, 0, 0, 0, 0.5, 0.67, 1, 1, 1 };
		
		for (int i = 0; i < fractions.length; i++) {
			Box[] boxes = new Box[] { new Box() };
			boxes[0].add("id", POPULATION_ID_1);
			boxes[0].addTag(REGION_ID_1, "REGION");
			boxes[0].addAtt(REGION_ID_1,"fraction", fractions[i]);
			Series series = makeSeriesForPopulation(boxes);
			
			MiniBox box = series._populations.get(POPULATION_ID_1);
			assertEquals(values[i], box.getDouble("(REGION)" + TAG_SEPARATOR + REGION_ID_1), EPSILON);
			assertFalse(box.contains("(REGION)" + TAG_SEPARATOR + REGION_ID_2));
		}
	}
	
	@Test
	public void updatePopulation_withRegionsInvalidFraction_setsTags() {
		String[] fractions = new String[] { "1.1", "2", "a", "-0.5" };
		
		for (String fraction : fractions) {
			Box[] boxes = new Box[]{new Box()};
			boxes[0].add("id", POPULATION_ID_1);
			boxes[0].addTag(REGION_ID_1, "REGION");
			boxes[0].addAtt(REGION_ID_1, "fraction", fraction);
			Series series = makeSeriesForPopulation(boxes);
			
			MiniBox box = series._populations.get(POPULATION_ID_1);
			assertEquals(0, box.getDouble("(REGION)" + TAG_SEPARATOR + REGION_ID_1), EPSILON);
			assertFalse(box.contains("(REGION)" + TAG_SEPARATOR + REGION_ID_2));
		}
	}
	
	@Test
	public void updatePopulation_noParametersOnePop_usesDefaults() {
		Box[] boxes = new Box[] { new Box() };
		boxes[0].add("id", POPULATION_ID_1);
		Series series = makeSeriesForPopulation(boxes);
		MiniBox box = series._populations.get(POPULATION_ID_1);
		
		for (String parameter : POPULATION_PARAMETER_NAMES) {
			assertEquals(POPULATION.get(parameter), box.get(parameter));
		}
	}
	
	@Test
	public void updatePopulation_givenParametersOnePop_updatesValues() {
		for (String populationParameter1 : POPULATION_PARAMETER_NAMES) {
			for (String populationParameter2 : POPULATION_PARAMETER_NAMES) {
				Box[] boxes = new Box[] { new Box() };
				boxes[0].add("id", POPULATION_ID_1);
				
				double value = randomDouble();
				double scale = randomDouble();
				boxes[0].addAtt(populationParameter1, "value", "" + value);
				boxes[0].addTag(populationParameter1, "PARAMETER");
				boxes[0].addAtt(populationParameter2, "scale", "" + scale);
				boxes[0].addTag(populationParameter2, "PARAMETER");
				
				Series series = makeSeriesForPopulation(boxes);
				MiniBox box = series._populations.get(POPULATION_ID_1);
				
				for (String parameter : POPULATION_PARAMETER_NAMES) {
					double expected = POPULATION.getDouble(parameter);
					if (parameter.equals(populationParameter1)) { expected = value; }
					if (parameter.equals(populationParameter2)) { expected *= scale; }
					assertEquals(expected, box.getDouble(parameter), EPSILON);
				}
			}
		}
	}
	
	@Test
	public void updatePopulation_noParametersMultiplePops_usesDefaults() {
		Box[] boxes = new Box[] { new Box(), new Box(), new Box() };
		boxes[0].add("id", POPULATION_ID_1);
		boxes[1].add("id", POPULATION_ID_2);
		boxes[2].add("id", POPULATION_ID_3);
		Series series = makeSeriesForPopulation(boxes);
		MiniBox box1 = series._populations.get(POPULATION_ID_1);
		MiniBox box2 = series._populations.get(POPULATION_ID_2);
		MiniBox box3 = series._populations.get(POPULATION_ID_3);
		
		for (String parameter : POPULATION_PARAMETER_NAMES) {
			assertEquals(POPULATION.get(parameter), box1.get(parameter));
			assertEquals(POPULATION.get(parameter), box2.get(parameter));
			assertEquals(POPULATION.get(parameter), box3.get(parameter));
		}
	}
	
	@Test
	public void updatePopulation_givenParametersMultiplePops_updatesValues() {
		for (String populationParameter1 : POPULATION_PARAMETER_NAMES) {
			for (String populationParameter2 : POPULATION_PARAMETER_NAMES) {
				Box[] boxes = new Box[] { new Box(), new Box(), new Box() };
				boxes[0].add("id", POPULATION_ID_1);
				boxes[1].add("id", POPULATION_ID_2);
				boxes[2].add("id", POPULATION_ID_3);
				
				double value = randomDouble();
				double scale = randomDouble();
				boxes[1].addAtt(populationParameter1, "value", "" + value);
				boxes[1].addTag(populationParameter1, "PARAMETER");
				boxes[1].addAtt(populationParameter2, "scale", "" + scale);
				boxes[1].addTag(populationParameter2, "PARAMETER");
				
				Series series = makeSeriesForPopulation(boxes);
				MiniBox box1 = series._populations.get(POPULATION_ID_1);
				MiniBox box2 = series._populations.get(POPULATION_ID_2);
				MiniBox box3 = series._populations.get(POPULATION_ID_3);
				
				for (String parameter : POPULATION_PARAMETER_NAMES) {
					assertEquals(POPULATION.get(parameter), box1.get(parameter));
					assertEquals(POPULATION.get(parameter), box3.get(parameter));
					
					double expected = POPULATION.getDouble(parameter);
					if (parameter.equals(populationParameter1)) { expected = value; }
					if (parameter.equals(populationParameter2)) { expected *= scale; }
					assertEquals(expected, box2.getDouble(parameter), EPSILON);
				}
			}
		}
	}
	
	@Test
	public void updatePopulation_noAdhesionOnePop_usesDefaults() {
		Box[] boxes = new Box[] { new Box() };
		boxes[0].add("id", POPULATION_ID_1);
		
		String adhesion = "" + randomDouble();
		boxes[0].addAtt("ADHESION", "value", adhesion);
		boxes[0].addTag("ADHESION", "PARAMETER");
		
		Series series = makeSeriesForPopulation(boxes);
		MiniBox box = series._populations.get(POPULATION_ID_1);
		
		assertEquals(adhesion, box.get("ADHESION:*"));
		assertEquals(adhesion, box.get("ADHESION" + TARGET_SEPARATOR + POPULATION_ID_1));
	}
	
	@Test
	public void updatePopulation_givenAdhesionOnePop_updatesValues() {
		String[] pops = new String[] { "*", POPULATION_ID_1 };
		for (String modifiedPop1 : pops) {
			for (String modifiedPop2 : pops) {
				Box[] boxes = new Box[] { new Box() };
				boxes[0].add("id", POPULATION_ID_1);
				
				double adhesion = randomDouble();
				boxes[0].addAtt("ADHESION", "value", "" + adhesion);
				boxes[0].addTag("ADHESION", "PARAMETER");
				
				double value = randomDouble();
				double scale = randomDouble();
				boxes[0].addAtt("ADHESION" + TARGET_SEPARATOR + modifiedPop1, "value", "" + value);
				boxes[0].addTag("ADHESION" + TARGET_SEPARATOR + modifiedPop1, "PARAMETER");
				boxes[0].addAtt("ADHESION" + TARGET_SEPARATOR + modifiedPop2, "scale", "" + scale);
				boxes[0].addTag("ADHESION" + TARGET_SEPARATOR + modifiedPop2, "PARAMETER");
				
				Series series = makeSeriesForPopulation(boxes);
				MiniBox box = series._populations.get(POPULATION_ID_1);
				
				for (String pop : pops) {
					double expected = adhesion;
					if (pop.equals(modifiedPop1)) { expected = value; }
					if (pop.equals(modifiedPop2)) { expected *= scale; }
					assertEquals(expected, box.getDouble("ADHESION" + TARGET_SEPARATOR + pop), EPSILON);
				}
			}
		}
	}
	
	@Test
	public void updatePopulation_noAdhesionMultiplePops_usesDefaults() {
		Box[] boxes = new Box[] { new Box(), new Box(), new Box() };
		boxes[0].add("id", POPULATION_ID_1);
		boxes[1].add("id", POPULATION_ID_2);
		boxes[2].add("id", POPULATION_ID_3);
		
		String adhesion = "" + randomDouble();
		boxes[1].addAtt("ADHESION", "value", adhesion);
		boxes[1].addTag("ADHESION", "PARAMETER");
		
		Series series = makeSeriesForPopulation(boxes);
		MiniBox box1 = series._populations.get(POPULATION_ID_1);
		MiniBox box2 = series._populations.get(POPULATION_ID_2);
		MiniBox box3 = series._populations.get(POPULATION_ID_3);
		
		assertEquals(POPULATION.get("ADHESION"), box1.get("ADHESION:*"));
		assertEquals(POPULATION.get("ADHESION"), box1.get("ADHESION" + TARGET_SEPARATOR + POPULATION_ID_1));
		assertEquals(POPULATION.get("ADHESION"), box1.get("ADHESION" + TARGET_SEPARATOR + POPULATION_ID_2));
		assertEquals(POPULATION.get("ADHESION"), box1.get("ADHESION" + TARGET_SEPARATOR + POPULATION_ID_3));
		assertEquals(adhesion, box2.get("ADHESION:*"));
		assertEquals(adhesion, box2.get("ADHESION" + TARGET_SEPARATOR + POPULATION_ID_1));
		assertEquals(adhesion, box2.get("ADHESION" + TARGET_SEPARATOR + POPULATION_ID_2));
		assertEquals(adhesion, box2.get("ADHESION" + TARGET_SEPARATOR + POPULATION_ID_3));
		assertEquals(POPULATION.get("ADHESION"), box3.get("ADHESION:*"));
		assertEquals(POPULATION.get("ADHESION"), box3.get("ADHESION" + TARGET_SEPARATOR + POPULATION_ID_1));
		assertEquals(POPULATION.get("ADHESION"), box3.get("ADHESION" + TARGET_SEPARATOR + POPULATION_ID_2));
		assertEquals(POPULATION.get("ADHESION"), box3.get("ADHESION" + TARGET_SEPARATOR + POPULATION_ID_3));
	}
	
	@Test
	public void updatePopulation_givenAdhesionMultiplePops_updatesValues() {
		String[] pops = new String[] { "*", POPULATION_ID_1, POPULATION_ID_2 };
		for (String modifiedPop1 : pops) {
			for (String modifiedPop2 : pops) {
				Box[] boxes = new Box[] { new Box(), new Box() };
				boxes[0].add("id", POPULATION_ID_1);
				boxes[1].add("id", POPULATION_ID_2);
				
				double adhesion = randomDouble();
				boxes[1].addAtt("ADHESION", "value", "" + adhesion);
				boxes[1].addTag("ADHESION", "PARAMETER");
				
				double value = randomDouble();
				double scale = randomDouble();
				boxes[1].addAtt("ADHESION" + TARGET_SEPARATOR + modifiedPop1, "value", "" + value);
				boxes[1].addTag("ADHESION" + TARGET_SEPARATOR + modifiedPop1, "PARAMETER");
				boxes[1].addAtt("ADHESION" + TARGET_SEPARATOR + modifiedPop2, "scale", "" + scale);
				boxes[1].addTag("ADHESION" + TARGET_SEPARATOR + modifiedPop2, "PARAMETER");
				
				Series series = makeSeriesForPopulation(boxes);
				MiniBox box1 = series._populations.get(POPULATION_ID_1);
				MiniBox box2 = series._populations.get(POPULATION_ID_2);
				
				for (String pop : pops) {
					assertEquals(POPULATION.get("ADHESION"), box1.get("ADHESION" + TARGET_SEPARATOR + pop));
					
					double expected = adhesion;
					if (pop.equals(modifiedPop1)) { expected = value; }
					if (pop.equals(modifiedPop2)) { expected *= scale; }
					assertEquals(expected, box2.getDouble("ADHESION" + TARGET_SEPARATOR + pop), EPSILON);
				}
			}
		}
	}
	
	@Test
	public void updatePopulation_noAdhesionWithRegions_usesDefaults() {
		String[] regions = new String[] { REGION_ID_1, REGION_ID_2 };
		Box[] boxes = new Box[] { new Box() };
		boxes[0].add("id", POPULATION_ID_1);
		boxes[0].addTag(REGION_ID_1, "REGION");
		boxes[0].addTag(REGION_ID_2, "REGION");
		boxes[0].addAtt(REGION_ID_1,"fraction", "0");
		boxes[0].addAtt(REGION_ID_2,"fraction", "0");
		
		Series series = makeSeriesForPopulation(boxes);
		MiniBox box = series._populations.get(POPULATION_ID_1);
		
		for (String region : regions) {
			for (String target : regions) {
				String name = region + TAG_SEPARATOR + "ADHESION" + TARGET_SEPARATOR + target;
				assertEquals(POPULATION.get(name), box.get(name));
			}
		}
	}
	
	@Test
	public void updatePopulation_givenAdhesionWithRegions_updateValues() {
		String[] regions = new String[] { REGION_ID_1, REGION_ID_2 };
		for (String modifiedRegion1 : regions) {
			for (String modifiedRegion2 : regions) {
				Box[] boxes = new Box[] { new Box() };
				boxes[0].add("id", POPULATION_ID_1);
				boxes[0].addTag(REGION_ID_1, "REGION");
				boxes[0].addTag(REGION_ID_2, "REGION");
				boxes[0].addAtt(REGION_ID_1,"fraction", "0");
				boxes[0].addAtt(REGION_ID_2,"fraction", "0");
				
				double value = randomDouble();
				double scale = randomDouble();
				boxes[0].addAtt(REGION_ID_2 + TAG_SEPARATOR + "ADHESION" + TARGET_SEPARATOR + modifiedRegion1, "value", "" + value);
				boxes[0].addTag(REGION_ID_2 + TAG_SEPARATOR + "ADHESION" + TARGET_SEPARATOR + modifiedRegion1, "PARAMETER");
				boxes[0].addAtt(REGION_ID_2 + TAG_SEPARATOR + "ADHESION" + TARGET_SEPARATOR + modifiedRegion2, "scale", "" + scale);
				boxes[0].addTag(REGION_ID_2 + TAG_SEPARATOR + "ADHESION" + TARGET_SEPARATOR + modifiedRegion2, "PARAMETER");
				
				Series series = makeSeriesForPopulation(boxes);
				MiniBox box = series._populations.get(POPULATION_ID_1);
				
				for (String target : regions) {
					String name1 = REGION_ID_1 + TAG_SEPARATOR + "ADHESION" + TARGET_SEPARATOR + target;
					String name2 = REGION_ID_2 + TAG_SEPARATOR + "ADHESION" + TARGET_SEPARATOR + target;
					assertEquals(POPULATION.getDouble(name1), box.getDouble(name1), EPSILON);
					double expected = POPULATION.getDouble(name2);
					if (target.equals(modifiedRegion1)) { expected = value; }
					if (target.equals(modifiedRegion2)) { expected *= scale; }
					assertEquals(expected, box.getDouble(name2), EPSILON);
				}
			}
		}
	}
	
	@Test
	public void updatePopulation_withConversionOnePop_convertsValue() {
		MiniBox conversion = new MiniBox();
		String convertedParameter = POPULATION_PARAMETER_NAMES[(int)(Math.random()*POPULATION_PARAMETER_NAMES.length)];
		conversion.put(convertedParameter, "DS");
		
		Box[] boxes = new Box[] { new Box() };
		boxes[0].add("id", POPULATION_ID_1);
		Series series = makeSeriesForPopulation(boxes, conversion);
		MiniBox box = series._populations.get(POPULATION_ID_1);
		
		for (String parameter : POPULATION_PARAMETER_NAMES) {
			double expected = POPULATION.getDouble(parameter);
			if (parameter.equals(convertedParameter)) { expected *= DS; }
			assertEquals(expected, box.getDouble(parameter), EPSILON);
		}
	}
	
	@Test
	public void updatePopulation_withConversionMultiplePops_convertsValue() {
		MiniBox conversion = new MiniBox();
		String convertedParameter = POPULATION_PARAMETER_NAMES[(int)(Math.random()*POPULATION_PARAMETER_NAMES.length)];
		conversion.put(convertedParameter, "DS");
		
		Box[] boxes = new Box[] { new Box(), new Box(), new Box() };
		boxes[0].add("id", POPULATION_ID_1);
		boxes[1].add("id", POPULATION_ID_2);
		boxes[2].add("id", POPULATION_ID_3);
		Series series = makeSeriesForPopulation(boxes, conversion);
		MiniBox box1 = series._populations.get(POPULATION_ID_1);
		MiniBox box2 = series._populations.get(POPULATION_ID_2);
		MiniBox box3 = series._populations.get(POPULATION_ID_3);
		
		for (String parameter : POPULATION_PARAMETER_NAMES) {
			double expected = POPULATION.getDouble(parameter);
			if (parameter.equals(convertedParameter)) { expected *= DS; }
			assertEquals(expected, box1.getDouble(parameter), EPSILON);
			assertEquals(expected, box2.getDouble(parameter), EPSILON);
			assertEquals(expected, box3.getDouble(parameter), EPSILON);
		}
	}
	
	@Test
	public void makeConstructors_given2D_createsConstructors() {
		Series series = mock(Series.class, CALLS_REAL_METHODS);
		
		try {
			Field field = Series.class.getDeclaredField("_height");
			field.setAccessible(true);
			field.setInt(series, 1);
		} catch (Exception ignored) { }
		
		series.makeConstructors();
		
		assertEquals("arcade.sim.PottsSimulation2D", series.simCons.getName());
		assertEquals("arcade.vis.PottsVisualization", series.visCons.getName());
	}
	
	@Test
	public void makeConstructors_given3D_createsConstructors() {
		Series series = mock(Series.class, CALLS_REAL_METHODS);
		
		try {
			Field field = Series.class.getDeclaredField("_height");
			field.setAccessible(true);
			field.setInt(series, 3);
		} catch (Exception ignored) { }
		
		series.makeConstructors();
		
		assertEquals("arcade.sim.PottsSimulation3D", series.simCons.getName());
		assertEquals("arcade.vis.PottsVisualization", series.visCons.getName());
	}
	
	@Test
	public void runSims_fromConstructors_callsMethods() throws Exception {
		int[] start = new int[] { 0, 0, 10, 1 };
		int[] end = new int[] { 0, 1, 12, 0 };
		int[] n = new int[] { 1, 2, 3, 0 };
		String[][] seeds = new String[][] {
				{ "00" },
				{ "00", "01" },
				{ "10", "11", "12" },
				{ }
		};
		
		for (int i = 0; i < n.length; i++) {
			HashMap<String, MiniBox> setupDicts = makeDicts();
			setupDicts.get("series").put("start", start[i]);
			setupDicts.get("series").put("end", end[i]);
			Series series = spy(new Series(setupDicts, setupListsMock, PARAMETERS, false));
			doNothing().when(series).runSim(any(SimState.class), any(String.class));
			
			series.simCons = spy(series.simCons);
			series.runSims();
			
			verify(series, times(n[i])).runSim(any(SimState.class), any(String.class));
			for (String seed : seeds[i]) {
				verify(series.simCons).newInstance(start[i] + i + SEED_OFFSET, series);
				verify(series).runSim(any(SimState.class), eq(seed));
			}
		}
	}
	
	@Test
	public void runSim_repeatingStep_callsMethods() {
		HashMap<String, MiniBox> setupDicts = makeDicts();
		int ticks = randomInt();
		setupDicts.get("series").put("ticks", ticks);
		Series series = spy(new Series(setupDicts, setupListsMock, PARAMETERS, false));
		
		SimState state = mock(SimState.class);
		state.schedule = spy(new Schedule());
		state.schedule.scheduleRepeating((Steppable) simState -> { }, 1);
		
		series.runSim(state, randomString());
		
		verify(state).start();
		verify(state).finish();
		verify(state.schedule, times(ticks)).step(state);
		verify(state.schedule, times(ticks)).getTime();
	}
	
	@Test
	public void runSim_singleStep_callsMethodsOnce() {
		HashMap<String, MiniBox> setupDicts = makeDicts();
		int ticks = randomInt();
		setupDicts.get("series").put("ticks", ticks + 1);
		Series series = spy(new Series(setupDicts, setupListsMock, PARAMETERS, false));
		
		SimState state = mock(SimState.class);
		state.schedule = spy(new Schedule());
		state.schedule.scheduleOnce(ticks - 1, (Steppable) simState -> { });
		
		series.runSim(state, randomString());
		
		verify(state).start();
		verify(state).finish();
		verify(state.schedule, times(2)).step(state);
		verify(state.schedule).getTime();
	}
	
	@Test
	public void runVis_fromConstructors_callsMethods() throws Exception {
		HashMap<String, MiniBox> setupDicts = makeDicts();
		Series series = spy(new Series(setupDicts, setupListsMock, PARAMETERS, false));
		
		series.simCons = spy(series.simCons);
		series.visCons = spy(series.visCons);
		
		Simulation simstate = mock(Simulation.class);
		GUIState guistate = spy(mock(GUIState.class));
		
		doReturn(null).when(guistate).createController();
		doReturn(simstate).when(series.simCons).newInstance(any(int.class), eq(series));
		doReturn(guistate).when(series.visCons).newInstance(any(Simulation.class));
		
		System.clearProperty("java.awt.headless");
		
		series.runVis();
		verify(series.simCons).newInstance(series.getStartSeed() + SEED_OFFSET, series);
		verify(series.visCons).newInstance(simstate);
		verify(guistate).createController();
	}
	
	@Test
	public void runVis_headlessFalse_callsMethods() throws Exception {
		HashMap<String, MiniBox> setupDicts = makeDicts();
		Series series = spy(new Series(setupDicts, setupListsMock, PARAMETERS, false));
		
		series.simCons = spy(series.simCons);
		series.visCons = spy(series.visCons);
		
		Simulation simstate = mock(Simulation.class);
		GUIState guistate = spy(mock(GUIState.class));
		
		doReturn(null).when(guistate).createController();
		doReturn(simstate).when(series.simCons).newInstance(any(int.class), eq(series));
		doReturn(guistate).when(series.visCons).newInstance(any(Simulation.class));
		
		System.setProperty("java.awt.headless", "false");
		
		series.runVis();
		verify(series.simCons).newInstance(series.getStartSeed() + SEED_OFFSET, series);
		verify(series.visCons).newInstance(simstate);
		verify(guistate).createController();
	}
	
	@Test
	public void runVis_headlessTrue_doesNothing() throws Exception {
		HashMap<String, MiniBox> setupDicts = makeDicts();
		Series series = spy(new Series(setupDicts, setupListsMock, PARAMETERS, false));
		
		series.simCons = spy(series.simCons);
		series.visCons = spy(series.visCons);
		
		Simulation simstate = mock(Simulation.class);
		GUIState guistate = spy(mock(GUIState.class));
		
		doReturn(null).when(guistate).createController();
		doReturn(simstate).when(series.simCons).newInstance(any(int.class), eq(series));
		doReturn(guistate).when(series.visCons).newInstance(any(Simulation.class));
		
		System.setProperty("java.awt.headless", "true");
		
		series.runVis();
		verify(series.simCons, never()).newInstance(series.getStartSeed() + SEED_OFFSET, series);
		verify(series.visCons, never()).newInstance(simstate);
		verify(guistate, never()).createController();
	}
}
