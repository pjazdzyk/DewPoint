package com.synerset.hvacengine.common.validation;

import com.synerset.hvacengine.common.exception.HvacEngineArgumentException;
import com.synerset.hvacengine.common.exception.HvacEngineMissingArgumentException;
import com.synerset.unitility.unitsystem.PhysicalQuantity;
import com.synerset.unitility.unitsystem.Unit;

import java.util.Collection;
import java.util.Objects;

public final class CommonValidators {

    private CommonValidators() {
    }

    public static void requireNotNull(Object object) {
        if (Objects.isNull(object)) {
            throw new HvacEngineMissingArgumentException("Argument cannot be null.");
        }
    }

    public static void requireNotNull(Object object, String variableName) {
        if (Objects.isNull(object)) {
            throw new HvacEngineMissingArgumentException(variableName + " cannot be null.");
        }
    }

    public static void requireNotEmpty(Collection<?> collection) {
        if (Objects.isNull(collection) || collection.isEmpty()) {
            throw new HvacEngineMissingArgumentException("Collection cannot be null and not empty.");
        }
    }

    public static <K extends Unit> void requireNonZero(PhysicalQuantity<K> quantityToCheck) {
        if (quantityToCheck.isEqualZero()) {
            throw new HvacEngineMissingArgumentException(quantityToCheck.getClass().getSimpleName() + " cannot hold ZERO value.");
        }
    }

    public static void requireNonZero(double valueToCheck, String variableName) {
        if (valueToCheck == 0) {
            throw new HvacEngineArgumentException("Value of " + variableName + " cannot be ZERO.");
        }
    }

    public static <K extends Unit> void requireAboveLowerBound(PhysicalQuantity<K> quantityToCheck, PhysicalQuantity<K> lowerBoundLimit) {
        if (quantityToCheck.isEqualOrLowerThan(lowerBoundLimit)) {
            throw new HvacEngineArgumentException(String.format("Lower bound limit exceeded. Actual: %s, limit: %s", quantityToCheck, lowerBoundLimit));
        }
    }

    public static <K extends Unit> void requireBelowUpperBound(PhysicalQuantity<K> quantityToCheck, PhysicalQuantity<K> upperBoundLimit) {
        if (quantityToCheck.isEqualOrGreaterThan(upperBoundLimit)) {
            throw new HvacEngineArgumentException(String.format("Upper bound limit exceeded. Actual:  %s, limit: %s", quantityToCheck, upperBoundLimit));
        }
    }

    public static <K extends Unit> void requireBetweenBounds(PhysicalQuantity<K> quantityToCheck, PhysicalQuantity<K> lowerBoundLimit, PhysicalQuantity<K> upperBoundLimit) {
        requireAboveLowerBound(quantityToCheck, lowerBoundLimit);
        requireBelowUpperBound(quantityToCheck, upperBoundLimit);
    }

    public static <K extends Unit> void requireAboveLowerBoundInclusive(PhysicalQuantity<K> quantityToCheck, PhysicalQuantity<K> lowerBoundLimit) {
        if (quantityToCheck.isLowerThan(lowerBoundLimit)) {
            throw new HvacEngineArgumentException(String.format("Lower bound limit reached or exceeded. Actual: %s, limit: %s", quantityToCheck, lowerBoundLimit));
        }
    }

    public static <K extends Unit> void requireBelowUpperBoundInclusive(PhysicalQuantity<K> quantityToCheck, PhysicalQuantity<K> upperBoundLimit) {
        if (quantityToCheck.isGreaterThan(upperBoundLimit)) {
            throw new HvacEngineArgumentException(String.format("Upper bound limit reached or exceeded: Actual: %s, limit: %s", quantityToCheck, upperBoundLimit));
        }
    }

    public static <K extends Unit> void requireBetweenBoundsInclusive(PhysicalQuantity<K> quantityToCheck, PhysicalQuantity<K> lowerBoundLimit, PhysicalQuantity<K> upperBoundLimit) {
        requireAboveLowerBoundInclusive(quantityToCheck, lowerBoundLimit);
        requireBelowUpperBoundInclusive(quantityToCheck, upperBoundLimit);
    }

}