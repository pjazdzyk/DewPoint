package com.synerset.hvaclib.flows;


import com.synerset.hvaclib.common.Defaults;
import com.synerset.hvaclib.flows.equations.FlowEquations;
import com.synerset.hvaclib.fluids.DryAir;
import com.synerset.unitility.unitsystem.flows.MassFlow;
import com.synerset.unitility.unitsystem.flows.MassFlowUnits;
import com.synerset.unitility.unitsystem.flows.VolumetricFlow;
import com.synerset.unitility.unitsystem.flows.VolumetricFlowUnits;
import com.synerset.unitility.unitsystem.thermodynamic.*;

import java.util.Objects;

public class FlowOfDryAir implements Flow<DryAir> {

    private final DryAir dryAir;
    private final MassFlow massFlow;
    private final VolumetricFlow volFlow;

    private FlowOfDryAir(DryAir dryAir, MassFlow massFlow) {
        this.dryAir = dryAir;
        this.massFlow = massFlow;
        this.volFlow = FlowEquations.massFlowToVolFlow(dryAir.density(), massFlow);
    }

    private FlowOfDryAir(DryAir dryAir, VolumetricFlow volFlow) {
        this.dryAir = dryAir;
        this.volFlow = volFlow;
        this.massFlow = FlowEquations.volFlowToMassFlow(dryAir.density(), volFlow);
    }

    @Override
    public DryAir fluid() {
        return dryAir;
    }

    @Override
    public MassFlow massFlow() {
        return massFlow;
    }

    @Override
    public VolumetricFlow volumetricFlow() {
        return volFlow;
    }

    @Override
    public Temperature temperature() {
        return dryAir.temperature();
    }

    @Override
    public Pressure pressure() {
        return dryAir.pressure();
    }

    @Override
    public Density density() {
        return dryAir.density();
    }

    @Override
    public SpecificHeat specificHeat() {
        return dryAir.specificHeat();
    }

    @Override
    public SpecificEnthalpy specificEnthalpy() {
        return dryAir.specificEnthalpy();
    }

    @Override
    public String toFormattedString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("FlowOfDryAir:\n\t")
                .append("G = ").append(massFlow.getValue()).append(" ").append(massFlow.getUnitSymbol()).append(" | ")
                .append("G = ").append(massFlow.getInKiloGramsPerHour()).append(" ").append(MassFlowUnits.KILOGRAM_PER_HOUR.getSymbol()).append(" | ")
                .append("V = ").append(volFlow.getValue()).append(" ").append(volFlow.getUnitSymbol()).append(" | ")
                .append("V = ").append(volFlow.getInCubicMetersPerHour()).append(" ").append(VolumetricFlowUnits.CUBIC_METERS_PER_HOUR.getSymbol())
                .append("\n\t")
                .append(dryAir.toFormattedString())
                .append("\n");

        return stringBuilder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FlowOfDryAir that = (FlowOfDryAir) o;
        return Objects.equals(dryAir, that.dryAir) && Objects.equals(massFlow, that.massFlow);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dryAir, massFlow);
    }

    @Override
    public String toString() {
        return "FlowOfDryAir{" +
                "dryAir=" + dryAir +
                ", massFlow=" + massFlow +
                ", volFlow=" + volFlow +
                '}';
    }

    // Class factory methods
    public FlowOfDryAir withMassFlow(MassFlow massFlow) {
        return FlowOfDryAir.of(dryAir, massFlow);
    }

    public FlowOfDryAir withVolFlow(VolumetricFlow volFlow) {
        return FlowOfDryAir.of(dryAir, volFlow);
    }

    public FlowOfDryAir withHumidAir(DryAir dryAir) {
        return FlowOfDryAir.of(dryAir, massFlow);
    }

    // Static factory methods
    public static FlowOfDryAir of(DryAir dryAir, MassFlow massFlow) {
        return new FlowOfDryAir(dryAir, massFlow);
    }

    public static FlowOfDryAir of(DryAir dryAir, VolumetricFlow volFlow) {
        return new FlowOfDryAir(dryAir, volFlow);
    }

    public static FlowOfDryAir ofValues(double absPressure, double temperature, double m3hVolFlow) {
        Pressure absPress = Pressure.ofPascal(absPressure);
        Temperature temp = Temperature.ofCelsius(temperature);
        VolumetricFlow volFlow = VolumetricFlow.ofCubicMetersPerHour(m3hVolFlow);
        DryAir dryAir = DryAir.of(absPress, temp);
        return of(dryAir, volFlow);
    }

    public static FlowOfDryAir ofValues(double temperature, double m3hVolFlow) {
        double pressure = Defaults.STANDARD_ATMOSPHERE.getInPascals();
        return ofValues(pressure, temperature, m3hVolFlow);
    }

}
