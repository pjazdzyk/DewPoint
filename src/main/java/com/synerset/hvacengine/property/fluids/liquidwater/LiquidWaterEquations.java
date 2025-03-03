package com.synerset.hvacengine.property.fluids.liquidwater;

import com.synerset.hvacengine.common.validation.CommonValidators;
import com.synerset.unitility.unitsystem.thermodynamic.*;

import java.util.List;

/**
 * LIQUID WATER EQUATIONS LIBRARY (PSYCHROMETRICS) <p>
 * Set of static methods outputs process result as an array with process heat, core output air parameters (temperature, humidity ratio) and condensate
 * properties. Methods do not create a separate instance of FlowOfMoistAir for performance reasons - each ot these methods may be used in iterative solvers, and we
 * do not want to lose memory or performance for unnecessary object creation. <p>
 * <p>
 * REFERENCE SOURCES: <p>
 * [1] F.E. Jones, G.L. Harris. ITS-90 Density of water formulation for volumetric standards' calibration. Journal of Research of the National Institute of Standards and Technology (1992) <p>
 * [2] Water specific heat tables: https://www.engineeringtoolbox.com/specific-heat-capacity-water-d_660.html <p>
 * [3] Antoine Equation Coefficient for pure substances: https://myengineeringtools.com/Data_Diagrams/Antoine_Law_Coefficients.html <p>
 * [4] M. L. Huber,a… R. A. Perkins, A. Laesecke, and D. G. Friend. J. V. Sengers M. J. Assael and I. N. Metaxa E. Vogel R. Mareš K. Miyagawa. New International Formulation for the Viscosity of H2O. Journal of Research of the National Institute of Standards and Technology (1992) <p>
 * <p>
 * REFERENCES LEGEND KEY: <p>
 * [reference no] [value symbology in standard, unit] (equation number) [page] <p>
 *
 * @author Piotr Jażdżyk, MSc Eng
 */
public final class LiquidWaterEquations {
    public static final double WATER_CRITICAL_TEMPERATURE_IN_K = 647.096;   // [K]
    public static final double HEAT_OF_WATER_VAPORIZATION = 2500.8982920;   // [kJ/kg]   - Water heat of vaporization (t=0oC)
    public static final double WATER_CRITICAL_DENSITY = 322.0;              // [kg/m3    - Water critical density

    private record Triple(int i, int j, double coefH) {
    }

    private static final List<Triple> hCoefficients = List.of(
            new Triple(0, 0, 0.520094),    // H(0,0)
            new Triple(1, 0, 0.0850895),   // H(1,0)
            new Triple(2, 0, -1.08374),    // H(2,0)
            new Triple(3, 0, -0.289555),   // H(3,0)
            new Triple(0, 1, 0.222531),    // H(0,1)
            new Triple(1, 1, 0.999115),    // H(1,1)
            new Triple(2, 1, 1.88797),     // H(2,1)
            new Triple(3, 1, 1.26613),     // H(3,1)
            new Triple(5, 1, 0.120573),    // H(5,1)
            new Triple(0, 2, -0.281378),   // H(0,2)
            new Triple(1, 2, -0.906851),   // H(1,2)
            new Triple(2, 2, -0.772479),   // H(2,2)
            new Triple(3, 2, -0.489837),   // H(3,2)
            new Triple(4, 2, -0.257040),   // H(4,2)
            new Triple(0, 3, 0.161913),    // H(0,3)
            new Triple(1, 3, 0.257399),    // H(1,3)
            new Triple(0, 4, -0.0325372),  // H(0,4)
            new Triple(3, 4, 0.0698452),   // H(3,4)
            new Triple(4, 5, 0.00872102),  // H(4,5)
            new Triple(3, 6, -0.00435673), // H(3,6)
            new Triple(5, 6, -0.000593264) // H(5,6)
    );

    private LiquidWaterEquations() {
    }

    /**
     * Returns water enthalpy at provided temperature in kJ/kg<p>
     * Outputs 0.0 for negative temperatures.
     * REFERENCE SOURCE: [-] [kJ/kg] (-) [-]<p>
     * EQUATION LIMITS: n/a <p>
     *
     * @param tx water temperature, oC
     * @return water enthalpy at provided temperature, kJ/kg
     */
    public static double specificEnthalpy(double tx) {
        return tx < 0.0 ? 0.0 : tx * specificHeat(tx);
    }

    public static SpecificEnthalpy specificEnthalpy(Temperature temperature) {
        CommonValidators.requireNotNull(temperature);
        double specificEnthalpyVal = specificEnthalpy(temperature.getInCelsius());
        return SpecificEnthalpy.ofKiloJoulePerKiloGram(specificEnthalpyVal);
    }

    /**
     * Returns water density at provided temperature and constant pressure at 101.325kPa Pa<p>
     * REFERENCE SOURCE: [1] [kg/m3] (1) [kg/m3]<p>
     * EQUATION LIMITS: {0.0 oC,+150.0 oC} at Pat=101.325kPa <p>
     *
     * @param tx water temperature, oC
     * @return water density at temperature tx and atmospheric pressure, kg/m3
     */
    public static double density(double tx) {
        return (999.83952 + 16.945176 * tx
                - 7.9870401 * Math.pow(10, -3) * Math.pow(tx, 2)
                - 46.170461 * Math.pow(10, -6) * Math.pow(tx, 3)
                + 105.56302 * Math.pow(10, -9) * Math.pow(tx, 4)
                - 280.54253 * Math.pow(10, -12) * Math.pow(tx, 5))
               / (1 + 16.89785 * Math.pow(10, -3) * tx);
    }

    public static Density density(Temperature temperature) {
        CommonValidators.requireNotNull(temperature);
        double densityVal = density(temperature.getInCelsius());
        return Density.ofKilogramPerCubicMeter(densityVal);
    }

    /**
     * Returns water isobaric specific heat kJ/kgK<p>
     * REFERENCE SOURCE: [2] [kJ/kgK] (1) [kg/m3]<p>
     * EQUATION LIMITS: {0.0 oC,+250 oC}<p>
     *
     * @param tx water temperature, oC
     * @return water isobaric specific heat
     */
    public static double specificHeat(double tx) {
        if (tx > 0 && tx <= 100) {
            return 3.93240161 * Math.pow(10, -13) * Math.pow(tx, 6)
                   - 1.525847751 * Math.pow(10, -10) * Math.pow(tx, 5)
                   + 2.479227180 * Math.pow(10, -8) * Math.pow(tx, 4)
                   - 2.166932275 * Math.pow(10, -6) * Math.pow(tx, 3)
                   + 1.156152199 * Math.pow(10, -4) * Math.pow(tx, 2)
                   - 3.400567477 * Math.pow(10, -3) * tx + 4.219924305;
        } else {
            return 2.588246403 * Math.pow(10, -15) * Math.pow(tx, 7)
                   - 3.604612987 * Math.pow(10, -12) * Math.pow(tx, 6)
                   + 2.112059173 * Math.pow(10, -9) * Math.pow(tx, 5)
                   - 6.727469888 * Math.pow(10, -7) * Math.pow(tx, 4)
                   + 1.255841880 * Math.pow(10, -4) * Math.pow(tx, 3)
                   - 1.370455849 * Math.pow(10, -2) * Math.pow(tx, 2)
                   + 8.093157187 * Math.pow(10, -1) * tx - 15.75651097;
        }
    }

    public static SpecificHeat specificHeat(Temperature temperature) {
        CommonValidators.requireNotNull(temperature);
        double specHeatVal = specificHeat(temperature.getInCelsius());
        return SpecificHeat.ofKiloJoulePerKiloGramKelvin(specHeatVal);
    }

    /**
     * Calculates the dynamic viscosity of water (in Pa·s) given the temperature (C) and density (kg/m³)
     * using a simplified version of the IAPWS-2008 formulation.
     * For simplicity, it is assumed that μ̄₂ = 1.
     * REFERENCE SOURCE: [4] [Pa*s] (11) [109] and (12) [109]<p>
     * EQUATION LIMITS: n/a <p>
     *
     * @param tx  Temperature in Celsius.
     * @param rho Density in kg/m³.
     * @return Dynamic viscosity in Pa·s.
     */
    public static double dynamicViscosity(double tx, double rho) {
        CommonValidators.requireNonZero(rho, "Density");

        double tempInK = tx + 273.15;

        // Reference constants (IAPWS values)
        final double T_star = WATER_CRITICAL_TEMPERATURE_IN_K;      // Critical temperature, K
        final double rho_star = WATER_CRITICAL_DENSITY;             // Critical density, kg/m³
        final double mu_star = 1e-6;                                // Reference viscosity, Pa·s

        // Reduced variables
        double T_bar = tempInK / T_star;
        double rho_bar = rho / rho_star;

        // --- Zero-density contribution μ̄₀ (Eq. 11) ---
        // Coefficients from Table 2 (for i = 0 to 3)
        double[] coefH = {1.67752, 2.20462, 0.6366564, -0.241605};

        double sumPoly = coefH[0]
                         + (coefH[1] / T_bar)
                         + (coefH[2] / (T_bar * T_bar))
                         + (coefH[3] / (T_bar * T_bar * T_bar));

        // Then
        double mu0_bar = 100.0 * Math.sqrt(T_bar) / sumPoly;

        // --- Residual contribution μ̄₁ (Eq. 12) is in hCoefficients ---
        // Each term is of the form: H_ij * theta^i * (rho_bar - 1)^j.
        // The following array holds {i, j, coefficient} for each term.

        // Define theta = (1/T_bar - 1) = (T_star/T - 1)
        double theta = (1.0 / T_bar) - 1.0;
        double deltaRho = rho_bar - 1.0;

        double internalSum = 0;
        for (Triple hRecord : hCoefficients) {
            internalSum += Math.pow(theta, hRecord.i) * hRecord.coefH * Math.pow(deltaRho, hRecord.j);
        }

        double mu1_bar = Math.exp(rho_bar * internalSum);

        // Critical enhancement contribution μ̄₂ (Eq. 19)
        // For simplicity, we set μ̄₂ = 1 in this method.
        double mu2_bar = 1.0;

        // Combining all contributions
        double mu_bar = mu0_bar * mu1_bar * mu2_bar;

        // Dynamic viscosity in Pa·s
        return mu_bar * mu_star;
    }

    public static DynamicViscosity dynamicViscosity(Temperature temperature, Density density) {
        CommonValidators.requireNotNull(temperature);
        CommonValidators.requireNotNull(density);
        double dynVIsValue = dynamicViscosity(temperature.getInCelsius(), density.getInKilogramsPerCubicMeters());
        return DynamicViscosity.ofPascalSecond(dynVIsValue);
    }

    /**
     * Calculates the kinematic viscosity of water (in m²/s) given the temperature (C) and density (kg/m³)
     *
     * @param tx  Temperature in Celsius.
     * @param rho Density in kg/m³.
     * @return Kinematic viscosity in m²/s.
     */
    public static double kinematicViscosity(double tx, double rho) {
        CommonValidators.requireNonZero(rho, "Density");
        return dynamicViscosity(tx, rho) / rho;
    }

    /**
     * Calculates the kinematic viscosity of water (in m²/s) given the temperature (C) and density (kg/m³)
     *
     * @param dynamicViscosity Dynamic viscosity in Pa * s.
     * @param density          Density in kg/m³.
     * @return Kinematic viscosity in m²/s.
     */
    public static double kinematicViscosityFromDynVis(double dynamicViscosity, double density) {
        return dynamicViscosity / density;
    }

    public static KinematicViscosity kinematicViscosity(Temperature temperature, Density density) {
        CommonValidators.requireNotNull(temperature);
        CommonValidators.requireNotNull(density);
        CommonValidators.requireNonZero(density);
        double kinematicVisValue = dynamicViscosity(temperature, density).toPascalSecond()
                .divide(density.toKilogramPerCubicMeter());
        return KinematicViscosity.ofSquareMeterPerSecond(kinematicVisValue);
    }

    public static KinematicViscosity kinematicViscosity(DynamicViscosity dynamicViscosity, Density density) {
        CommonValidators.requireNotNull(dynamicViscosity);
        CommonValidators.requireNotNull(density);
        CommonValidators.requireNonZero(density);
        double kinematicVisValue = dynamicViscosity.toPascalSecond()
                .divide(density.toKilogramPerCubicMeter());
        return KinematicViscosity.ofSquareMeterPerSecond(kinematicVisValue);
    }

}