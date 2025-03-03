package com.synerset.hvacengine.hydraulic.dataobject;

import com.synerset.hvacengine.common.ConsoleOutputFormatters;
import com.synerset.hvacengine.common.ConsolePrintable;
import com.synerset.unitility.unitsystem.thermodynamic.Pressure;

public record HydraulicLossResult(
        Pressure linearPressureLoss,
        Pressure localPressureLoss,
        Pressure totalPressureLoss
) implements ConsolePrintable {

    // Default constructor
    public HydraulicLossResult() {
        this(Pressure.ofPascal(0), Pressure.ofPascal(0), Pressure.ofPascal(0));
    }

    // Constructor with linear and local pressure loss, calculates total pressure loss
    public HydraulicLossResult(Pressure linearPressureLoss, Pressure localPressureLoss) {
        this(linearPressureLoss, localPressureLoss, linearPressureLoss.plus(localPressureLoss));
    }

    @Override
    public String toConsoleOutput() {
        return ConsoleOutputFormatters.hydraulicConsoleOutput(this);
    }

    public static Builder builder() {
        return new Builder();
    }

    // Builder pattern for HydraulicResults
    public static class Builder {
        private Pressure linearPressureLoss = Pressure.ofPascal(0);
        private Pressure localPressureLoss = Pressure.ofPascal(0);

        // Set linearPressureLoss with builder pattern
        public Builder withLinearPressureLoss(Pressure linearPressureLoss) {
            this.linearPressureLoss = linearPressureLoss;
            return this;
        }

        // Set localPressureLoss with builder pattern
        public Builder withLocalPressureLoss(Pressure localPressureLoss) {
            this.localPressureLoss = localPressureLoss;
            return this;
        }

        // Build the HydraulicResults instance, automatically calculating totalPressureLoss
        public HydraulicLossResult build() {
            Pressure totalPressureLoss = linearPressureLoss.plus(localPressureLoss);
            return new HydraulicLossResult(linearPressureLoss, localPressureLoss, totalPressureLoss);
        }
    }

    public static HydraulicLossResult createEmpty(){
        return new HydraulicLossResult(Pressure.ofPascal(0), Pressure.ofPascal(0));
    }

}