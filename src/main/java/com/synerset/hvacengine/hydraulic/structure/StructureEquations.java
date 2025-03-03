package com.synerset.hvacengine.hydraulic.structure;

import com.synerset.hvacengine.common.validation.CommonValidators;
import com.synerset.hvacengine.hydraulic.material.MaterialLayer;
import com.synerset.unitility.unitsystem.common.*;

/**
 * This class provides methods to calculate basic properties related to conduit cross-section shapes,
 * such as cross-section area, perimeter, equivalent hydraulic diameter and others.
 * <p>
 * REFERENCE SOURCES: <p>
 * [1] Barnard R.W., Pearce K., Schovanec L. Inequalities for the Perimeter of an Ellipse. Department of Mathematics and Statistic. Texas Tech University (2001r)
 * <p>
 * REFERENCES LEGEND KEY: <p>
 * [reference no] [value symbology in standard, unit] (equation number) [page] <p>
 *
 * @author Piotr Jażdżyk, MSc Eng
 */
public class StructureEquations {

    private StructureEquations() {
        throw new IllegalStateException("Utility class");
    }

    public static double equivalentDiameter(double area, double perimeter) {
        return 4.0 * area / perimeter;
    }

    public static Diameter equivalentDiameter(Area area, Perimeter perimeter) {
        CommonValidators.requireNotNull(area);
        CommonValidators.requireNonZero(perimeter);
        double equivDiameterValue = equivalentDiameter(area.getInSquareMeters(), perimeter.getInMeters());
        return Diameter.ofMeters(equivDiameterValue);
    }

    public static double circularArea(double diameter) {
        return Math.PI * diameter * diameter / 4.0;
    }

    public static Area circularArea(Diameter diameter) {
        CommonValidators.requireNotNull(diameter);
        double areaValue = circularArea(diameter.getInMeters());
        return Area.ofSquareMeters(areaValue);
    }

    public static double circularPerimeter(double diameter) {
        return 2.0 * Math.PI * diameter / 2.0;
    }

    public static Perimeter circularPerimeter(Diameter diameter) {
        CommonValidators.requireNotNull(diameter);
        double perimeterValue = circularPerimeter(diameter.getInMeters());
        return Perimeter.ofMeters(perimeterValue);
    }

    public static double rectangularArea(double width, double height) {
        return width * height;
    }

    public static Area rectangularArea(Width width, Height height) {
        CommonValidators.requireNotNull(width);
        CommonValidators.requireNotNull(height);
        double areaValue = rectangularArea(width.getInMeters(), height.getInMeters());
        return Area.ofSquareMeters(areaValue);
    }

    public static double rectangularPerimeter(double width, double height) {
        return 2.0 * width + 2.0 * height;
    }

    public static Perimeter rectangularPerimeter(Width width, Height height) {
        CommonValidators.requireNotNull(width);
        CommonValidators.requireNotNull(height);
        double perimeterValue = rectangularPerimeter(width.getInMeters(), height.getInMeters());
        return Perimeter.ofMeters(perimeterValue);
    }

    public static double ellipticArea(double majorAxis, double minorAxis) {
        return Math.PI * (majorAxis / 2.0) * (minorAxis / 2.0);
    }

    public static Area ellipticArea(Diameter majorAxis, Diameter minorAxis) {
        CommonValidators.requireNotNull(majorAxis);
        CommonValidators.requireNotNull(minorAxis);
        double areaValue = ellipticArea(majorAxis.getInMeters(), minorAxis.getInMeters());
        return Area.ofSquareMeters(areaValue);
    }

    /**
     * Returns ellipse shape perimeter based on Jacobsen (1985) approximation. <p>
     * REFERENCE SOURCE: [1] [m] (T1/13) [4]<p>
     * EQUATION LIMITS: n/a <p>
     *
     * @param majorAxis Ellipse full length of major (wider) axis (m)
     * @param minorAxis Ellipse full length of minor (shorter) axis (m)
     * @return approximation of ellipse perimeter based on Jacobsen (1978)
     */
    public static double ellipticPerimeter(double majorAxis, double minorAxis) {
        double a = majorAxis / 2.0;
        double b = minorAxis / 2.0;

        double lambda = (a - b) / (a + b);

        double numerator = 256 - (48 * lambda * lambda) - (21 * lambda * lambda * lambda * lambda);
        double denominator = 256 - (112 * lambda * lambda) - (3 * lambda * lambda * lambda * lambda);
        double result = numerator / denominator;

        return Math.PI * (a + b) * result;
    }

    public static Perimeter ellipticPerimeter(Diameter majorAxis, Diameter minorAxis) {
        CommonValidators.requireNotNull(majorAxis);
        CommonValidators.requireNotNull(minorAxis);
        double perimeterValue = ellipticPerimeter(majorAxis.getInMeters(), minorAxis.getInMeters());
        return Perimeter.ofMeters(perimeterValue);
    }

    public static LinearMassDensity calculateCircularLinearMassDensity(Diameter baseOuterDiameter, MaterialLayer cladingMaterialLayer) {
        CommonValidators.requireNotNull(baseOuterDiameter);
        CommonValidators.requireNotNull(cladingMaterialLayer);
        Area innerArea = StructureEquations.circularArea(baseOuterDiameter);
        Area outerArea = StructureEquations.circularArea(baseOuterDiameter.plus(cladingMaterialLayer.thickness().multiply(2.0)));
        Area sectionDiff = outerArea.minus(innerArea);
        double layerVolumePer1m = sectionDiff.getInSquareMeters(); // * 1m
        double density = cladingMaterialLayer.material().density().getInKilogramsPerCubicMeters();
        return LinearMassDensity.ofKilogramsPerMeter(density * layerVolumePer1m);
    }

    public static LinearMassDensity calculateRectangularLinearMassDensity(Width baseOuterWidth, Height baseOuterheight, MaterialLayer materialLayer) {
        CommonValidators.requireNotNull(baseOuterWidth);
        CommonValidators.requireNotNull(baseOuterheight);
        CommonValidators.requireNotNull(materialLayer);
        Area innerArea = StructureEquations.rectangularArea(baseOuterWidth, baseOuterheight);
        Area outerArea = StructureEquations.rectangularArea(
                baseOuterWidth.plus(materialLayer.thickness().multiply(2.0)),
                baseOuterheight.plus(materialLayer.thickness().multiply(2.0))
        );
        Area sectionDiff = outerArea.minus(innerArea);
        double layerVolumePer1m = sectionDiff.getInSquareMeters(); // * 1m
        double density = materialLayer.material().density().getInKilogramsPerCubicMeters();
        return LinearMassDensity.ofKilogramsPerMeter(density * layerVolumePer1m);
    }

}
