package com.synerset.hvacengine.property.fluids.liquidwater;

import com.synerset.unitility.unitsystem.thermodynamic.Density;
import com.synerset.unitility.unitsystem.thermodynamic.DynamicViscosity;
import com.synerset.unitility.unitsystem.thermodynamic.KinematicViscosity;
import com.synerset.unitility.unitsystem.thermodynamic.Temperature;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.withPrecision;

class LiquidWaterEquationsTest {

    @Test
    @DisplayName("should return liquid water specific enthalpy when water temperature is given")
    void specificEnthalpy_shouldReturnLiquidWaterSpecificEnthalpy_whenWaterTemperatureIsGiven() {
        // Given
        double waterTemp = 15.0;

        // When
        double actualSpecEnthalpy = LiquidWaterEquations.specificEnthalpy(waterTemp);

        // Then
        double expectedSpecEnthalpy = 62.83139309762801;
        assertThat(actualSpecEnthalpy).isEqualTo(expectedSpecEnthalpy);

    }

    @Test
    @DisplayName("should return liquid water density when water temperature is given")
    void specificEnthalpy_shouldReturnLiquidWaterDensity_whenWaterTemperatureIsGiven() {
        // Given
        double waterTemp = 15.0;

        // When
        double actualDensity = LiquidWaterEquations.density(waterTemp);

        // Then
        double expectedDensity = 998.8844003066922;
        assertThat(actualDensity).isEqualTo(expectedDensity);

    }

    @ParameterizedTest
    @MethodSource("specificHeatInlineData")
    @DisplayName("should return liquid water specific heat when water temperature is given")
    void specificEnthalpy_shouldReturnLiquidWaterSpecificHeat_whenWaterTemperatureIsGiven(double inletTemp, double expectedSpecHeat) {
        // Given
        // When
        double actualSpecHeat = LiquidWaterEquations.specificHeat(inletTemp);

        // Then
        assertThat(actualSpecHeat).isEqualTo(expectedSpecHeat, withPrecision(1E-4));
    }

    //INLINE DATA SEED -> Based on https://www.engineeringtoolbox.com/specific-heat-capacity-water-d_660.html
    static Stream<Arguments> specificHeatInlineData() {
        return Stream.of(
                Arguments.of(0.01, 4.2199),
                Arguments.of(10, 4.1955),
                Arguments.of(20, 4.1844),
                Arguments.of(25, 4.1816),
                Arguments.of(30, 4.1801),
                Arguments.of(40, 4.1796),
                Arguments.of(50, 4.1815),
                Arguments.of(60, 4.1851),
                Arguments.of(70, 4.1902),
                Arguments.of(80, 4.1969),
                Arguments.of(90, 4.2053)
        );
    }

    @ParameterizedTest
    @CsvSource({
            "298.15, 998, 0.000889735",
            "298.15, 1200, 0.001437649",
            "373.15, 1000, 0.000307883",
            "433.15, 1, 0.000014538",
            "433.15, 1000, 0.000217685",
            "873.15, 1, 0.000032619",
            "873.15, 100, 0.000035802",
            "873.15, 600, 0.000077430",
            "1173.15, 1, 0.000044217",
            "1173.15, 100, 0.000047640",
            "1173.15, 400, 0.000064154"
    })
    @DisplayName("should return liquid water dynamic viscosity when temperature and density is given")
    void testDynamicViscosity(double temperature, double density, double expectedViscosity) {
        double actualViscosityVal = LiquidWaterEquations.dynamicViscosity(temperature - 273.15, density);
        DynamicViscosity actualViscosity = LiquidWaterEquations.dynamicViscosity(Temperature.ofKelvins(temperature),
                Density.ofKilogramPerCubicMeter(density));
        assertThat(expectedViscosity)
                .isEqualTo(actualViscosityVal, Offset.offset(1E-6))
                .isEqualTo(actualViscosity.getValue(), Offset.offset(1E-6));
    }

    @Test
    @DisplayName("should return liquid water kinematic viscosity when temperature and density is given")
    void testKinematicViscosity(){
        // Given
        double tx = 25;
        double rho = 998;
        double expectedKinVis = 0.000889735 / rho;

        // When
        double actualKinVisValue = LiquidWaterEquations.kinematicViscosity(tx, rho);
        KinematicViscosity actualKinVis = LiquidWaterEquations.kinematicViscosity(Temperature.ofCelsius(25),
                Density.ofKilogramPerCubicMeter(rho));

        double actualKinVisFromDynVisValue = LiquidWaterEquations.kinematicViscosityFromDynVis(0.000889735, rho);
        KinematicViscosity actualKinVisFromDynVis = LiquidWaterEquations.kinematicViscosity(DynamicViscosity.ofPascalSecond(0.000889735),
                Density.ofKilogramPerCubicMeter(rho));

        // Then
        assertThat(actualKinVisValue).isEqualTo(expectedKinVis, withPrecision(1E-11));
        assertThat(actualKinVis.getInSquareMetersPerSecond()).isEqualTo(expectedKinVis, withPrecision(1E-11));
        assertThat(actualKinVisFromDynVisValue).isEqualTo(expectedKinVis, withPrecision(1E-11));
        assertThat(actualKinVisFromDynVis.getInSquareMetersPerSecond()).isEqualTo(expectedKinVis, withPrecision(1E-11));
    }

}
