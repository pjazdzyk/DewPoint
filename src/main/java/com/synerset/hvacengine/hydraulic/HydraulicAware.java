package com.synerset.hvacengine.hydraulic;

import com.synerset.hvacengine.hydraulic.dataobject.HydraulicLossResult;

/**
 * Represents an entity capable of providing hydraulic calculation results.
 * Implementing classes should return {@link HydraulicLossResult} containing
 * relevant pressure drop results.
 */
public interface HydraulicAware {

    /**
     * Retrieves the hydraulic results.
     *
     * @return an instance of {@link HydraulicLossResult} containing hydraulic results.
     */
    HydraulicLossResult getHydraulicLossResult();

}
