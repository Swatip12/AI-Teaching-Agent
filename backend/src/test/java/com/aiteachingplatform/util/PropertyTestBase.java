package com.aiteachingplatform.util;

import net.java.quickcheck.Generator;
import net.java.quickcheck.QuickCheck;
import net.java.quickcheck.characteristic.AbstractCharacteristic;
import org.junit.jupiter.api.Test;

/**
 * Base class for property-based tests using QuickCheck
 * Provides common utilities and configuration for property testing
 */
public abstract class PropertyTestBase {
    
    protected static final int DEFAULT_TEST_RUNS = 100;
    
    /**
     * Helper method to run property tests with default configuration
     */
    protected void assertProperty(AbstractCharacteristic<?> characteristic) {
        QuickCheck.forAll(DEFAULT_TEST_RUNS, characteristic);
    }
    
    /**
     * Helper method to run property tests with custom number of runs
     */
    protected void assertProperty(int runs, AbstractCharacteristic<?> characteristic) {
        QuickCheck.forAll(runs, characteristic);
    }
    
    @Test
    void propertyTestFrameworkWorks() {
        // Simple test to verify QuickCheck is working
        assertProperty(new AbstractCharacteristic<Integer>() {
            @Override
            protected void doSpecify(Integer integer) throws Throwable {
                // Property: adding zero to any integer returns the same integer
                assert integer + 0 == integer;
            }
        });
    }
}