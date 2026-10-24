package com.example.calculator.engine

data class ConstantItem(
    val symbol: String,
    val name: String,
    val category: String,
    val value: String,
    val numericValue: Double,
    val unit: String
)

object ScientificConstants {
    val list = listOf(
        // Mathematical
        ConstantItem("π", "Pi", "Math", "3.141592653589793", Math.PI, "ratio"),
        ConstantItem("e", "Euler's Number", "Math", "2.718281828459045", Math.E, "base"),
        ConstantItem("φ", "Golden Ratio", "Math", "1.618033988749895", 1.618033988749895, "ratio"),
        ConstantItem("√2", "Square root of 2", "Math", "1.414213562373095", 1.414213562373095, ""),
        ConstantItem("ln(2)", "Natural log of 2", "Math", "0.693147180559945", 0.693147180559945, ""),

        // Universal Physics
        ConstantItem("c", "Speed of Light in Vacuum", "Physics", "299792458", 299792458.0, "m/s"),
        ConstantItem("h", "Planck Constant", "Physics", "6.62607015e-34", 6.62607015e-34, "J·s"),
        ConstantItem("ħ", "Reduced Planck Constant", "Physics", "1.054571817e-34", 1.054571817e-34, "J·s"),
        ConstantItem("G", "Gravitational Constant", "Physics", "6.67430e-11", 6.67430e-11, "m³/(kg·s²)"),
        ConstantItem("g", "Standard Earth Gravity", "Physics", "9.80665", 9.80665, "m/s²"),

        // Electromagnetic & Atomic
        ConstantItem("e_charge", "Elementary Charge (e)", "Atomic", "1.602176634e-19", 1.602176634e-19, "C"),
        ConstantItem("m_e", "Electron Rest Mass", "Atomic", "9.1093837e-31", 9.1093837e-31, "kg"),
        ConstantItem("m_p", "Proton Rest Mass", "Atomic", "1.67262192e-27", 1.67262192e-27, "kg"),
        ConstantItem("m_n", "Neutron Rest Mass", "Atomic", "1.67492750e-27", 1.67492750e-27, "kg"),
        ConstantItem("ε₀", "Vacuum Permittivity", "Atomic", "8.8541878128e-12", 8.8541878128e-12, "F/m"),
        ConstantItem("μ₀", "Vacuum Permeability", "Atomic", "1.25663706212e-6", 1.25663706212e-6, "N/A²"),

        // Thermodynamic & Chemistry
        ConstantItem("N_A", "Avogadro Constant", "Chemistry", "6.02214076e23", 6.02214076e23, "mol⁻¹"),
        ConstantItem("k_B", "Boltzmann Constant", "Chemistry", "1.380649e-23", 1.380649e-23, "J/K"),
        ConstantItem("R", "Molar Gas Constant", "Chemistry", "8.314462618", 8.314462618, "J/(mol·K)"),
        ConstantItem("F", "Faraday Constant", "Chemistry", "96485.33212", 96485.33212, "C/mol"),
        ConstantItem("σ", "Stefan-Boltzmann Constant", "Physics", "5.670374419e-8", 5.670374419e-8, "W/(m²·K⁴)"),
        ConstantItem("atm", "Standard Atmosphere", "Physics", "101325", 101325.0, "Pa")
    )
}
