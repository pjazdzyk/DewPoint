package com.synerset.hvacengine.process.blockmodel;

import com.synerset.hvacengine.common.exception.HvacEngineMissingArgumentException;
import com.synerset.hvacengine.common.validation.CommonValidators;

/**
 * Interface representing a connection that provides output data of type K.
 *
 * @param <K> The type of data that is transferred through the connection.
 */
public interface OutputConnection<K> {

    /**
     * Retrieves the output connector associated with this object.
     *
     * @return The {@link ConnectorOutput} instance representing the output connector.
     */
    ConnectorOutput<K> getOutputConnector();

    /**
     * Connects the output of this object to the input of another process, represented by
     * an {@link InputConnection}.
     * <p>
     * This method will ensure that the input connection is not null using validation.
     * It then establishes a connection between the output connector of this object
     * and the input connector of the target.
     *
     * @param processWithInput The target {@link InputConnection} to which the output should be connected.
     * @throws HvacEngineMissingArgumentException if the provided {@code processWithInput} is null.
     */
    default void connectThisOutputToTargetInput(InputConnection<K> processWithInput) {
        CommonValidators.requireNotNull(processWithInput);
        ConnectorInput<K> targetInputConnector = processWithInput.getInputConnector();
        targetInputConnector.connectAndConsumeDataFrom(getOutputConnector());
    }
}
