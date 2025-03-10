package com.synerset.hvacengine.hydraulic.structure;

import com.synerset.hvacengine.common.validation.CommonValidators;
import com.synerset.hvacengine.hydraulic.ConduitShape;
import com.synerset.hvacengine.hydraulic.material.MaterialLayer;
import com.synerset.unitility.unitsystem.common.Area;
import com.synerset.unitility.unitsystem.common.Diameter;
import com.synerset.unitility.unitsystem.common.LinearMassDensity;
import com.synerset.unitility.unitsystem.common.Perimeter;

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
 * inner perimeter, and section area, are computed based on its major and minor axes. <p>
 * The term "inner" refers to the most internal hydraulic diameter of the conduit,
 * while "outer" includes the external diameter, incorporating all layers and
 * the base structure material thickness.
 */
public class CircularStructure implements ConduitStructure {

    private static final ConduitShape CONDUIT_SHAPE = ConduitShape.CIRCULAR;
    private final Diameter innerDiameter;
    private final Diameter outerDiameter;
    private final MaterialLayer baseMaterial;
    private final List<MaterialLayer> outerLayers;
    private final Perimeter innerPerimeter;
    private final Area innerSectionArea;
    private final Perimeter outerPerimeter;
    private final Area outerSectionArea;
    private final Diameter equivHydraulicDiameter;
    private final LinearMassDensity baseLinearMassDensity;
    private final LinearMassDensity totalLinearMassDensity;

    public CircularStructure(MaterialLayer baseMaterial, Diameter innerDiameter, List<MaterialLayer> outerMaterialLayers) {
        CommonValidators.requireNotNull(baseMaterial, "baseMaterial");
        CommonValidators.requireNotNull(innerDiameter, "innerDiameter");

        this.baseMaterial = baseMaterial;
        this.innerDiameter = innerDiameter;
        this.outerLayers = outerMaterialLayers == null ? Collections.emptyList() : outerMaterialLayers;

        this.innerPerimeter = StructureEquations.circularPerimeter(innerDiameter);
        this.innerSectionArea = StructureEquations.circularArea(innerDiameter);

        this.outerDiameter = innerDiameter.plus(calculateTotalThickness().multiply(2.0));
        this.outerPerimeter = StructureEquations.circularPerimeter(outerDiameter);
        this.outerSectionArea = StructureEquations.circularArea(outerDiameter);
        this.equivHydraulicDiameter = StructureEquations.equivalentDiameter(innerSectionArea, innerPerimeter);

        this.baseLinearMassDensity = StructureEquations.calculateCircularLinearMassDensity(innerDiameter, baseMaterial);
        this.totalLinearMassDensity = determineTotalLinearMassDensity();
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder().baseMaterial(this.baseMaterial)
                .innerDiameter(this.innerDiameter)
                .outerMaterialLayers(this.outerLayers);
    }

    private LinearMassDensity determineTotalLinearMassDensity() {
        Diameter nextLayerInnerDiameter = innerDiameter.plus(baseMaterial.thickness().multiply(2.0));
        LinearMassDensity currentLinearMassDensity = baseLinearMassDensity;
        for (MaterialLayer currentLayer : outerLayers) {
            if (currentLayer == null) {
                continue;
            }
            currentLinearMassDensity = currentLinearMassDensity.plus(
                    StructureEquations.calculateCircularLinearMassDensity(nextLayerInnerDiameter, currentLayer)
            );
            nextLayerInnerDiameter = nextLayerInnerDiameter.plus(currentLayer.thickness().multiply(2.0));
        }
        return currentLinearMassDensity;
    }

    public CircularStructure withBaseMaterial(MaterialLayer baseMaterial) {
        return new CircularStructure(baseMaterial, this.innerDiameter, this.outerLayers);
    }

    public CircularStructure withInnerDiameter(Diameter innerDiameter) {
        return new CircularStructure(this.baseMaterial, innerDiameter, this.outerLayers);
    }

    public CircularStructure withOuterMaterialLayers(List<MaterialLayer> outerMaterialLayers) {
        return new CircularStructure(this.baseMaterial, this.innerDiameter, outerMaterialLayers);
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

    public Diameter getInnerDiameter() {
        return this.innerDiameter;
    }

    public Diameter getOuterDiameter() {
        return outerDiameter;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        CircularStructure that = (CircularStructure) o;
        return Objects.equals(innerDiameter, that.innerDiameter) && Objects.equals(baseMaterial, that.baseMaterial) && Objects.equals(outerLayers, that.outerLayers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(innerDiameter, baseMaterial, outerLayers);
    }

    @Override
    public String toString() {
        return "CircularStructure{" +
               "innerDiameter=" + innerDiameter +
               ", outerDiameter=" + outerDiameter +
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

    public static class Builder {
        private MaterialLayer baseMaterial;
        private Diameter innerDiameter;
        private List<MaterialLayer> outerMaterialLayers;

        public Builder baseMaterial(MaterialLayer baseMaterial) {
            this.baseMaterial = baseMaterial;
            return this;
        }

        public Builder innerDiameter(Diameter innerDiameter) {
            this.innerDiameter = innerDiameter;
            return this;
        }

        public Builder outerMaterialLayers(List<MaterialLayer> outerMaterialLayers) {
            this.outerMaterialLayers = outerMaterialLayers;
            return this;
        }

        public CircularStructure build() {
            return new CircularStructure(baseMaterial, innerDiameter, outerMaterialLayers);
        }
    }

    // Static factory methods
    public static CircularStructure of(MaterialLayer baseMaterial, Diameter innerDiameter, List<MaterialLayer> outerMaterialLayers) {
        return new CircularStructure(baseMaterial, innerDiameter, outerMaterialLayers);
    }

    public static CircularStructure of(MaterialLayer baseMaterial, Diameter innerDiameter) {
        return new CircularStructure(baseMaterial, innerDiameter, Collections.emptyList());
    }

}
