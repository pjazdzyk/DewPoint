package com.synerset.hvacengine.hydraulic;

import com.synerset.hvacengine.hydraulic.dataobject.LocalLossFactorData;
import com.synerset.hvacengine.hydraulic.dataobject.LocalLossPressureData;
import com.synerset.unitility.unitsystem.hydraulic.LocalLossFactor;
import com.synerset.unitility.unitsystem.thermodynamic.Pressure;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LocalLossInputDataTest {

    @Test
    void testConstructorWithBothLists() {
        // Prepare test data
        LocalLossFactorData coilCoef1 = LocalLossFactorData.builder().name("Coil1").lossCoefficient(LocalLossFactor.of(0.5)).build();
        LocalLossFactorData coilCoef2 = new LocalLossFactorData("Coil2", LocalLossFactor.of(0.5));
        LocalLossPressureData valve1 = LocalLossPressureData.builder().name("Valve1").localPressureLoss(Pressure.ofPascal(100)).build();
        LocalLossPressureData valve2 = new LocalLossPressureData("Valve2", Pressure.ofPascal(100));

        // Create LocalLossInputData with both lists
        LocalLossInputData inputData = new LocalLossInputData(List.of(coilCoef1, coilCoef2), List.of(valve1, valve2));

        // Assertions
        assertThat(inputData.getLocalLossCoefficients()).hasSize(2);
        assertThat(inputData.getLocalLossPressures()).hasSize(2);
        assertThat(inputData.getSumOfAllInputLossCoefs()).isEqualTo(LocalLossFactor.of(1.0));
        assertThat(inputData.getTotalInputPressureLoss()).isEqualTo(Pressure.ofPascal(200));
    }

    @Test
    void testConstructorWithOnlyLossCoefficients() {
        // Prepare test data
        LocalLossFactorData coefficient = new LocalLossFactorData("Coil1", LocalLossFactor.of(0.7));

        // Create LocalLossInputData with only coefficients
        LocalLossInputData inputData = new LocalLossInputData(List.of(coefficient), null);

        // Assertions
        assertThat(inputData.getLocalLossCoefficients()).hasSize(1);
        assertThat(inputData.getLocalLossPressures()).isEmpty();
        assertThat(inputData.getSumOfAllInputLossCoefs()).isEqualTo(LocalLossFactor.of(0.7));
        assertThat(inputData.getTotalInputPressureLoss()).isEqualTo(Pressure.ofPascal(0));
    }

    @Test
    void testConstructorWithOnlyLossPressures() {
        // Prepare test data
        LocalLossPressureData pressure = new LocalLossPressureData("Pressure1", Pressure.ofPascal(150));

        // Create LocalLossInputData with only pressures
        LocalLossInputData inputData = new LocalLossInputData(null, List.of(pressure));

        // Assertions
        assertThat(inputData.getLocalLossCoefficients()).isEmpty();
        assertThat(inputData.getLocalLossPressures()).hasSize(1);
        assertThat(inputData.getSumOfAllInputLossCoefs()).isEqualTo(LocalLossFactor.of(0.0));
        assertThat(inputData.getTotalInputPressureLoss()).isEqualTo(Pressure.ofPascal(150));
    }

    @Test
    void testCreateEmpty() {
        // Create an empty LocalLossInputData
        LocalLossInputData inputData = LocalLossInputData.createEmpty();

        // Assertions
        assertThat(inputData.getLocalLossCoefficients()).isEmpty();
        assertThat(inputData.getLocalLossPressures()).isEmpty();
        assertThat(inputData.getSumOfAllInputLossCoefs()).isEqualTo(LocalLossFactor.of(0.0));
        assertThat(inputData.getTotalInputPressureLoss()).isEqualTo(Pressure.ofPascal(0));
    }

    @Test
    void testNullConstructorParameters() {
        // Create LocalLossInputData with null parameters
        LocalLossInputData inputData = new LocalLossInputData(null, null);

        // Assertions
        assertThat(inputData.getLocalLossCoefficients()).isEmpty();
        assertThat(inputData.getLocalLossPressures()).isEmpty();
    }

}
