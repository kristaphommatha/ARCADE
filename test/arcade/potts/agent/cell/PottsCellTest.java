package arcade.potts.agent.cell;

import java.util.EnumMap;
import java.util.EnumSet;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.stubbing.Answer;
import sim.engine.Schedule;
import sim.engine.Stoppable;
import ec.util.MersenneTwisterFast;
import arcade.core.agent.module.*;
import arcade.core.env.loc.*;
import arcade.core.util.MiniBox;
import arcade.potts.agent.module.PottsModule;
import arcade.potts.agent.module.PottsModuleApoptosis;
import arcade.potts.agent.module.PottsModuleAutosis;
import arcade.potts.agent.module.PottsModuleNecrosis;
import arcade.potts.agent.module.PottsModuleProliferation;
import arcade.potts.agent.module.PottsModuleQuiescence;
import arcade.potts.env.loc.PottsLocation;
import arcade.potts.sim.PottsSimulation;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static arcade.core.ARCADETestUtilities.*;
import static arcade.core.util.Enums.Region;
import static arcade.core.util.Enums.State;
import static arcade.potts.agent.cell.PottsCellFactoryTest.*;
import static arcade.potts.util.PottsEnums.Ordering;
import static arcade.potts.util.PottsEnums.Phase;

public class PottsCellTest {
    private static final double EPSILON = 1E-8;
    private static final double OFFSET = 0.01;
    private static final MersenneTwisterFast RANDOM = new MersenneTwisterFast(randomSeed());
    static EnumMap<Region, Double> criticalVolumesRegionMock;
    static EnumMap<Region, Double> criticalHeightsRegionMock;
    static Location locationMock;
    static int locationVolume;
    static int locationHeight;
    static int locationSurface;
    static EnumMap<Region, Integer> locationRegionVolumes;
    static EnumMap<Region, Integer> locationRegionHeights;
    static EnumMap<Region, Integer> locationRegionSurfaces;
    static int cellID = randomIntBetween(1, 10);
    static int cellParent = randomIntBetween(1, 10);
    static int cellPop = randomIntBetween(1, 10);
    static int cellAge = randomIntBetween(1, 1000);
    static int cellDivisions = randomIntBetween(1, 100);
    static double cellCriticalVolume = randomDoubleBetween(10, 100);
    static double cellCriticalHeight = randomDoubleBetween(10, 100);
    static State cellState = State.random(RANDOM);
    static PottsCell cellDefault;
    static PottsCell cellWithRegions;
    static PottsCell cellWithoutRegions;
    static EnumSet<Region> regionList;
    static MiniBox parametersMock;
    
    @BeforeClass
    public static void setupMocks() {
        parametersMock = mock(MiniBox.class);
        locationMock = mock(PottsLocation.class);
        
        regionList = EnumSet.of(Region.DEFAULT, Region.NUCLEUS);
        when(locationMock.getRegions()).thenReturn(regionList);
        
        Answer<Double> answer = invocation -> {
            Double value1 = invocation.getArgument(0);
            Double value2 = invocation.getArgument(1);
            return value1 * value2;
        };
        when(((PottsLocation) locationMock).convertSurface(anyDouble(), anyDouble())).thenAnswer(answer);
        
        locationRegionVolumes = new EnumMap<>(Region.class);
        locationRegionHeights = new EnumMap<>(Region.class);
        locationRegionSurfaces = new EnumMap<>(Region.class);
        
        // Random volumes and surfaces for regions.
        for (Region region : regionList) {
            locationRegionVolumes.put(region, randomIntBetween(10, 20));
            locationRegionHeights.put(region, randomIntBetween(10, 20));
            locationRegionSurfaces.put(region, randomIntBetween(10, 20));
            
            when(locationMock.getVolume(region)).thenReturn(locationRegionVolumes.get(region));
            when(locationMock.getHeight(region)).thenReturn(locationRegionHeights.get(region));
            when(locationMock.getSurface(region)).thenReturn(locationRegionSurfaces.get(region));
            
            locationVolume += locationRegionVolumes.get(region);
            locationHeight += locationRegionHeights.get(region);
            locationSurface += locationRegionSurfaces.get(region);
        }
        
        when(locationMock.getVolume()).thenReturn(locationVolume);
        when(locationMock.getHeight()).thenReturn(locationHeight);
        when(locationMock.getSurface()).thenReturn(locationSurface);
        
        // Region criticals.
        criticalVolumesRegionMock = new EnumMap<>(Region.class);
        criticalHeightsRegionMock = new EnumMap<>(Region.class);
        for (Region region : regionList) {
            criticalVolumesRegionMock.put(region, (double) locationRegionVolumes.get(region));
            criticalHeightsRegionMock.put(region, (double) locationRegionHeights.get(region));
        }
        
        cellDefault = new PottsCell(cellID, cellParent, cellPop, cellState, cellAge, cellDivisions,
                locationMock, false, parametersMock, cellCriticalVolume, cellCriticalHeight,
                null, null);
        cellWithRegions = new PottsCell(cellID, cellParent, 1, cellState, cellAge, cellDivisions,
                locationMock, true, parametersMock, cellCriticalVolume, cellCriticalHeight,
                criticalVolumesRegionMock, criticalHeightsRegionMock);
        cellWithoutRegions = new PottsCell(cellID, cellParent, 1, cellState, cellAge, cellDivisions,
                locationMock, false, parametersMock, cellCriticalVolume, cellCriticalHeight,
                criticalVolumesRegionMock, criticalHeightsRegionMock);
    }
    
    static PottsCell make(boolean regions) {
        return make(locationMock, regions);
    }
    
    static PottsCell make(Location location, boolean regions) {
        if (!regions) {
            return new PottsCell(cellID, cellParent, cellPop, cellState, cellAge, cellDivisions,
                    location, false, parametersMock, cellCriticalVolume, cellCriticalHeight,
                    null, null);
        } else {
            return new PottsCell(cellID, cellParent, cellPop, cellState, cellAge, cellDivisions,
                    location, true, parametersMock, cellCriticalVolume, cellCriticalHeight,
                    criticalVolumesRegionMock, criticalHeightsRegionMock);
        }
    }
    
    @Test
    public void getID_defaultConstructor_returnsValue() {
        assertEquals(cellID, cellDefault.getID());
    }
    
    @Test
    public void getParent_defaultConstructor_returnsValue() {
        assertEquals(cellParent, cellDefault.getParent());
    }
    
    @Test
    public void getParent_valueAssigned_returnsValue() {
        int parent = randomIntBetween(0, 100);
        PottsCell cell = new PottsCell(cellID, parent, cellPop, cellState, cellAge, cellDivisions,
                locationMock, false, parametersMock, cellCriticalVolume, cellCriticalHeight,
                null, null);
        assertEquals(parent, cell.getParent());
    }
    
    @Test
    public void getPop_defaultConstructor_returnsValue() {
        assertEquals(cellPop, cellDefault.getPop());
    }
    
    @Test
    public void getPop_valueAssigned_returnsValue() {
        int pop = randomIntBetween(0, 100);
        PottsCell cell = new PottsCell(cellID, cellParent, pop, cellState, cellAge, cellDivisions,
                locationMock, false, parametersMock, cellCriticalVolume, cellCriticalHeight,
                null, null);
        assertEquals(pop, cell.getPop());
    }
    
    @Test
    public void getState_defaultConstructor_returnsValue() {
        assertEquals(cellState, cellDefault.getState());
    }
    
    @Test
    public void getState_valueAssigned_returnsValue() {
        State state = State.random(RANDOM);
        PottsCell cell = new PottsCell(cellID, cellParent, cellPop, state, cellAge, cellDivisions,
                locationMock, false, parametersMock, cellCriticalVolume, cellCriticalHeight,
                null, null);
        assertEquals(state, cell.getState());
    }
    
    @Test
    public void getAge_defaultConstructor_returnsValue() {
        assertEquals(cellAge, cellDefault.getAge());
    }
    
    @Test
    public void getAge_valueAssigned_returnsValue() {
        int age = randomIntBetween(0, 100);
        PottsCell cell = new PottsCell(cellID, cellParent, cellPop, cellState, age, cellDivisions,
                locationMock, false, parametersMock, cellCriticalVolume, cellCriticalHeight,
                null, null);
        assertEquals(age, cell.getAge());
    }
    
    @Test
    public void getDivisions_defaultConstructor_returnsValue() {
        assertEquals(cellDivisions, cellDefault.getDivisions());
    }
    
    @Test
    public void getDivisions_valueAssigned_returnsValue() {
        int divisions = randomIntBetween(0, 100);
        PottsCell cell = new PottsCell(cellID, cellParent, cellPop, cellState, cellAge, divisions,
                locationMock, false, parametersMock, cellCriticalVolume, cellCriticalHeight,
                null, null);
        assertEquals(divisions, cell.getDivisions());
    }
    
    @Test
    public void hasRegions_withoutRegions_returnsFalse() {
        assertFalse(cellWithoutRegions.hasRegions());
    }
    
    @Test
    public void hasRegions_withRegions_returnsTrue() {
        assertTrue(cellWithRegions.hasRegions());
    }
    
    @Test
    public void getLocation_defaultConstructor_returnsObject() {
        assertSame(locationMock, cellDefault.getLocation());
    }
    
    @Test
    public void getModule_defaultConstructor_returnsObject() {
        assertTrue(cellDefault.getModule() instanceof PottsModule);
    }
    
    @Test
    public void getParameters_defaultConstructor_returnsObject() {
        assertSame(parametersMock, cellDefault.getParameters());
    }
    
    @Test
    public void getVolume_defaultConstructor_returnsValue() {
        assertEquals(locationVolume, cellDefault.getVolume());
    }
    
    @Test
    public void getVolume_validRegions_returnsValue() {
        for (Region region : regionList) {
            assertEquals((int) locationRegionVolumes.get(region), cellWithRegions.getVolume(region));
        }
    }
    
    @Test
    public void getVolume_nullRegion_returnsZero() {
        assertEquals(0, cellWithRegions.getVolume(null));
    }
    
    @Test
    public void getVolume_noRegions_returnsZero() {
        for (Region region : regionList) {
            assertEquals(0, cellWithoutRegions.getVolume(region));
        }
    }
    
    @Test
    public void getHeight_defaultConstructor_returnsValue() {
        assertEquals(locationHeight, cellDefault.getHeight());
    }
    
    @Test
    public void getHeight_validRegions_returnsValue() {
        for (Region region : regionList) {
            assertEquals((int) locationRegionHeights.get(region), cellWithRegions.getHeight(region));
        }
    }
    
    @Test
    public void getHeight_nullRegion_returnsZero() {
        assertEquals(0, cellWithRegions.getHeight(null));
    }
    
    @Test
    public void getHeight_noRegions_returnsZero() {
        for (Region region : regionList) {
            assertEquals(0, cellWithoutRegions.getHeight(region));
        }
    }
    
    @Test
    public void getSurface_defaultConstructor_returnsValue() {
        assertEquals(locationSurface, cellDefault.getSurface());
    }
    
    @Test
    public void getSurface_validRegions_returnsValue() {
        for (Region region : regionList) {
            assertEquals((int) locationRegionSurfaces.get(region), cellWithRegions.getSurface(region));
        }
    }
    
    @Test
    public void getSurface_nullRegion_returnsZero() {
        assertEquals(0, cellWithRegions.getSurface(null));
    }
    
    @Test
    public void getSurface_noRegions_returnsZero() {
        for (Region region : regionList) {
            assertEquals(0, cellWithoutRegions.getSurface(region));
        }
    }
    
    @Test
    public void getTargetVolume_beforeInitialize_returnsZero() {
        assertEquals(0, cellDefault.getTargetVolume(), EPSILON);
    }
    
    @Test
    public void getTargetVolume_beforeInitializeValidRegion_returnsZero() {
        for (Region region : regionList) {
            assertEquals(0, cellWithRegions.getTargetVolume(region), EPSILON);
        }
    }
    
    @Test
    public void getTargetVolume_beforeInitializeInvalidRegion_returnsZero() {
        assertEquals(0, cellWithRegions.getTargetVolume(null), EPSILON);
    }
    
    @Test
    public void getTargetVolume_beforeInitializeNoRegions_returnsZero() {
        for (Region region : regionList) {
            assertEquals(0, cellWithoutRegions.getTargetVolume(region), EPSILON);
        }
    }
    
    @Test
    public void getTargetVolume_afterInitialize_returnsValue() {
        PottsCell cell = make(false);
        cell.initialize(null, null);
        assertEquals(locationVolume, cell.getTargetVolume(), EPSILON);
    }
    
    @Test
    public void getTargetVolume_afterInitializeValidRegion_returnsValue() {
        PottsCell cell = make(true);
        cell.initialize(null, null);
        for (Region region : regionList) {
            assertEquals(locationRegionVolumes.get(region), cell.getTargetVolume(region), EPSILON);
        }
    }
    
    @Test
    public void getTargetVolume_afterInitializeInvalidRegion_returnsZero() {
        PottsCell cell = make(true);
        cell.initialize(null, null);
        assertEquals(0, cell.getTargetVolume(null), EPSILON);
        assertEquals(0, cell.getTargetVolume(Region.UNDEFINED), EPSILON);
    }
    
    @Test
    public void getTargetVolume_afterInitializeNoRegion_returnsZero() {
        PottsCell cell = make(false);
        cell.initialize(null, null);
        for (Region region : regionList) {
            assertEquals(0, cell.getTargetVolume(region), EPSILON);
        }
    }
    
    @Test
    public void getTargetSurface_beforeInitialize_returnsZero() {
        assertEquals(0, cellDefault.getTargetSurface(), EPSILON);
    }
    
    @Test
    public void getTargetSurface_beforeInitializeValidRegion_returnsZero() {
        for (Region region : regionList) {
            assertEquals(0, cellWithRegions.getTargetSurface(region), EPSILON);
        }
    }
    
    @Test
    public void getTargetSurface_beforeInitializeInvalidRegion_returnsZero() {
        assertEquals(0, cellWithRegions.getTargetSurface(null), EPSILON);
    }
    
    @Test
    public void getTargetSurface_beforeInitializeNoRegions_returnsZero() {
        for (Region region : regionList) {
            assertEquals(0, cellWithoutRegions.getTargetSurface(region), EPSILON);
        }
    }
    
    @Test
    public void getTargetSurface_afterInitialize_returnsValue() {
        PottsCell cell = make(false);
        cell.initialize(null, null);
        assertEquals(locationSurface, cell.getTargetSurface(), EPSILON);
    }
    
    @Test
    public void getTargetSurface_afterInitializeValidRegion_returnsValue() {
        PottsCell cell = make(true);
        cell.initialize(null, null);
        for (Region region : regionList) {
            assertEquals(locationRegionSurfaces.get(region), cell.getTargetSurface(region), EPSILON);
        }
    }
    
    @Test
    public void getTargetSurface_afterInitializeInvalidRegion_returnsZero() {
        PottsCell cell = make(true);
        cell.initialize(null, null);
        assertEquals(0, cell.getTargetSurface(null), EPSILON);
        assertEquals(0, cell.getTargetSurface(Region.UNDEFINED), EPSILON);
    }
    
    @Test
    public void getTargetSurface_afterInitializeNoRegion_returnsZero() {
        PottsCell cell = make(false);
        cell.initialize(null, null);
        for (Region region : regionList) {
            assertEquals(0, cell.getTargetSurface(region), EPSILON);
        }
    }
    
    @Test
    public void getCriticalVolume_beforeInitialize_returnsValue() {
        assertEquals(cellCriticalVolume, cellDefault.getCriticalVolume(), EPSILON);
    }
    
    @Test
    public void getCriticalVolume_beforeInitializeValidRegion_returnsValue() {
        for (Region region : regionList) {
            assertEquals(locationRegionVolumes.get(region), cellWithRegions.getCriticalVolume(region), EPSILON);
        }
    }
    
    @Test
    public void getCriticalVolume_beforeInitializeInvalidRegion_returnsZero() {
        assertEquals(0, cellWithRegions.getCriticalVolume(null), EPSILON);
        assertEquals(0, cellWithRegions.getCriticalVolume(Region.UNDEFINED), EPSILON);
    }
    
    @Test
    public void getCriticalVolume_beforeInitializeNoRegions_returnsZero() {
        for (Region region : regionList) {
            assertEquals(0, cellWithoutRegions.getCriticalVolume(region), EPSILON);
        }
    }
    
    @Test
    public void getCriticalVolume_afterInitialize_returnsValue() {
        PottsCell cell = make(false);
        cell.initialize(null, null);
        assertEquals(cellCriticalVolume, cell.getCriticalVolume(), EPSILON);
    }
    
    @Test
    public void getCriticalVolume_afterInitializeValidRegion_returnsValue() {
        PottsCell cell = make(true);
        cell.initialize(null, null);
        for (Region region : regionList) {
            assertEquals(locationRegionVolumes.get(region), cell.getCriticalVolume(region), EPSILON);
        }
    }
    
    @Test
    public void getCriticalVolume_afterInitializeInvalidRegion_returnsZero() {
        PottsCell cell = make(true);
        cell.initialize(null, null);
        assertEquals(0, cell.getCriticalVolume(null), EPSILON);
    }
    
    @Test
    public void getCriticalVolume_afterInitializeNoRegion_returnsZero() {
        PottsCell cell = make(false);
        cell.initialize(null, null);
        for (Region region : regionList) {
            assertEquals(0, cell.getCriticalVolume(region), EPSILON);
        }
    }
    
    @Test
    public void getCriticalHeight_beforeInitialize_returnsValue() {
        assertEquals(cellCriticalHeight, cellDefault.getCriticalHeight(), EPSILON);
    }
    
    @Test
    public void getCriticalHeight_beforeInitializeValidRegion_returnsValue() {
        for (Region region : regionList) {
            assertEquals(locationRegionHeights.get(region), cellWithRegions.getCriticalHeight(region), EPSILON);
        }
    }
    
    @Test
    public void getCriticalHeight_beforeInitializeInvalidRegion_returnsZero() {
        assertEquals(0, cellWithRegions.getCriticalHeight(null), EPSILON);
        assertEquals(0, cellWithRegions.getCriticalHeight(Region.UNDEFINED), EPSILON);
    }
    
    @Test
    public void getCriticalHeight_beforeInitializeNoRegions_returnsZero() {
        for (Region region : regionList) {
            assertEquals(0, cellWithoutRegions.getCriticalHeight(region), EPSILON);
        }
    }
    
    @Test
    public void getCriticalHeight_afterInitialize_returnsValue() {
        PottsCell cell = make(false);
        cell.initialize(null, null);
        assertEquals(cellCriticalHeight, cell.getCriticalHeight(), EPSILON);
    }
    
    @Test
    public void getCriticalHeight_afterInitializeValidRegion_returnsValue() {
        PottsCell cell = make(true);
        cell.initialize(null, null);
        for (Region region : regionList) {
            assertEquals(locationRegionHeights.get(region), cell.getCriticalHeight(region), EPSILON);
        }
    }
    
    @Test
    public void getCriticalHeight_afterInitializeInvalidRegion_returnsZero() {
        PottsCell cell = make(true);
        cell.initialize(null, null);
        assertEquals(0, cell.getCriticalHeight(null), EPSILON);
    }
    
    @Test
    public void getCriticalHeight_afterInitializeNoRegion_returnsZero() {
        PottsCell cell = make(false);
        cell.initialize(null, null);
        for (Region region : regionList) {
            assertEquals(0, cell.getCriticalHeight(region), EPSILON);
        }
    }
    
    @Test
    public void setState_givenState_assignsValue() {
        PottsCell cell = make(false);
        
        cell.setState(State.QUIESCENT);
        assertEquals(State.QUIESCENT, cell.getState());
        
        cell.setState(State.PROLIFERATIVE);
        assertEquals(State.PROLIFERATIVE, cell.getState());
        
        cell.setState(State.APOPTOTIC);
        assertEquals(State.APOPTOTIC, cell.getState());
        
        cell.setState(State.NECROTIC);
        assertEquals(State.NECROTIC, cell.getState());
        
        cell.setState(State.AUTOTIC);
        assertEquals(State.AUTOTIC, cell.getState());
    }
    
    @Test
    public void setState_givenState_updatesModule() {
        PottsCell cell = make(false);
        
        cell.setState(State.QUIESCENT);
        assertTrue(cell.module instanceof PottsModuleQuiescence);
        
        cell.setState(State.PROLIFERATIVE);
        assertTrue(cell.module instanceof PottsModuleProliferation);
        
        cell.setState(State.APOPTOTIC);
        assertTrue(cell.module instanceof PottsModuleApoptosis);
        
        cell.setState(State.NECROTIC);
        assertTrue(cell.module instanceof PottsModuleNecrosis);
        
        cell.setState(State.AUTOTIC);
        assertTrue(cell.module instanceof PottsModuleAutosis);
    }
    
    @Test
    public void setState_invalidState_setsNull() {
        PottsCell cell = make(false);
        cell.setState(State.UNDEFINED);
        assertNull(cell.getModule());
    }
    
    @Test
    public void stop_called_callsMethod() {
        PottsCell cell = make(false);
        cell.stopper = mock(Stoppable.class);
        cell.stop();
        verify(cell.stopper).stop();
    }
    
    @Test
    public void make_noRegions_setsFields() {
        double criticalVolume = randomDoubleBetween(10, 100);
        double criticalHeight = randomDoubleBetween(10, 100);
        MiniBox parameters = mock(MiniBox.class);
        Location location1 = mock(PottsLocation.class);
        Location location2 = mock(PottsLocation.class);
        
        PottsCell cell1 = new PottsCell(cellID, cellParent, cellPop, cellState, cellAge, cellDivisions,
                location1, false, parameters, criticalVolume, criticalHeight,
                null, null);
        PottsCell cell2 = cell1.make(cellID + 1, State.QUIESCENT, location2);
        
        assertEquals(cellID + 1, cell2.id);
        assertEquals(cellID, cell2.parent);
        assertEquals(cellPop, cell2.pop);
        assertEquals(cellAge, cell2.getAge());
        assertEquals(cellDivisions + 1, cell1.getDivisions());
        assertEquals(cellDivisions + 1, cell2.getDivisions());
        assertFalse(cell2.hasRegions());
        assertEquals(location2, cell2.getLocation());
        assertEquals(cell2.parameters, parameters);
        assertEquals(criticalVolume, cell2.getCriticalVolume(), EPSILON);
        assertEquals(criticalHeight, cell2.getCriticalHeight(), EPSILON);
    }
    
    @Test
    public void make_hasRegions_setsFields() {
        double criticalVolume = randomDoubleBetween(10, 100);
        double criticalHeight = randomDoubleBetween(10, 100);
        MiniBox parameters = mock(MiniBox.class);
        Location location1 = mock(PottsLocation.class);
        Location location2 = mock(PottsLocation.class);
        EnumMap<Region, Double> criticalVolumesRegion = new EnumMap<>(Region.class);
        EnumMap<Region, Double> criticalHeightsRegion = new EnumMap<>(Region.class);
        
        for (Region region : Region.values()) {
            criticalVolumesRegion.put(region, randomDoubleBetween(10, 100));
            criticalHeightsRegion.put(region, randomDoubleBetween(10, 100));
        }
        
        EnumSet<Region> allRegions = EnumSet.allOf(Region.class);
        doReturn(allRegions).when(location1).getRegions();
        doReturn(allRegions).when(location2).getRegions();
        
        PottsCell cell1 = new PottsCell(cellID, cellParent, cellPop, cellState, cellAge, cellDivisions,
                location1, true, parameters, criticalVolume, criticalHeight,
                criticalVolumesRegion, criticalHeightsRegion);
        PottsCell cell2 = cell1.make(cellID + 1, State.QUIESCENT, location2);
        
        assertEquals(cellID + 1, cell2.id);
        assertEquals(cellID, cell2.parent);
        assertEquals(cellPop, cell2.pop);
        assertEquals(cellAge, cell2.getAge());
        assertEquals(cellDivisions + 1, cell1.getDivisions());
        assertEquals(cellDivisions + 1, cell2.getDivisions());
        assertTrue(cell2.hasRegions());
        assertEquals(location2, cell2.getLocation());
        assertEquals(cell2.parameters, parameters);
        assertEquals(criticalVolume, cell2.getCriticalVolume(), EPSILON);
        assertEquals(criticalHeight, cell2.getCriticalHeight(), EPSILON);
        for (Region region : Region.values()) {
            assertEquals(criticalVolumesRegion.get(region), cell2.getCriticalVolume(region), EPSILON);
            assertEquals(criticalHeightsRegion.get(region), cell2.getCriticalHeight(region), EPSILON);
        }
    }
    
    @Test
    public void schedule_validInput_callsMethod() {
        Schedule schedule = spy(mock(Schedule.class));
        PottsCell cell = make(false);
        doReturn(mock(Stoppable.class)).when(schedule).scheduleRepeating(cell, Ordering.CELLS.ordinal(), 1);
        cell.schedule(schedule);
        verify(schedule).scheduleRepeating(cell, Ordering.CELLS.ordinal(), 1);
    }
    
    @Test
    public void schedule_validInput_assignStopper() {
        Schedule schedule = spy(mock(Schedule.class));
        PottsCell cell = make(false);
        doReturn(mock(Stoppable.class)).when(schedule).scheduleRepeating(cell, Ordering.CELLS.ordinal(), 1);
        cell.schedule(schedule);
        assertNotNull(cell.stopper);
    }
    
    @Test
    public void initialize_withoutRegions_callsMethod() {
        PottsLocation location = mock(PottsLocation.class);
        PottsCell cell = make(location, false);
        int[][][] array = new int[1][3][3];
        cell.initialize(array, null);
        
        verify(location).update(cellID, array, null);
    }
    
    @Test
    public void initialize_withRegions_callsMethod() {
        PottsLocation location = mock(PottsLocation.class);
        when(location.getRegions()).thenReturn(regionList);
        PottsCell cell = make(location, true);
        int[][][] array1 = new int[1][3][3];
        int[][][] array2 = new int[1][3][3];
        cell.initialize(array1, array2);
        
        verify(location).update(cellID, array1, array2);
    }
    
    @Test
    public void initialize_withoutRegions_updatesTargets() {
        int volume = randomIntBetween(0, 100);
        int surface = randomIntBetween(0, 100);
        PottsLocation location = mock(PottsLocation.class);
        when(location.getVolume()).thenReturn(volume);
        when(location.getSurface()).thenReturn(surface);
        
        PottsCell cell = make(location, false);
        cell.initialize(new int[1][3][3], null);
        
        assertEquals(volume, cell.getTargetVolume(), EPSILON);
        assertEquals(surface, cell.getTargetSurface(), EPSILON);
        assertEquals(0, cell.getTargetVolume(Region.DEFAULT), EPSILON);
        assertEquals(0, cell.getTargetSurface(Region.DEFAULT), EPSILON);
    }
    
    @Test
    public void initialize_withRegions_updatesTargets() {
        int volume1 = randomIntBetween(0, 100);
        int volume2 = randomIntBetween(0, 100);
        int surface1 = randomIntBetween(0, 100);
        int surface2 = randomIntBetween(0, 100);
        Location location = mock(PottsLocation.class);
        when(location.getVolume()).thenReturn(volume1 + volume2);
        when(location.getSurface()).thenReturn(surface1 + surface2);
        when(location.getVolume(Region.DEFAULT)).thenReturn(volume1);
        when(location.getSurface(Region.DEFAULT)).thenReturn(surface1);
        when(location.getVolume(Region.NUCLEUS)).thenReturn(volume2);
        when(location.getSurface(Region.NUCLEUS)).thenReturn(surface2);
        when(location.getRegions()).thenReturn(regionList);
        
        PottsCell cell = make(location, true);
        cell.initialize(new int[1][3][3], new int[1][3][3]);
        
        assertEquals(volume1 + volume2, cell.getTargetVolume(), EPSILON);
        assertEquals(surface1 + surface2, cell.getTargetSurface(), EPSILON);
        assertEquals(volume1, cell.getTargetVolume(Region.DEFAULT), EPSILON);
        assertEquals(surface1, cell.getTargetSurface(Region.DEFAULT), EPSILON);
        assertEquals(volume2, cell.getTargetVolume(Region.NUCLEUS), EPSILON);
        assertEquals(surface2, cell.getTargetSurface(Region.NUCLEUS), EPSILON);
        assertEquals(0, cell.getTargetVolume(Region.UNDEFINED), EPSILON);
        assertEquals(0, cell.getTargetSurface(Region.UNDEFINED), EPSILON);
    }
    
    @Test
    public void initialize_targetsSetWithoutRegions_doesNothing() {
        int volume = randomIntBetween(0, 100);
        int surface = randomIntBetween(0, 100);
        Location location = mock(PottsLocation.class);
        when(location.getVolume()).thenReturn(volume);
        when(location.getSurface()).thenReturn(surface);
        
        int targetVolume = randomIntBetween(1, 100);
        int targetSurface = randomIntBetween(1, 100);
        
        PottsCell cell = make(location, false);
        cell.setTargets(targetVolume, targetSurface);
        cell.initialize(new int[1][3][3], null);
        
        assertEquals(targetVolume, cell.getTargetVolume(), EPSILON);
        assertEquals(targetSurface, cell.getTargetSurface(), EPSILON);
        assertEquals(0, cell.getTargetVolume(Region.DEFAULT), EPSILON);
        assertEquals(0, cell.getTargetSurface(Region.DEFAULT), EPSILON);
    }
    
    @Test
    public void initialize_targetsSetWithRegions_updatesTargets() {
        int volume1 = randomIntBetween(0, 100);
        int volume2 = randomIntBetween(0, 100);
        int surface1 = randomIntBetween(0, 100);
        int surface2 = randomIntBetween(0, 100);
        Location location = mock(PottsLocation.class);
        when(location.getVolume()).thenReturn(volume1 + volume2);
        when(location.getSurface()).thenReturn(surface1 + surface2);
        when(location.getVolume(Region.DEFAULT)).thenReturn(volume1);
        when(location.getSurface(Region.DEFAULT)).thenReturn(surface1);
        when(location.getVolume(Region.NUCLEUS)).thenReturn(volume2);
        when(location.getSurface(Region.NUCLEUS)).thenReturn(surface2);
        when(location.getRegions()).thenReturn(regionList);
        
        int targetVolume = randomIntBetween(1, 100);
        int targetSurface = randomIntBetween(1, 100);
        int targetRegionVolume1 = randomIntBetween(1, 100);
        int targetRegionSurface1 = randomIntBetween(1, 100);
        int targetRegionVolume2 = randomIntBetween(1, 100);
        int targetRegionSurface2 = randomIntBetween(1, 100);
        
        PottsCell cell = make(location, true);
        cell.setTargets(targetVolume, targetSurface);
        cell.setTargets(Region.DEFAULT, targetRegionVolume1, targetRegionSurface1);
        cell.setTargets(Region.NUCLEUS, targetRegionVolume2, targetRegionSurface2);
        cell.initialize(new int[1][3][3], new int[1][3][3]);
        
        assertEquals(targetVolume, cell.getTargetVolume(), EPSILON);
        assertEquals(targetSurface, cell.getTargetSurface(), EPSILON);
        assertEquals(targetRegionVolume1, cell.getTargetVolume(Region.DEFAULT), EPSILON);
        assertEquals(targetRegionSurface1, cell.getTargetSurface(Region.DEFAULT), EPSILON);
        assertEquals(targetRegionVolume2, cell.getTargetVolume(Region.NUCLEUS), EPSILON);
        assertEquals(targetRegionSurface2, cell.getTargetSurface(Region.NUCLEUS), EPSILON);
        assertEquals(0, cell.getTargetVolume(Region.UNDEFINED), EPSILON);
        assertEquals(0, cell.getTargetSurface(Region.UNDEFINED), EPSILON);
    }
    
    @Test
    public void initialize_targetsMixed_updatesTargets() {
        int volume = randomIntBetween(0, 100);
        int surface = randomIntBetween(0, 100);
        Location location = mock(PottsLocation.class);
        when(location.getVolume()).thenReturn(volume);
        when(location.getSurface()).thenReturn(surface);
        
        PottsCell cell1 = make(location, false);
        cell1.setTargets(0, randomIntBetween(0, 100));
        cell1.initialize(new int[1][3][3], null);
        
        assertEquals(volume, cell1.getTargetVolume(), EPSILON);
        assertEquals(surface, cell1.getTargetSurface(), EPSILON);
        
        PottsCell cell2 = make(location, false);
        cell2.setTargets(randomIntBetween(0, 100), 0);
        cell2.initialize(new int[1][3][3], null);
        
        assertEquals(volume, cell2.getTargetVolume(), EPSILON);
        assertEquals(surface, cell2.getTargetSurface(), EPSILON);
    }
    
    @Test
    public void reset_withoutRegions_callsMethod() {
        PottsLocation location = mock(PottsLocation.class);
        PottsCell cell = make(location, false);
        int[][][] array = new int[1][3][3];
        cell.initialize(array, null);
        cell.reset(array, null);
        
        verify(location, times(2)).update(cellID, array, null);
    }
    
    @Test
    public void reset_withRegions_callsMethod() {
        PottsLocation location = mock(PottsLocation.class);
        when(location.getRegions()).thenReturn(regionList);
        PottsCell cell = make(location, true);
        int[][][] array1 = new int[1][3][3];
        int[][][] array2 = new int[1][3][3];
        cell.initialize(array1, array2);
        cell.reset(array1, array2);
        
        verify(location, times(2)).update(cellID, array1, array2);
    }
    
    @Test
    public void reset_withoutRegions_updatesTargets() {
        PottsCell cell = make(false);
        cell.initialize(new int[1][3][3], new int[1][3][3]);
        cell.updateTarget(randomDoubleBetween(0, 10), randomDoubleBetween(0, 10));
        cell.reset(new int[1][3][3], null);
        
        assertEquals(cellCriticalVolume, cell.getTargetVolume(), EPSILON);
        assertEquals(cellCriticalVolume * cellCriticalHeight, cell.getTargetSurface(), EPSILON);
        assertEquals(0, cell.getTargetVolume(Region.DEFAULT), EPSILON);
        assertEquals(0, cell.getTargetSurface(Region.DEFAULT), EPSILON);
    }
    
    @Test
    public void reset_withRegions_updatesTargets() {
        PottsCell cell = make(true);
        cell.initialize(new int[1][3][3], new int[1][3][3]);
        cell.updateTarget(Region.DEFAULT, randomDoubleBetween(0, 10), randomDoubleBetween(0, 10));
        cell.updateTarget(Region.NUCLEUS, randomDoubleBetween(0, 10), randomDoubleBetween(0, 10));
        cell.reset(new int[1][3][3], new int[1][3][3]);
        
        assertEquals(cellCriticalVolume, cell.getTargetVolume(), EPSILON);
        assertEquals(cellCriticalVolume * cellCriticalHeight, cell.getTargetSurface(), EPSILON);
        
        double volumeDefault = criticalVolumesRegionMock.get(Region.DEFAULT);
        double heightDefault = criticalHeightsRegionMock.get(Region.DEFAULT);
        assertEquals(volumeDefault, cell.getTargetVolume(Region.DEFAULT), EPSILON);
        assertEquals(volumeDefault * heightDefault, cell.getTargetSurface(Region.DEFAULT), EPSILON);
        
        double volumeNucleus = criticalVolumesRegionMock.get(Region.NUCLEUS);
        double heightNucleus = criticalHeightsRegionMock.get(Region.NUCLEUS);
        assertEquals(volumeNucleus, cell.getTargetVolume(Region.NUCLEUS), EPSILON);
        assertEquals(volumeNucleus * heightNucleus, cell.getTargetSurface(Region.NUCLEUS), EPSILON);
    }
    
    @Test
    public void step_singleStep_updatesAge() {
        PottsCell cell = make(false);
        PottsSimulation sim = mock(PottsSimulation.class);
        cell.module = mock(Module.class);
        
        cell.step(sim);
        assertEquals(cellAge + 1, cell.getAge(), EPSILON);
    }
    
    @Test
    public void setTargets_noRegions_updateValues() {
        double targetVolume = randomDoubleBetween(0, 10);
        double targetSurface = randomDoubleBetween(0, 10);
        PottsCell cell = make(false);
        cell.setTargets(targetVolume, targetSurface);
        assertEquals(targetVolume, cell.getTargetVolume(), EPSILON);
        assertEquals(targetSurface, cell.getTargetSurface(), EPSILON);
    }
    
    @Test
    public void setTargets_withRegions_updateValues() {
        double targetVolume = randomDoubleBetween(0, 10);
        double targetSurface = randomDoubleBetween(0, 10);
        PottsCell cell = make(true);
        cell.setTargets(Region.NUCLEUS, targetVolume, targetSurface);
        
        assertEquals(targetVolume, cell.getTargetVolume(Region.NUCLEUS), EPSILON);
        assertEquals(targetSurface, cell.getTargetSurface(Region.NUCLEUS), EPSILON);
        assertEquals(0, cell.getTargetVolume(Region.UNDEFINED), EPSILON);
        assertEquals(0, cell.getTargetSurface(Region.UNDEFINED), EPSILON);
        assertEquals(0, cell.getTargetVolume(Region.DEFAULT), EPSILON);
        assertEquals(0, cell.getTargetSurface(Region.DEFAULT), EPSILON);
    }
    
    @Test
    public void updateTarget_noScale_doesNothing() {
        double scale = 1.0;
        double rate = randomDoubleBetween(0, 100);
        
        PottsCell cell = make(false);
        cell.initialize(null, null);
        cell.updateTarget(rate, scale);
        
        assertEquals(locationVolume, cell.getTargetVolume(), EPSILON);
        assertEquals(locationSurface, cell.getTargetSurface(), EPSILON);
    }
    
    @Test
    public void updateTarget_increaseScaleNoRegion_updatesValues() {
        double scale = randomDoubleBetween(1 + OFFSET, 2);
        double rate = randomDoubleBetween(0, scale - 1) * cellCriticalVolume;
        
        PottsCell cell = make(false);
        cell.reset(null, null);
        cell.updateTarget(rate, scale);
        
        double targetVolume = cellCriticalVolume + rate;
        assertEquals(targetVolume, cell.getTargetVolume(), EPSILON);
        
        double targetSurface = cellCriticalHeight * targetVolume;
        assertEquals(targetSurface, cell.getTargetSurface(), EPSILON);
    }
    
    @Test
    public void updateTarget_increaseScaleNoRegionWithThreshold_updatesValues() {
        double scale = randomDoubleBetween(1 + OFFSET, 2);
        double rate = randomDoubleBetween(scale - 1, scale) * cellCriticalVolume;
        
        PottsCell cell = make(false);
        cell.reset(null, null);
        cell.updateTarget(rate, scale);
        
        double targetVolume = scale * cellCriticalVolume;
        assertEquals(targetVolume, cell.getTargetVolume(), EPSILON);
        
        double targetSurface = cellCriticalHeight * targetVolume;
        assertEquals(targetSurface, cell.getTargetSurface(), EPSILON);
    }
    
    @Test
    public void updateTarget_increaseScaleWithRegions_updatesValues() {
        double scale = randomDoubleBetween(1 + OFFSET, 2);
        double rate = randomDoubleBetween(0, scale - 1) * cellCriticalVolume;
        
        PottsCell cell = make(true);
        cell.reset(null, null);
        cell.updateTarget(rate, scale);
        
        double targetVolume = cellCriticalVolume + rate;
        assertEquals(targetVolume, cell.getTargetVolume(), EPSILON);
        
        double targetSurface = cellCriticalHeight * targetVolume;
        assertEquals(targetSurface, cell.getTargetSurface(), EPSILON);
        
        double criticalRegionVolume = criticalVolumesRegionMock.get(Region.DEFAULT);
        double targetRegionVolume = criticalRegionVolume - cellCriticalVolume + targetVolume;
        assertEquals(targetRegionVolume, cell.getTargetVolume(Region.DEFAULT), EPSILON);
        
        double criticalRegionHeight = criticalHeightsRegionMock.get(Region.DEFAULT);
        double targetRegionSurface = criticalRegionHeight * targetRegionVolume;
        assertEquals(targetRegionSurface, cell.getTargetSurface(Region.DEFAULT), EPSILON);
    }
    
    @Test
    public void updateTarget_decreaseScaleNoRegion_updatesValues() {
        double scale = randomDoubleBetween(0, 1 - OFFSET);
        double rate = randomDoubleBetween(0, 1 - scale) * cellCriticalVolume;
        
        PottsCell cell = make(false);
        cell.reset(null, null);
        cell.updateTarget(rate, scale);
        
        double targetVolume = cellCriticalVolume - rate;
        assertEquals(targetVolume, cell.getTargetVolume(), EPSILON);
        
        double targetSurface = cellCriticalHeight * targetVolume;
        assertEquals(targetSurface, cell.getTargetSurface(), EPSILON);
    }
    
    @Test
    public void updateTarget_decreaseScaleNoRegionWithThreshold_updatesValues() {
        double scale = randomDoubleBetween(0, 1 - OFFSET);
        double rate = randomDoubleBetween(1 - scale, 2 - scale) * cellCriticalVolume;
        
        PottsCell cell = make(false);
        cell.reset(null, null);
        cell.updateTarget(rate, scale);
        
        double targetVolume = scale * cellCriticalVolume;
        assertEquals(targetVolume, cell.getTargetVolume(), EPSILON);
        
        double targetSurface = cellCriticalHeight * targetVolume;
        assertEquals(targetSurface, cell.getTargetSurface(), EPSILON);
    }
    
    @Test
    public void updateTarget_decreaseScaleWithRegion_updatesValues() {
        double scale = randomDoubleBetween(0, 1 - OFFSET);
        double rate = randomDoubleBetween(0, 1 - scale) * cellCriticalVolume;
        rate = Math.min(rate, locationRegionVolumes.get(Region.DEFAULT));
        
        PottsCell cell = make(true);
        cell.reset(null, null);
        cell.updateTarget(rate, scale);
        
        double targetVolume = cellCriticalVolume - rate;
        assertEquals(targetVolume, cell.getTargetVolume(), EPSILON);
        
        double targetSurface = cellCriticalHeight * targetVolume;
        assertEquals(targetSurface, cell.getTargetSurface(), EPSILON);
        
        double criticalRegionVolume = criticalVolumesRegionMock.get(Region.DEFAULT);
        double targetRegionVolume = criticalRegionVolume - cellCriticalVolume + targetVolume;
        assertEquals(targetRegionVolume, cell.getTargetVolume(Region.DEFAULT), EPSILON);
        
        double criticalRegionHeight = criticalHeightsRegionMock.get(Region.DEFAULT);
        double targetRegionSurface = criticalRegionHeight * targetRegionVolume;
        assertEquals(targetRegionSurface, cell.getTargetSurface(Region.DEFAULT), EPSILON);
    }
    
    @Test
    public void updateTarget_decreaseScaleWithRegionWithThreshold_updatesRate() {
        double threshold = criticalVolumesRegionMock.get(Region.DEFAULT);
        double scale = randomDoubleBetween(0, 1 - threshold / cellCriticalVolume);
        double rate = randomDoubleBetween(threshold, (1 - scale) * cellCriticalVolume);
        
        PottsCell cell = make(true);
        cell.reset(null, null);
        cell.updateTarget(rate, scale);
        
        double targetVolume = cellCriticalVolume - threshold;
        assertEquals(targetVolume, cell.getTargetVolume(), EPSILON);
        
        double targetSurface = cellCriticalHeight * targetVolume;
        assertEquals(targetSurface, cell.getTargetSurface(), EPSILON);
        
        double criticalRegionVolume = criticalVolumesRegionMock.get(Region.DEFAULT);
        double targetRegionVolume = criticalRegionVolume - cellCriticalVolume + targetVolume;
        assertEquals(targetRegionVolume, cell.getTargetVolume(Region.DEFAULT), EPSILON);
        
        double criticalRegionHeight = criticalHeightsRegionMock.get(Region.DEFAULT);
        double targetRegionSurface = criticalRegionHeight * targetRegionVolume;
        assertEquals(targetRegionSurface, cell.getTargetSurface(Region.DEFAULT), EPSILON);
    }
    
    @Test
    public void updateTarget_regionNoScale_doesNothing() {
        double scale = 1.0;
        double rate = randomDoubleBetween(0, 100);
        
        PottsCell cell = make(true);
        cell.initialize(null, null);
        cell.updateTarget(Region.DEFAULT, rate, scale);
        
        assertEquals(locationRegionVolumes.get(Region.DEFAULT), cell.getTargetVolume(Region.DEFAULT), EPSILON);
        assertEquals(locationRegionSurfaces.get(Region.DEFAULT), cell.getTargetSurface(Region.DEFAULT), EPSILON);
    }
    
    @Test
    public void updateTarget_regionIncreaseScale_updatesValues() {
        double locationRegionVolume = criticalVolumesRegionMock.get(Region.DEFAULT);
        double locationRegionHeight = criticalHeightsRegionMock.get(Region.DEFAULT);
        double scale = randomDoubleBetween(1 + OFFSET, 2);
        double rate = randomDoubleBetween(0, scale - 1) * locationRegionVolume;
        
        PottsCell cell = make(true);
        cell.reset(null, null);
        cell.updateTarget(Region.DEFAULT, rate, scale);
        
        double targetRegionVolume = locationRegionVolume + rate;
        assertEquals(targetRegionVolume, cell.getTargetVolume(Region.DEFAULT), EPSILON);
        
        double targetRegionSurface = locationRegionHeight * targetRegionVolume;
        assertEquals(targetRegionSurface, cell.getTargetSurface(Region.DEFAULT), EPSILON);
        
        double targetVolume = cellCriticalVolume + rate;
        assertEquals(targetVolume, cell.getTargetVolume(), EPSILON);
        
        double targetSurface = cellCriticalHeight * targetVolume;
        assertEquals(targetSurface, cell.getTargetSurface(), EPSILON);
    }
    
    @Test
    public void updateTarget_regionIncreaseScaleWithThreshold_updatesValues() {
        double locationRegionVolume = criticalVolumesRegionMock.get(Region.DEFAULT);
        double locationRegionHeight = criticalHeightsRegionMock.get(Region.DEFAULT);
        double scale = randomDoubleBetween(1 + OFFSET, 2);
        double rate = randomDoubleBetween(scale - 1, scale) * locationRegionVolume;
        
        PottsCell cell = make(true);
        cell.reset(null, null);
        cell.updateTarget(Region.DEFAULT, rate, scale);
        
        double targetRegionVolume = scale * locationRegionVolume;
        assertEquals(targetRegionVolume, cell.getTargetVolume(Region.DEFAULT), EPSILON);
        
        double targetRegionSurface = locationRegionHeight * targetRegionVolume;
        assertEquals(targetRegionSurface, cell.getTargetSurface(Region.DEFAULT), EPSILON);
        
        double targetVolume = cellCriticalVolume - locationRegionVolume + targetRegionVolume;
        assertEquals(targetVolume, cell.getTargetVolume(), EPSILON);
        
        double targetSurface = cellCriticalHeight * targetVolume;
        assertEquals(targetSurface, cell.getTargetSurface(), EPSILON);
    }
    
    @Test
    public void updateTarget_regionDecreaseScale_updatesValues() {
        double locationRegionVolume = criticalVolumesRegionMock.get(Region.DEFAULT);
        double locationRegionHeight = criticalHeightsRegionMock.get(Region.DEFAULT);
        double scale = randomDoubleBetween(0, 1 - OFFSET);
        double rate = randomDoubleBetween(0, 1 - scale) * locationRegionVolume;
        
        PottsCell cell = make(true);
        cell.reset(null, null);
        cell.updateTarget(Region.DEFAULT, rate, scale);
        
        double targetRegionVolume = locationRegionVolume - rate;
        assertEquals(targetRegionVolume, cell.getTargetVolume(Region.DEFAULT), EPSILON);
        
        double targetRegionSurface = locationRegionHeight * targetRegionVolume;
        assertEquals(targetRegionSurface, cell.getTargetSurface(Region.DEFAULT), EPSILON);
        
        double targetVolume = cellCriticalVolume - rate;
        assertEquals(targetVolume, cell.getTargetVolume(), EPSILON);
        
        double targetSurface = cellCriticalHeight * targetVolume;
        assertEquals(targetSurface, cell.getTargetSurface(), EPSILON);
    }
    
    @Test
    public void updateTarget_regionDecreaseScaleWithThreshold_updatesValues() {
        double locationRegionVolume = criticalVolumesRegionMock.get(Region.DEFAULT);
        double locationRegionHeight = criticalHeightsRegionMock.get(Region.DEFAULT);
        double scale = randomDoubleBetween(0, 1 - OFFSET);
        double rate = randomDoubleBetween(1 - scale, 2 - scale) * locationRegionVolume;
        
        PottsCell cell = make(true);
        cell.reset(null, null);
        cell.updateTarget(Region.DEFAULT, rate, scale);
        
        double targetRegionVolume = scale * locationRegionVolume;
        assertEquals(targetRegionVolume, cell.getTargetVolume(Region.DEFAULT), EPSILON);
        
        double targetRegionSurface = locationRegionHeight * targetRegionVolume;
        assertEquals(targetRegionSurface, cell.getTargetSurface(Region.DEFAULT), EPSILON);
        
        double targetVolume = cellCriticalVolume - locationRegionVolume + targetRegionVolume;
        assertEquals(targetVolume, cell.getTargetVolume(), EPSILON);
        
        double targetSurface = cellCriticalHeight * targetVolume;
        assertEquals(targetSurface, cell.getTargetSurface(), EPSILON);
    }
    
    @Test
    public void convert_noRegions_createsContainer() {
        Location location = mock(PottsLocation.class);
        MiniBox parameters = mock(MiniBox.class);
        
        int id = randomIntBetween(1, 10);
        int parent = randomIntBetween(1, 10);
        int pop = randomIntBetween(1, 10);
        int age = randomIntBetween(1, 100);
        int divisions = randomIntBetween(1, 100);
        State state = State.random(RANDOM);
        Phase phase = Phase.random(RANDOM);
        double criticalVolume = randomDoubleBetween(10, 100);
        double criticalHeight = randomDoubleBetween(10, 100);
        
        PottsCell cell = new PottsCell(id, parent, pop, state, age, divisions, location,
                false, parameters, criticalVolume, criticalHeight,
                null, null);
        ((PottsModule) cell.getModule()).setPhase(phase);
        
        int voxels = randomIntBetween(1, 100);
        doReturn(voxels).when(location).getVolume();
        
        int targetVolume = randomIntBetween(1, 100);
        int targetSurface = randomIntBetween(1, 100);
        cell.setTargets(targetVolume, targetSurface);
        
        PottsCellContainer container = (PottsCellContainer) cell.convert();
        
        assertEquals(id, container.id);
        assertEquals(parent, container.parent);
        assertEquals(pop, container.pop);
        assertEquals(age, container.age);
        assertEquals(divisions, container.divisions);
        assertEquals(state, container.state);
        assertEquals(phase, container.phase);
        assertEquals(voxels, container.voxels);
        assertNull(container.regionVoxels);
        assertEquals(targetVolume, container.targetVolume, EPSILON);
        assertEquals(targetSurface, container.targetSurface, EPSILON);
        assertNull(container.regionTargetVolume);
        assertNull(container.regionTargetSurface);
    }
    
    @Test
    public void convert_withRegions_createsContainer() {
        Location location = mock(PottsLocation.class);
        MiniBox parameters = mock(MiniBox.class);
        
        int id = randomIntBetween(1, 10);
        int parent = randomIntBetween(1, 10);
        int pop = randomIntBetween(1, 10);
        int age = randomIntBetween(1, 100);
        int divisions = randomIntBetween(1, 100);
        State state = State.random(RANDOM);
        Phase phase = Phase.random(RANDOM);
        double criticalVolume = randomDoubleBetween(10, 100);
        double criticalHeight = randomDoubleBetween(10, 100);
        
        EnumSet<Region> regions = EnumSet.of(Region.NUCLEUS, Region.UNDEFINED);
        doReturn(regions).when(location).getRegions();
        
        EnumMap<Region, Double> criticalVolumesRegion = makeRegionEnumMap();
        EnumMap<Region, Double> criticalHeightsRegion = makeRegionEnumMap();
        
        PottsCell cell = new PottsCell(id, parent, pop, state, age, divisions, location, true,
                parameters, criticalVolume, criticalHeight,
                criticalVolumesRegion, criticalHeightsRegion);
        ((PottsModule) cell.getModule()).setPhase(phase);
        
        int voxels = randomIntBetween(1, 100);
        doReturn(voxels).when(location).getVolume();
        
        EnumMap<Region, Integer> regionVoxels = new EnumMap<>(Region.class);
        for (Region region : regions) {
            int value = randomIntBetween(1, 100);
            regionVoxels.put(region, value);
            doReturn(value).when(location).getVolume(region);
        }
        
        int targetVolume = randomIntBetween(1, 100);
        int targetSurface = randomIntBetween(1, 100);
        cell.setTargets(targetVolume, targetSurface);
        
        EnumMap<Region, Integer> regionTargetVolume = new EnumMap<>(Region.class);
        EnumMap<Region, Integer> regionTargetSurface = new EnumMap<>(Region.class);
        for (Region region : regions) {
            int volume = randomIntBetween(1, 100);
            int surface = randomIntBetween(1, 100);
            regionTargetVolume.put(region, volume);
            regionTargetSurface.put(region, surface);
            cell.setTargets(region, volume, surface);
        }
        
        PottsCellContainer container = (PottsCellContainer) cell.convert();
        
        assertEquals(id, container.id);
        assertEquals(parent, container.parent);
        assertEquals(pop, container.pop);
        assertEquals(age, container.age);
        assertEquals(divisions, container.divisions);
        assertEquals(state, container.state);
        assertEquals(phase, container.phase);
        assertEquals(voxels, container.voxels);
        assertEquals(targetVolume, container.targetVolume, EPSILON);
        assertEquals(targetSurface, container.targetSurface, EPSILON);
        
        for (Region region : regions) {
            assertEquals(regionVoxels.get(region), container.regionVoxels.get(region));
            assertEquals(regionTargetVolume.get(region), container.regionTargetVolume.get(region), EPSILON);
            assertEquals(regionTargetSurface.get(region), container.regionTargetSurface.get(region), EPSILON);
        }
    }
}
