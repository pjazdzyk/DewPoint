package com.synerset.hvacengine.hydraulic.dataobject;

import com.synerset.unitility.unitsystem.thermodynamic.Pressure;

/**
 * Represents a local pressure loss in a fluid flow system.
 * This is typically used to quantify the reduction in pressure due to local elements such as valves or other fittings.
 */
public record LocalLossPressureData(
        String name,
        Pressure localPressureLoss
) {

    /**
     * Builder class for creating instances of LocalLossPressure.
     */
    public static class Builder {
        private String name;
        private Pressure localPressureLoss;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder localPressureLoss(Pressure localPressureLoss) {
            this.localPressureLoss = localPressureLoss;
            return this;
        }

        public LocalLossPressureData build() {
            return new LocalLossPressureData(name, localPressureLoss);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static LocalLossPressureData of(String name, Pressure minorPressureLoss) {
        return new LocalLossPressureData(name, minorPressureLoss);
    }

    public static LocalLossPressureData of(Pressure minorPressureLoss) {
        return new LocalLossPressureData("User defined ΔPm", minorPressureLoss);
    }

}
