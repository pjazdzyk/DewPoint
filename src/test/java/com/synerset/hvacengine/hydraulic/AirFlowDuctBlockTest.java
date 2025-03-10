package com.synerset.hvacengine.hydraulic;

import com.synerset.hvacengine.hydraulic.dataobject.HydraulicLossResult;
import com.synerset.hvacengine.hydraulic.dataobject.HydraulicProcessResult;
import com.synerset.hvacengine.hydraulic.dataobject.LocalLossFactorData;
import com.synerset.hvacengine.hydraulic.dataobject.LocalLossPressureData;
import com.synerset.hvacengine.hydraulic.material.MaterialLayer;
import com.synerset.hvacengine.hydraulic.material.Materials;
import com.synerset.hvacengine.hydraulic.structure.CircularStructure;
import com.synerset.hvacengine.process.ProcessType;
import com.synerset.hvacengine.process.source.SimpleDataSource;
import com.synerset.hvacengine.property.fluids.humidair.FlowOfHumidAir;
import com.synerset.unitility.unitsystem.common.Diameter;
import com.synerset.unitility.unitsystem.common.Height;
import com.synerset.unitility.unitsystem.common.Length;
import com.synerset.unitility.unitsystem.hydraulic.LocalLossFactor;
import com.synerset.unitility.unitsystem.thermodynamic.Power;
import com.synerset.unitility.unitsystem.thermodynamic.Pressure;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.withPrecision;

class AirFlowDuctBlockTest {

    @Test
    @DisplayName("VentilationDuct: should successfully create ventilation duct and resolve pressure difference")
    void shouldCreateVentilationDuctBlockAndCalculateResults() {
        // Given
        MaterialLayer baseMaterialLayer = MaterialLayer.builder()
                .thickness(Height.ofMillimeters(1))
                .material(Materials.INDUSTRIAL_STEEL)
                .build();

        CircularStructure circularStructure = CircularStructure.builder()
                .baseMaterial(baseMaterialLayer)
                .innerDiameter(Diameter.ofMillimeters(200))
                .build();

        SimpleDataSource<FlowOfHumidAir> inletFlowSource = SimpleDataSource.of(FlowOfHumidAir.ofValues(20, 50, 500));

        LocalLossInputData localLossData = LocalLossInputData.of(
                List.of(LocalLossFactorData.of(LocalLossFactor.of(0.2)), LocalLossFactorData.of(LocalLossFactor.of(0.2))),
                List.of(LocalLossPressureData.of(Pressure.ofPascal(10)), LocalLossPressureData.of(Pressure.ofPascal(10)))
        );

        AirFlowDuctBlock airFlowDuctBlock = AirFlowDuctBlock.of(circularStructure, Length.ofMeters(100), localLossData, inletFlowSource);

        // When
        HydraulicProcessResult actualProcessResults = (HydraulicProcessResult) airFlowDuctBlock.runProcessCalculations();
        HydraulicLossResult actualHydraulicLossResults = airFlowDuctBlock.getHydraulicLossResult();

        // Then
        assertThat(actualProcessResults).isNotNull();
        assertThat(actualHydraulicLossResults).isNotNull();

        assertThat(actualProcessResults).isEqualTo(airFlowDuctBlock.getProcessResult());
        assertThat(actualProcessResults.processType()).isEqualTo(ProcessType.CONDUIT_FLOW);

        assertThat(airFlowDuctBlock.getInputConnector()).isNotNull();
        assertThat(airFlowDuctBlock.getOutputConnector()).isNotNull();
        assertThat(airFlowDuctBlock.getConduitLength()).isEqualTo(Length.ofMeters(100));
        assertThat(airFlowDuctBlock.getConduitStructureData()).isEqualTo(circularStructure);

        assertThat(actualHydraulicLossResults.totalPressureLoss().getInPascals()).isEqualTo(161.439, withPrecision(1E-3));
        assertThat(actualProcessResults.outletAirFlow().getPressure()).isEqualTo(actualProcessResults.inletAirFlow().getPressure().minus(actualHydraulicLossResults.totalPressureLoss()));
        assertThat(actualProcessResults.outletAirFlow().getHumidityRatio()).isEqualTo(actualProcessResults.inletAirFlow().getHumidityRatio());
        assertThat(actualProcessResults.outletAirFlow().getRelativeHumidity().getInPercent()).isLessThan(actualProcessResults.inletAirFlow().getRelativeHumidity().getInPercent());

        assertThat(actualProcessResults.volume().getInCubicMeters()).isEqualTo(3.14159, withPrecision(1E-3));
        assertThat(actualProcessResults.length()).isEqualTo(Length.ofMeters(100));
        assertThat(actualProcessResults.volume().getInCubicMeters()).isEqualTo(3.14159, withPrecision(1E-3));
        assertThat(actualProcessResults.heatOfProcess()).isEqualTo(Power.ofWatts(0));
        assertThat(actualProcessResults.velocity().getInMetersPerSecond()).isEqualTo(4.42097, withPrecision(1E-3));
    }

}