package com.synerset.hvacengine.hydraulic.material;

import com.synerset.unitility.unitsystem.common.Height;
import com.synerset.unitility.unitsystem.thermodynamic.Density;
import com.synerset.unitility.unitsystem.thermodynamic.ThermalConductivity;

import java.util.Objects;

public record MaterialData(
        String name,
        Density density,
        ThermalConductivity thermalConductivity,
        Height absoluteRoughness
) {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String name;
        private Density density;
        private ThermalConductivity thermalConductivity;
        private Height absoluteRoughness;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder density(Density density) {
            this.density = density;
            return this;
        }

        public Builder thermalConductivity(ThermalConductivity thermalConductivity) {
            this.thermalConductivity = thermalConductivity;
            return this;
        }

        public Builder absoluteRoughness(Height absoluteRoughness) {
            this.absoluteRoughness = absoluteRoughness;
            return this;
        }

        public MaterialData build() {
            return new MaterialData(name, density, thermalConductivity, absoluteRoughness);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        MaterialData that = (MaterialData) o;
        return Objects.equals(name, that.name) && Objects.equals(density, that.density) && Objects.equals(absoluteRoughness, that.absoluteRoughness) && Objects.equals(thermalConductivity, that.thermalConductivity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, density, thermalConductivity, absoluteRoughness);
    }

    @Override
    public String toString() {
        return "MaterialData{" +
               "name='" + name + '\'' +
               ", density=" + density +
               ", thermalConductivity=" + thermalConductivity +
               ", absoluteRoughness=" + absoluteRoughness +
               '}';
    }
}
