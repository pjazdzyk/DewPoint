package com.synerset.hvacengine.common;

/**
 * Interface that represents an object whose output can be printed to the console.
 * <p>
 * Implementing classes must define the {@link #toConsoleOutput()} method to return
 * a string representation of the object's data in a format suitable for console output.
 */
public interface ConsolePrintable {

    /**
     * Converts the object to a string format that can be printed to the console.
     *
     * @return A string representation of the object suitable for console output.
     */
    String toConsoleOutput();
}
