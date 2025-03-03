package com.synerset.hvacengine.hydraulic.structure;

import com.synerset.hvacengine.common.exception.HvacEngineMissingArgumentException;
import com.synerset.hvacengine.hydraulic.ConduitShape;
import com.synerset.hvacengine.hydraulic.material.MaterialLayer;
import com.synerset.hvacengine.hydraulic.material.Materials;
import com.synerset.unitility.unitsystem.common.Area;
import com.synerset.unitility.unitsystem.common.Diameter;
import com.synerset.unitility.unitsystem.common.Height;
import com.synerset.unitility.unitsystem.common.Perimeter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.withPrecision;

class CircularStructureTest {

    @Test
    @DisplayName("Should correctly calculate CircularStructure properties")
    void shouldCalculatePropertiesCorrectly() {
        // Given: Base material and insulation layers
        MaterialLayer baseMaterial = new MaterialLayer(Materials.INDUSTRIAL_STEEL, Height.ofMillimeters(1.2));
        MaterialLayer insulationLayer = new MaterialLayer(Materials.INSUL_MINERAL_WOOL, Height.ofMillimeters(50));
        MaterialLayer secondInsulationLayer = new MaterialLayer(Materials.INSUL_MINERAL_WOOL, Height.ofMillimeters(50));

        Diameter innerDiameter = Diameter.ofMillimeters(1600);
        List<MaterialLayer> outerLayers = List.of(insulationLayer, secondInsulationLayer);

        // When: Creating CircularStructure
        CircularStructure structure = new CircularStructure(baseMaterial, innerDiameter, outerLayers);

        // Expected Values
        Height totalThickness = baseMaterial.thickness().plus(insulationLayer.thickness().plus(secondInsulationLayer.thickness()));
        Diameter expectedOuterDiameter = innerDiameter.plus(totalThickness.multiply(2.0));

        Perimeter expectedInnerPerimeter = StructureEquations.circularPerimeter(innerDiameter);
        Area expectedInnerSectionArea = StructureEquations.circularArea(innerDiameter);
        Perimeter expectedOuterPerimeter = StructureEquations.circularPerimeter(expectedOuterDiameter);
        Area expectedOuterSectionArea = StructureEquations.circularArea(expectedOuterDiameter);
        Diameter expectedEquivHydraulicDiameter = StructureEquations.equivalentDiameter(expectedInnerSectionArea, expectedInnerPerimeter);

        // Then: Validate calculations using AssertJ
        assertThat(structure.getInnerDiameter()).isEqualTo(innerDiameter);
        assertThat(structure.getOuterDiameter()).isEqualTo(expectedOuterDiameter);

        assertThat(structure.getInnerPerimeter()).isEqualTo(expectedInnerPerimeter);
        assertThat(structure.getInnerSectionArea()).isEqualTo(expectedInnerSectionArea);

        assertThat(structure.getOuterPerimeter()).isEqualTo(expectedOuterPerimeter);
        assertThat(structure.getOuterSectionArea()).isEqualTo(expectedOuterSectionArea);

        assertThat(structure.getEquivHydraulicDiameter()).isEqualTo(expectedEquivHydraulicDiameter);

        // Validate material layers
        assertThat(structure.getConduitBaseLayer()).isEqualTo(baseMaterial);
        assertThat(structure.getOuterLayers()).containsExactlyElementsOf(outerLayers);

        // Validate linear mass density
        assertThat(structure.getBaseLinearMassDensity().getInKilogramsPerMeter()).isEqualTo(47.386, withPrecision(1E-3));
        assertThat(structure.getTotalLinearMassDensity().getInKilogramsPerMeter()).isEqualTo(90.171, withPrecision(1E-3));

        // Others
        assertThat(structure).isEqualTo(new CircularStructure(baseMaterial, innerDiameter, outerLayers))
                .hasSameHashCodeAs(new CircularStructure(baseMaterial, innerDiameter, outerLayers));
        assertThat(structure.toString()).contains("CircularStructure");

        assertThat(structure.getConduitShape()).isEqualTo(ConduitShape.CIRCULAR);

    }

    @Test
    @DisplayName("Should handle null outer layers gracefully")
    void shouldHandleNullOuterLayers() {
        // Given: Base material
        MaterialLayer baseMaterial = new MaterialLayer(Materials.ALUMINIUM, Height.ofMillimeters(3));
        Diameter innerDiameter = Diameter.ofMillimeters(150);

        // When: Creating CircularStructure with null outer layers
        CircularStructure structure = new CircularStructure(baseMaterial, innerDiameter, null);

        // Then: Outer layers should be an empty list
        assertThat(structure.getOuterLayers()).isEmpty();
    }

    @Test
    @DisplayName("Should throw exception when base material is null")
    void shouldThrowExceptionForNullBaseMaterial() {
        // Given: Null base material
        Diameter innerDiameter = Diameter.ofMillimeters(100);
        List<MaterialLayer> outerLayers = Collections.emptyList();

        // Then: Expect exception when baseMaterial is null
        assertThatThrownBy(() -> new CircularStructure(null, innerDiameter, outerLayers))
                .isInstanceOf(HvacEngineMissingArgumentException.class);
    }

    @Test
    @DisplayName("Should throw exception when inner diameter is null")
    void shouldThrowExceptionForNullInnerDiameter() {
        // Given: Null inner diameter
        MaterialLayer baseMaterial = new MaterialLayer(Materials.PVC, Height.ofMillimeters(2));
        List<MaterialLayer> outerLayers = Collections.emptyList();

        // Then: Expect exception when innerDiameter is null
        assertThatThrownBy(() -> new CircularStructure(baseMaterial, null, outerLayers))
                .isInstanceOf(HvacEngineMissingArgumentException.class);
    }
}
