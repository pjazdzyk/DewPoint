package com.synerset.hvacengine.hydraulic.material;

import com.synerset.unitility.unitsystem.common.Height;
import com.synerset.unitility.unitsystem.thermodynamic.Density;
import com.synerset.unitility.unitsystem.thermodynamic.ThermalConductivity;

public class Materials {

    private Materials() {
        throw new IllegalStateException("Utility class");
    }

    public static final MaterialData INDUSTRIAL_STEEL = MaterialData.builder()
            .name("Industrial Steel")
            .density(Density.ofKilogramPerCubicMeter(7850))
            .thermalConductivity(ThermalConductivity.ofWattsPerMeterKelvin(54))
            .absoluteRoughness(Height.ofMillimeters(0.2))
            .build();

    public static final MaterialData ALUMINIUM = MaterialData.builder()
            .name("Aluminium")
            .density(Density.ofKilogramPerCubicMeter(2700))
            .thermalConductivity(ThermalConductivity.ofWattsPerMeterKelvin(205))
            .absoluteRoughness(Height.ofMillimeters(0.0015))
            .build();

    public static final MaterialData PVC = MaterialData.builder()
            .name("PVC")
            .density(Density.ofKilogramPerCubicMeter(1380))
            .thermalConductivity(ThermalConductivity.ofWattsPerMeterKelvin(0.19))
            .absoluteRoughness(Height.ofMillimeters(0.0015))
            .build();

    public static final MaterialData INSUL_MINERAL_WOOL = MaterialData.builder()
            .name("Insulating Mineral Wool")
            .density(Density.ofKilogramPerCubicMeter(80))
            .thermalConductivity(ThermalConductivity.ofWattsPerMeterKelvin(0.036))
            .absoluteRoughness(Height.ofMillimeters(1.0))
            .build();

    public static final MaterialData INSUL_RUBBER_FOAM = MaterialData.builder()
            .name("Insulating Rubber Foam")
            .density(Density.ofKilogramPerCubicMeter(100))
            .thermalConductivity(ThermalConductivity.ofWattsPerMeterKelvin(0.035))
            .absoluteRoughness(Height.ofMillimeters(0.5))
            .build();
}
