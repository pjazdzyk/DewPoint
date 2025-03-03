package com.synerset.hvacengine.hydraulic.structure;

import com.synerset.hvacengine.hydraulic.ConduitShape;
import com.synerset.hvacengine.hydraulic.material.MaterialLayer;
import com.synerset.unitility.unitsystem.CalculableQuantity;
import com.synerset.unitility.unitsystem.common.*;

import java.util.List;

/**
 * Represents a generic conduit layered structure, providing methods to access its material layers and cross-sectional properties.
 * Each duct structure is composed of a base material and optional additional layers (such as insulation, cladding, or coatings).
 * The interface also provides methods to retrieve geometric properties, including the equivalent hydraulic diameter,
 * which is derived from the inner perimeter and inner cross-sectional area.
 */
public interface ConduitStructure {

    /**
     * Returns {@link ConduitShape} of a conduit cross-section.
     *
     * @return the {@link ConduitShape} of a conduit cross-section.
     */
    ConduitShape getConduitShape();

    /**
     * Retrieves the base material layer of the conduit structure.
     * The base layer is the primary structural material that forms the conduit.
     *
     * @return the base material layer
     */
    MaterialLayer getConduitBaseLayer();

    /**
     * Retrieves the outer layers of the conduit structure.
     * Outer layers may include insulation, protective cladding, or coatings applied over the base material.
     *
     * @return a list of outer material layers
     */
    List<MaterialLayer> getOuterLayers();

    // Cross-section data

    /**
     * Retrieves the inner perimeter of the conduit.
     * The inner perimeter is measured at the interface between the fluid and the conduit wall.
     *
     * @return the inner perimeter
     */
    Perimeter getInnerPerimeter();

    /**
     * Retrieves the inner cross-sectional area of the conduit.
     * This area represents the open flow section inside the conduit, excluding wall thickness.
     *
     * @return the inner cross-sectional area
     */
    Area getInnerSectionArea();

    /**
     * Retrieves the equivalent hydraulic diameter of the conduit.
     * The hydraulic diameter is calculated based on the inner perimeter and inner cross-sectional area,
     * providing a representative dimension for flow calculations.
     *
     * @return the equivalent hydraulic diameter
     */
    Diameter getEquivHydraulicDiameter();

    /**
     * Retrieves the outer perimeter of the conduit.
     * The outer perimeter is measured at the outermost boundary of the conduit, including any additional layers.
     *
     * @return the outer perimeter
     */
    Perimeter getOuterPerimeter();

    /**
     * Retrieves the outer cross-sectional area of the conduit.
     * This area includes the base material and any additional outer layers.
     *
     * @return the outer cross-sectional area
     */
    Area getOuterSectionArea();

    /**
     * Retrieves the base linear mass density of the conduit.
     * This represents the mass per unit length of the base material alone, excluding any outer layers.
     *
     * @return the base linear mass density
     */
    LinearMassDensity getBaseLinearMassDensity();

    /**
     * Retrieves the total linear mass density of the conduit, including all outer layers.
     * This accounts for the combined mass of the base material and additional layers per unit length.
     *
     * @return the total linear mass density
     */
    LinearMassDensity getTotalLinearMassDensity();

    /**
     * Calculates the total thickness of the conduit wall, considering the base material and all outer layers.
     * This is determined by summing the thicknesses of the base material and any additional layers.
     *
     * @return the total thickness of the conduit wall
     */
    default Height calculateTotalThickness() {
        return getOuterLayers().stream()
                .map(MaterialLayer::thickness)
                .reduce(getConduitBaseLayer().thickness(), CalculableQuantity::plus);
    }
}
