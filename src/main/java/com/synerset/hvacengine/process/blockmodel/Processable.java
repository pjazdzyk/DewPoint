package com.synerset.hvacengine.process.blockmodel;

/**
 * Interface representing a process that can run calculations and return a result of type K.
 *
 * @param <K> The type of the result produced by the process calculations.
 */
public interface Processable<K> {

    /**
     * Executes the process calculations and returns the result.
     * This method should contain the logic to perform the necessary calculations or operations
     * and return the result of type {@code K}.
     *
     * @return The result of the process calculation.
     */
    K runProcessCalculations();

    /**
     * Retrieves the result of the process after the calculations are performed.
     * This method provides access to the process result after the calculation has been run.
     *
     * @return The result of type {@code K} after the process has been executed.
     */
    K getProcessResult();
}
