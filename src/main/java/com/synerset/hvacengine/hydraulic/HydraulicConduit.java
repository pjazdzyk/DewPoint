package com.synerset.hvacengine.hydraulic;

import com.synerset.hvacengine.common.validation.CommonValidators;
import com.synerset.hvacengine.hydraulic.structure.ConduitStructure;
import com.synerset.hvacengine.property.fluids.Flow;
import com.synerset.hvacengine.property.fluids.Fluid;
import com.synerset.unitility.unitsystem.common.Height;
import com.synerset.unitility.unitsystem.common.Length;
import com.synerset.unitility.unitsystem.common.Velocity;
import com.synerset.unitility.unitsystem.dimensionless.ReynoldsNumber;
import com.synerset.unitility.unitsystem.hydraulic.FrictionFactor;
import com.synerset.unitility.unitsystem.hydraulic.LinearResistance;
import com.synerset.unitility.unitsystem.thermodynamic.Pressure;

import java.util.Objects;

/**
 * Represents a hydraulic conduit used in HVAC and fluid flow applications. <p>
 * This class models a section of a conduit (such as a duct or pipe) that transports a fluid,
 * incorporating geometric properties, flow characteristics, and friction loss calculations.
 *
 * <p>The {@code HydraulicConduit} class provides computed values such as velocity, Reynolds number,
 * friction factor, and pressure loss based on the conduit structure and fluid properties.
 * It supports different types of conduit structures and fluids using generics.</p>
 *
 * <h2>Key Features:</h2>
 * <ul>
 *     <li>Encapsulates conduit geometry, fluid flow, and pressure loss parameters.</li>
 *     <li>Computes Reynolds number and determines friction factor based on flow regime.</li>
 *     <li>Calculates linear pressure loss and hydraulic resistance.</li>
 *     <li>Provides static factory methods and a builder pattern for object creation.</li>
 * </ul>
 *
 * @param <S> Type of the conduit structure, defining its geometry and material properties.
 * @param <F> Type of the fluid flowing through the conduit.
 */
public class HydraulicConduit<S extends ConduitStructure, F extends Fluid> {
    // Duct geometric properties
    private final S structure;
    private final Length length;

    // Flow data
    private final Flow<F> flowOfFluid;
    private final Velocity velocity;

    // Friction loss data
    private final Length characteristicLength;
    private final ReynoldsNumber reynoldsNumber;
    private final FrictionFactor frictionFactor;
    private final Pressure linearPressureLoss;
    private final LinearResistance linearResistance;

    public HydraulicConduit(S structure, Flow<F> flowOfFluid, Length length) {
        CommonValidators.requireNotNull(structure);
        CommonValidators.requireNotNull(flowOfFluid);
        CommonValidators.requireNotNull(length);
        this.structure = structure;
        this.length = length;
        this.flowOfFluid = flowOfFluid;
        this.velocity = HydraulicEquations.flowVelocity(flowOfFluid.getVolFlow(), structure.getInnerSectionArea());
        this.characteristicLength = Length.of(structure.getEquivHydraulicDiameter());
        this.reynoldsNumber = HydraulicEquations.reynoldsNumber(velocity, characteristicLength,
                flowOfFluid.getDensity(), flowOfFluid.getFluid().getDynamicViscosity());
        Height absoluteRoughness = structure.getConduitBaseLayer().material().absoluteRoughness();
        this.frictionFactor = reynoldsNumber.getValue() <= 2300
                ? HydraulicEquations.frictionFactorLaminar(reynoldsNumber)
                : HydraulicEquations.frictionFactorByColebrooke(structure.getEquivHydraulicDiameter(), absoluteRoughness, reynoldsNumber);
        this.linearPressureLoss = HydraulicEquations.linearPressureLoss(frictionFactor, structure.getEquivHydraulicDiameter(), length, flowOfFluid.getDensity(), velocity);
        this.linearResistance = HydraulicEquations.linearResistance(linearPressureLoss, length);
    }

    // Static Factory Methods
    public static <S extends ConduitStructure, F extends Fluid> HydraulicConduit<S, F> of(S structure, Flow<F> flowOfFluid, Length length) {
        return new HydraulicConduit<>(structure, flowOfFluid, length);
    }

    public static <S extends ConduitStructure, F extends Fluid> HydraulicConduit<S, F> of(S structure, Flow<F> flowOfFluid) {
        return new HydraulicConduit<>(structure, flowOfFluid, Length.ofMeters(1.0));
    }

    // With methods
    public HydraulicConduit<S, F> withStructure(S structure) {
        return new HydraulicConduit<>(structure, this.flowOfFluid, this.length);
    }

    public HydraulicConduit<S, F> withLength(Length length) {
        return new HydraulicConduit<>(this.structure, this.flowOfFluid, length);
    }

    public HydraulicConduit<S, F> withFlowOfFluid(Flow<F> flowOfFluid) {
        return new HydraulicConduit<>(this.structure, flowOfFluid, this.length);
    }

    // Getters
    public S getStructure() {
        return structure;
    }

    public Length getLength() {
        return length;
    }

    public Flow<F> getFlowOfFluid() {
        return flowOfFluid;
    }

    public Velocity getVelocity() {
        return velocity;
    }

    public Length getCharacteristicLength() {
        return characteristicLength;
    }

    public ReynoldsNumber getReynoldsNumber() {
        return reynoldsNumber;
    }

    public FrictionFactor getFrictionFactor() {
        return frictionFactor;
    }

    public Pressure getLinearPressureLoss() {
        return linearPressureLoss;
    }

    public LinearResistance getLinearResistance() {
        return linearResistance;
    }

    // Builder pattern
    public static <S extends ConduitStructure, F extends Fluid> Builder<S, F> builder() {
        return new Builder<>();
    }

    public Builder<S, F> toBuilder() {
        return new Builder<S, F>().structure(this.structure)
                .length(this.length)
                .flowOfFluid(this.flowOfFluid);
    }

    public static class Builder<S extends ConduitStructure, F extends Fluid> {
        private S structure;
        private Length length;
        private Flow<F> flowOfFluid;

        public Builder<S, F> structure(S structure) {
            this.structure = structure;
            return this;
        }

        public Builder<S, F> length(Length length) {
            this.length = length;
            return this;
        }

        public Builder<S, F> flowOfFluid(Flow<F> flowOfFluid) {
            this.flowOfFluid = flowOfFluid;
            return this;
        }

        public HydraulicConduit<S, F> build() {
            return new HydraulicConduit<>(structure, flowOfFluid, length);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        HydraulicConduit<?, ?> that = (HydraulicConduit<?, ?>) o;
        return Objects.equals(structure, that.structure) && Objects.equals(length, that.length);
    }

    @Override
    public int hashCode() {
        return Objects.hash(structure, length);
    }

    @Override
    public String toString() {
        return "HydraulicConduit{" +
               "structure=" + structure +
               ", length=" + length +
               ", flowOfFluid=" + flowOfFluid +
               ", velocity=" + velocity +
               ", characteristicLength=" + characteristicLength +
               ", reynoldsNumber=" + reynoldsNumber +
               ", frictionFactor=" + frictionFactor +
               ", linearPressureLoss=" + linearPressureLoss +
               ", linearResistance=" + linearResistance +
               '}';
    }

}
