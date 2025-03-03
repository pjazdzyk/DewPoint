package com.synerset.hvacengine.property.fluids.liquidwater;

import com.synerset.hvacengine.common.validation.CommonValidators;
import com.synerset.hvacengine.property.fluids.Fluid;
import com.synerset.unitility.unitsystem.thermodynamic.*;

import java.util.Objects;

/**
 * Represents liquid water with associated thermodynamic properties.
 * This class implements the Fluid interface.
 */
public class LiquidWater implements Fluid {
    public static final Pressure PRESSURE_MIN_LIMIT = Pressure.ofPascal(0);
    public static final Temperature TEMPERATURE_MIN_LIMIT = Temperature.ofCelsius(0);
    public static final Temperature TEMPERATURE_MAX_LIMIT = Temperature.ofCelsius(200);
    private final Temperature temperature;
    private final Pressure pressure;
    private final Density density;
    private final SpecificHeat specificHeat;
    private final SpecificEnthalpy specificEnthalpy;
    private final DynamicViscosity dynamicViscosity;
    private final KinematicViscosity kinematicViscosity;

    /**
     * Constructs a LiquidWater object with the specified pressure and temperature.
     *
     * @param pressure    The pressure of the liquid water.
     * @param temperature The temperature of the liquid water.
     * @throws IllegalArgumentException If either pressure or temperature is null or if the values are out of bounds.
     */
    public LiquidWater(Pressure pressure, Temperature temperature) {
        CommonValidators.requireNotNull(pressure);
        CommonValidators.requireNotNull(temperature);
        CommonValidators.requireAboveLowerBound(pressure, PRESSURE_MIN_LIMIT);
        CommonValidators.requireAboveLowerBound(temperature, TEMPERATURE_MIN_LIMIT);
        CommonValidators.requireBelowUpperBoundInclusive(temperature, TEMPERATURE_MAX_LIMIT);
        this.temperature = temperature;
        this.pressure = pressure;
        this.density = LiquidWaterEquations.density(temperature);
        this.specificHeat = LiquidWaterEquations.specificHeat(temperature);
        this.specificEnthalpy = LiquidWaterEquations.specificEnthalpy(temperature);
        this.dynamicViscosity = LiquidWaterEquations.dynamicViscosity(temperature, density);
        this.kinematicViscosity = LiquidWaterEquations.kinematicViscosity(dynamicViscosity, density);
    }

    @Override
    public Temperature getTemperature() {
        return temperature;
    }

    @Override
    public Pressure getPressure() {
        return pressure;
    }

    @Override
    public Density getDensity() {
        return density;
    }

    @Override
    public SpecificHeat getSpecificHeat() {
        return specificHeat;
    }

    @Override
    public SpecificEnthalpy getSpecificEnthalpy() {
        return specificEnthalpy;
    }

    @Override
    public DynamicViscosity getDynamicViscosity() {
        return dynamicViscosity;
    }

    @Override
    public KinematicViscosity getKinematicViscosity() {
        return kinematicViscosity;
    }

    /**
     * Returns a formatted string for console output, representation of the LiquidWater object.
     *
     * @return A formatted string representation.
     */
    @Override
    public String toConsoleOutput() {
        String separator = " | ";
        String end = "\n\t";
        int digits = 3;
        return "LiquidWater:" + end +
               pressure.toEngineeringFormat("P_abs", digits) + separator +
               temperature.toEngineeringFormat("t_w", digits) + end +
               specificEnthalpy.toEngineeringFormat("i_w", digits) + separator +
               density.toEngineeringFormat("ρ_w", digits) + separator +
               specificHeat.toEngineeringFormat("cp_w", digits) + end +
               dynamicViscosity.toEngineeringFormat("μ_w", digits) + separator +
               kinematicViscosity.toEngineeringFormat("ν_w", digits) + end;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (LiquidWater) obj;
        return Objects.equals(this.temperature, that.temperature) &&
               Objects.equals(this.pressure, that.pressure) &&
               Objects.equals(this.density, that.density) &&
               Objects.equals(this.specificHeat, that.specificHeat) &&
               Objects.equals(this.specificEnthalpy, that.specificEnthalpy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(temperature, pressure, density);
    }

    @Override
    public String toString() {
        return "LiquidWater{" +
               "temperature=" + temperature +
               ", pressure=" + pressure +
               ", density=" + density +
               ", specificHeat=" + specificHeat +
               ", specificEnthalpy=" + specificEnthalpy +
               ", dynamicViscosity=" + dynamicViscosity +
               ", kinematicViscosity=" + kinematicViscosity +
               '}';
    }

    // Static factory methods

    /**
     * Creates a new LiquidWater object with the specified pressure and temperature.
     *
     * @param pressure    The pressure of the liquid water.
     * @param temperature The temperature of the liquid water.
     * @return A new LiquidWater object.
     */
    public static LiquidWater of(Pressure pressure, Temperature temperature) {
        return new LiquidWater(pressure, temperature);
    }

    /**
     * Creates a new LiquidWater object with the specified temperature at standard atmosphere pressure.
     *
     * @param temperature The temperature of the liquid water.
     * @return A new LiquidWater object.
     */
    public static LiquidWater of(Temperature temperature) {
        return new LiquidWater(Pressure.STANDARD_ATMOSPHERE, temperature);
    }

}
