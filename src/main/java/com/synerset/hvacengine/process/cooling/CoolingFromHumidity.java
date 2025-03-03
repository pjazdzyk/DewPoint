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
import com.synerset.unitility.unitsystem.humidity.RelativeHumidity;
import com.synerset.unitility.unitsystem.thermodynamic.Power;
import com.synerset.unitility.unitsystem.thermodynamic.Pressure;

public class CoolingFromHumidity implements AirFlowProcessBlock {

    private static final ProcessType PROCESS_TYPE = ProcessType.COOLING;
    private static final CoolingMode COOLING_MODE = CoolingMode.FROM_HUMIDITY;
    private final ConnectorInput<FlowOfHumidAir> inputAirFlowConnector;
    private final ConnectorOutput<FlowOfHumidAir> outputAirFlowConnector;
    private final ConnectorInput<CoolantData> coolantDataInputConnector;
    private final ConnectorOutput<FlowOfLiquidWater> outputCondensateConnector;
    private final ConnectorOutput<Power> heatConnector;
    private final ConnectorInput<RelativeHumidity> targetRelativeHumidityConnector;
    private CoolingResult processResult;
    private HydraulicLossResult hydraulicResults;

    public CoolingFromHumidity() {
        this.inputAirFlowConnector = ConnectorInput.of(FlowOfHumidAir.class);
        this.outputAirFlowConnector = ConnectorOutput.of(FlowOfHumidAir.class);
        this.coolantDataInputConnector = ConnectorInput.of(CoolantData.class);
        this.outputCondensateConnector = ConnectorOutput.of(FlowOfLiquidWater.class);
        this.heatConnector = ConnectorOutput.of(Power.class);
        this.targetRelativeHumidityConnector = ConnectorInput.of(RelativeHumidity.class);
        this.hydraulicResults = HydraulicLossResult.createEmpty();
    }

    public CoolingFromHumidity(OutputConnection<FlowOfHumidAir> blockWithAirFlowOutput,
                               OutputConnection<CoolantData> blockWithCoolantDataOutput,
                               OutputConnection<RelativeHumidity> blockWithHumidityOutput,
                               Pressure coilPressureLoss) {

        this();
        CommonValidators.requireNotNull(blockWithAirFlowOutput);
        CommonValidators.requireNotNull(blockWithCoolantDataOutput);
        CommonValidators.requireNotNull(blockWithHumidityOutput);
        this.inputAirFlowConnector.connectAndConsumeDataFrom(blockWithAirFlowOutput.getOutputConnector());
        this.coolantDataInputConnector.connectAndConsumeDataFrom(blockWithCoolantDataOutput.getOutputConnector());
        this.targetRelativeHumidityConnector.connectAndConsumeDataFrom(blockWithHumidityOutput.getOutputConnector());
        this.outputCondensateConnector.setConnectorData(FlowOfLiquidWater.zeroFlow(inputAirFlowConnector.getConnectorData().getTemperature()));
        this.heatConnector.setConnectorData(Power.ofWatts(0));
        this.outputAirFlowConnector.setConnectorData(inputAirFlowConnector.getConnectorData());
        this.hydraulicResults = HydraulicLossResult.builder().withLocalPressureLoss(coilPressureLoss).build();
    }

    @Override
    public CoolingResult runProcessCalculations() {
        inputAirFlowConnector.updateConnectorData();
        coolantDataInputConnector.updateConnectorData();
        targetRelativeHumidityConnector.updateConnectorData();

        FlowOfHumidAir inletAirFlow = inputAirFlowConnector.getConnectorData();
        Pressure pressureLoss = hydraulicResults.totalPressureLoss();
        PressureChangeResult pressureChangeResult = PressureChangeEquations.pressureDropDueFriction(inletAirFlow, pressureLoss);

        // Inlet flow is assigned again, because at the final result we need show inlet air entering block, not intermediate inlet air from pressure change process
        RelativeHumidity targetRelativeHum = this.targetRelativeHumidityConnector.getConnectorData();
        CoolantData coolantData = coolantDataInputConnector.getConnectorData();
        CoolingResult results = CoolingEquations.coolingFromTargetRelativeHumidity(pressureChangeResult.outletAirFlow(), coolantData, targetRelativeHum)
                .withInletFlow(inletAirFlow);

        outputAirFlowConnector.setConnectorData(results.outletAirFlow());
        heatConnector.setConnectorData(results.heatOfProcess());
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

    // Method specific for this process
    public ConnectorInput<RelativeHumidity> getTargetRelativeHumidityConnector() {
        return targetRelativeHumidityConnector;
    }

    public RelativeHumidity getUnwrappedTargetRelativeHumidity() {
        return targetRelativeHumidityConnector.getConnectorData();
    }

    public void connectCoolantDataSource(OutputConnection<CoolantData> blockWithOutputCoolantData) {
        CommonValidators.requireNotNull(blockWithOutputCoolantData);
        this.coolantDataInputConnector.connectAndConsumeDataFrom(blockWithOutputCoolantData.getOutputConnector());
    }

    public void connectRelativeHumidityDataSource(OutputConnection<RelativeHumidity> blockWithRelativeHumidityData) {
        CommonValidators.requireNotNull(blockWithRelativeHumidityData);
        this.targetRelativeHumidityConnector.connectAndConsumeDataFrom(blockWithRelativeHumidityData.getOutputConnector());
    }

    // Static factory methods
    public static CoolingFromHumidity of() {
        return new CoolingFromHumidity();
    }

    public static CoolingFromHumidity of(OutputConnection<CoolantData> blockWithCoolantDataOutput,
                                         OutputConnection<RelativeHumidity> blockWithHumidityOutput) {

        CommonValidators.requireNotNull(blockWithCoolantDataOutput);
        CommonValidators.requireNotNull(blockWithHumidityOutput);
        CoolingFromHumidity coolingFromHumidity = new CoolingFromHumidity();
        coolingFromHumidity.connectCoolantDataSource(blockWithCoolantDataOutput);
        coolingFromHumidity.connectRelativeHumidityDataSource(blockWithHumidityOutput);
        return coolingFromHumidity;
    }

    public static CoolingFromHumidity of(OutputConnection<FlowOfHumidAir> blockWithAirFlowOutput,
                                         OutputConnection<CoolantData> blockWithCoolantDataOutput,
                                         OutputConnection<RelativeHumidity> blockWithHumidityOutput,
                                         Pressure coilPressureLoss) {

        return new CoolingFromHumidity(blockWithAirFlowOutput, blockWithCoolantDataOutput, blockWithHumidityOutput, coilPressureLoss);
    }

    public static CoolingFromHumidity of(OutputConnection<FlowOfHumidAir> blockWithAirFlowOutput,
                                         OutputConnection<CoolantData> blockWithCoolantDataOutput,
                                         OutputConnection<RelativeHumidity> blockWithHumidityOutput) {

        return new CoolingFromHumidity(blockWithAirFlowOutput, blockWithCoolantDataOutput, blockWithHumidityOutput, Pressure.ofPascal(0));
    }

    @Override
    public HydraulicLossResult getHydraulicLossResult() {
        return hydraulicResults;
    }
}