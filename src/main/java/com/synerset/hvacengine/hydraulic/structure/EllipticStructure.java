package com.synerset.hvacengine.hydraulic.structure;

import com.synerset.hvacengine.common.validation.CommonValidators;
import com.synerset.hvacengine.hydraulic.ConduitShape;
import com.synerset.hvacengine.hydraulic.material.MaterialLayer;
import com.synerset.unitility.unitsystem.common.*;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents an elliptical conduit structure with layered materials.
 * This class defines the geometric and material properties of an elliptic conduit,
 * including its inner and outer dimensions, cross-sectional properties, and mass distribution.
 * The conduit consists of a base material layer and optional outer layers, which may include
 * insulation, cladding, or other protective coatings. <p>
 * The hydraulic properties of the elliptical structure, such as equivalent hydraulic diameter,
 * inner perimeter, and section area, are computed based on its major and minor axes.<p>
 * The term "inner" refers to the most internal hydraulic space of the conduit,
 * while "outer" includes the external boundary, incorporating all layers and
 * the base structure material thickness.
 */
public class EllipticStructure implements ConduitStructure {

    private static final ConduitShape CONDUIT_SHAPE = ConduitShape.ELLIPTIC;
    private final Diameter innerMajorAxis;
    private final Diameter innerMinorAxis;
    private final Diameter outerMajorAxis;
    private final Diameter outerMinorAxis;
    private final MaterialLayer baseMaterial;
    private final List<MaterialLayer> outerLayers;
    private final Perimeter innerPerimeter;
    private final Area innerSectionArea;
    private final Perimeter outerPerimeter;
    private final Area outerSectionArea;
    private final Diameter equivHydraulicDiameter;
    private final LinearMassDensity baseLinearMassDensity;
    private final LinearMassDensity totalLinearMassDensity;

    public EllipticStructure(MaterialLayer baseMaterial, Diameter innerMajorAxis, Diameter innerMinorAxis, List<MaterialLayer> outerMaterialLayers) {
        CommonValidators.requireNotNull(baseMaterial, "baseMaterial");
        CommonValidators.requireNotNull(innerMajorAxis, "innerMajorAxis");
        CommonValidators.requireNotNull(innerMinorAxis, "innerMinorAxis");

        this.baseMaterial = baseMaterial;
        this.innerMajorAxis = innerMajorAxis;
        this.innerMinorAxis = innerMinorAxis;
        this.outerLayers = outerMaterialLayers == null ? Collections.emptyList() : outerMaterialLayers;

        this.innerPerimeter = StructureEquations.ellipticPerimeter(innerMajorAxis, innerMinorAxis);
        this.innerSectionArea = StructureEquations.ellipticArea(innerMajorAxis, innerMinorAxis);

        Height totalThickness = calculateTotalThickness();
        this.outerMajorAxis = innerMajorAxis.plus(totalThickness.multiply(2.0));
        this.outerMinorAxis = innerMinorAxis.plus(totalThickness.multiply(2.0));

        this.outerPerimeter = StructureEquations.ellipticPerimeter(outerMajorAxis, outerMinorAxis);
        this.outerSectionArea = StructureEquations.ellipticArea(outerMajorAxis, outerMinorAxis);
        this.equivHydraulicDiameter = StructureEquations.equivalentDiameter(innerSectionArea, innerPerimeter);

        this.baseLinearMassDensity = StructureEquations.calculateCircularLinearMassDensity(innerMajorAxis, baseMaterial);
        this.totalLinearMassDensity = determineTotalLinearMassDensity();
    }

    private LinearMassDensity determineTotalLinearMassDensity() {
        Diameter nextLayerInnerMajor = innerMajorAxis.plus(baseMaterial.thickness().multiply(2.0));
        Diameter nextLayerInnerMinor = innerMinorAxis.plus(baseMaterial.thickness().multiply(2.0));
        LinearMassDensity currentLinearMassDensity = baseLinearMassDensity;
        for (MaterialLayer currentLayer : outerLayers) {
            if (currentLayer == null) continue;
            currentLinearMassDensity = currentLinearMassDensity.plus(
                    StructureEquations.calculateCircularLinearMassDensity(nextLayerInnerMajor, currentLayer)
            );
            nextLayerInnerMajor = nextLayerInnerMajor.plus(currentLayer.thickness().multiply(2.0));
            nextLayerInnerMinor = nextLayerInnerMinor.plus(currentLayer.thickness().multiply(2.0));
        }
        return currentLinearMassDensity;
    }

    @Override
    public ConduitShape getConduitShape() {
        return CONDUIT_SHAPE;
    }

    @Override
    public MaterialLayer getConduitBaseLayer() {
        return baseMaterial;
    }

    @Override
    public List<MaterialLayer> getOuterLayers() {
        return outerLayers;
    }

    @Override
    public Perimeter getInnerPerimeter() {
        return innerPerimeter;
    }

    @Override
    public Diameter getEquivHydraulicDiameter() {
        return equivHydraulicDiameter;
    }

    @Override
    public Area getInnerSectionArea() {
        return innerSectionArea;
    }

    @Override
    public Perimeter getOuterPerimeter() {
        return outerPerimeter;
    }

    @Override
    public Area getOuterSectionArea() {
        return outerSectionArea;
    }

    @Override
    public LinearMassDensity getBaseLinearMassDensity() {
        return baseLinearMassDensity;
    }

    @Override
    public LinearMassDensity getTotalLinearMassDensity() {
        return totalLinearMassDensity;
    }

    public Diameter getInnerMajorAxis() {
        return innerMajorAxis;
    }

    public Diameter getInnerMinorAxis() {
        return innerMinorAxis;
    }

    public Diameter getOuterMajorAxis() {
        return outerMajorAxis;
    }

    public Diameter getOuterMinorAxis() {
        return outerMinorAxis;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        EllipticStructure that = (EllipticStructure) o;
        return Objects.equals(innerMajorAxis, that.innerMajorAxis) && Objects.equals(innerMinorAxis, that.innerMinorAxis) && Objects.equals(baseMaterial, that.baseMaterial) && Objects.equals(outerLayers, that.outerLayers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(innerMajorAxis, innerMinorAxis, baseMaterial, outerLayers);
    }

    @Override
    public String toString() {
        return "EllipticStructure{" +
               "innerMajorAxis=" + innerMajorAxis +
               ", innerMinorAxis=" + innerMinorAxis +
               ", outerMajorAxis=" + outerMajorAxis +
               ", outerMinorAxis=" + outerMinorAxis +
               ", baseMaterial=" + baseMaterial +
               ", outerLayers=" + outerLayers +
               ", innerPerimeter=" + innerPerimeter +
               ", innerSectionArea=" + innerSectionArea +
               ", outerPerimeter=" + outerPerimeter +
               ", outerSectionArea=" + outerSectionArea +
               ", equivHydraulicDiameter=" + equivHydraulicDiameter +
               ", baseLinearMassDensity=" + baseLinearMassDensity +
               ", totalLinearMassDensity=" + totalLinearMassDensity +
               '}';
    }
}
