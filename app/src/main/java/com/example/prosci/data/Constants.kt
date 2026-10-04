package com.example.prosci.data

data class CodataConstant(
    val symbol: String,
    val name: String,
    val value: Double,
    val unit: String
)

data class UnitConversion(
    val fromUnit: String,
    val toUnit: String,
    val factor: Double,
    val isForward: Boolean = true,
    val isTemperature: Boolean = false
)

object ConstantsData {
    // 40 CODATA physical constants
    val CONSTANTS = listOf(
        CodataConstant("mp", "Proton mass", 1.67262192369e-27, "kg"),
        CodataConstant("mn", "Neutron mass", 1.67492749804e-27, "kg"),
        CodataConstant("me", "Electron mass", 9.1093837015e-31, "kg"),
        CodataConstant("mμ", "Muon mass", 1.883531627e-28, "kg"),
        CodataConstant("a0", "Bohr radius", 5.29177210903e-11, "m"),
        CodataConstant("h", "Planck constant", 6.62607015e-34, "J·s"),
        CodataConstant("μN", "Nuclear magneton", 5.0507837461e-27, "J/T"),
        CodataConstant("μB", "Bohr magneton", 9.2740100783e-24, "J/T"),
        CodataConstant("ħ", "Reduced Planck const", 1.054571817e-34, "J·s"),
        CodataConstant("α", "Fine structure const", 7.2973525693e-3, ""),
        CodataConstant("re", "Classical electron radius", 2.8179403262e-15, "m"),
        CodataConstant("λc", "Compton wavelength", 2.42631023867e-12, "m"),
        CodataConstant("γp", "Proton gyromagnetic ratio", 2.6752218744e8, "s⁻¹·T⁻¹"),
        CodataConstant("λcp", "Proton Compton wavelength", 1.32140985539e-15, "m"),
        CodataConstant("λcn", "Neutron Compton wavelength", 1.31959090581e-15, "m"),
        CodataConstant("R∞", "Rydberg constant", 10973731.56816, "m⁻¹"),
        CodataConstant("u", "Atomic mass constant", 1.6605390666e-27, "kg"),
        CodataConstant("μp", "Proton magnetic moment", 1.41060679736e-26, "J/T"),
        CodataConstant("μe", "Electron magnetic moment", -9.2847647043e-24, "J/T"),
        CodataConstant("μn", "Neutron magnetic moment", -9.6623651e-27, "J/T"),
        CodataConstant("μμ", "Muon magnetic moment", -4.4904483e-26, "J/T"),
        CodataConstant("F", "Faraday constant", 96485.33212, "C/mol"),
        CodataConstant("e", "Elementary charge", 1.602176634e-19, "C"),
        CodataConstant("NA", "Avogadro constant", 6.02214076e23, "mol⁻¹"),
        CodataConstant("k", "Boltzmann constant", 1.380649e-23, "J/K"),
        CodataConstant("Vm", "Molar volume of ideal gas", 0.022413969545, "m³/mol"),
        CodataConstant("R", "Molar gas constant", 8.314462618, "J/(mol·K)"),
        CodataConstant("c0", "Speed of light in vacuum", 299792458.0, "m/s"),
        CodataConstant("c1", "1st radiation constant", 3.741771852e-16, "W·m²"),
        CodataConstant("c2", "2nd radiation constant", 0.01438776877, "m·K"),
        CodataConstant("σ", "Stefan-Boltzmann const", 5.670374419e-8, "W/(m²·K⁴)"),
        CodataConstant("ε0", "Electric constant", 8.8541878128e-12, "F/m"),
        CodataConstant("μ0", "Magnetic constant", 1.25663706212e-6, "N/A²"),
        CodataConstant("Φ0", "Magnetic flux quantum", 2.067833848e-15, "Wb"),
        CodataConstant("g", "Standard gravity", 9.80665, "m/s²"),
        CodataConstant("G0", "Conductance quantum", 7.748091729e-5, "S"),
        CodataConstant("Z0", "Impedance of vacuum", 376.730313668, "Ω"),
        CodataConstant("t", "Celsius zero point", 273.15, "K"),
        CodataConstant("G", "Newtonian gravitational const", 6.6743e-11, "m³/(kg·s²)"),
        CodataConstant("atm", "Standard atmosphere", 101325.0, "Pa")
    )

    // 40 unit conversions (forward and reverse pairs)
    val CONVERSIONS: List<UnitConversion> = run {
        val raw = listOf(
            Triple("in", "cm", 2.54),
            Triple("ft", "m", 0.3048),
            Triple("yd", "m", 0.9144),
            Triple("mile", "km", 1.609344),
            Triple("nmi", "m", 1852.0),
            Triple("acre", "m²", 4046.8564224),
            Triple("ha", "m²", 10000.0),
            Triple("gal(US)", "L", 3.785411784),
            Triple("gal(UK)", "L", 4.54609),
            Triple("pc", "km", 30856775814913.67),
            Triple("km/h", "m/s", 0.277777777778),
            Triple("oz", "g", 28.349523125),
            Triple("lb", "kg", 0.45359237),
            Triple("atm", "Pa", 101325.0),
            Triple("mmHg", "Pa", 133.322387415),
            Triple("bar", "Pa", 100000.0),
            Triple("hp", "kW", 0.7456998716),
            Triple("kgf/cm²", "Pa", 98066.5),
            Triple("kgf·m", "J", 9.80665),
            Triple("cal", "J", 4.1868),
            Triple("°F", "°C", 0.0) // special temp handling
        )
        val list = mutableListOf<UnitConversion>()
        for ((a, b, f) in raw) {
            val isTemp = (a == "°F" && b == "°C")
            list.add(UnitConversion(a, b, f, isForward = true, isTemperature = isTemp))
            list.add(UnitConversion(b, a, f, isForward = false, isTemperature = isTemp))
        }
        list
    }
}
