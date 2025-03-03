package com.synerset.hvacengine.process.pressurechange.dataobject;

import com.synerset.hvacengine.common.ConsoleOutputFormatters;
import com.synerset.hvacengine.process.ProcessResult;
import com.synerset.hvacengine.process.ProcessType;
import com.synerset.hvacengine.process.pressurechange.PressureMode;
import com.synerset.hvacengine.property.fluids.humidair.FlowOfHumidAir;
import com.synerset.unitility.unitsystem.thermodynamic.Power;
import com.synerset.unitility.unitsystem.thermodynamic.Pressure;

/**
 * Represents the result of an air heating process.
 */
public record PressureChangeResult(ProcessType processType,
                                   PressureMode processMode,
                                   FlowOfHumidAir inletAirFlow,
                                   FlowOfHumidAir outletAirFlow,
                                   Power heatOfProcess,
                                   Pressure pressureChange) implements ProcessResult {
    @Override
    public String toConsoleOutput() {
        return ConsoleOutputFormatters.pressureChangeOutput(this);
    }

    public static class Builder {
        private static final ProcessType processType = ProcessType.PRESSURE_CHANGE;
        private PressureMode processMode;
        private FlowOfHumidAir inletAirFlow;
        private FlowOfHumidAir outletAirFlow;
        private Power heatOfProcess;
        private Pressure pressureChange;

        public Builder processMode(PressureMode processMode) {
            this.processMode = processMode;
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

        public Builder pressureChange(Pressure pressureChange) {
            this.pressureChange = pressureChange;
            return this;
        }

        public PressureChangeResult build() {
            return new PressureChangeResult(processType, processMode, inletAirFlow, outletAirFlow, heatOfProcess, pressureChange);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

}