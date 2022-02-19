package Physics;

public class LibDefaults {

    //  Default names
    public static final String DEF_NAME = "New Air";           //                      - default moist air instance name

    // Default property values
    public final static double DEF_PAT = 101_325;              // [Pa]                 - Standard atmospheric pressure (physical atmosphere)
    public final static double DEF_AIR_TEMP = 20.0;                // [oC]                 - default air temperature oC
    public final static double DEF_AIR_RH = 50.0;                  // [%]                  - default relative humidity in %
    public final static double CST_G = 9.80665;                // [m/s^2]              - Gravitational acceleration
    public final static double DEF_WV_CP = 4190.0;             // [J/(kg*K)]           - Water specific heat
    public final static double DEF_WT_TW = 10.0;               // [oC]                 - Water temperature (default)
    public final static double DEF_ICE_CP = 2.09;              // [kJ/(kg*K)]          - Ice specific heat
    public final static double DEF_DA_RHO = 1.2;               // [kg/m3]              - Dry air default density
    public final static double DEF_DA_CP = 1.005;              // [kJ/(kg*K)]          - Default dry air specific heat
    public final static double DEF_ST_T = 105.0;               // [oC]                 - Saturated steam temperature for humidification (default)

    //Heat exchanger defaults
    public final static double DEF_RECOVERY = 0.5;             // -                    - Default heat recovery exchanger efficiency
    public final static double DEF_BPS_OPEN_LIMIT = 1.0;       // [oC]                 - Safety margin over dew point temperature for calculation of the bypass open temperature

    //Math
    public final static double DEF_MATH_ACCURACY = 0.000001;   // -                    - Default acceptable math accuracy

}
