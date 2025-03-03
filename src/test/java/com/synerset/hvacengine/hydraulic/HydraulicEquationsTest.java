package com.synerset.hvacengine.hydraulic;

import com.synerset.hvacengine.hydraulic.structure.StructureEquations;
import com.synerset.hvacengine.property.fluids.liquidwater.FlowOfLiquidWater;
import com.synerset.hvacengine.property.fluids.liquidwater.LiquidWater;
import com.synerset.unitility.unitsystem.common.*;
import com.synerset.unitility.unitsystem.dimensionless.ReynoldsNumber;
import com.synerset.unitility.unitsystem.flow.VolumetricFlow;
import com.synerset.unitility.unitsystem.hydraulic.FrictionFactor;
import com.synerset.unitility.unitsystem.hydraulic.LocalLossFactor;
import com.synerset.unitility.unitsystem.thermodynamic.Density;
import com.synerset.unitility.unitsystem.thermodynamic.Pressure;
import com.synerset.unitility.unitsystem.thermodynamic.Temperature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.withPrecision;

class HydraulicEquationsTest {

    @Test
    @DisplayName("Hydraulics: should calculate flow velocity when flow rate and diameter is given")
    void flowVelocityTest() {
        // Given
        VolumetricFlow volumetricFlow = VolumetricFlow.ofCubicMetersPerSecond(10);
        Area area = Area.ofSquareMeters(0.2);

        // When
        Velocity velocity = HydraulicEquations.flowVelocity(volumetricFlow, area);

        // Then
        assertThat(velocity).isEqualTo(Velocity.ofMetersPerSecond(50));
    }

    @Test
    @DisplayName("Hydraulics: should calculate Reynolds number when flow, area and characteristic length is given")
    void reynoldsNumberTest() {
        // Given
        FlowOfLiquidWater flowOfLiquidWater = FlowOfLiquidWater.of(LiquidWater.of(Temperature.ofCelsius(10)),
                VolumetricFlow.ofCubicMetersPerSecond(2));

        Diameter diameter = Diameter.ofMeters(1);
        double areaValue = StructureEquations.circularArea(diameter.getInMeters());
        Area area = Area.ofSquareMeters(areaValue);
        Length characteristicLength = Length.of(diameter);

        // Then
        ReynoldsNumber reynoldsNumber = HydraulicEquations.reynoldsNumber(flowOfLiquidWater, area, characteristicLength);

        // When
        assertThat(reynoldsNumber.getValue()).isEqualTo(1948651.40, withPrecision(10E-3));
    }

    @Test
    @DisplayName("Hydraulics: should calculate laminar friction factor when Reynolds number is given")
    void frictionFactorLaminarTest() {
        // Given
        ReynoldsNumber reynoldsNumber = ReynoldsNumber.of(6400);

        // When
        FrictionFactor actualFrictionFactor = HydraulicEquations.frictionFactorLaminar(reynoldsNumber);

        // Then
        assertThat(actualFrictionFactor).isEqualTo(FrictionFactor.of(0.01));

    }

    @Test
    @DisplayName("Hydraulics: should calculate friction factor by Vatankhah when Reynolds number, diameter and abs roughness is given")
    void frictionFactorByVatankhahTest() {
        // Given
        ReynoldsNumber reynoldsNumber = ReynoldsNumber.of(4000);
        Diameter diameter = Diameter.ofMeters(0.2);
        Height absRoughness = Height.ofMeters(0.01);

        // When
        FrictionFactor actualFrictionFactor = HydraulicEquations.frictionFactorByVatankhah(diameter, absRoughness, reynoldsNumber);

        // Then
        assertThat(actualFrictionFactor).isEqualTo(FrictionFactor.of(0.07707274335909842));

    }

    @Test
    @DisplayName("Hydraulics: should calculate friction factor by Colebrooke when Reynolds number, diameter and abs roughness is given")
    void frictionFactorByColebrookeTest() {
        // Given
        ReynoldsNumber reynoldsNumber = ReynoldsNumber.of(4000);
        Diameter diameter = Diameter.ofMeters(0.2);
        Height absRoughness = Height.ofMeters(0.01);

        // When
        FrictionFactor actualFrictionFactor = HydraulicEquations.frictionFactorByColebrooke(diameter, absRoughness, reynoldsNumber);

        // Then
        assertThat(actualFrictionFactor.getValue()).isEqualTo(0.076987, withPrecision(1E-6));

    }

    @Test
    @DisplayName("Hydraulics: should calculate local pressure loss")
    void localPressureLossTest() {
        // Given
        Density density = Density.ofKilogramPerCubicMeter(1.2);

        // When
        Pressure localPressureLoss = HydraulicEquations.localPressureLoss(LocalLossFactor.of(0.2),
                Velocity.ofMetersPerSecond(2), density);

        // Then
        assertThat(localPressureLoss).isEqualTo(Pressure.ofPascal(0.48));

    }

}