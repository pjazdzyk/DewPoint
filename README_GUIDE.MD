## HVAC|Engine - Psychrometrics Analysis Library.<br> Thermodynamics of Humid Air for MEP/HVAC Engineers.<br>

## USER GUIDE

> Author: **Piotr Jażdżyk**, M.Sc.Eng.<br>
> LinkedIn: https://www.linkedin.com/in/pjazdzyk <br>

#### [\[ BACK TO HOME \]](README.MD)

HVAC|Engine library is composed of two main packages: 'fluids' and 'process.' The 'fluids' package comprises algorithms
for
calculating properties of dry air, humid air, water vapor, liquid water, and solid ice. The 'process' package includes
calculation procedures for obtaining results related to various thermodynamic processes typical in the HVAC industry,
such as heating, cooling, and mixing. Additional processes will be incorporated in the upcoming stages of
development.<br>
Thermophysical and thermodynamic properties are readily available through a collection of static methods based on
primitive types or by using Utility's physical units value objects. Public access to primitive types is provided to
facilitate iterative calculations, enhancing speed and reducing memory requirements.<br>
For developing your own application using this library, dedicated classes are available to represent the thermodynamic
state of fluids with all associated calculated parameters, along with proper input argument validation and physical
units.

## TABLE OF CONTENTS

1. [Physical properties](#1-physical-properties) <br>
   1.1 [Dry and humid air](#11-dry-and-humid-air) <br>
   1.2 [Liquid water and ice](#12-liquid-water-and-ice) <br>
2. [Flow of fluid](#2-flow-of-fluid) <br>
3. [Thermodynamic processes](#3-thermodynamic-processes) <br>
   3.1 [Air heating](#31-air-heating) <br>
   3.2 [Air cooling](#32-air-cooling) <br>
   3.3 [Air mixing](#33-air-mixing) <br>
4. [HVAC Modular Dataflow Processing](#4-hvac-modular-dataflow-processing-system) <br>
   4.1 [Data model components](#41-data-model-components) <br>
   4.2 [Custom HVAC system example](#42-example-of-custom-hvac-system-calculation) <br>
   4.3 [Thread safety and mutability](#43-thread-safety-and-mutability) <br>
5. [Symbols and abbreviations](#5-symbols-and-abbreviations) <br>

### 1. PHYSICAL PROPERTIES

#### 1.1 DRY AND HUMID AIR

Air properties, whether for dry air or humid air, can be calculated independently using the provided equations or as
a thermodynamic state representing all properties based on the given temperature, pressure, and humidity for humid air.
Below is an example of direct property calculation for the humidity ratio of humid air using primitive argument types
and physical units.

```java
// Outputs: 0.00754
double humidityRatioValue = HumidAirEquations.humidityRatio(45, 2662, 100_000);

// Outputs: HumidityRatio{0.00754 kg/kg}
HumidityRatio humRatioWithUnits = HumidAirEquations.humidityRatio(
        RelativeHumidity.ofPercentage(45),
        Pressure.ofPascal(2662),
        Pressure.ofPascal(100_000)
);
```

Methods utilizing primitive types are designed for iterative calculations to minimize server memory usage and CPU time
consumption. However, for non-intensive workloads, it is advisable to employ Utility types as arguments to specify input
values in any of the supported units:

```java
// Outputs: Pressure{2338.8 Pa}
Pressure saturationPressure_c = HumidAirEquations.saturationPressure(Temperature.ofCelsius(20));
// Outputs: Pressure{2338.8 Pa}
Pressure saturationPressure_f = HumidAirEquations.saturationPressure(Temperature.ofFahrenheit(68));
```

Both methods will produce the same value regardless of the unit provided as an argument. By default, the resulting value
is maintained in the predefined unit, but it can be readily converted to any other unit or to a value in the desired
unit.
For example:

```java
// Outputs: Pressure{100000 Pa}
Pressure pressureInPa = Pressure.ofPascal(100_000);
// Outputs: Pressure{1.0 bar}
Pressure pressureInBar = pressureInPa.toBar();
// Outputs: 1.0
double pressureValueInBar = pressureInPa.getInBar();
```

For more detailed information on handling physical units, please refer to
the [UNITILITY - User Guide](https://github.com/pjazdzyk/unitility).<br>
To model the air state based on provided initial conditions there are dedicated classes available for each supported
fluid type. An instance of these classes encompasses all available parameters calculated for the provided input values,
including pressure, temperature, and humidity. Here's an example of an object representing the state of humid air under
typical summer outdoor conditions

```java
HumidAir summerAir = HumidAir.of(
        Pressure.STANDARD_ATMOSPHERE,
        Temperature.ofCelsius(30),
        RelativeHumidity.ofPercentage(45)
);
```

For users or developers utilizing the console for result output, we have provided a dedicated method,
_toConsoleOutput()_,
which will display the air state in a readable and structured format. This method also automatically rounds values to
four relevant digits

```text
HumidAir:
	P_abs = 101325 [Pa] | DBT = 30 [°C] | RH = 45 [%] | x = 0.012 [kg/kg] | x" = 0.0272 [kg/kg]
	Ps = 4246.03 [Pa] | WBT = 16.776 [°C] | TDP = 21.053 [°C] | Vapour status: UNSATURATED
	i = 60.72 [kJ/kg] | ρ = 1.142 [kg/m³] | cp = 1.027 [kJ/(kg·K)]
	ν = 0.0000161 [m²/s] | μ = 0.0000184 [kg/(m·s)] | k = 0.0271 [W/(m·K)]
	α = 0.0000231 [m²/s] | Pr = 0.697
	DryAir:
	P_abs = 101325 [Pa] | DBT = 30 [°C]
	i_da = 30.156 [kJ/kg] | ρ_da = 1.164 [kg/m³] | cp_da = 1.005 [kJ/(kg·K)]
	ν_da = 0.0000159 [m²/s] | μ_da = 0.0000185 [kg/(m·s)] | k_da = 0.0267 [W/(m·K)] | Pr_da = 0.698
```

Please note that an instance of DryAir is a subcomponent of HumidAir. The property symbols used in this context adhere
to commonly accepted practices in the engineering field. However, if you have any doubts, you can refer to
the [SYMBOLS](#symbols-and-abbreviations)
section for clarification.<br>
Using the DryAir state class is analogous, with the distinction that humidity is not required as an argument.

#### 1.2 LIQUID WATER AND ICE

Water properties can be computed directly using the equation class or initiated as a liquid water state with specific
temperature and pressure values. It's important to note that, at this stage, all liquid properties are not considered
pressure-dependent, which is sufficient for psychrometric calculations. However, it's possible that this feature will
be added in the future. Please be aware that water state can only be created for positive temperatures. <br>
Here's an example of calculating liquid water density based on temperature

```java
// Outputs: 998.884
double rho_val = LiquidWaterEquations.density(15);
// Outputs: Density{998.884 kg/kg}
Density rho = LiquidWaterEquations.density(Temperature.ofCelsius(15));
```

Example of creating liquid water state for a condensate:

```java
LiquidWater condensate = LiquidWater.of(Temperature.ofCelsius(30));
```

Using toConsoleOutput() method will result in the following output:

```text
LiquidWater:
	P_abs = 101325 [Pa] | t_w = 30 [°C]
	i_w = 125.403 [kJ/kg] | ρ_w = 995.29 [kg/m³] | cp_w = 4.18 [kJ/(kg·K)]
```

For the calculation of ice mist enthalpy, we have also provided a class to represent the ice state and its properties:

```java
// Outputs: 1.9995
IceEquations.specificHeat(-10);
// SpecificHeat{1.9995 kJ/(kg·K)}
IceEquations.specificHeat(Temperature.ofCelsius(-10));
```

Or using Ice state class:

```java
Ice.of(Temperature.ofCelsius(-11));
```

Resulting formatted output:

```text
Ice:
	P_abs = 101325 [Pa] | t_ice = -11 [°C]
	i_ice = -356.035 [kJ/kg] | ρ_ice = 918.831 [kg/m³] | cp_ice = 1.994 [kJ/(kg·K)]
```

### 2. FLOW OF FLUID

Supported fluids have dedicated classes to represent the fluid flow. An instance of this class is constructed by
providing an instance of a fluid and one of the flow components, such as mass flow or volumetric flow. Based on this
input, all other flows are calculated using the fluid properties. The Flow class encapsulates the most relevant
properties
from a composite Fluid, including temperature, pressure, and others. The Flow class is a crucial component for
psychrometric
process algorithms. <br>
Below is an example of how to create an instance representing the flow of humid air:

```java
HumidAir winterAir = HumidAir.of(Temperature.ofCelsius(-20), HumidityRatio.ofKilogramPerKilogram(0.001));
FlowOfHumidAir.of(winterAir, VolumetricFlow.ofCubicMetersPerHour(5000));
```

We have specified the flow in m3/h, and there's no need to concern ourselves with unit conversions because, internally,
the Unitility framework ensures that values are always correctly converted to the appropriate units for calculations.
For psychrometric calculations involving humid air, we require mass flow, volumetric flow, and dry air mass flow, and
all of these flows are calculated and stored within the Flow instance.
Below is an example of how the console output for the Flow instance defined above: <br>

```text
FlowOfHumidAir:
	G = 1.933 [kg/s] | G = 6959.496 [kg/h] | V = 1.389 [m³/s] | V = 5000 [m³/h]
	G_da = 1.931 [kg/s] | G_da = 6952.544 [kg/h]
	HumidAir:
	P_abs = 101325 [Pa] | DBT = -20 [°C] | RH = 100 [%] | x = 0.001 [kg/kg] | x" = 0.000634 [kg/kg]
	Ps = 103.26 [Pa] | WBT = -20 [°C] | TDP = -20 [°C] | Vapour status: ICE_FOG
	i = -18.633 [kJ/kg] | ρ = 1.392 [kg/m³] | cp = 1.005 [kJ/(kg·K)]
	ν = 0.0000115 [m²/s] | μ = 0.0000161 [kg/(m·s)] | k = 0.0228 [W/(m·K)]
	α = 0.0000163 [m²/s] | Pr = 0.707
	DryAir:
	P_abs = 101325 [Pa] | DBT = -20 [°C]
	i_da = -20.06 [kJ/kg] | ρ_da = 1.394 [kg/m³] | cp_da = 1.003 [kJ/(kg·K)]
	ν_da = 0.0000115 [m²/s] | μ_da = 0.0000161 [kg/(m·s)] | k_da = 0.0228 [W/(m·K)] | α = 0.0000163 [m²/s] | Pr_da = 0.707
```

The result above includes all flow values and additionally humid air properties and humid air dry air component
properties.
Handling flows for the dry air or liquid water is analogous.

### 3. THERMODYNAMIC PROCESSES

#### 3.1. AIR HEATING

There are three humid air heating strategies:

- based on input heating power,
- based on target output temperature,
- based on target relative humidity

Let's assume a typical winter case, when we want to heat up outdoor air up to +16 °C. First we need to create initial
state
of winter humid air, then select the correct heating strategy to create a heating process result object.

```java
// Creating initial state of ambient air properties
HumidAir winterAmbientAir = HumidAir.of(Temperature.ofCelsius(-20), RelativeHumidity.ofPercentage(99));
// Initializing flow of air instance
FlowOfHumidAir flowOfHumidAir = FlowOfHumidAir.of(winterAmbientAir, VolumetricFlow.ofCubicMetersPerHour(5000));
// Defining target outlet temperature
Temperature targetOutTemperature = Temperature.ofCelsius(16);
// Calculating heating process, we expect as result: Q_heat = 69928.557 W
HeatingResult heatingResult = HeatingEquations.heatingFromTargetTemperature(flowOfHumidAir, targetOutTemperature);
```

The heating process, upon creation, will invoke calculation procedures stored inside the selected strategy, ultimately
determining the heating power. Like all other process classes, the Heating class encapsulates the most relevant outlet
air properties, including temperature, pressure, humidity, enthalpy, and others, for simplified value extraction when
necessary.<br>
For quick console output, you can use the toConsoleOutput() method:

```text
PROCESS OF HEATING:
	INPUT FLOW:
	V_in = 5000 [m³/h] | G_in = 1.934 [kg/s] | G_in.da = 1.933 [kg/s]
	DBT_in = -20 [°C] | RH_in = 99 [%] | x_in = 0.000628 [kg/kg] | i_in = -18.512 [kJ/kg]
	HEATING POWER:
	Q_heat = 69928.557 [W] | Q_heat = 69.929 [kW]
	OUTLET FLOW:
	V_out = 5711.041 [m³/h] | G_out = 1.934 [kg/s] | G_out.da = 1.933 [kg/s]
	DBT_out = 16 [°C] | RH_out = 5.622 [%] | x_out = 0.000628 [kg/kg] | i_out = 17.661 [kJ/kg]
```

As you can observe, the resulting volumetric flow is greater than inlet flow. This is because the density of heated is
lower
than cold ambient air, resulting in increased volume of air after the heater. Mass flow on the other hand, as expected,
remained unchanged.

#### 3.2. AIR COOLING

There are two main procedures available: dry cooling and real cooling coil with condensate discharge.
There are the following strategies cooling available:

- based on input heating power,
- based on target output temperature,
- based on target relative humidity

Dry cooling does not support the strategy of cooling by target relative humidity (RH), and its usage is similar to the
heating process. For real coil cooling, one additional step is required: Coolant data must be provided. Here's an
example
of cooling the ambient summer air to a typical indoor temperature of 25°C. We assume supply and return temperatures
in the coil as 7/12°C:

```java
// Creating initial state of ambient air properties
HumidAir summerAir = HumidAir.of(Temperature.ofCelsius(35), RelativeHumidity.ofPercentage(45));
// Initializing flow of air instance
FlowOfHumidAir flowOfSummerAir = FlowOfHumidAir.of(summerAir, VolumetricFlow.ofCubicMetersPerHour(5000));
// Defining coolant properties
CoolantData chilledWater = CoolantData.of(Temperature.ofCelsius(7), Temperature.ofCelsius(12));
// Defining target outlet temperature
Temperature targetOutTemp = Temperature.ofCelsius(25);
// Calculating cooling process, we expect as result: Q_heat = 69928.557 W
CoolingResult coolingResult = CoolingEquations.coolingFromTargetTemperature(flowOfSummerAir, chilledWater, targetOutTemp);
```

This process will result in the following output:

```text
PROCESS OF REAL COOLING:
	INPUT FLOW:
	V_in = 5000 [m³/h] | G_in = 1.551 [kg/s] | G_in.da = 1.527 [kg/s]
	DBT_in = 35 [°C] | RH_in = 45 [%] | x_in = 0.0159 [kg/kg] | i_in = 76.105 [kJ/kg]
	COOLANT DATA:
	t_su = 7 [°C] | t_rt = 12 [°C] | t_m = 9.5 [°C]
	V_coolant = 4.93 [m³/h] | G_coolant = 1.369 [kg/s]
	COOLING POWER:
	Q_cool = 28730.585 [W] | Q_cool = 28.731 [kW]
	OUTLET FLOW:
	V_out = 4796.343 [m³/h] | G_out = 1.546 [kg/s] | G_out.da = 1.527 [kg/s]
	DBT_out = 25 [°C] | RH_out = 63.403 [%] | x_out = 0.0126 [kg/kg] | i_out = 57.18 [kJ/kg]
	CONDENSATE:
	G_cond = 0.00513 [kg/s] | t_cond = 9.5 [°C] | i_cond = 39.866 [kJ/kg]
```

It can be observed that the air was successfully cooled to the expected temperature while also experiencing partial
dehumidification. Additionally, the condensate flow has been calculated, along with the cooling power and coil bypass
factor. It's worth noting that the outlet volumetric flow is smaller than the inlet flow due to the lower density after
cooling, but mass flow is conserved.

#### 3.3. AIR MIXING

There are two strategies available for a mixing process:

- mixing inlet flow with one recirculation flow
- mixing inlet flow with multiple recirculation flows

Let's take a typical industry case, where we mix ambient external winter air in mixing plenum
with air inside a typical heated production hall:

```java
HumidAir winterAmbientAir = HumidAir.of(Temperature.ofCelsius(-20), RelativeHumidity.ofPercentage(99));
FlowOfHumidAir inletWinterFlow = FlowOfHumidAir.of(winterAmbientAir, VolumetricFlow.ofCubicMetersPerHour(1000));
HumidAir recirculationAir = HumidAir.of(Temperature.ofCelsius(16), RelativeHumidity.ofPercentage(25));
FlowOfHumidAir flowOfRecirculationAir = FlowOfHumidAir.of(recirculationAir, VolumetricFlow.ofCubicMetersPerHour(5000));
Mixing mixingProcess = Mixing.of(MixingStrategy.of(inletWinterFlow, flowOfRecirculationAir));
```

This process will result in the following output:

```text
 // Creating initial state of ambient fresh air properties
        HumidAir winterAmbientAir = HumidAir.of(Temperature.ofCelsius(-20), RelativeHumidity.ofPercentage(99));
        // Initializing flow of fresh air
        FlowOfHumidAir inletWinterFlow = FlowOfHumidAir.of(winterAmbientAir, VolumetricFlow.ofCubicMetersPerHour(1000));
        // Creating initial state of ambient recirculation air properties
        HumidAir recirculationAir = HumidAir.of(Temperature.ofCelsius(16), RelativeHumidity.ofPercentage(25));
        // Initializing flow of recirculation air
        FlowOfHumidAir flowOfRecirculationAir = FlowOfHumidAir.of(recirculationAir, VolumetricFlow.ofCubicMetersPerHour(5000));
        // Calculating mixing process outcome
        MixingResult mixingResult = MixingEquations.mixingOfTwoAirFlows(inletWinterFlow, flowOfRecirculationAir);
```

As expected, the resulting temperature is approximately 9°C, including all other parameters of the outlet flow.

### 4. HVAC MODULAR DATAFLOW PROCESSING SYSTEM

To enable calculations for any custom, user-defined HVAC device, a dedicated dataflow model was developed. This model
allows users
to independently connect multiple processes, providing a flexible framework for constructing and customizing HVAC
simulations.

Users can build HVAC processes by linking various process blocks and data sources. The sequential nature of the
algorithm
ensures that the output from one process serves as the input for the next, allowing for a structured simulation of any
HVAC
system based on currently available and supported processes.

#### 4.1. DATA MODEL COMPONENTS

The data model consists of the following components, each represented by a dedicated class:

- **Data Connectors**: Available as input or output connectors, these serve as interface points where data is either
  received for calculations or provided as results after computation. Connector can be connected with other connector of
  the same datatype. Output connector can be connected only to input connector.
- **Source Blocks**: Represent a physical quantity or an object aggregating multiple quantities. Each source block has
  a single output connector and defines the boundary conditions of the simulation.
- **Process Blocks**: Can have multiple inlet and outlet connectors but must include at least one airflow input and one
  airflow output connector.
  These blocks encapsulate the processing logic of specific HVAC processes. During computation, data is retrieved from
  input
  connectors, processed, and then sent to output connectors.

![SequentialDiagram.png](src/main/resources/architecture/SequentialDiagram.png)

The diagram above illustrates the logical interpretation of the modular processing model. At this stage, only the
SequentialProcessingEngine is implemented, which sequentially computes processes based on their order and generates
the simulation output. The diagram presents an example where ambient summer air is mixed with interior recirculated air,
then cooled to reach the target temperature, and subsequently heated to achieve the desired relative humidity.

#### 4.2. EXAMPLE OF CUSTOM HVAC SYSTEM CALCULATION

Let's try to implement the exemplary HVAC system presented on the diagram above. Following input data will be used:

```text
External summer inlet flow:
te = 35°C, RH = 55%, Vin = 1000 m³/h
Rercirculation flow:
tr = 25°C, RH = 70%, Vrec = 1000 m³/h
Coolant data:
ts = 7°C, tret = 14°C, 
Cooling target:
tset = 25°C
Heating target:
tset = 30%
```

Now, based on this input data lets build our components. We will use SourceBlock class to define boundary conditions for
process blocks inlet connectors. In the next step, we use correct implementation of HvacProcessBlock depending on which
process and its variant we want to use:

```java
// Inlet Air data source
FlowOfHumidAir inletFlow = FlowOfHumidAir.ofValues(35, 55, 1000);
SimpleDataSource<FlowOfHumidAir> airFlowSource = new SimpleDataSource<>(inletFlow);
// Recirculation air data source
FlowOfHumidAir recirculationAir = FlowOfHumidAir.ofValues(25, 70, 1000);
SimpleDataSource<FlowOfHumidAir> mixingFlowSource = SimpleDataSource.of(recirculationAir);
// Mixing block config
Mixing mixingBlock = Mixing.of(airFlowSource, List.of(mixingFlowSource));
// Cooling data config
Temperature targetCoolingTemperature = Temperature.ofCelsius(25);
CoolantData coolantData = CoolantData.ofValues(7, 14);
SimpleDataSource<CoolantData> coolantDataSource = SimpleDataSource.of(coolantData);
SimpleDataSource<Temperature> temperatureDataSource = SimpleDataSource.of(targetCoolingTemperature);
CoolingFromTemperature coolingBlock = CoolingFromTemperature.of(coolantDataSource, temperatureDataSource);
// Heating block config
RelativeHumidity targetRH = RelativeHumidity.ofPercentage(30);
SimpleDataSource<RelativeHumidity> humiditySourceBlock = new SimpleDataSource<>(targetRH);
HeatingFromHumidity heatingBlock = HeatingFromHumidity.of(humiditySourceBlock);
```

When all blocks are defined and proper boundary conditions are set, we can add our blocks to
processing engine. Processing engine will connect airflow outputs with airflow input connector of all
added blocks. There is no need to manually do that.

```java
// Initialize processing engine, add all blocks. Order matters, in this order computation will be executed.
SequentialProcessingEngine processComputation = SequentialProcessingEngine.of(mixingBlock, coolingBlock, heatingBlock);
// Run calculations and collect the results at the outlet of simulation boundary (after las process)
ProcessResult lastResult = processComputation.runCalculationsForAllNodes();
// Tu can also extract results for each intermediate process as well
List<ProcessResult> allResults = processComputation.getProcessResults();
// Or print everything to console for quick check
System.out.println(processComputation.toConsoleOutput());
```

Let's print results to the console:

```text
PROCESS OF MIXING:
	INPUT FLOW:
	V_in = 1000 [m³/h] | G_in = 0.308 [kg/s] | G_in.da = 0.302 [kg/s]
	DBT_in = 35 [°C] | RH_in = 55 [%] | x_in = 0.0196 [kg/kg] | i_in = 85.484 [kJ/kg]
	RECIRCULATION AIR_0:
	V_rec_0 = 1000 [m³/h] | G_rec_0 = 0.322 [kg/s] | G_rec_0.da = 0.317 [kg/s]
	DBT_rec_0 = 25 [°C] | RH_rec_0 = 70 [%] | x_rec_0 = 0.0139 [kg/kg] | i_rec_0 = 60.591 [kJ/kg]
	OUTLET FLOW:
	V_out = 1999.913 [m³/h] | G_out = 0.63 [kg/s] | G_out.da = 0.62 [kg/s]
	DBT_out = 29.908 [°C] | RH_out = 62.704 [%] | x_out = 0.0167 [kg/kg] | i_out = 72.742 [kJ/kg]
	
PROCESS OF REAL COOLING:
	INPUT FLOW:
	V_in = 1999.913 [m³/h] | G_in = 0.63 [kg/s] | G_in.da = 0.62 [kg/s]
	DBT_in = 29.908 [°C] | RH_in = 62.704 [%] | x_in = 0.0167 [kg/kg] | i_in = 72.742 [kJ/kg]
	COOLANT DATA:
	t_su = 7 [°C] | t_rt = 14 [°C] | t_m = 10.5 [°C]
	V_coolant = 0.813 [m³/h] | G_coolant = 0.226 [kg/s]
	COOLING POWER:
	Q_cool = 6629.681 [W] | Q_cool = 6.63 [kW]
	OUTLET FLOW:
	V_out = 1956.376 [m³/h] | G_out = 0.629 [kg/s] | G_out.da = 0.62 [kg/s]
	DBT_out = 25 [°C] | RH_out = 72.679 [%] | x_out = 0.0145 [kg/kg] | i_out = 61.98 [kJ/kg]
	CONDENSATE:
	G_cond = 0.00138 [kg/s] | t_cond = 10.5 [°C] | i_cond = 44.045 [kJ/kg]
	
PROCESS OF HEATING:
	INPUT FLOW:
	V_in = 1956.376 [m³/h] | G_in = 0.629 [kg/s] | G_in.da = 0.62 [kg/s]
	DBT_in = 25 [°C] | RH_in = 72.679 [%] | x_in = 0.0145 [kg/kg] | i_in = 61.98 [kJ/kg]
	HEATING POWER:
	Q_heat = 10082.581 [W] | Q_heat = 10.083 [kW]
	OUTLET FLOW:
	V_out = 2059.627 [m³/h] | G_out = 0.629 [kg/s] | G_out.da = 0.62 [kg/s]
	DBT_out = 40.735 [°C] | RH_out = 30 [%] | x_out = 0.0145 [kg/kg] | i_out = 78.25 [kJ/kg]
```

In the code example above, not all details of block connections are visible due to the use of a static factory method,
which is provided for developer convenience. However, you can manually link connectors at any time if fine-grained
control
is required. Additionally, blocks can be connected directly to each other, in which case the connectors will be
automatically
linked.

```java
// Inlet Air
FlowOfHumidAir inletFlow = FlowOfHumidAir.ofValues(35, 55, 1000);
SimpleDataSource<FlowOfHumidAir> airFlowSource = new SimpleDataSource<>(inletFlow);
// Recirculation flow
FlowOfHumidAir recirculationAir = FlowOfHumidAir.ofValues(25, 70, 1000);
SimpleDataSource<FlowOfHumidAir> mixingFlowSource = SimpleDataSource.of(recirculationAir);
// Creating empty mixin block
Mixing mixingBlock = new Mixing();
// Linking two connectors directly
mixingBlock.

getInputConnector().

connectAndConsumeDataFrom(airFlowSource.getOutputConnector());
// Linking two blocks
        mixingBlock.

connectMixingFlowDataSource(mixingFlowSource);
```

#### 4.3. THREAD SAFETY AND MUTABILITY

As the **exceptional case**, presented module dataflow HVAC processing system and its components **is NOT thread safe**.
Blocks are designed to be **stateful** and **mutable** objects. Please consider this when designing a system using
processing
engine components.

### 5. SYMBOLS AND ABBREVIATIONS

Property symbols used in console as formatted values:

| Symbol | Description                       |
|--------|-----------------------------------|
| P_abs  | Absolute pressure                 |
| DBT    | Dry bulb temperature              |
| WBT    | Wet bulb temperature              |
| TDP    | Dew point temperature             |
| RH     | Relative humidity                 |
| x      | Humidity ratio                    |
| Ps     | Saturation pressure               |
| i      | Specific enthalpy                 |
| ρ      | Density                           |
| cp     | Specific heat (constant pressure) |
| ν      | Kinematic viscosity               |
| μ      | Dynamic viscosity                 |
| k      | Thermal conductivity              |
| α      | Thermal diffusivity               |
| Pr     | Prandtl number                    |
| t_su   | Supply temperature                |
| t_rt   | Return temperature                |
| t_m    | Average temperature               |

Commonly used abbreviations:

| Suffix | Description  |
|--------|--------------|
| da     | Dry air      |
| w      | Liquid water |
| wv     | Water vapor  |
| ha     | Humid air    |
| ice    | Water ice    |

Water vapour status:

| Type        | Description                                                    |
|-------------|----------------------------------------------------------------|
| UNSATURATED | Water in the form of unsaturated vapour (x < x.max)            |
| SATURATED   | Water in the form of saturated vapour (x = x.max)              |
| WATER_MIST  | Water partially in the form of water mist (x > x.max, DBT > 0) |
| ICE_FOG     | Water partially in the form of ice fog (x > x.max, DBT <= 0)   |