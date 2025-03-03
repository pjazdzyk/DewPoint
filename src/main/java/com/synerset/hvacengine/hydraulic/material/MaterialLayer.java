package com.synerset.hvacengine.hydraulic.material;

import com.synerset.unitility.unitsystem.common.Height;

import java.util.Objects;

public record MaterialLayer(
        MaterialData material,
        Height thickness
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private MaterialData materialData;
        private Height thickness;

        public Builder materialData(MaterialData material) {
            this.materialData = material;
            return this;
        }

        public Builder thickness(Height thickness) {
            this.thickness = thickness;
            return this;
        }

        public MaterialLayer build() {
            return new MaterialLayer(materialData, thickness);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        MaterialLayer that = (MaterialLayer) o;
        return Objects.equals(thickness, that.thickness) && Objects.equals(material, that.material);
    }

    @Override
    public int hashCode() {
        return Objects.hash(material, thickness);
    }

    @Override
    public String toString() {
        return "MaterialLayer{" +
               "material=" + material +
               ", thickness=" + thickness +
               '}';
    }

    public static MaterialLayer of(MaterialData material, Height thickness) {
        return new MaterialLayer(material, thickness);
    }
}
