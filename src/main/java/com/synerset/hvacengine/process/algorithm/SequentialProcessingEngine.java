package com.synerset.hvacengine.process.algorithm;

import com.synerset.hvacengine.common.ConsolePrintable;
import com.synerset.hvacengine.common.exception.HvacEngineArgumentException;
import com.synerset.hvacengine.process.AirFlowProcessBlock;
import com.synerset.hvacengine.process.ProcessResult;
import com.synerset.hvacengine.process.ProcessType;
import com.synerset.hvacengine.process.source.SimpleDataSource;
import com.synerset.hvacengine.property.fluids.humidair.FlowOfHumidAir;

import java.util.*;

/**
 * A class that manages and executes a sequence of HVAC process blocks, computes their results, and provides
 * functionality to retrieve and output those results. <p>
 * This engine operates sequentially, meaning each process block runs after the previous one has completed.
 * It can connect a flow of humid air data source to the first process block, and it processes the sequence
 * of blocks while storing the results of each block. <p>
 * The algorithm ensures that added blocks are properly connected to each other so the output of one block is
 * fed as the input to the next block.
 */
public class SequentialProcessingEngine implements ConsolePrintable {

    private final List<AirFlowProcessBlock> processBlocksToCompute;
    private final List<ProcessResult> processResults;
    private SimpleDataSource<FlowOfHumidAir> airFlowSource;

    /**
     * Constructs a new empty sequential processing engine.
     */
    public SequentialProcessingEngine() {
        this.processResults = new ArrayList<>();
        this.processBlocksToCompute = new ArrayList<>();
    }

    /**
     * Adds a new process node (block) to the processing engine.
     * If the engine already contains previous process nodes, the output of the last node will be connected
     * to the input of the new node.
     *
     * @param processNode The {@link AirFlowProcessBlock} to be added to the sequence.
     * @return The index at which the process node was added, or -1 if the node is null.
     */
    public int addProcessNode(AirFlowProcessBlock processNode) {
        if (processNode == null) {
            return -1;
        }
        if (!processBlocksToCompute.isEmpty()) {
            AirFlowProcessBlock previousNode = processBlocksToCompute.get(processBlocksToCompute.size() - 1);
            processNode.getInputConnector().connectAndConsumeDataFrom(previousNode.getOutputConnector());
        }
        processBlocksToCompute.add(processNode);
        return processBlocksToCompute.size() - 1;
    }

    /**
     * Runs the calculations for all process blocks in the engine.
     * Ensures that the necessary airflow data is available and connected before starting the calculations.
     *
     * @return The final {@link ProcessResult} after all calculations have been completed.
     * @throws HvacEngineArgumentException if there are no process blocks or if airflow data is missing.
     */
    public ProcessResult runCalculationsForAllNodes() {
        if (processBlocksToCompute.isEmpty()) {
            throw new HvacEngineArgumentException("No process found. Cannot run calculations");
        }

        AirFlowProcessBlock firstProcessBlock = processBlocksToCompute.get(0);

        if (airFlowSource != null) {
            firstProcessBlock.connectAirFlowDataSource(airFlowSource);
        }

        if (firstProcessBlock.getInputConnector().getConnectorData() == null) {
            throw new HvacEngineArgumentException("No inlet airflow data found. Cannot run calculations");
        }

        processBlocksToCompute.forEach(node -> {
            node.runProcessCalculations();
            processResults.add(node.getProcessResult());
        });

        return getLastResult();
    }

    /**
     * Retrieves all the process results collected during the execution.
     *
     * @return An unmodifiable list of {@link ProcessResult} instances.
     */
    public List<ProcessResult> getProcessResults() {
        return Collections.unmodifiableList(processResults);
    }

    /**
     * Retrieves the process results filtered by the specified process type.
     *
     * @param processType The {@link ProcessType} to filter by.
     * @return A list of {@link ProcessResult} instances that match the specified process type.
     */
    public List<ProcessResult> getResults(ProcessType processType) {
        return processResults.stream().filter(result -> result.processType() == processType).toList();
    }

    /**
     * Retrieves the last process result in the sequence.
     *
     * @return The final {@link ProcessResult} from the process block sequence.
     */
    public ProcessResult getLastResult() {
        return processResults.get(processResults.size() - 1);
    }

    /**
     * Retrieves all the process blocks that have been added to the engine.
     *
     * @return An unmodifiable list of {@link AirFlowProcessBlock} instances.
     */
    public List<AirFlowProcessBlock> getAllProcessBlocks() {
        return Collections.unmodifiableList(processBlocksToCompute);
    }

    /**
     * Converts the engine's results into a string format suitable for console output.
     *
     * @return A string containing all process results formatted for console output.
     */
    @Override
    public String toConsoleOutput() {
        StringBuilder stringBuilder = new StringBuilder();
        processResults.forEach(result -> stringBuilder.append(result.toConsoleOutput()).append("\n"));
        return stringBuilder.toString();
    }

    /**
     * Converts the last process block's result into a string format for console output.
     *
     * @return A string containing the output of the last process block's result, or a message indicating
     * that results are not available.
     */
    public String toConsoleOutputLastResult() {
        return getLastProcessBlock()
                .map(ProcessResult::toConsoleOutput)
                .orElse("Results not available");
    }

    /**
     * Connects the data source for the airflow to the engine. This will be used by the first process block
     * in the sequence.
     *
     * @param airFlowSource The {@link SimpleDataSource} containing {@link FlowOfHumidAir} data to be used.
     */
    public void connectInletAirFlowDataSource(SimpleDataSource<FlowOfHumidAir> airFlowSource) {
        this.airFlowSource = airFlowSource;
    }

    // Helper Methods

    /**
     * Retrieves the last process block result, if available.
     *
     * @return An {@link Optional} containing the last {@link ProcessResult}, or empty if no blocks have been processed.
     */
    private Optional<ProcessResult> getLastProcessBlock() {
        if (processBlocksToCompute.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(processResults.get(processResults.size() - 1));
    }

    // Static factory methods

    /**
     * Static factory method to create an instance of {@link SequentialProcessingEngine}.
     *
     * @return A new instance of {@link SequentialProcessingEngine}.
     */
    public static SequentialProcessingEngine of() {
        return new SequentialProcessingEngine();
    }

    /**
     * Static factory method to create an instance of {@link SequentialProcessingEngine} with a connected
     * airflow data source.
     *
     * @param airFlowSource The {@link SimpleDataSource} containing airflow data.
     * @return A new instance of {@link SequentialProcessingEngine} with the specified data source.
     */
    public static SequentialProcessingEngine of(SimpleDataSource<FlowOfHumidAir> airFlowSource) {
        SequentialProcessingEngine sequentialProcessingEngine = new SequentialProcessingEngine();
        sequentialProcessingEngine.connectInletAirFlowDataSource(airFlowSource);
        return sequentialProcessingEngine;
    }

    /**
     * Static factory method to create an instance of {@link SequentialProcessingEngine} with a list of process nodes.
     *
     * @param processNodes The {@link AirFlowProcessBlock} nodes to be added to the processing engine.
     * @return A new instance of {@link SequentialProcessingEngine} with the specified nodes.
     */
    public static SequentialProcessingEngine of(AirFlowProcessBlock... processNodes) {
        SequentialProcessingEngine sequentialProcessingEngine = new SequentialProcessingEngine();
        Arrays.stream(processNodes).forEach(sequentialProcessingEngine::addProcessNode);
        return sequentialProcessingEngine;
    }

    /**
     * Static factory method to create an instance of {@link SequentialProcessingEngine} with both a connected
     * airflow data source and a list of process nodes.
     *
     * @param airFlowSource The {@link SimpleDataSource} containing airflow data.
     * @param processNodes  The {@link AirFlowProcessBlock} nodes to be added to the processing engine.
     * @return A new instance of {@link SequentialProcessingEngine} with the specified data source and nodes.
     */
    public static SequentialProcessingEngine of(SimpleDataSource<FlowOfHumidAir> airFlowSource, AirFlowProcessBlock... processNodes) {
        SequentialProcessingEngine sequentialProcessingEngine = new SequentialProcessingEngine();
        sequentialProcessingEngine.connectInletAirFlowDataSource(airFlowSource);
        Arrays.stream(processNodes).forEach(sequentialProcessingEngine::addProcessNode);
        return sequentialProcessingEngine;
    }
}
