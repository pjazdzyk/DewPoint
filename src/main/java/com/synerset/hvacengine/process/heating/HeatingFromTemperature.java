package com.synerset.hvacengine.process.heating;

import com.synerset.hvacengine.common.validation.CommonValidators;
import com.synerset.hvacengine.hydraulic.dataobject.HydraulicLossResult;
import com.synerset.hvacengine.process.AirFlowProcessBlock;
import com.synerset.hvacengine.process.ProcessType;
import com.synerset.hvacengine.process.blockmodel.ConnectorInput;
import com.synerset.hvacengine.process.blockmodel.ConnectorOutput;
import com.synerset.hvacengine.process.blockmodel.OutputConnection;
import com.synerset.hvacengine.process.heating.dataobject.HeatingResult;
import com.synerset.hvacengine.process.pressurechange.PressureChangeEquations;
import com.synerset.hvacengine.process.pressurechange.dataobject.PressureChangeResult;
import com.synerset.hvacengine.property.fluids.humidair.FlowOfHumidAir;
import com.synerset.unitility.unitsystem.thermodynamic.Power;
import com.synerset.unitility.unitsystem.thermodynamic.Pressure;
import com.synerset.unitility.unitsystem.thermodynamic.Temperature;

public class HeatingFromTemperature implements AirFlowProcessBlock {

    private static final ProcessType PROCESS_TYPE = ProcessType.HEATING;
    private static final HeatingMode HEATING_MODE = HeatingMode.FROM_TEMPERATURE;
    private final ConnectorInput<FlowOfHumidAir> inputAirFlowConnector;
    private final ConnectorInput<Temperature> targetTemperatureConnector;
    private final ConnectorOutput<FlowOfHumidAir> outputAirFlowConnector;
    private final ConnectorOutput<Power> outputHeatConnector;
    private HeatingResult processResult;
    private HydraulicLossResult hydraulicResults;

    public HeatingFromTemperature() {
        this.inputAirFlowConnector = ConnectorInput.of(FlowOfHumidAir.class);
        this.targetTemperatureConnector = ConnectorInput.of(Temperature.class);
        this.outputHeatConnector = ConnectorOutput.of(Power.class);
        this.outputAirFlowConnector = ConnectorOutput.of(FlowOfHumidAir.class);
        this.hydraulicResults = HydraulicLossResult.createEmpty();
    }

    public HeatingFromTemperature(OutputConnection<FlowOfHumidAir> blockWithAirFlowOutput,
                                  OutputConnection<Temperature> blockWithTemperatureOutput,
                                  Pressure coilPressureLoss) {

        this();
        CommonValidators.requireNotNull(blockWithAirFlowOutput);
        CommonValidators.requireNotNull(blockWithTemperatureOutput);
        this.inputAirFlowConnector.connectAndConsumeDataFrom(blockWithAirFlowOutput.getOutputConnector());
        this.targetTemperatureConnector.connectAndConsumeDataFrom(blockWithTemperatureOutput.getOutputConnector());
        this.outputHeatConnector.setConnectorData(Power.ofWatts(0));
        this.outputAirFlowConnector.setConnectorData(inputAirFlowConnector.getConnectorData());
        this.hydraulicResults = HydraulicLossResult.builder().withLocalPressureLoss(coilPressureLoss).build();
    }

    @Override
    public HeatingResult runProcessCalculations() {
        inputAirFlowConnector.updateConnectorData();
        targetTemperatureConnector.updateConnectorData();
        Temperature targetTemperature = targetTemperatureConnector.getConnectorData();

        FlowOfHumidAir inletAirFlow = inputAirFlowConnector.getConnectorData();
        Pressure pressureLoss = hydraulicResults.totalPressureLoss();
        PressureChangeResult pressureChangeResult = PressureChangeEquations.pressureDropDueFriction(inletAirFlow, pressureLoss);

        // Inlet flow is assigned again, because at the final result we need show inlet air entering block, not intermediate inlet air from pressure change process
        HeatingResult heatingProcessResults = HeatingEquations.heatingFromTargetTemperature(pressureChangeResult.outletAirFlow(), targetTemperature)
                .withInletFlow(inletAirFlow);

        outputAirFlowConnector.setConnectorData(heatingProcessResults.outletAirFlow());
        outputHeatConnector.setConnectorData(heatingProcessResults.heatOfProcess());
        this.processResult = heatingProcessResults;

        return heatingProcessResults;
    }

    @Override
    public HeatingResult getProcessResult() {
        return processResult;
    }

    @Override
    public ProcessType getProcessType() {
        return PROCESS_TYPE;
    }

    public HeatingMode getProcessMode() {
        return HEATING_MODE;
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
    public Temperature getUnwrappedTargetTemperature() {
        return targetTemperatureConnector.getConnectorData();
    }

    public ConnectorInput<Temperature> getTargetTemperatureConnector() {
        return targetTemperatureConnector;
    }

    public void connectTemperatureSource(OutputConnection<Temperature> sourceWithTemperatureOutput) {
        CommonValidators.requireNotNull(sourceWithTemperatureOutput);
        this.targetTemperatureConnector.connectAndConsumeDataFrom(sourceWithTemperatureOutput.getOutputConnector());
    }

    // Static factor methods
    public static HeatingFromTemperature of() {
        return new HeatingFromTemperature();
    }

    public static HeatingFromTemperature of(OutputConnection<Temperature> blockWithTemperatureOutput) {
        CommonValidators.requireNotNull(blockWithTemperatureOutput);
        HeatingFromTemperature heatingFromTemperature = new HeatingFromTemperature();
        heatingFromTemperature.connectTemperatureSource(blockWithTemperatureOutput);
        return heatingFromTemperature;
    }

    public static HeatingFromTemperature of(OutputConnection<FlowOfHumidAir> blockWithAirFlowOutput,
                                            OutputConnection<Temperature> blockWithTemperatureOutput) {

        return new HeatingFromTemperature(blockWithAirFlowOutput, blockWithTemperatureOutput, Pressure.ofPascal(0));
    }

    public static HeatingFromTemperature of(OutputConnection<FlowOfHumidAir> blockWithAirFlowOutput,
                                            OutputConnection<Temperature> blockWithTemperatureOutput,
                                            Pressure coilPressureLoss) {

        return new HeatingFromTemperature(blockWithAirFlowOutput, blockWithTemperatureOutput, coilPressureLoss);
    }

    @Override
    public HydraulicLossResult getHydraulicLossResult() {
        return hydraulicResults;
    }

}