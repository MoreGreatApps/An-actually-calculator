package com.example.calculator.engine

enum class UnitCategory(val displayName: String) {
    LENGTH("Length"),
    MASS("Mass & Weight"),
    TEMPERATURE("Temperature"),
    AREA("Area"),
    VOLUME("Volume"),
    SPEED("Speed"),
    TIME("Time"),
    DATA("Data Storage"),
    PRESSURE("Pressure"),
    ENERGY("Energy")
}

data class UnitDefinition(
    val id: String,
    val name: String,
    val symbol: String,
    val toBase: (Double) -> Double,
    val fromBase: (Double) -> Double
)

object UnitConverterEngine {

    val categories: List<UnitCategory> = UnitCategory.values().toList()

    fun getUnitsForCategory(category: UnitCategory): List<UnitDefinition> {
        return when (category) {
            UnitCategory.LENGTH -> listOf(
                // Base: Meter
                UnitDefinition("m", "Meter", "m", { it }, { it }),
                UnitDefinition("km", "Kilometer", "km", { it * 1000.0 }, { it / 1000.0 }),
                UnitDefinition("cm", "Centimeter", "cm", { it * 0.01 }, { it / 0.01 }),
                UnitDefinition("mm", "Millimeter", "mm", { it * 0.001 }, { it / 0.001 }),
                UnitDefinition("mi", "Mile", "mi", { it * 1609.344 }, { it / 1609.344 }),
                UnitDefinition("yd", "Yard", "yd", { it * 0.9144 }, { it / 0.9144 }),
                UnitDefinition("ft", "Foot", "ft", { it * 0.3048 }, { it / 0.3048 }),
                UnitDefinition("in", "Inch", "in", { it * 0.0254 }, { it / 0.0254 }),
                UnitDefinition("nmi", "Nautical Mile", "NM", { it * 1852.0 }, { it / 1852.0 })
            )
            UnitCategory.MASS -> listOf(
                // Base: Kilogram
                UnitDefinition("kg", "Kilogram", "kg", { it }, { it }),
                UnitDefinition("g", "Gram", "g", { it * 0.001 }, { it / 0.001 }),
                UnitDefinition("mg", "Milligram", "mg", { it * 1e-6 }, { it / 1e-6 }),
                UnitDefinition("lb", "Pound", "lb", { it * 0.45359237 }, { it / 0.45359237 }),
                UnitDefinition("oz", "Ounce", "oz", { it * 0.028349523125 }, { it / 0.028349523125 }),
                UnitDefinition("ton", "Metric Ton", "t", { it * 1000.0 }, { it / 1000.0 }),
                UnitDefinition("st", "Stone", "st", { it * 6.35029318 }, { it / 6.35029318 })
            )
            UnitCategory.TEMPERATURE -> listOf(
                // Base: Celsius
                UnitDefinition("c", "Celsius", "°C", { it }, { it }),
                UnitDefinition("f", "Fahrenheit", "°F", { (it - 32.0) * 5.0 / 9.0 }, { it * 9.0 / 5.0 + 32.0 }),
                UnitDefinition("k", "Kelvin", "K", { it - 273.15 }, { it + 273.15 })
            )
            UnitCategory.AREA -> listOf(
                // Base: Square Meter
                UnitDefinition("sq_m", "Square Meter", "m²", { it }, { it }),
                UnitDefinition("sq_km", "Square Kilometer", "km²", { it * 1e6 }, { it / 1e6 }),
                UnitDefinition("sq_ft", "Square Foot", "ft²", { it * 0.09290304 }, { it / 0.09290304 }),
                UnitDefinition("sq_in", "Square Inch", "in²", { it * 0.00064516 }, { it / 0.00064516 }),
                UnitDefinition("acre", "Acre", "ac", { it * 4046.8564224 }, { it / 4046.8564224 }),
                UnitDefinition("ha", "Hectare", "ha", { it * 10000.0 }, { it / 10000.0 })
            )
            UnitCategory.VOLUME -> listOf(
                // Base: Liter
                UnitDefinition("l", "Liter", "L", { it }, { it }),
                UnitDefinition("ml", "Milliliter", "mL", { it * 0.001 }, { it / 0.001 }),
                UnitDefinition("cu_m", "Cubic Meter", "m³", { it * 1000.0 }, { it / 1000.0 }),
                UnitDefinition("gal", "US Gallon", "gal", { it * 3.785411784 }, { it / 3.785411784 }),
                UnitDefinition("qt", "US Quart", "qt", { it * 0.946352946 }, { it / 0.946352946 }),
                UnitDefinition("pt", "US Pint", "pt", { it * 0.473176473 }, { it / 0.473176473 }),
                UnitDefinition("cup", "US Cup", "cup", { it * 0.2365882365 }, { it / 0.2365882365 }),
                UnitDefinition("floz", "Fluid Ounce", "fl oz", { it * 0.0295735295625 }, { it / 0.0295735295625 })
            )
            UnitCategory.SPEED -> listOf(
                // Base: m/s
                UnitDefinition("mps", "Meters per second", "m/s", { it }, { it }),
                UnitDefinition("kph", "Kilometers per hour", "km/h", { it / 3.6 }, { it * 3.6 }),
                UnitDefinition("mph", "Miles per hour", "mph", { it * 0.44704 }, { it / 0.44704 }),
                UnitDefinition("knot", "Knot", "kn", { it * 0.514444444 }, { it / 0.514444444 })
            )
            UnitCategory.TIME -> listOf(
                // Base: Second
                UnitDefinition("s", "Second", "s", { it }, { it }),
                UnitDefinition("min", "Minute", "min", { it * 60.0 }, { it / 60.0 }),
                UnitDefinition("hr", "Hour", "hr", { it * 3600.0 }, { it / 3600.0 }),
                UnitDefinition("day", "Day", "d", { it * 86400.0 }, { it / 86400.0 }),
                UnitDefinition("wk", "Week", "wk", { it * 604800.0 }, { it / 604800.0 }),
                UnitDefinition("yr", "Year (365d)", "yr", { it * 31536000.0 }, { it / 31536000.0 })
            )
            UnitCategory.DATA -> listOf(
                // Base: Byte
                UnitDefinition("b", "Byte", "B", { it }, { it }),
                UnitDefinition("kb", "Kilobyte (1024)", "KB", { it * 1024.0 }, { it / 1024.0 }),
                UnitDefinition("mb", "Megabyte", "MB", { it * 1048576.0 }, { it / 1048576.0 }),
                UnitDefinition("gb", "Gigabyte", "GB", { it * 1073741824.0 }, { it / 1073741824.0 }),
                UnitDefinition("tb", "Terabyte", "TB", { it * 1099511627776.0 }, { it / 1099511627776.0 })
            )
            UnitCategory.PRESSURE -> listOf(
                // Base: Pascal
                UnitDefinition("pa", "Pascal", "Pa", { it }, { it }),
                UnitDefinition("kpa", "Kilopascal", "kPa", { it * 1000.0 }, { it / 1000.0 }),
                UnitDefinition("bar", "Bar", "bar", { it * 100000.0 }, { it / 100000.0 }),
                UnitDefinition("psi", "Pound per sq in", "psi", { it * 6894.75729 }, { it / 6894.75729 }),
                UnitDefinition("atm", "Atmosphere", "atm", { it * 101325.0 }, { it / 101325.0 }),
                UnitDefinition("torr", "Torr / mmHg", "Torr", { it * 133.322368 }, { it / 133.322368 })
            )
            UnitCategory.ENERGY -> listOf(
                // Base: Joule
                UnitDefinition("j", "Joule", "J", { it }, { it }),
                UnitDefinition("kj", "Kilojoule", "kJ", { it * 1000.0 }, { it / 1000.0 }),
                UnitDefinition("cal", "Calorie", "cal", { it * 4.184 }, { it / 4.184 }),
                UnitDefinition("kcal", "Kilocalorie", "kcal", { it * 4184.0 }, { it / 4184.0 }),
                UnitDefinition("kwh", "Kilowatt-hour", "kWh", { it * 3.6e6 }, { it / 3.6e6 }),
                UnitDefinition("ev", "Electronvolt", "eV", { it * 1.602176634e-19 }, { it / 1.602176634e-19 }),
                UnitDefinition("btu", "BTU", "BTU", { it * 1055.05585 }, { it / 1055.05585 })
            )
        }
    }

    fun convert(value: Double, fromUnit: UnitDefinition, toUnit: UnitDefinition): Double {
        val baseVal = fromUnit.toBase(value)
        return toUnit.fromBase(baseVal)
    }
}
