package com.synerset.hvacengine.hydraulic.structure;

import com.synerset.hvacengine.common.validation.CommonValidators;
import com.synerset.hvacengine.hydraulic.ConduitShape;
import com.synerset.hvacengine.hydraulic.material.MaterialLayer;
import com.synerset.unitility.unitsystem.common.*;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a circular conduit structure with layered materials.
 * This class defines the geometric and material properties of a circular conduit,
 * including its inner and outer diameters, cross-sectional properties, and mass distribution.
 * The conduit consists of a base material layer and optional outer layers, which may include
 * insulation, cladding, or other protective coatings.<p>
 * The hydraulic properties of the circular structure, such as equivalent hydraulic diameter,
 * inner perimeter, and section area, are computed based on its diameter. <p>
 * * The term "inner" refers to the most internal hydraulic space of the conduit,
 * * while "outer" includes the external boundary, incorporating all layers and
 * * the base structure material thickness.
 */
public class RectangularStructure implements ConduitStructure {

    private static final ConduitShape CONDUIT_SHAPE = ConduitShape.RECTANGULAR;
    private final Width innerWidth;
    private final Height innerHeight;
    private final Width outerWidth;
    private final Height outerHeight;
    private final MaterialLayer baseMaterial;
    private final List<MaterialLayer> outerLayers;
    private final Perimeter innerPerimeter;
    private final Area innerSectionArea;
    private final Perimeter outerPerimeter;
    private final Area outerSectionArea;
    private final Diameter equivHydraulicDiameter;
    private final LinearMassDensity baseLinearMassDensity;
    private final LinearMassDensity totalLinearMassDensity;

    public RectangularStructure(MaterialLayer baseMaterial, Width innerWidth, Height innerHeight, List<MaterialLayer> outerMaterialLayers) {
        CommonValidators.requireNotNull(baseMaterial, "baseMaterial");
        CommonValidators.requireNotNull(innerWidth, "innerWidth");
        CommonValidators.requireNotNull(innerHeight, "innerHeight");

        this.baseMaterial = baseMaterial;
        this.innerWidth = innerWidth;
        this.innerHeight = innerHeight;
        this.outerLayers = outerMaterialLayers == null ? Collections.emptyList() : outerMaterialLayers;

        this.innerPerimeter = StructureEquations.rectangularPerimeter(innerWidth, innerHeight);
        this.innerSectionArea = StructureEquations.rectangularArea(innerWidth, innerHeight);

        Height totalThickness = calculateTotalThickness();
        this.outerWidth = innerWidth.plus(totalThickness.multiply(2.0));
        this.outerHeight = innerHeight.plus(totalThickness.multiply(2.0));

        this.outerPerimeter = StructureEquations.rectangularPerimeter(outerWidth, outerHeight);
        this.outerSectionArea = StructureEquations.rectangularArea(outerWidth, outerHeight);
        this.equivHydraulicDiameter = StructureEquations.equivalentDiameter(innerSectionArea, innerPerimeter);

        this.baseLinearMassDensity = StructureEquations.calculateRectangularLinearMassDensity(innerWidth, innerHeight, baseMaterial);
        this.totalLinearMassDensity = determineTotalLinearMassDensity();
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder().baseMaterial(this.baseMaterial)
                .innerWidth(this.innerWidth)
                .innerHeight(this.innerHeight)
                .outerMaterialLayers(this.outerLayers);
    }

    private LinearMassDensity determineTotalLinearMassDensity() {
        Width nextLayerInnerWidth = innerWidth.plus(baseMaterial.thickness().multiply(2.0));
        Height nextLayerInnerHeight = innerHeight.plus(baseMaterial.thickness().multiply(2.0));
        LinearMassDensity currentLinearMassDensity = baseLinearMassDensity;
        for (MaterialLayer currentLayer : outerLayers) {
            if (currentLayer == null) {
                continue;
            }
            currentLinearMassDensity = currentLinearMassDensity.plus(
                    StructureEquations.calculateRectangularLinearMassDensity(nextLayerInnerWidth, nextLayerInnerHeight, currentLayer)
            );
            nextLayerInnerWidth = nextLayerInnerWidth.plus(currentLayer.thickness().multiply(2.0));
            nextLayerInnerHeight = nextLayerInnerHeight.plus(currentLayer.thickness().multiply(2.0));
        }
        return currentLinearMassDensity;
    }

    public RectangularStructure withBaseMaterial(MaterialLayer baseMaterial) {
        return new RectangularStructure(baseMaterial, this.innerWidth, this.innerHeight, this.outerLayers);
    }

    public RectangularStructure withInnerWidth(Width innerWidth) {
        return new RectangularStructure(this.baseMaterial, innerWidth, this.innerHeight, this.outerLayers);
    }

    public RectangularStructure withInnerHeight(Height innerHeight) {
        return new RectangularStructure(this.baseMaterial, this.innerWidth, innerHeight, this.outerLayers);
    }

    public RectangularStructure withOuterMaterialLayers(List<MaterialLayer> outerMaterialLayers) {
        return new RectangularStructure(this.baseMaterial, this.innerWidth, this.innerHeight, outerMaterialLayers);
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

    public Width getInnerWidth() {
        return innerWidth;
    }

    public Height getInnerHeight() {
        return innerHeight;
    }

    public Width getOuterWidth() {
        return outerWidth;
    }

    public Height getOuterHeight() {
        return outerHeight;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        RectangularStructure that = (RectangularStructure) o;
        return Objects.equals(innerWidth, that.innerWidth) && Objects.equals(innerHeight, that.innerHeight) && Objects.equals(baseMaterial, that.baseMaterial) && Objects.equals(outerLayers, that.outerLayers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(innerWidth, innerHeight, baseMaterial, outerLayers);
    }

    @Override
    public String toString() {
        return "RectangularStructure{" +
               "innerWidth=" + innerWidth +
               ", innerHeight=" + innerHeight +
               ", outerWidth=" + outerWidth +
               ", outerHeight=" + outerHeight +
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
        private Width innerWidth;
        private Height innerHeight;
        private List<MaterialLayer> outerMaterialLayers;

        public Builder baseMaterial(MaterialLayer baseMaterial) {
            this.baseMaterial = baseMaterial;
            return this;
        }

        public Builder innerWidth(Width innerWidth) {
            this.innerWidth = innerWidth;
            return this;
        }

        public Builder innerHeight(Height innerHeight) {
            this.innerHeight = innerHeight;
            return this;
        }

        public Builder outerMaterialLayers(List<MaterialLayer> outerMaterialLayers) {
            this.outerMaterialLayers = outerMaterialLayers;
            return this;
        }

        public RectangularStructure build() {
            return new RectangularStructure(baseMaterial, innerWidth, innerHeight, outerMaterialLayers);
        }
    }
}
