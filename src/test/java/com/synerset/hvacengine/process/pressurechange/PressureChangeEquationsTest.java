package com.synerset.hvacengine.process.pressurechange;

import com.synerset.hvacengine.process.ProcessType;
import com.synerset.hvacengine.process.pressurechange.dataobject.PressureChangeResult;
import com.synerset.hvacengine.property.fluids.humidair.FlowOfHumidAir;
import com.synerset.hvacengine.property.fluids.humidair.HumidAir;
import com.synerset.unitility.unitsystem.flow.VolumetricFlow;
import com.synerset.unitility.unitsystem.humidity.HumidityRatio;
import com.synerset.unitility.unitsystem.thermodynamic.Power;
import com.synerset.unitility.unitsystem.thermodynamic.Pressure;
import com.synerset.unitility.unitsystem.thermodynamic.Temperature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.withPrecision;

class PressureChangeEquationsTest {

    @Test
    @DisplayName("Pressure drop process: should successfully dissipate friction pressure loss to heat")
    void pressureDropDueFrictionTest() {
        // Given
        HumidAir humidAir = HumidAir.of(
                Pressure.STANDARD_ATMOSPHERE,
                Temperature.ofCelsius(25),
                HumidityRatio.ofKilogramPerKilogram(0.01)
        );

        FlowOfHumidAir flowOfHumidAir = FlowOfHumidAir.of(humidAir, VolumetricFlow.ofCubicMetersPerSecond(1.5));

        Pressure pressureDrop = Pressure.ofPascal(300);

        // When
        PressureChangeResult pressureChangeResult = PressureChangeEquations.pressureDropDueFriction(flowOfHumidAir, pressureDrop);

        // Then
        assertThat(pressureChangeResult).isNotNull();
        assertThat(pressureChangeResult.processType()).isEqualTo(ProcessType.PRESSURE_CHANGE);
        assertThat(pressureChangeResult.processMode()).isEqualTo(PressureMode.PRESSURE_DROP);
        assertThat(pressureChangeResult.heatOfProcess()).isEqualTo(Power.ofWatts(450));

        assertThat(pressureChangeResult.inletAirFlow()).isEqualTo(flowOfHumidAir);
        assertThat(pressureChangeResult.outletAirFlow().getDryAirMassFlow()).isEqualTo(flowOfHumidAir.getDryAirMassFlow());
        assertThat(pressureChangeResult.outletAirFlow().getMassFlow()).isEqualTo(flowOfHumidAir.getMassFlow());
        assertThat(pressureChangeResult.outletAirFlow().getHumidityRatio()).isEqualTo(flowOfHumidAir.getHumidityRatio());

        assertThat(pressureChangeResult.outletAirFlow().getPressure()).isEqualTo(flowOfHumidAir.getPressure().minus(pressureDrop));
        assertThat(pressureChangeResult.outletAirFlow().getTemperature().getValue()).isEqualTo(25.2515, withPrecision(1E-4));
    }

}