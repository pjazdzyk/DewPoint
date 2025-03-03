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

public class CoolingFromPower implements AirFlowProcessBlock {

    private static final ProcessType PROCESS_TYPE = ProcessType.COOLING;
    private static final CoolingMode COOLING_MODE = CoolingMode.FROM_POWER;
    private final ConnectorInput<FlowOfHumidAir> inputAirFlowConnector;
    private final ConnectorOutput<FlowOfHumidAir> outputAirFlowConnector;
    private final ConnectorInput<CoolantData> coolantDataInputConnector;
    private final ConnectorOutput<FlowOfLiquidWater> outputCondensateConnector;
    private final ConnectorInput<Power> heatConnector;
    private CoolingResult processResult;
    private HydraulicLossResult hydraulicResults;

    public CoolingFromPower() {
        this.inputAirFlowConnector = ConnectorInput.of(FlowOfHumidAir.class);
        this.outputAirFlowConnector = ConnectorOutput.of(FlowOfHumidAir.class);
        this.coolantDataInputConnector = ConnectorInput.of(CoolantData.class);
        this.outputCondensateConnector = ConnectorOutput.of(FlowOfLiquidWater.class);
        this.heatConnector = ConnectorInput.of(Power.class);
        this.hydraulicResults = HydraulicLossResult.createEmpty();
    }

    public CoolingFromPower(OutputConnection<FlowOfHumidAir> blockWithAirFlowOutput,
                            OutputConnection<CoolantData> blockWithCoolantDataOutput,
                            OutputConnection<Power> blockWithPowerOutput,
                            Pressure coilPressureLoss) {

        this();
        CommonValidators.requireNotNull(blockWithAirFlowOutput);
        CommonValidators.requireNotNull(blockWithCoolantDataOutput);
        CommonValidators.requireNotNull(blockWithPowerOutput);
        this.inputAirFlowConnector.connectAndConsumeDataFrom(blockWithAirFlowOutput.getOutputConnector());
        this.coolantDataInputConnector.connectAndConsumeDataFrom(blockWithCoolantDataOutput.getOutputConnector());
        this.heatConnector.connectAndConsumeDataFrom(blockWithPowerOutput.getOutputConnector());
        this.outputAirFlowConnector.setConnectorData(inputAirFlowConnector.getConnectorData());
        this.outputCondensateConnector.setConnectorData(FlowOfLiquidWater.zeroFlow(inputAirFlowConnector.getConnectorData().getTemperature()));
        this.hydraulicResults = HydraulicLossResult.builder().withLocalPressureLoss(coilPressureLoss).build();
    }

    @Override
    public CoolingResult runProcessCalculations() {
        inputAirFlowConnector.updateConnectorData();
        coolantDataInputConnector.updateConnectorData();
        heatConnector.updateConnectorData();

        Power coolingPower = heatConnector.getConnectorData();
        CoolantData coolantData = coolantDataInputConnector.getConnectorData();

        FlowOfHumidAir inletAirFlow = inputAirFlowConnector.getConnectorData();
        Pressure pressureLoss = hydraulicResults.totalPressureLoss();
        PressureChangeResult pressureChangeResult = PressureChangeEquations.pressureDropDueFriction(inletAirFlow, pressureLoss);

        // Inlet flow is assigned again, because at the final result we need show inlet air entering block, not intermediate inlet air from pressure change process
        CoolingResult results = CoolingEquations.coolingFromPower(pressureChangeResult.outletAirFlow(), coolantData, coolingPower)
                .withInletFlow(inletAirFlow);

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

    // Methods specific for this class
    public ConnectorInput<Power> getHeatConnector() {
        return heatConnector;
    }

    public Power getUnwrappedInputCoolingPower() {
        return heatConnector.getConnectorData();
    }

    public void connectCoolantDataSource(OutputConnection<CoolantData> blockWithOutputCoolantData) {
        CommonValidators.requireNotNull(blockWithOutputCoolantData);
        this.coolantDataInputConnector.connectAndConsumeDataFrom(blockWithOutputCoolantData.getOutputConnector());
    }

    public void connectPowerDataSource(OutputConnection<Power> blockWithPowerData) {
        CommonValidators.requireNotNull(blockWithPowerData);
        this.heatConnector.connectAndConsumeDataFrom(blockWithPowerData.getOutputConnector());
    }

    // Static factory methods
    public static CoolingFromPower of() {
        return new CoolingFromPower();
    }

    public static CoolingFromPower of(OutputConnection<CoolantData> blockWithCoolantDataOutput,
                                      OutputConnection<Power> blockWithPowerOutput) {

        CommonValidators.requireNotNull(blockWithCoolantDataOutput);
        CommonValidators.requireNotNull(blockWithPowerOutput);
        CoolingFromPower coolingFromPower = new CoolingFromPower();
        coolingFromPower.connectCoolantDataSource(blockWithCoolantDataOutput);
        coolingFromPower.connectPowerDataSource(blockWithPowerOutput);
        return coolingFromPower;
    }

    public static CoolingFromPower of(OutputConnection<FlowOfHumidAir> blockWithAirFlowOutput,
                                      OutputConnection<CoolantData> blockWithCoolantDataOutput,
                                      OutputConnection<Power> blockWithPowerOutput) {

        return new CoolingFromPower(blockWithAirFlowOutput, blockWithCoolantDataOutput, blockWithPowerOutput, Pressure.ofPascal(0));
    }

    public static CoolingFromPower of(OutputConnection<FlowOfHumidAir> blockWithAirFlowOutput,
                                      OutputConnection<CoolantData> blockWithCoolantDataOutput,
                                      OutputConnection<Power> blockWithPowerOutput,
                                      Pressure coilPressureLoss) {

        return new CoolingFromPower(blockWithAirFlowOutput, blockWithCoolantDataOutput, blockWithPowerOutput, coilPressureLoss);
    }

    @Override
    public HydraulicLossResult getHydraulicLossResult() {
        return hydraulicResults;
    }
}