package com.synerset.hvacengine.hydraulic.dataobject;

import com.synerset.hvacengine.common.ConsoleOutputFormatters;
import com.synerset.hvacengine.hydraulic.structure.ConduitStructure;
import com.synerset.hvacengine.process.ProcessResult;
import com.synerset.hvacengine.process.ProcessType;
import com.synerset.hvacengine.property.fluids.humidair.FlowOfHumidAir;
import com.synerset.unitility.unitsystem.common.Length;
import com.synerset.unitility.unitsystem.common.Velocity;
import com.synerset.unitility.unitsystem.common.Volume;
import com.synerset.unitility.unitsystem.thermodynamic.Power;

/**
 * Represents the result of an air heating process.
 */
public record HydraulicProcessResult(ProcessType processType,
                                     FlowOfHumidAir inletAirFlow,
                                     FlowOfHumidAir outletAirFlow,
                                     Power heatOfProcess,
                                     Velocity velocity,
                                     HydraulicLossResult hydraulicLossResult,
                                     ConduitStructure conduitStructure,
                                     Length length,
                                     Volume volume
) implements ProcessResult {

    public HydraulicProcessResult(FlowOfHumidAir inletAirFlow, FlowOfHumidAir outletAirFlow, Power heatOfProcess, Velocity velocity, HydraulicLossResult hydraulicLossResult, ConduitStructure conduitStructure, Length length, Volume volume) {
        this(ProcessType.CONDUIT_FLOW, inletAirFlow, outletAirFlow, heatOfProcess, velocity, hydraulicLossResult, conduitStructure, length, volume);
    }

    @Override
    public String toConsoleOutput() {
        return ConsoleOutputFormatters.hydraulicResultsOutput(this);
    }

    public static class Builder {
        private ProcessType processType = ProcessType.CONDUIT_FLOW;
        private FlowOfHumidAir inletAirFlow;
        private FlowOfHumidAir outletAirFlow;
        private Power heatOfProcess = Power.ofWatts(0);
        private HydraulicLossResult hydraulicLossResult;
        private ConduitStructure conduitStructure;
        private Velocity velocity;
        private Length length;
        private Volume volume;

        public Builder processType(ProcessType processType) {
            this.processType = processType;
            return this;
        }

        public Builder inletAirFlow(FlowOfHumidAir inletAirFlow) {
            this.inletAirFlow = inletAirFlow;
            return this;
        }

        public Builder outletAirFlow(FlowOfHumidAir outletAirFlow) {
            this.outletAirFlow = outletAirFlow;
            return this;
        }

        public Builder heatOfProcess(Power heatOfProcess) {
            this.heatOfProcess = heatOfProcess;
            return this;
        }

        public Builder velocity(Velocity velocity) {
            this.velocity = velocity;
            return this;
        }

        public Builder length(Length length) {
            this.length = length;
            return this;
        }

        public Builder hydraulicLossResult(HydraulicLossResult hydraulicLossResult) {
            this.hydraulicLossResult = hydraulicLossResult;
            return this;
        }

        public Builder conduitStructure(ConduitStructure conduitStructure) {
            this.conduitStructure = conduitStructure;
            return this;
        }

        public Builder volume(Volume volume) {
            this.volume = volume;
            return this;
        }

        public HydraulicProcessResult build() {
            return new HydraulicProcessResult(processType, inletAirFlow, outletAirFlow, heatOfProcess, velocity, hydraulicLossResult, conduitStructure, length, volume);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
