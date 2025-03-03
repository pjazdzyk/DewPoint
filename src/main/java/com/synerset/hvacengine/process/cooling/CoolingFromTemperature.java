package com.synerset.hvacengine.process.cooling;

import com.synerset.hvacengine.common.validation.CommonValidators;
import com.synerset.hvacengine.hydraulic.dataobject.HydraulicLossResult;
import com.synerset.hvacengine.process.AirFlowProcessBlock;
import com.synerset.hvacengine.process.ProcessType;
import com.synerset.hvacengine.process.blockmodel.ConnectorInput;
import com.synerset.hvacengine.process.blockmodel.ConnectorOutput;
import com.synerset.hvacengine.process.blockmodel.OutputConnection;
import com.synerset.hvacengine.process.cooling.dataobject.CoolingResult;
import com.synerset.hvacengine.process.pressurechange.PressureChangeEquations;
import com.synerset.hvacengine.process.pressurechange.dataobject.PressureChangeResult;
import com.synerset.hvacengine.property.fluids.humidair.FlowOfHumidAir;
import com.synerset.hvacengine.property.fluids.liquidwater.FlowOfLiquidWater;
import com.synerset.unitility.unitsystem.thermodynamic.Power;
import com.synerset.unitility.unitsystem.thermodynamic.Pressure;
import com.synerset.unitility.unitsystem.thermodynamic.Temperature;

public class CoolingFromTemperature implements AirFlowProcessBlock {

    private static final ProcessType PROCESS_TYPE = ProcessType.COOLING;
    private static final CoolingMode COOLING_MODE = CoolingMode.FROM_TEMPERATURE;
    private final ConnectorInput<FlowOfHumidAir> inputAirFlowConnector;
    private final ConnectorOutput<FlowOfHumidAir> outputAirFlowConnector;
    private final ConnectorInput<CoolantData> coolantDataInputConnector;
    private final ConnectorOutput<FlowOfLiquidWater> outputCondensateConnector;
    private final ConnectorOutput<Power> heatConnector;
    private final ConnectorInput<Temperature> targetTemperatureConnector;
    private CoolingResult processResult;
    private HydraulicLossResult hydraulicResults;

    public CoolingFromTemperature() {
        this.inputAirFlowConnector = ConnectorInput.of(FlowOfHumidAir.class);
        this.outputAirFlowConnector = ConnectorOutput.of(FlowOfHumidAir.class);
        this.coolantDataInputConnector = ConnectorInput.of(CoolantData.class);
        this.outputCondensateConnector = ConnectorOutput.of(FlowOfLiquidWater.class);
        this.heatConnector = ConnectorOutput.of(Power.class);
        this.targetTemperatureConnector = ConnectorInput.of(Temperature.class);
        this.hydraulicResults = HydraulicLossResult.createEmpty();
    }

    public CoolingFromTemperature(OutputConnection<FlowOfHumidAir> blockWithAirFlowOutput,
                                  OutputConnection<CoolantData> blockWithCoolantDataOutput,
                                  OutputConnection<Temperature> blockWithTemperatureOutput,
                                  Pressure coilPressureLoss) {

        this();
        CommonValidators.requireNotNull(blockWithAirFlowOutput);
        CommonValidators.requireNotNull(blockWithCoolantDataOutput);
        CommonValidators.requireNotNull(blockWithTemperatureOutput);
        this.inputAirFlowConnector.connectAndConsumeDataFrom(blockWithAirFlowOutput.getOutputConnector());
        this.coolantDataInputConnector.connectAndConsumeDataFrom(blockWithCoolantDataOutput.getOutputConnector());
        this.targetTemperatureConnector.connectAndConsumeDataFrom(blockWithTemperatureOutput.getOutputConnector());
        this.outputCondensateConnector.setConnectorData(FlowOfLiquidWater.zeroFlow(inputAirFlowConnector.getConnectorData().getTemperature()));
        this.heatConnector.setConnectorData(Power.ofWatts(0));
        this.outputAirFlowConnector.setConnectorData(inputAirFlowConnector.getConnectorData());
        this.hydraulicResults = HydraulicLossResult.builder().withLocalPressureLoss(coilPressureLoss).build();
    }

    @Override
    public CoolingResult runProcessCalculations() {
        inputAirFlowConnector.updateConnectorData();
        coolantDataInputConnector.updateConnectorData();
        targetTemperatureConnector.updateConnectorData();

        Temperature targetTemperature = targetTemperatureConnector.getConnectorData();
        CoolantData coolantData = coolantDataInputConnector.getConnectorData();

        FlowOfHumidAir inletAirFlow = inputAirFlowConnector.getConnectorData();
        Pressure pressureLoss = hydraulicResults.totalPressureLoss();
        PressureChangeResult pressureChangeResult = PressureChangeEquations.pressureDropDueFriction(inletAirFlow, pressureLoss);

        // Inlet flow is assigned again, because at the final result we need show inlet air entering block, not intermediate inlet air from pressure change process
        CoolingResult results = CoolingEquations.coolingFromTargetTemperature(pressureChangeResult.outletAirFlow(), coolantData, targetTemperature)
                .withInletFlow(inletAirFlow);

        heatConnector.setConnectorData(results.heatOfProcess());
        outputAirFlowConnector.setConnectorData(results.outletAirFlow());
        outputCondensateConnector.setConnectorData(results.condensateFlow());

        this.processResult = results;
        return results;
    }

    @Override
    public CoolingResult getProcessResult() {
        return processResult;
    }

    @Override
    public ProcessType getProcessType() {
        return PROCESS_TYPE;
    }

    public CoolingMode getProcessMode() {
        return COOLING_MODE;
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
    public ConnectorInput<Temperature> getTargetTemperatureConnector() {
        return targetTemperatureConnector;
    }

    public Temperature getUnwrappedTargetTemperature() {
        return targetTemperatureConnector.getConnectorData();
    }

    public void connectCoolantDataSource(OutputConnection<CoolantData> blockWithOutputCoolantData) {
        CommonValidators.requireNotNull(blockWithOutputCoolantData);
        this.coolantDataInputConnector.connectAndConsumeDataFrom(blockWithOutputCoolantData.getOutputConnector());
    }

    public void connectTemperatureDataSource(OutputConnection<Temperature> blockWithTemperatureData) {
        CommonValidators.requireNotNull(blockWithTemperatureData);
        this.targetTemperatureConnector.connectAndConsumeDataFrom(blockWithTemperatureData.getOutputConnector());
    }

    // Static factory methods
    public static CoolingFromTemperature of() {
        return new CoolingFromTemperature();
    }

    public static CoolingFromTemperature of(OutputConnection<CoolantData> blockWithCoolantDataOutput,
                                            OutputConnection<Temperature> blockWithTemperatureOutput) {

        CommonValidators.requireNotNull(blockWithCoolantDataOutput);
        CommonValidators.requireNotNull(blockWithCoolantDataOutput);
        CoolingFromTemperature coolingFromTemperature = new CoolingFromTemperature();
        coolingFromTemperature.connectCoolantDataSource(blockWithCoolantDataOutput);
        coolingFromTemperature.connectTemperatureDataSource(blockWithTemperatureOutput);
        return coolingFromTemperature;
    }

    public static CoolingFromTemperature of(OutputConnection<FlowOfHumidAir> blockWithAirFlowOutput,
                                            OutputConnection<CoolantData> blockWithCoolantDataOutput,
                                            OutputConnection<Temperature> blockWithTemperatureOutput,
                                            Pressure coilPressureLoss) {

        return new CoolingFromTemperature(blockWithAirFlowOutput, blockWithCoolantDataOutput, blockWithTemperatureOutput, coilPressureLoss);
    }

    public static CoolingFromTemperature of(OutputConnection<FlowOfHumidAir> blockWithAirFlowOutput,
                                            OutputConnection<CoolantData> blockWithCoolantDataOutput,
                                            OutputConnection<Temperature> blockWithTemperatureOutput) {

        return new CoolingFromTemperature(blockWithAirFlowOutput, blockWithCoolantDataOutput, blockWithTemperatureOutput, Pressure.ofPascal(0));
    }

    @Override
    public HydraulicLossResult getHydraulicLossResult() {
        return hydraulicResults;
    }
}