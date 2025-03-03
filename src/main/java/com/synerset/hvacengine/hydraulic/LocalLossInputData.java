package com.synerset.hvacengine.hydraulic;

import com.synerset.hvacengine.hydraulic.dataobject.LocalLossFactorData;
import com.synerset.hvacengine.hydraulic.dataobject.LocalLossPressureData;
import com.synerset.unitility.unitsystem.CalculableQuantity;
import com.synerset.unitility.unitsystem.hydraulic.LocalLossFactor;
import com.synerset.unitility.unitsystem.thermodynamic.Pressure;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Class that holds the input data for local losses, either in terms of loss coefficients or pressure losses.
 * It does not calculate pressure loss, but aggregates local pressure loss data in terms of already provided
 * pressure loss or as the loss coefficients. This data will be used to determine pressures during the
 * calculation in the processable block.
 */
public class LocalLossInputData {

    private final List<LocalLossFactorData> localLossCoefficients;
    private final List<LocalLossPressureData> localLossPressures;
    private final LocalLossFactor sumOfAllInputLossCoefs;
    private final Pressure totalInputPressureLoss;

    /**
     * Constructor that initializes the lists of LocalLossCoefficients and LocalLossPressures.
     * If either of these lists is null, they are defaulted to empty lists.
     *
     * @param localLossCoefficients List of LocalLossCoefficient (can be null).
     * @param localLossPressures    List of LocalLossPressure (can be null).
     */
    public LocalLossInputData(List<LocalLossFactorData> localLossCoefficients, List<LocalLossPressureData> localLossPressures) {
        this.localLossCoefficients = localLossCoefficients != null ? localLossCoefficients : Collections.emptyList();
        this.localLossPressures = localLossPressures != null ? localLossPressures : Collections.emptyList();
        this.sumOfAllInputLossCoefs = calcSumOfAllCoefficients(this.localLossCoefficients);
        this.totalInputPressureLoss = calcSumOfAllPressures(this.localLossPressures);
    }

    /**
     * Calculates the sum of all loss coefficients from a list of LocalLossCoefficient.
     *
     * @param coefficients List of LocalLossCoefficient (can be empty).
     * @return The sum of all coefficients.
     */
    private LocalLossFactor calcSumOfAllCoefficients(List<LocalLossFactorData> coefficients) {
        return coefficients.stream()
                .filter(Objects::nonNull)
                .map(LocalLossFactorData::lossCoefficient)
                .reduce(LocalLossFactor.of(0), CalculableQuantity::plus);
    }

    /**
     * Calculates the sum of all pressure losses from a list of LocalLossPressure.
     *
     * @param pressureLosses List of LocalLossPressure (can be empty).
     * @return The sum of all pressure losses.
     */
    private Pressure calcSumOfAllPressures(List<LocalLossPressureData> pressureLosses) {
        return pressureLosses.stream()
                .filter(Objects::nonNull)
                .map(LocalLossPressureData::localPressureLoss)
                .reduce(Pressure.ofPascal(0), CalculableQuantity::plus);
    }

    /**
     * Gets the list of LocalLossCoefficients.
     *
     * @return The list of LocalLossCoefficient (can be empty).
     */
    public List<LocalLossFactorData> getLocalLossCoefficients() {
        return localLossCoefficients;
    }

    /**
     * Gets the list of LocalLossPressures.
     *
     * @return The list of LocalLossPressure (can be empty).
     */
    public List<LocalLossPressureData> getLocalLossPressures() {
        return localLossPressures;
    }

    /**
     * Gets the sum of all input loss coefficients.
     *
     * @return The sum of all input loss coefficients.
     */
    public LocalLossFactor getSumOfAllInputLossCoefs() {
        return sumOfAllInputLossCoefs;
    }

    /**
     * Gets the total input pressure loss.
     *
     * @return The total input pressure loss.
     */
    public Pressure getTotalInputPressureLoss() {
        return totalInputPressureLoss;
    }

    // Static factory methods

    /**
     * Creates a LocalLossInputData object with both LocalLossCoefficients and LocalLossPressures.
     *
     * @param localLossCoefficients The list of LocalLossCoefficient.
     * @param localLossPressures    The list of LocalLossPressure.
     * @return A new LocalLossInputData object.
     */
    public static LocalLossInputData of(List<LocalLossFactorData> localLossCoefficients, List<LocalLossPressureData> localLossPressures) {
        return new LocalLossInputData(localLossCoefficients, localLossPressures);
    }

    /**
     * Creates a LocalLossInputData object with only LocalLossCoefficients, with LocalLossPressures as an empty list.
     *
     * @param localLossCoefficients The list of LocalLossCoefficient.
     * @return A new LocalLossInputData object.
     */
    public static LocalLossInputData ofCoefs(List<LocalLossFactorData> localLossCoefficients) {
        return new LocalLossInputData(localLossCoefficients, Collections.emptyList());
    }

    /**
     * Creates a LocalLossInputData object with only LocalLossPressures, with LocalLossCoefficients as an empty list.
     *
     * @param localLossPressures The list of LocalLossPressure.
     * @return A new LocalLossInputData object.
     */
    public static LocalLossInputData ofLosses(List<LocalLossPressureData> localLossPressures) {
        return new LocalLossInputData(Collections.emptyList(), localLossPressures);
    }

    /**
     * Creates an empty LocalLossInputData object.
     *
     * @return A new empty LocalLossInputData object.
     */
    public static LocalLossInputData createEmpty() {
        return new LocalLossInputData(Collections.emptyList(), Collections.emptyList());
    }

}