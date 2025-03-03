package com.synerset.hvacengine.hydraulic.dataobject;

import com.synerset.unitility.unitsystem.hydraulic.LocalLossFactor;

/**
 * Represents a local loss coefficient in a fluid flow system.
 * This is typically used to quantify the resistance to flow caused by local fittings, bends, or changes in cross-section.
 */
public record LocalLossFactorData(
        String name,
        LocalLossFactor lossCoefficient
) {

    /**
     * Builder class for creating instances of LocalLossCoefficient.
     */
    public static class Builder {
        private String name;
        private LocalLossFactor lossCoefficient;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder lossCoefficient(LocalLossFactor lossCoefficient) {
            this.lossCoefficient = lossCoefficient;
            return this;
        }

        public LocalLossFactorData build() {
            return new LocalLossFactorData(name, lossCoefficient);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static LocalLossFactorData of(String name, LocalLossFactor lossCoefficient) {
        return new LocalLossFactorData(name, lossCoefficient);
    }

    public static LocalLossFactorData of(LocalLossFactor lossCoefficient) {
        return new LocalLossFactorData("User defined ζ", lossCoefficient);
    }

}
