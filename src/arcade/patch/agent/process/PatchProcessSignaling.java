package arcade.patch.agent.process;

import java.util.List;
import arcade.patch.agent.cell.PatchCell;

/**
 * Implementation of {@link java.lang.Process} for cell signaling.
 * <p>
 * The {@code PatchProcessSignaling} module can be used for networks comprising
 * a system of ODEs.
 */

public abstract class PatchProcessSignaling extends PatchProcess {
    /** Molecules in nM */
    static final double MOLEC_TO_NM = 1355.0;
    
    /** Molecular weight of TGFa [g/mol] */
    static final double TGFA_MW = 17006.0;
    
    /** Step size for module (in seconds) */
    static final double STEP_SIZE = 1.0;
    
    /** List of internal names */
    List<String> names;
    
    /** List of internal concentrations */
    double[] concs;
    
    /**
     * Creates a signaling {@link PatchProcess} for the given cell.
     *
     * @param cell  the {@link PatchCell} the process is associated with
     */
    PatchProcessSignaling(PatchCell cell) {
        super(cell);
    }
}
