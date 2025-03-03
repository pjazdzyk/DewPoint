package com.synerset.hvacengine.process;

import com.synerset.hvacengine.common.ConsolePrintable;
import com.synerset.hvacengine.common.exception.HvacEngineMissingArgumentException;
import com.synerset.hvacengine.common.validation.CommonValidators;
import com.synerset.hvacengine.hydraulic.HydraulicAware;
import com.synerset.hvacengine.process.blockmodel.OutputConnection;
import com.synerset.hvacengine.process.blockmodel.Processable;
import com.synerset.hvacengine.property.fluids.humidair.FlowOfHumidAir;

/**
 * Represents a block in the HVAC processing pipeline that handles both input and output of
 * airflow data, and performs specific processing to produce a result. <p>
 * This interface extends several key functionalities:
 * <ul>
 *     <li>{@link ConsolePrintable}: Allows the block to produce console output.</li>
 *     <li>{@link Processable}: Defines the ability to run calculations and retrieve results.</li>
 *     <li>{@link HydraulicAware}: Provides access to hydraulic loss calculations.</li>
 *     <li>{@link AirFlowInputConnection}: Allows receiving airflow data from another source.</li>
 *     <li>{@link AirFlowOutputConnection}: Allows sending airflow data to another process.</li>
 * </ul>
 * <p>
 * The {@link AirFlowProcessBlock} serves as a unit of computation in an HVAC system, where
 * airflow data is processed to generate a specific result. Each block is associated with a
 * specific {@link ProcessType}, which defines the type of transformation applied to the data.
 * These blocks can be connected to form a larger processing chain.
 */
public interface AirFlowProcessBlock extends Processable<ProcessResult>, AirFlowInputConnection, AirFlowOutputConnection, HydraulicAware, ConsolePrintable {

    /**
     * Gets the type of process this block performs.
     *
     * @return The {@link ProcessType} associated with this block.
     */
    ProcessType getProcessType();

    /**
     * Connects the output of another block (with airflow data) to this block's input. <p>
     * This method validates the provided output connection, ensuring it is not null,
     * and connects the data output from another block to the input of this block for further processing.
     *
     * @param blockWithAirFlowOutput The output connection of another block that provides airflow data.
     * @throws HvacEngineMissingArgumentException if the provided output connection is null.
     */
    default void connectAirFlowDataSource(OutputConnection<FlowOfHumidAir> blockWithAirFlowOutput) {
        CommonValidators.requireNotNull(blockWithAirFlowOutput);
        getInputConnector().connectAndConsumeDataFrom(blockWithAirFlowOutput.getOutputConnector());
    }

    /**
     * Generates a console-friendly output for this process block. <p>
     * If results are unavailable (i.e., the process has not been executed), a message is returned.
     * Otherwise, the method returns formatted outputs of both the main process and hydraulic loss calculations.
     *
     * @return A string representation of the process results and hydraulic losses.
     */
    @Override
    default String toConsoleOutput() {
        if (getInputConnector().getConnectorData() == null || getProcessResult() == null) {
            return "Results not available. Run process first.";
        }
        return getProcessResult().toConsoleOutput() + "\n" + getHydraulicLossResult().toConsoleOutput();
    }

}
