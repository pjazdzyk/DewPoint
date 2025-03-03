package com.synerset.hvacengine.hydraulic;

import com.synerset.hvacengine.common.exception.HvacEngineMissingArgumentException;
import com.synerset.hvacengine.hydraulic.material.MaterialLayer;
import com.synerset.hvacengine.hydraulic.material.Materials;
import com.synerset.hvacengine.hydraulic.structure.CircularStructure;
import com.synerset.hvacengine.property.fluids.humidair.FlowOfHumidAir;
import com.synerset.hvacengine.property.fluids.humidair.HumidAir;
import com.synerset.unitility.unitsystem.common.Diameter;
import com.synerset.unitility.unitsystem.common.Height;
import com.synerset.unitility.unitsystem.common.Length;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.withPrecision;

class HydraulicConduitTest {

    @Test
    @DisplayName("Conduit: should create conduit from flow and geometry, and calculate all internal properties")
    void createHydraulicConduitTest() {
        // Given
        Diameter diameter = Diameter.ofMillimeters(200);
        Length ductLength = Length.ofMeters(10);
        MaterialLayer baseMaterialLayer = MaterialLayer.builder().materialData(Materials.INDUSTRIAL_STEEL).thickness(Height.ofMillimeters(1)).build();
        MaterialLayer insulationLayer = MaterialLayer.builder().materialData(Materials.INDUSTRIAL_STEEL).thickness(Height.ofMillimeters(50)).build();
        CircularStructure circularStructure = CircularStructure.builder()
                .baseMaterial(baseMaterialLayer)
                .outerMaterialLayers(List.of(insulationLayer))
                .innerDiameter(diameter)
                .build();

        FlowOfHumidAir flowOfHumidAir = FlowOfHumidAir.ofValues(10, 50, 500);

        // When
        HydraulicConduit<CircularStructure, HumidAir> actualConduit = new HydraulicConduit<>(circularStructure, flowOfHumidAir, ductLength);

        // Then
        assertThat(actualConduit).isNotNull();
        assertThat(actualConduit.getFlowOfFluid()).isEqualTo(flowOfHumidAir);
        assertThat(actualConduit.getStructure()).isEqualTo(circularStructure);
        assertThat(actualConduit.getLength()).isEqualTo(ductLength);
        assertThat(actualConduit.getVelocity().getInMetersPerSecond()).isEqualTo(4.42097, withPrecision(1E-3));
        assertThat(actualConduit.getCharacteristicLength()).isEqualTo(Length.of(diameter));
        assertThat(actualConduit.getReynoldsNumber().getValue()).isEqualTo(62503.6766, withPrecision(1E-3));
        assertThat(actualConduit.getFrictionFactor().getValue()).isEqualTo(0.0233350, withPrecision(1E-5));
        assertThat(actualConduit.getLinearResistance().getInPascalPerMeter()).isEqualTo(1.4125, withPrecision(1E-3));
        assertThat(actualConduit.getLinearPressureLoss().getInPascals()).isEqualTo(14.125, withPrecision(1E-3));

        assertThat(actualConduit).isEqualTo(new HydraulicConduit<>(circularStructure, flowOfHumidAir, ductLength))
                .hasSameHashCodeAs(new HydraulicConduit<>(circularStructure, flowOfHumidAir, ductLength));
        assertThat(actualConduit.toString()).contains("HydraulicConduit");

    }

    @Test
    @DisplayName("Conduit: should throw exception if structure is null")
    void createHydraulicConduitWithNullStructureTest() {
        // Given
        FlowOfHumidAir flowOfHumidAir = FlowOfHumidAir.ofValues(10, 50, 500);
        Length ductLength = Length.ofMeters(10);

        // When & Then
        assertThatThrownBy(() -> new HydraulicConduit<>(null, flowOfHumidAir, ductLength))
                .isInstanceOf(HvacEngineMissingArgumentException.class);
    }

    @Test
    @DisplayName("Conduit: should throw exception if flowOfFluid is null")
    void createHydraulicConduitWithNullFlowTest() {
        // Given
        Diameter diameter = Diameter.ofMillimeters(200);
        Length ductLength = Length.ofMeters(10);
        MaterialLayer baseMaterialLayer = MaterialLayer.builder().materialData(Materials.INDUSTRIAL_STEEL).thickness(Height.ofMillimeters(1)).build();
        CircularStructure circularStructure = CircularStructure.builder()
                .baseMaterial(baseMaterialLayer)
                .innerDiameter(diameter)
                .build();

        // When & Then
        assertThatThrownBy(() -> new HydraulicConduit<>(circularStructure, null, ductLength))
                .isInstanceOf(HvacEngineMissingArgumentException.class);
    }

    @Test
    @DisplayName("Conduit: should throw exception if length is null")
    void createHydraulicConduitWithNullLengthTest() {
        // Given
        Diameter diameter = Diameter.ofMillimeters(200);
        MaterialLayer baseMaterialLayer = MaterialLayer.builder().materialData(Materials.INDUSTRIAL_STEEL).thickness(Height.ofMillimeters(1)).build();
        CircularStructure circularStructure = CircularStructure.builder()
                .baseMaterial(baseMaterialLayer)
                .innerDiameter(diameter)
                .build();
        FlowOfHumidAir flowOfHumidAir = FlowOfHumidAir.ofValues(10, 50, 500);

        // When & Then
        assertThatThrownBy(() -> new HydraulicConduit<>(circularStructure, flowOfHumidAir, null))
                .isInstanceOf(HvacEngineMissingArgumentException.class);
    }

    @Test
    @DisplayName("Conduit: should correctly create conduit using static factory method 'of'")
    void staticFactoryTest() {
        // Given
        Diameter diameter = Diameter.ofMillimeters(200);
        Length ductLength = Length.ofMeters(10);
        MaterialLayer baseMaterialLayer = MaterialLayer.builder().materialData(Materials.INDUSTRIAL_STEEL).thickness(Height.ofMillimeters(1)).build();
        CircularStructure circularStructure = CircularStructure.builder()
                .baseMaterial(baseMaterialLayer)
                .innerDiameter(diameter)
                .build();
        FlowOfHumidAir flowOfHumidAir = FlowOfHumidAir.ofValues(10, 50, 500);

        // When
        HydraulicConduit<CircularStructure, HumidAir> actualConduit = HydraulicConduit.of(circularStructure, flowOfHumidAir, ductLength);

        // Then
        assertThat(actualConduit).isNotNull();
        assertThat(actualConduit.getFlowOfFluid()).isEqualTo(flowOfHumidAir);
        assertThat(actualConduit.getStructure()).isEqualTo(circularStructure);
        assertThat(actualConduit.getLength()).isEqualTo(ductLength);
    }

    @Test
    @DisplayName("Conduit: should correctly create conduit using static factory method 'of' with default length")
    void staticFactoryWithDefaultLengthTest() {
        // Given
        Diameter diameter = Diameter.ofMillimeters(200);
        MaterialLayer baseMaterialLayer = MaterialLayer.builder().materialData(Materials.INDUSTRIAL_STEEL).thickness(Height.ofMillimeters(1)).build();
        CircularStructure circularStructure = CircularStructure.builder()
                .baseMaterial(baseMaterialLayer)
                .innerDiameter(diameter)
                .build();
        FlowOfHumidAir flowOfHumidAir = FlowOfHumidAir.ofValues(10, 50, 500);

        // When
        HydraulicConduit<CircularStructure, HumidAir> actualConduit = HydraulicConduit.of(circularStructure, flowOfHumidAir);

        // Then
        assertThat(actualConduit).isNotNull();
        assertThat(actualConduit.getFlowOfFluid()).isEqualTo(flowOfHumidAir);
        assertThat(actualConduit.getStructure()).isEqualTo(circularStructure);
        assertThat(actualConduit.getLength()).isEqualTo(Length.ofMeters(1.0));
    }

    @Test
    @DisplayName("Conduit: should modify structure with 'with' method")
    void withStructureTest() {
        // Given
        Diameter diameter = Diameter.ofMillimeters(200);
        Length ductLength = Length.ofMeters(10);
        MaterialLayer baseMaterialLayer = MaterialLayer.builder().materialData(Materials.INDUSTRIAL_STEEL).thickness(Height.ofMillimeters(1)).build();
        CircularStructure circularStructure = CircularStructure.builder()
                .baseMaterial(baseMaterialLayer)
                .innerDiameter(diameter)
                .build();
        FlowOfHumidAir flowOfHumidAir = FlowOfHumidAir.ofValues(10, 50, 500);
        HydraulicConduit<CircularStructure, HumidAir> originalConduit = new HydraulicConduit<>(circularStructure, flowOfHumidAir, ductLength);

        // When
        CircularStructure newStructure = CircularStructure.builder()
                .baseMaterial(baseMaterialLayer)
                .innerDiameter(Diameter.ofMillimeters(250))
                .build();
        HydraulicConduit<CircularStructure, HumidAir> modifiedConduit = originalConduit.withStructure(newStructure);

        // Then
        assertThat(modifiedConduit.getStructure()).isEqualTo(newStructure);
        assertThat(modifiedConduit.getLength()).isEqualTo(originalConduit.getLength());
    }

}