package com.synerset.hvacengine.process.mixing;

import com.synerset.hvacengine.common.validation.CommonValidators;
import com.synerset.hvacengine.hydraulic.dataobject.HydraulicLossResult;
import com.synerset.hvacengine.process.AirFlowProcessBlock;
import com.synerset.hvacengine.process.ProcessType;
import com.synerset.hvacengine.process.blockmodel.ConnectorInput;
import com.synerset.hvacengine.process.blockmodel.ConnectorOutput;
import com.synerset.hvacengine.process.blockmodel.OutputConnection;
import com.synerset.hvacengine.process.mixing.dataobject.MixingResult;
import com.synerset.hvacengine.process.pressurechange.PressureChangeEquations;
import com.synerset.hvacengine.process.pressurechange.dataobject.PressureChangeResult;
import com.synerset.hvacengine.property.fluids.humidair.FlowOfHumidAir;
import com.synerset.unitility.unitsystem.thermodynamic.Pressure;

import java.util.ArrayList;
import java.util.List;

public class Mixing implements AirFlowProcessBlock {

    private static final ProcessType PROCESS_TYPE = ProcessType.MIXING;
    private final ConnectorInput<FlowOfHumidAir> inputAirFlowConnector;
    private final ConnectorOutput<FlowOfHumidAir> outputAirFlowConnector;
    private final List<ConnectorInput<FlowOfHumidAir>> inputMixingFlowConnectors;
    private MixingResult processResult;
    private MixingMode mixingMode;
    private HydraulicLossResult hydraulicResults;

    public Mixing() {
        this.inputAirFlowConnector = ConnectorInput.of(FlowOfHumidAir.class);
        this.inputMixingFlowConnectors = new ArrayList<>();
        this.outputAirFlowConnector = ConnectorOutput.of(FlowOfHumidAir.class);
        this.hydraulicResults = HydraulicLossResult.createEmpty();
    }

    public Mixing(OutputConnection<FlowOfHumidAir> blockWithAirFlow,
                  List<? extends OutputConnection<FlowOfHumidAir>> blocksWithInputMixingAirFlows,
                  Pressure coilPressureLoss) {

        this();
        CommonValidators.requireNotNull(blockWithAirFlow);
        CommonValidators.requireNotNull(blocksWithInputMixingAirFlows);
        this.inputAirFlowConnector.connectAndConsumeDataFrom(blockWithAirFlow.getOutputConnector());
        blocksWithInputMixingAirFlows.forEach(blockWithFlowOutput -> inputMixingFlowConnectors.add(
                ConnectorInput.of(blockWithFlowOutput.getOutputConnector()))
        );
        this.hydraulicResults = HydraulicLossResult.builder().withLocalPressureLoss(coilPressureLoss).build();
    }

    @Override
    public MixingResult runProcessCalculations() {
        inputAirFlowConnector.updateConnectorData();
        inputMixingFlowConnectors.forEach(ConnectorInput::updateConnectorData);
        List<FlowOfHumidAir> recirculationFlows = inputMixingFlowConnectors.stream()
                .map(ConnectorInput::getConnectorData)
                .toList();

        FlowOfHumidAir inletAirFlow = inputAirFlowConnector.getConnectorData();

        MixingResult mixingProcessResults;

        if (recirculationFlows.isEmpty()) {
            mixingMode = MixingMode.SIMPLE_MIXING;
            mixingProcessResults = MixingResult.builder()
                    .processMode(mixingMode)
                    .inletAirFlow(inletAirFlow)
                    .outletAirFlow(inletAirFlow)
                    .recirculationFlows(List.of())
                    .build();
        } else if (recirculationFlows.size() == 1) {
            mixingProcessResults = MixingEquations.mixingOfTwoAirFlows(inletAirFlow, recirculationFlows.get(0));
            mixingMode = MixingMode.SIMPLE_MIXING;
        } else {
            mixingProcessResults = MixingEquations.mixingOfMultipleFlows(inletAirFlow, recirculationFlows);
            mixingMode = MixingMode.MULTIPLE_MIXING;
        }

        Pressure pressureLoss = hydraulicResults.totalPressureLoss();
        PressureChangeResult pressureChangeResult = PressureChangeEquations.pressureDropDueFriction(mixingProcessResults.outletAirFlow(), pressureLoss);

        outputAirFlowConnector.setConnectorData(pressureChangeResult.outletAirFlow());
        this.processResult = mixingProcessResults;

        return mixingProcessResults;
    }

    @Override
    public MixingResult getProcessResult() {
        return processResult;
    }

    @Override
    public ProcessType getProcessType() {
        return PROCESS_TYPE;
    }

    public MixingMode getProcessMode() {
        return mixingMode;
    }

    @Override
    public ConnectorInput<FlowOfHumidAir> getInputConnector() {
        return inputAirFlowConnector;
    }

    @Override
    public ConnectorOutput<FlowOfHumidAir> getOutputConnector() {
        return outputAirFlowConnector;
    }

    // Methods specific for this class
    public List<ConnectorInput<FlowOfHumidAir>> getInputMixingFlowConnectors() {
        return inputMixingFlowConnectors;
    }

    public List<FlowOfHumidAir> getUnwrappedMixingFlows() {
        return inputMixingFlowConnectors.stream()
                .map(ConnectorInput::getConnectorData)
                .toList();
    }

    public void resetMixingFlows() {
        inputMixingFlowConnectors.clear();
    }

    public void connectMixingFlowDataSources(List<? extends OutputConnection<FlowOfHumidAir>> blocksWithInputMixingAirFlows) {
        CommonValidators.requireNotNull(blocksWithInputMixingAirFlows);
        resetMixingFlows();
        blocksWithInputMixingAirFlows.forEach(blockWithFlowOutput -> inputMixingFlowConnectors.add(
                ConnectorInput.of(blockWithFlowOutput.getOutputConnector()))
        );
    }

    public void connectMixingFlowDataSource(OutputConnection<FlowOfHumidAir> blockWithInputMixingAirFlow) {
        CommonValidators.requireNotNull(blockWithInputMixingAirFlow);
        resetMixingFlows();
        inputMixingFlowConnectors.add(ConnectorInput.of(blockWithInputMixingAirFlow.getOutputConnector()));
    }

    public void addMixingFlowDataSource(OutputConnection<FlowOfHumidAir> blockWithInputMixingAirFlow) {
        CommonValidators.requireNotNull(blockWithInputMixingAirFlow);
        inputMixingFlowConnectors.add(ConnectorInput.of(blockWithInputMixingAirFlow.getOutputConnector()));
    }

    // Static factory methods
    public static Mixing of() {
        return new Mixing();
    }

    public static Mixing of(List<? extends OutputConnection<FlowOfHumidAir>> blocksWithInputMixingAirFlows) {
        Mixing mixing = new Mixing();
        mixing.connectMixingFlowDataSources(blocksWithInputMixingAirFlows);
        return mixing;
    }

    public static Mixing of(OutputConnection<FlowOfHumidAir> blockWithInputMixingAirFlow) {
        Mixing mixing = new Mixing();
        mixing.connectMixingFlowDataSources(List.of(blockWithInputMixingAirFlow));
        return mixing;
    }

    public static Mixing of(OutputConnection<FlowOfHumidAir> blockWithAirFlow,
                            List<? extends OutputConnection<FlowOfHumidAir>> blocksWithInputMixingAirFlows,
                            Pressure coilPressureLoss) {

        return new Mixing(blockWithAirFlow, blocksWithInputMixingAirFlows, coilPressureLoss);
    }

    public static Mixing of(OutputConnection<FlowOfHumidAir> blockWithAirFlow,
                            List<? extends OutputConnection<FlowOfHumidAir>> blocksWithInputMixingAirFlows) {

        return new Mixing(blockWithAirFlow, blocksWithInputMixingAirFlows, Pressure.ofPascal(0));
    }

    public static Mixing of(OutputConnection<FlowOfHumidAir> blockWithAirFlow,
                            OutputConnection<FlowOfHumidAir> blockWithInputMixingAirFlow,
                            Pressure coilPressureLoss) {

        return new Mixing(blockWithAirFlow, List.of(blockWithInputMixingAirFlow), coilPressureLoss);
    }

    public static Mixing of(OutputConnection<FlowOfHumidAir> blockWithAirFlow,
                            OutputConnection<FlowOfHumidAir> blockWithInputMixingAirFlow) {

        return new Mixing(blockWithAirFlow, List.of(blockWithInputMixingAirFlow), Pressure.ofPascal(0));
    }

    @Override
    public HydraulicLossResult getHydraulicLossResult() {
        return hydraulicResults;
    }

}