package com.synerset.hvacengine.hydraulic.structure;

import com.synerset.hvacengine.common.exception.HvacEngineMissingArgumentException;
import com.synerset.hvacengine.hydraulic.ConduitShape;
import com.synerset.hvacengine.hydraulic.material.MaterialLayer;
import com.synerset.hvacengine.hydraulic.material.Materials;
import com.synerset.unitility.unitsystem.common.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.withPrecision;

class RectangularStructureTest {

    @Test
    @DisplayName("Should correctly calculate RectangularStructure properties")
    void shouldCalculatePropertiesCorrectly() {
        // Given: Base material and insulation layers
        MaterialLayer baseMaterial = new MaterialLayer(Materials.INDUSTRIAL_STEEL, Height.ofMillimeters(1.2));
        MaterialLayer insulationLayer = new MaterialLayer(Materials.INSUL_MINERAL_WOOL, Height.ofMillimeters(20));
        MaterialLayer nextInsulationLayer = new MaterialLayer(Materials.INSUL_MINERAL_WOOL, Height.ofMillimeters(20));

        Width innerWidth = Width.ofMillimeters(1000);
        Height innerHeight = Height.ofMillimeters(1000);
        List<MaterialLayer> outerLayers = List.of(insulationLayer, nextInsulationLayer);

        // When: Creating RectangularStructure
        RectangularStructure structure = new RectangularStructure(baseMaterial, innerWidth, innerHeight, outerLayers);

        // Expected Values
        Height totalThickness = baseMaterial.thickness().plus(insulationLayer.thickness());
        Width expectedOuterWidth = innerWidth.plus(totalThickness.plus(nextInsulationLayer.thickness()).multiply(2.0));
        Height expectedOuterHeight = innerHeight.plus(totalThickness.plus(nextInsulationLayer.thickness()).multiply(2.0));

        Perimeter expectedInnerPerimeter = StructureEquations.rectangularPerimeter(innerWidth, innerHeight);
        Area expectedInnerSectionArea = StructureEquations.rectangularArea(innerWidth, innerHeight);
        Perimeter expectedOuterPerimeter = StructureEquations.rectangularPerimeter(expectedOuterWidth, expectedOuterHeight);
        Area expectedOuterSectionArea = StructureEquations.rectangularArea(expectedOuterWidth, expectedOuterHeight);
        Diameter expectedEquivHydraulicDiameter = StructureEquations.equivalentDiameter(expectedInnerSectionArea, expectedInnerPerimeter);

        // Then: Validate calculations using AssertJ
        assertThat(structure.getInnerWidth()).isEqualTo(innerWidth);
        assertThat(structure.getInnerHeight()).isEqualTo(innerHeight);
        assertThat(structure.getOuterWidth()).isEqualTo(expectedOuterWidth);
        assertThat(structure.getOuterHeight()).isEqualTo(expectedOuterHeight);

        assertThat(structure.getInnerPerimeter()).isEqualTo(expectedInnerPerimeter);
        assertThat(structure.getInnerSectionArea()).isEqualTo(expectedInnerSectionArea);

        assertThat(structure.getOuterPerimeter()).isEqualTo(expectedOuterPerimeter);
        assertThat(structure.getOuterSectionArea()).isEqualTo(expectedOuterSectionArea);

        assertThat(structure.getEquivHydraulicDiameter()).isEqualTo(expectedEquivHydraulicDiameter);

        // Validate material layers
        assertThat(structure.getConduitBaseLayer()).isEqualTo(baseMaterial);
        assertThat(structure.getOuterLayers()).containsExactlyElementsOf(outerLayers);

        // Validate linear mass density
        assertThat(structure.getBaseLinearMassDensity().getInKilogramsPerMeter()).isEqualTo(37.725, withPrecision(1E-3));
        assertThat(structure.getTotalLinearMassDensity().getInKilogramsPerMeter()).isEqualTo(51.068, withPrecision(1E-3));

        // Others
        assertThat(structure).isEqualTo(new RectangularStructure(baseMaterial, innerWidth, innerHeight, outerLayers))
                .hasSameHashCodeAs(new RectangularStructure(baseMaterial, innerWidth, innerHeight, outerLayers));
        assertThat(structure.toString()).contains("RectangularStructure");

        assertThat(structure.getConduitShape()).isEqualTo(ConduitShape.RECTANGULAR);

    }

    @Test
    @DisplayName("Should handle null outer layers gracefully")
    void shouldHandleNullOuterLayers() {
        // Given: Base material
        MaterialLayer baseMaterial = new MaterialLayer(Materials.ALUMINIUM, Height.ofMillimeters(3));
        Width innerWidth = Width.ofMillimeters(150);
        Height innerHeight = Height.ofMillimeters(75);

        // When: Creating RectangularStructure with null outer layers
        RectangularStructure structure = new RectangularStructure(baseMaterial, innerWidth, innerHeight, null);

        // Then: Outer layers should be an empty list
        assertThat(structure.getOuterLayers()).isEmpty();
    }

    @Test
    @DisplayName("Should throw exception when base material is null")
    void shouldThrowExceptionForNullBaseMaterial() {
        // Given: Null base material
        Width innerWidth = Width.ofMillimeters(100);
        Height innerHeight = Height.ofMillimeters(50);
        List<MaterialLayer> outerLayers = Collections.emptyList();

        // Then: Expect exception when baseMaterial is null
        assertThatThrownBy(() -> new RectangularStructure(null, innerWidth, innerHeight, outerLayers))
                .isInstanceOf(HvacEngineMissingArgumentException.class);
    }

    @Test
    @DisplayName("Should throw exception when inner width is null")
    void shouldThrowExceptionForNullInnerWidth() {
        // Given: Null inner width
        MaterialLayer baseMaterial = new MaterialLayer(Materials.PVC, Height.ofMillimeters(2));
        Height innerHeight = Height.ofMillimeters(50);
        List<MaterialLayer> outerLayers = Collections.emptyList();

        // Then: Expect exception when innerWidth is null
        assertThatThrownBy(() -> new RectangularStructure(baseMaterial, null, innerHeight, outerLayers))
                .isInstanceOf(HvacEngineMissingArgumentException.class);
    }

    @Test
    @DisplayName("Should throw exception when inner height is null")
    void shouldThrowExceptionForNullInnerHeight() {
        // Given: Null inner height
        MaterialLayer baseMaterial = new MaterialLayer(Materials.PVC, Height.ofMillimeters(2));
        Width innerWidth = Width.ofMillimeters(100);
        List<MaterialLayer> outerLayers = Collections.emptyList();

        // Then: Expect exception when innerHeight is null
        assertThatThrownBy(() -> new RectangularStructure(baseMaterial, innerWidth, null, outerLayers))
                .isInstanceOf(HvacEngineMissingArgumentException.class);
    }

}
