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

class EllipticStructureTest {

    @Test
    @DisplayName("Should correctly calculate properties for EllipticStructure")
    void shouldCalculatePropertiesCorrectly() {
        // Given: Base material and insulation layers
        MaterialLayer baseMaterial = new MaterialLayer(Materials.INDUSTRIAL_STEEL, Height.ofMillimeters(1.2));
        MaterialLayer insulationLayer = new MaterialLayer(Materials.INSUL_MINERAL_WOOL, Height.ofMillimeters(20));
        MaterialLayer nextInsulationLayer = new MaterialLayer(Materials.INSUL_MINERAL_WOOL, Height.ofMillimeters(20));

        Diameter innerMajorAxis = Diameter.ofMillimeters(1000);
        Diameter innerMinorAxis = Diameter.ofMillimeters(600);
        List<MaterialLayer> outerLayers = List.of(insulationLayer, nextInsulationLayer);

        // When: Creating EllipticStructure
        EllipticStructure structure = new EllipticStructure(baseMaterial, innerMajorAxis, innerMinorAxis, outerLayers);

        // Expected Values
        Height totalThickness = baseMaterial.thickness().plus(insulationLayer.thickness()).plus(nextInsulationLayer.thickness());
        Diameter expectedOuterMajorAxis = innerMajorAxis.plus(totalThickness.multiply(2.0));
        Diameter expectedOuterMinorAxis = innerMinorAxis.plus(totalThickness.multiply(2.0));

        Perimeter expectedInnerPerimeter = StructureEquations.ellipticPerimeter(innerMajorAxis, innerMinorAxis);
        Area expectedInnerSectionArea = StructureEquations.ellipticArea(innerMajorAxis, innerMinorAxis);
        Perimeter expectedOuterPerimeter = StructureEquations.ellipticPerimeter(expectedOuterMajorAxis, expectedOuterMinorAxis);
        Area expectedOuterSectionArea = StructureEquations.ellipticArea(expectedOuterMajorAxis, expectedOuterMinorAxis);
        Diameter expectedEquivHydraulicDiameter = StructureEquations.equivalentDiameter(expectedInnerSectionArea, expectedInnerPerimeter);

        // Then: Validate calculations using AssertJ
        assertThat(structure.getInnerMajorAxis()).isEqualTo(innerMajorAxis);
        assertThat(structure.getInnerMinorAxis()).isEqualTo(innerMinorAxis);
        assertThat(structure.getOuterMajorAxis()).isEqualTo(expectedOuterMajorAxis);
        assertThat(structure.getOuterMinorAxis()).isEqualTo(expectedOuterMinorAxis);

        assertThat(structure.getInnerPerimeter()).isEqualTo(expectedInnerPerimeter);
        assertThat(structure.getInnerSectionArea()).isEqualTo(expectedInnerSectionArea);

        assertThat(structure.getOuterPerimeter()).isEqualTo(expectedOuterPerimeter);
        assertThat(structure.getOuterSectionArea()).isEqualTo(expectedOuterSectionArea);

        assertThat(structure.getEquivHydraulicDiameter()).isEqualTo(expectedEquivHydraulicDiameter);

        // Validate material layers
        assertThat(structure.getConduitBaseLayer()).isEqualTo(baseMaterial);
        assertThat(structure.getOuterLayers()).containsExactlyElementsOf(outerLayers);

        // Validate linear mass density
        assertThat(structure.getBaseLinearMassDensity().getInKilogramsPerMeter()).isEqualTo(29.629, withPrecision(1E-3));
        assertThat(structure.getTotalLinearMassDensity().getInKilogramsPerMeter()).isEqualTo(40.108, withPrecision(1E-3));

        // Others
        assertThat(structure).isEqualTo(new EllipticStructure(baseMaterial, innerMajorAxis, innerMinorAxis, outerLayers))
                .hasSameHashCodeAs(new EllipticStructure(baseMaterial, innerMajorAxis, innerMinorAxis, outerLayers));
        assertThat(structure.toString()).contains("EllipticStructure");

        assertThat(structure.getConduitShape()).isEqualTo(ConduitShape.ELLIPTIC);
    }

    @Test
    @DisplayName("Should handle null outer layers correctly")
    void shouldHandleNullOuterLayers() {
        // Given: Base material
        MaterialLayer baseMaterial = new MaterialLayer(Materials.ALUMINIUM, Height.ofMillimeters(3));
        Diameter innerMajorAxis = Diameter.ofMillimeters(150);
        Diameter innerMinorAxis = Diameter.ofMillimeters(75);

        // When: Creating EllipticStructure with null outer layers
        EllipticStructure structure = new EllipticStructure(baseMaterial, innerMajorAxis, innerMinorAxis, null);

        // Then: Outer layers should be an empty list
        assertThat(structure.getOuterLayers()).isEmpty();
    }

    @Test
    @DisplayName("Should throw exception for null base material")
    void shouldThrowExceptionForNullBaseMaterial() {
        // Given: Null base material
        Diameter innerMajorAxis = Diameter.ofMillimeters(100);
        Diameter innerMinorAxis = Diameter.ofMillimeters(50);
        List<MaterialLayer> outerLayers = Collections.emptyList();

        // Then: Expect exception when baseMaterial is null
        assertThatThrownBy(() -> new EllipticStructure(null, innerMajorAxis, innerMinorAxis, outerLayers))
                .isInstanceOf(HvacEngineMissingArgumentException.class);
    }

    @Test
    @DisplayName("Should throw exception for null inner minor axis")
    void shouldThrowExceptionForNullInnerMajorAxis() {
        // Given: Null inner major axis
        MaterialLayer baseMaterial = new MaterialLayer(Materials.PVC, Height.ofMillimeters(2));
        Diameter innerMinorAxis = Diameter.ofMillimeters(50);
        List<MaterialLayer> outerLayers = Collections.emptyList();

        // Then: Expect exception when innerMajorAxis is null
        assertThatThrownBy(() -> new EllipticStructure(baseMaterial, null, innerMinorAxis, outerLayers))
                .isInstanceOf(HvacEngineMissingArgumentException.class);
    }

    @Test
    @DisplayName("Should throw exception for null inner major axis")
    void shouldThrowExceptionForNullInnerMinorAxis() {
        // Given: Null inner minor axis
        MaterialLayer baseMaterial = new MaterialLayer(Materials.PVC, Height.ofMillimeters(2));
        Diameter innerMajorAxis = Diameter.ofMillimeters(100);
        List<MaterialLayer> outerLayers = Collections.emptyList();

        // Then: Expect exception when innerMinorAxis is null
        assertThatThrownBy(() -> new EllipticStructure(baseMaterial, innerMajorAxis, null, outerLayers))
                .isInstanceOf(HvacEngineMissingArgumentException.class);
    }
}
