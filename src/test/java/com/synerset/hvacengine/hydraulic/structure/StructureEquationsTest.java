package com.synerset.hvacengine.hydraulic.structure;

import com.synerset.hvacengine.hydraulic.material.MaterialLayer;
import com.synerset.hvacengine.hydraulic.material.Materials;
import com.synerset.unitility.unitsystem.common.Diameter;
import com.synerset.unitility.unitsystem.common.Height;
import com.synerset.unitility.unitsystem.common.LinearMassDensity;
import com.synerset.unitility.unitsystem.common.Width;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StructureEquationsTest {

    @Test
    @DisplayName("Should correctly calculate circular linear mass density for steel")
    void calculateCircularLinearMassDensityTestSteel() {
        // Given
        Diameter diameter = Diameter.ofMillimeters(1600);
        MaterialLayer materialLayer = MaterialLayer.builder().material(Materials.INDUSTRIAL_STEEL).thickness(Height.ofMillimeters(1.2)).build();

        // When
        LinearMassDensity linearMassDensity = StructureEquations.calculateCircularLinearMassDensity(diameter, materialLayer);

        // Then
        assertThat(linearMassDensity).isEqualTo(LinearMassDensity.ofKilogramsPerMeter(47.38559703825971));

    }

    @Test
    @DisplayName("Should correctly calculate rectangular linear mass density for steel")
    void calculateRectangularLinearMassDensityTestSteel() {
        // Given
        Width width = Width.ofMillimeters(1000);
        Height height = Height.ofMillimeters(1000);
        MaterialLayer materialLayer = MaterialLayer.builder().material(Materials.INDUSTRIAL_STEEL).thickness(Height.ofMillimeters(1.0)).build();

        // When
        LinearMassDensity linearMassDensity = StructureEquations.calculateRectangularLinearMassDensity(width, height, materialLayer);

        // Then
        assertThat(linearMassDensity).isEqualTo(LinearMassDensity.ofKilogramsPerMeter(31.431399999999186));

    }

}