package com.synerset.hvacengine.process.pressurechange;

import com.synerset.hvacengine.common.validation.CommonValidators;
import com.synerset.hvacengine.process.pressurechange.dataobject.PressureChangeResult;
import com.synerset.hvacengine.property.fluids.humidair.FlowOfHumidAir;
import com.synerset.hvacengine.property.fluids.humidair.HumidAir;
import com.synerset.unitility.unitsystem.thermodynamic.Power;
import com.synerset.unitility.unitsystem.thermodynamic.Pressure;
import com.synerset.unitility.unitsystem.thermodynamic.Temperature;

public class PressureChangeEquations {

    private PressureChangeEquations() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Calculates the effects of pressure drop due to friction in an airflow system.
     * It computes the temperature increase caused by mechanical energy dissipation into heat,
     * and decreases the outlet pressure accordingly.
     *
     * @param inletAirFlow the incoming flow of humid air
     * @param pressureDrop the pressure drop due to friction
     * @return PressureChangeResult containing the updated air properties after the pressure drop
     * @throws IllegalArgumentException if input parameters are null or pressure drop is negative
     */
    public static PressureChangeResult pressureDropDueFriction(FlowOfHumidAir inletAirFlow, Pressure pressureDrop) {
        CommonValidators.requireNotNull(inletAirFlow);
        CommonValidators.requireNotNull(pressureDrop);
        CommonValidators.requireAboveLowerBoundInclusive(pressureDrop, Pressure.ofPascal(0));

        if (pressureDrop.isCloseToZero() || inletAirFlow.getMassFlow().isCloseToZero()) {
            return PressureChangeResult.builder()
                    .processMode(PressureMode.PRESSURE_DROP)
                    .inletAirFlow(inletAirFlow)
                    .outletAirFlow(inletAirFlow)
                    .heatOfProcess(Power.ofWatts(0))
                    .pressureChange(pressureDrop)
                    .build();
        }

        double volFlow = inletAirFlow.getVolFlow().getInCubicMetersPerSecond();
        double massFlow = inletAirFlow.getMassFlow().getInKilogramsPerSecond();
        double specHeat = inletAirFlow.getSpecificHeat().getInJoulePerKiloGramKelvin();
        double tIn = inletAirFlow.getTemperature().getInCelsius();
        double pIn = inletAirFlow.getPressure().getInPascals();

        double pressLoss = pressureDrop.toPascal().getInPascals();
        double workDissipatedToHeat = volFlow * pressLoss;

        double tempDiff = workDissipatedToHeat / (massFlow * specHeat);

        // Determining outlet parameters
        double tOut = tIn + tempDiff;
        double pOut = pIn - pressLoss;

        HumidAir outletHumidAir = HumidAir.of(
                Pressure.ofPascal(pOut),
                Temperature.ofCelsius(tOut),
                inletAirFlow.getHumidityRatio()
        );
        FlowOfHumidAir outletFlow = FlowOfHumidAir.ofDryAirMassFlow(
                outletHumidAir,
                inletAirFlow.getDryAirMassFlow()
        );

        return PressureChangeResult.builder()
                .processMode(PressureMode.PRESSURE_DROP)
                .inletAirFlow(inletAirFlow)
                .outletAirFlow(outletFlow)
                .heatOfProcess(Power.ofWatts(workDissipatedToHeat))
                .pressureChange(pressureDrop)
                .build();
    }

}
