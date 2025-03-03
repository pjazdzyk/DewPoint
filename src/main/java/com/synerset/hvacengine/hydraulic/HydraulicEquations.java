package com.synerset.hvacengine.hydraulic;

import com.synerset.brentsolver.BrentSolver;
import com.synerset.hvacengine.common.validation.CommonValidators;
import com.synerset.hvacengine.property.fluids.Flow;
import com.synerset.hvacengine.property.fluids.Fluid;
import com.synerset.unitility.unitsystem.common.*;
import com.synerset.unitility.unitsystem.dimensionless.ReynoldsNumber;
import com.synerset.unitility.unitsystem.flow.VolumetricFlow;
import com.synerset.unitility.unitsystem.hydraulic.FrictionFactor;
import com.synerset.unitility.unitsystem.hydraulic.LinearResistance;
import com.synerset.unitility.unitsystem.hydraulic.LocalLossFactor;
import com.synerset.unitility.unitsystem.thermodynamic.Density;
import com.synerset.unitility.unitsystem.thermodynamic.DynamicViscosity;
import com.synerset.unitility.unitsystem.thermodynamic.Pressure;

import java.util.function.DoubleUnaryOperator;

/**
 * This class provides methods to calculate hydraulic properties of fluid flow in conduits,
 * including the Darcy friction factor calculated by resolving Colebrooke-White equation.
 * <p>
 * REFERENCE SOURCES: <p>
 * [1] Mitosek M. Mechanika płynów w inżynierii i ochronie środowiska. Polskie Wydawnictwo Naukowe PWN (2001r).
 * [2] Lotfi Z., Jean Loup R., Bachir A. Explicit solutions for turbulent flow friction factor: A review, assessment and approaches classification. Ain Shams Engineering Journal (2019r) <p>
 * <p>
 * REFERENCES LEGEND KEY: <p>
 * [reference no] [value symbology in standard, unit] (equation number) [page] <p>
 *
 * @author Piotr Jażdżyk, MSc Eng
 */
public class HydraulicEquations {

    private HydraulicEquations() {
        throw new IllegalStateException("Utility Class");
    }

    public static double flowVelocity(double volumetricFlow, double hydraulicArea) {
        CommonValidators.requireNonZero(hydraulicArea, "Hydraulic Diameter");
        return volumetricFlow / hydraulicArea;
    }

    public static Velocity flowVelocity(VolumetricFlow volumetricFlow, Area hydraulicArea) {
        CommonValidators.requireNotNull(volumetricFlow, "volumetricFlow");
        CommonValidators.requireNotNull(hydraulicArea, "hydraulicArea");
        CommonValidators.requireNonZero(hydraulicArea);
        double velocityValue = flowVelocity(volumetricFlow.getInCubicMetersPerSecond(), hydraulicArea.getInSquareMeters());
        return Velocity.ofMetersPerSecond(velocityValue);
    }

    /**
     * Returns Reynolds number for provided flow parameters.<p>
     * REFERENCE SOURCE: [1] [-] (7.13) [108]<p>
     * EQUATION LIMITS: n/a <p>
     *
     * @param velocity             Flow velocity (m/s)
     * @param characteristicLength Duct hydraulic or equivalent diameter (m)
     * @param density              Fluid density (kg/m³)
     * @param dynViscosity         Dynamic viscosity (Pa.s)
     * @return Reynolds number (dimensionless)
     */
    public static double reynoldsNumber(double velocity, double characteristicLength, double density, double dynViscosity) {
        CommonValidators.requireNonZero(dynViscosity, "Dynamic Viscosity");
        // Re = (density * velocity * diameter) / dynViscosity
        return (density * velocity * characteristicLength) / dynViscosity;
    }

    public static ReynoldsNumber reynoldsNumber(Velocity velocity, Length characteristicLength, Density density, DynamicViscosity dynViscosity) {
        CommonValidators.requireNotNull(velocity, "velocity");
        CommonValidators.requireNotNull(characteristicLength, "characteristicLength");
        CommonValidators.requireNotNull(density, "density");
        CommonValidators.requireNotNull(dynViscosity, "dynViscosity");
        // Using the formula: Re = (density * velocity * diameter) / viscosity
        double reynoldsNumberValue = reynoldsNumber(
                velocity.getInMetersPerSecond(),
                characteristicLength.getInMeters(),
                density.getInKilogramsPerCubicMeters(),
                dynViscosity.getInPascalsSecond()
        );

        return ReynoldsNumber.of(reynoldsNumberValue);

    }

    public static ReynoldsNumber reynoldsNumber(Flow<? extends Fluid> flowOfFluid, Area hydraulicArea, Length characteristicLength) {
        CommonValidators.requireNotNull(flowOfFluid, "flowOfFluid");
        Fluid fluid = flowOfFluid.getFluid();
        Velocity flowVelocity = flowVelocity(flowOfFluid.getVolFlow(), hydraulicArea);
        return reynoldsNumber(flowVelocity, characteristicLength, fluid.getDensity(), fluid.getDynamicViscosity());
    }

    /**
     * Returns laminar friction factor for provided flow parameters. Used only for Re < 2300.
     * Roughness does not affect friction in laminar flow.
     * REFERENCE SOURCE: [2] [-] (3) [244]<p>
     * EQUATION LIMITS: Re < 2300 <p>
     *
     * @param reynoldsNumber Flow Reynolds number
     * @return approximation of friction factor for turbulent flow
     */
    public static double frictionFactorLaminar(double reynoldsNumber) {
        CommonValidators.requireNonZero(reynoldsNumber, "Reynolds Number");
        return 64.0 / reynoldsNumber;
    }

    public static FrictionFactor frictionFactorLaminar(ReynoldsNumber reynoldsNumber) {
        CommonValidators.requireNotNull(reynoldsNumber, "reynoldsNumber");
        double laminarFrictionFactorValue = frictionFactorLaminar(reynoldsNumber.getValue());
        return FrictionFactor.of(laminarFrictionFactorValue);
    }

    /**
     * Returns friction factor for provided flow parameters, based on Ali R. Vatankhah equation (2014r) <p>
     * REFERENCE SOURCE: [2] [-] (54, 55) [248]<p>
     * EQUATION LIMITS: 4000 <= Re <= 10^8, 10^-6 <= k/Dh <= 5 * 10^-2<p>
     * ACCURACY: 0.146% over Colebrooke-White equation [4] table 1.
     *
     * @param hydraulicDiameter Hydraulic diameter or equivalent hydraulic diameter in [m]
     * @param absRoughness      Absolute conduit wall material roughness in [m]
     * @param reynoldsNumber    Flow Reynolds number
     * @return approximation of friction factor for turbulent flow
     */
    public static double frictionFactorByVatankhah(double hydraulicDiameter, double absRoughness, double reynoldsNumber) {
        double relRoughness = absRoughness / hydraulicDiameter;

        // Compute δ using equation (55)
        double delta = (6.0173 / reynoldsNumber * Math.pow((0.07 * relRoughness + Math.pow(reynoldsNumber, -0.885)), 0.109))
                       + (relRoughness / 3.71);

        // Compute f using equation (54)
        double numerator = (2.51 / reynoldsNumber) + (1.1513 * delta);
        double denominator = delta - (relRoughness / 3.71) - (2.3026 * delta * Math.log10(delta));

        return Math.pow(numerator / denominator, 2);
    }

    public static FrictionFactor frictionFactorByVatankhah(Diameter hydraulicDiameter, Height absRoughness, ReynoldsNumber reynoldsNumber) {
        CommonValidators.requireNotNull(hydraulicDiameter, "hydraulicDiameter");
        CommonValidators.requireNotNull(absRoughness, "absRoughness");
        CommonValidators.requireNotNull(reynoldsNumber, "reynoldsNumber");
        double frictionFactorValue = frictionFactorByVatankhah(
                hydraulicDiameter.getInMeters(),
                absRoughness.getInMeters(),
                reynoldsNumber.getValue()
        );
        return FrictionFactor.of(frictionFactorValue);
    }

    /**
     * Returns friction factor for provided flow parameters, based on Colebrooke-White equation (1937r).
     * Calculation is carried out using iterative solver {@link BrentSolver}.<p>
     * REFERENCE SOURCE: [2] [-] (6) [244]<p>
     * EQUATION LIMITS: Re >= 4000<p>
     *
     * @param hydraulicDiameter Hydraulic diameter or equivalent hydraulic diameter in [m]
     * @param absRoughness      Absolute conduit wall material roughness in [m]
     * @param reynoldsNumber    Flow Reynolds number
     * @return computed friction factor for turbulent flow
     */
    public static double frictionFactorByColebrooke(double hydraulicDiameter, double absRoughness, double reynoldsNumber) {
        // 1. Determining first guess based on Vatankhah (2014) approximation
        double lambdaApprox = frictionFactorByVatankhah(hydraulicDiameter, absRoughness, reynoldsNumber);

        // 2. Colebrook-White equation
        DoubleUnaryOperator colebrookEqn = lambda ->
                1 / Math.sqrt(lambda)
                + 2 * Math.log10(
                        (absRoughness / (3.7 * hydraulicDiameter)
                         + 2.51 / (reynoldsNumber * Math.sqrt(lambda)))
                );

        double a0 = lambdaApprox * 0.905;
        double b0 = lambdaApprox * 1.105;

        BrentSolver solver = BrentSolver.of("Colebrooke-Solver");
        return solver.findRoot(colebrookEqn, a0, b0);
    }

    public static FrictionFactor frictionFactorByColebrooke(Diameter hydraulicDiameter, Height absRoughness, ReynoldsNumber reynoldsNumber) {
        CommonValidators.requireNotNull(hydraulicDiameter, "hydraulicDiameter");
        CommonValidators.requireNotNull(absRoughness, "absRoughness");
        CommonValidators.requireNotNull(reynoldsNumber, "reynoldsNumber");
        double frictionFactorValue = frictionFactorByColebrooke(
                hydraulicDiameter.getInMeters(),
                absRoughness.getInMeters(),
                reynoldsNumber.getValue()
        );
        return new FrictionFactor(frictionFactorValue);
    }

    /**
     * Calculates the linear pressure loss in a conduit using the Darcy-Weisbach equation.
     * REFERENCE SOURCE: [1] [-] (8.18) [134]<p>
     * EQUATION LIMITS: n/a<p>
     *
     * @param frictionFactor    The Darcy friction factor (dimensionless).
     * @param hydraulicDiameter The hydraulic diameter of the conduit (m).
     * @param conduitLength     The length of the conduit (m).
     * @param density           The fluid density (kg/m³).
     * @param velocity          The flow velocity of the fluid (m/s).
     * @return The pressure loss in Pascals (Pa).
     */
    public static double linearPressureLoss(double frictionFactor, double hydraulicDiameter,
                                            double conduitLength, double density, double velocity) {
        return frictionFactor * (conduitLength / hydraulicDiameter) * ((density * velocity * velocity) / 2.0);
    }

    public static Pressure linearPressureLoss(FrictionFactor frictionFactor, Diameter hydraulicDiameter, Length length,
                                              Density density, Velocity velocity) {

        CommonValidators.requireNotNull(frictionFactor, "frictionFactor");
        CommonValidators.requireNotNull(hydraulicDiameter, "hydraulicDiameter");
        CommonValidators.requireNotNull(length, "length");
        CommonValidators.requireNotNull(density, "density");
        CommonValidators.requireAboveLowerBound(hydraulicDiameter, Diameter.ofMeters(0));

        double linearPressureLossValue = linearPressureLoss(
                frictionFactor.getValue(),
                hydraulicDiameter.getInMeters(),
                length.getInMeters(),
                density.getInKilogramsPerCubicMeters(),
                velocity.getInMetersPerSecond()
        );

        return Pressure.ofPascal(linearPressureLossValue);
    }

    /**
     * Calculates the linear resistance of a conduit.
     * Linear resistance represents the pressure drop per unit length of the conduit.
     *
     * @param pressureDrop  The total pressure drop across the conduit (Pa).
     * @param conduitLength The length of the conduit (m).
     * @return The linear resistance in Pascals per meter (Pa/m).
     */
    public static double linearResistance(double pressureDrop, double conduitLength) {
        return pressureDrop / conduitLength;
    }

    public static LinearResistance linearResistance(Pressure pressureDrop, Length conduitLength) {
        CommonValidators.requireNotNull(pressureDrop, "pressureDrop");
        CommonValidators.requireNotNull(conduitLength, "conduitLength");
        if (conduitLength.isEqualZero()) {
            return LinearResistance.ofPascalPerMeter(0);
        }
        double linearResistanceValue = linearResistance(pressureDrop.getInPascals(), conduitLength.getInMeters());
        return LinearResistance.ofPascalPerMeter(linearResistanceValue);
    }

    /**
     * Returns local pressure loss for provided local loss coefficient ('dzeta').
     * REFERENCE SOURCE: [1] [Pa] (8.32) [139]<p>
     * EQUATION LIMITS: -<p>
     *
     * @param localLossFactor Local loss coefficient 'dzeta' specific for given fitting installed in a conduit in [-]
     * @param velocity        Average flow velocity in fitting face area [m/s]
     * @param density         Fluid density in [kg/m3]
     * @return computed friction factor for turbulent flow
     */
    public static double localPressureLoss(double localLossFactor, double velocity, double density) {
        return localLossFactor * density * velocity * velocity / 2.0;
    }

    public static Pressure localPressureLoss(LocalLossFactor localLossFactor, Velocity velocity, Density density) {
        CommonValidators.requireNotNull(localLossFactor, "localLossFactor");
        CommonValidators.requireNotNull(velocity, "velocity");
        CommonValidators.requireNotNull(density, "density");
        double localPressureLoss = localPressureLoss(
                localLossFactor.getValue(),
                velocity.getInMetersPerSecond(),
                density.getInKilogramsPerCubicMeters()
        );
        return Pressure.ofPascal(localPressureLoss);
    }

}
