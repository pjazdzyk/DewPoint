package com.synerset.hvacengine.hydraulic.dataobject;

import com.synerset.hvacengine.common.ConsoleOutputFormatters;
import com.synerset.hvacengine.process.ProcessResult;
import com.synerset.hvacengine.process.ProcessType;
import com.synerset.hvacengine.property.fluids.humidair.FlowOfHumidAir;
import com.synerset.unitility.unitsystem.thermodynamic.Power;

/**
 * Represents the result of an air heating process.
 */
public record HydraulicProcessResult(ProcessType processType,
                                     FlowOfHumidAir inletAirFlow,
                                     FlowOfHumidAir outletAirFlow,
                                     Power heatOfProcess
) implements ProcessResult {

    public HydraulicProcessResult(FlowOfHumidAir inletAirFlow, FlowOfHumidAir outletAirFlow, Power heatOfProcess) {
        this(ProcessType.PRESSURE_CHANGE, inletAirFlow, outletAirFlow, heatOfProcess);
    }

    @Override
    public String toConsoleOutput() {
        return ConsoleOutputFormatters.hydraulicResultsOutput(this);
    }

    public static class Builder {
        private FlowOfHumidAir inletAirFlow;
        private FlowOfHumidAir outletAirFlow;
        private Power heatOfProcess = Power.ofWatts(0);
        private ProcessType processType = ProcessType.PRESSURE_CHANGE;

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

        public Builder processType(ProcessType processType) {
            this.processType = processType;
            return this;
        }

        public HydraulicProcessResult build() {
            return new HydraulicProcessResult(processType, inletAirFlow, outletAirFlow, heatOfProcess);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
