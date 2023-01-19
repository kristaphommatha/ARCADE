package arcade.core.util;

import ec.util.MersenneTwisterFast;

/**
 * Container class for enums.
 * <p>
 * Implemented enums include:
 * <ul>
 *     <li>{@code State} defining cell states</li>
 *     <li>{@code Region} defining subcellular regions</li>
 * </ul>
 * <p>
 * Implementations are not required to use all values of an enum but should make
 * sure to account for unused values.
 */

public final class Enums {
    /**
     * Hidden utility class constructor.
     */
    protected Enums() {
        throw new UnsupportedOperationException();
    }
    
    /** Cell state codes. */
    public enum State {
        /** Code for undefined state. */
        UNDEFINED,
        
        /** Code for quiescent cells. */
        QUIESCENT,
        
        /** Code for proliferative cells. */
        PROLIFERATIVE,
        
        /** Code for migratory cells. */
        MIGRATORY,
        
        /** Code for apoptotic cells. */
        APOPTOTIC,
        
        /** Code for necrotic cells. */
        NECROTIC,
        
        /** Code for senescent cells. */
        SENESCENT,
        
        /** Code for autotic cells. */
        AUTOTIC;
        
        /**
         * Randomly selects a {@code State}.
         *
         * @param rng  the random number generator
         * @return  a random {@code State}
         */
        public static State random(MersenneTwisterFast rng) {
            return values()[rng.nextInt(values().length - 1) + 1];
        }
    }
    
    /** Cell region codes. */
    public enum Region {
        /** Undefined region. */
        UNDEFINED,
        
        /** Region for cytoplasm. */
        DEFAULT,
        
        /** Region for nucleus. */
        NUCLEUS;
        
        /**
         * Randomly selects a {@code Region}.
         *
         * @param rng  the random number generator
         * @return  a random {@code Region}
         */
        public static Region random(MersenneTwisterFast rng) {
            return values()[rng.nextInt(values().length - 1) + 1];
        }
    }
    
    /** Process domain codes. */
    public enum Domain {
        /** Code for undefined domain. */
        UNDEFINED,
        
        /** Code for metabolism domain. */
        METABOLISM,
        
        /** Code for signaling domain. */
        SIGNALING;
        
        /**
         * Randomly selects a {@code Domain}.
         *
         * @param rng  the random number generator
         * @return  a random {@code Domain}
         */
        public static Domain random(MersenneTwisterFast rng) {
            return values()[rng.nextInt(values().length - 1) + 1];
        }
    }
    
    /** Operation category codes. */
    public enum Category {
        /** Code for undefined category. */
        UNDEFINED,
        
        /** Code for metabolism category. */
        DIFFUSER,
        
        /** Code for signaling category. */
        GENERATOR;
        
        /**
         * Randomly selects a {@code Category}.
         *
         * @param rng  the random number generator
         * @return  a random {@code Operation}
         */
        public static Category random(MersenneTwisterFast rng) {
            return values()[rng.nextInt(values().length - 1) + 1];
        }
    }
}
