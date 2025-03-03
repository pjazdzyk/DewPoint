package com.synerset.hvacengine.hydraulic;

import com.synerset.hvacengine.common.validation.CommonValidators;
import com.synerset.hvacengine.hydraulic.dataobject.HydraulicLossResult;
import com.synerset.hvacengine.hydraulic.dataobject.HydraulicProcessResult;
import com.synerset.hvacengine.hydraulic.structure.ConduitStructure;
import com.synerset.hvacengine.process.AirFlowProcessBlock;
import com.synerset.hvacengine.process.ProcessResult;
import com.synerset.hvacengine.process.ProcessType;
import com.synerset.hvacengine.process.blockmodel.ConnectorInput;
import com.synerset.hvacengine.process.blockmodel.ConnectorOutput;
import com.synerset.hvacengine.process.blockmodel.OutputConnection;
import com.synerset.hvacengine.property.fluids.humidair.FlowOfHumidAir;
import com.synerset.hvacengine.property.fluids.humidair.HumidAir;
import com.synerset.unitility.unitsystem.common.Length;
import com.synerset.unitility.unitsystem.thermodynamic.Power;
import com.synerset.unitility.unitsystem.thermodynamic.Pressure;

/**
 * The AirFlowDuctBlock class models a component in a HVAC system responsible for handling
 * the airflow through a duct. This block performs hydraulic calculations based on the
 * characteristics of the conduit and the airflow, including the pressure losses and flow adjustments.
 * This component represents process type of pressure change. Outlet flow pressure will be reduced by
 * total pressure loss generated in this component.
 * <p>
 * The class implements the {@link AirFlowProcessBlock} interface and provides methods for
 * running calculations to determine the resulting airflow and pressure changes as it travels
 * through a conduit. It also provides connectors for input and output airflow, allowing
 * integration with other components in the system.
 * <p>
 * This class supports both standalone operation and integration with other components via
 * input/output connections.
 * <p>
 * Example usage:
 * <pre>
 * ConduitStructure conduitStructure = ...;
 * Length conduitLength = ...;
 * AirFlowDuctBlock ductBlock = new AirFlowDuctBlock(conduitStructure, conduitLength);
 * ProcessResult result = ductBlock.runProcessCalculations();
 * </pre>
 *
 * @see AirFlowProcessBlock
 * @see HydraulicLossResult
 * @see HydraulicProcessResult
 * @see ConduitStructure
 * @see FlowOfHumidAir
 */
public class AirFlowDuctBlock implements AirFlowProcessBlock {

    private final ConnectorInput<FlowOfHumidAir> inputAirFlowConnector;
    private final ConnectorOutput<FlowOfHumidAir> outputAirFlowConnector;
    private final ConduitStructure conduitStructure;
    private final Length conduitLength;
    private final LocalLossInputData localLossInputData;
    private HydraulicLossResult hydraulicLossResults;
    private HydraulicProcessResult hydraulicProcessResult;

    public AirFlowDuctBlock(ConduitStructure conduitStructure, Length conduitLength, LocalLossInputData localLossInputData) {
        CommonValidators.requireNotNull(conduitStructure, "conduitStructure");
        CommonValidators.requireNotNull(conduitLength, "conduitLength");
        this.inputAirFlowConnector = ConnectorInput.of(FlowOfHumidAir.class);
        this.outputAirFlowConnector = ConnectorOutput.of(FlowOfHumidAir.class);
        this.conduitStructure = conduitStructure;
        this.conduitLength = conduitLength;
        this.localLossInputData = localLossInputData;
    }

    public AirFlowDuctBlock(ConduitStructure conduitStructure,
                            Length conduitLength,
                            LocalLossInputData localLossInputData,
                            OutputConnection<FlowOfHumidAir> blockWithAirFlowOutput) {

        this(conduitStructure, conduitLength, localLossInputData);
        CommonValidators.requireNotNull(blockWithAirFlowOutput, "blockWithAirFlowOutput");
        inputAirFlowConnector.connectAndConsumeDataFrom(blockWithAirFlowOutput.getOutputConnector());
    }

    @Override
    public ProcessResult runProcessCalculations() {
        inputAirFlowConnector.updateConnectorData();

        FlowOfHumidAir inletFlowOfHumidAir = inputAirFlowConnector.getConnectorData();

        HydraulicConduit<ConduitStructure, HumidAir> conduitData = HydraulicConduit.<ConduitStructure, HumidAir>builder()
                .flowOfFluid(inletFlowOfHumidAir)
                .length(conduitLength)
                .structure(conduitStructure)
                .build();

        HydraulicConduit.of(conduitStructure, inletFlowOfHumidAir, conduitLength);

        // Assuming, that pressure drops decreases outlet air pressure, resulting in slight RH change.
        Pressure minorHeadLossFromFactors = HydraulicEquations.localPressureLoss(
                localLossInputData.getSumOfAllInputLossCoefs(),
                conduitData.getVelocity(),
                inletFlowOfHumidAir.getDensity()
        );
        Pressure minorHeadLossDirect = localLossInputData.getTotalInputPressureLoss();
        Pressure localPressureLoss = minorHeadLossFromFactors.plus(minorHeadLossDirect);
        Pressure linearPressureLoss = conduitData.getLinearPressureLoss();
        // Determining total pressure loss
        Pressure totalPressureLoss = linearPressureLoss.plus(localPressureLoss);
        // And finally, calculating new pressure at outlet
        Pressure pressureReducedByLosses = inletFlowOfHumidAir.getPressure().minus(totalPressureLoss);

        HumidAir newHumidAir = inletFlowOfHumidAir.getFluid().withPressure(pressureReducedByLosses);
        FlowOfHumidAir adjustedHumidAirFlow = inletFlowOfHumidAir.withHumidAirFixedDryAirMassFlow(newHumidAir);

        HydraulicProcessResult hydraulicResult = HydraulicProcessResult.builder()
                .inletAirFlow(inletFlowOfHumidAir)
                .outletAirFlow(adjustedHumidAirFlow)
                .heatOfProcess(Power.ofWatts(0))
                .build();

        HydraulicLossResult hydraulicLossResult = HydraulicLossResult.builder()
                .withLinearPressureLoss(linearPressureLoss)
                .withLocalPressureLoss(localPressureLoss)
                .build();

        outputAirFlowConnector.setConnectorData(adjustedHumidAirFlow);

        this.hydraulicLossResults = hydraulicLossResult;
        this.hydraulicProcessResult = hydraulicResult;

        return hydraulicProcessResult;
    }

    @Override
    public ProcessResult getProcessResult() {
        return hydraulicProcessResult;
    }

    @Override
    public ProcessType getProcessType() {
        return ProcessType.PRESSURE_CHANGE;
    }

    @Override
    public HydraulicLossResult getHydraulicLossResult() {
        return hydraulicLossResults;
    }

    @Override
    public ConnectorInput<FlowOfHumidAir> getInputConnector() {
        return inputAirFlowConnector;
    }

    @Override
    public ConnectorOutput<FlowOfHumidAir> getOutputConnector() {
        return outputAirFlowConnector;
    }

    // Methods specific for this process
    public ConduitStructure getConduitStructureData() {
        return conduitStructure;
    }

    public Length getConduitLength() {
        return conduitLength;
    }

    public LocalLossInputData getLocalLossInputData() {
        return localLossInputData;
    }

    // Static factory methods
    public static AirFlowDuctBlock of(ConduitStructure conduitStructure, Length conduitLength) {
        return new AirFlowDuctBlock(conduitStructure, conduitLength, LocalLossInputData.createEmpty());
    }

    public static AirFlowDuctBlock of(ConduitStructure conduitStructure, Length conduitLength, LocalLossInputData localLossInputData) {
        return new AirFlowDuctBlock(conduitStructure, conduitLength, localLossInputData);
    }

    public static AirFlowDuctBlock of(ConduitStructure conduitStructure,
                                      Length conduitLength,
                                      OutputConnection<FlowOfHumidAir> blockWithAirFlowOutput) {

        return new AirFlowDuctBlock(conduitStructure, conduitLength, LocalLossInputData.createEmpty(), blockWithAirFlowOutput);
    }

    public static AirFlowDuctBlock of(ConduitStructure conduitStructure,
                                      Length conduitLength,
                                      LocalLossInputData localLossInputData,
                                      OutputConnection<FlowOfHumidAir> blockWithAirFlowOutput) {

        return new AirFlowDuctBlock(conduitStructure, conduitLength, localLossInputData, blockWithAirFlowOutput);
    }

}
