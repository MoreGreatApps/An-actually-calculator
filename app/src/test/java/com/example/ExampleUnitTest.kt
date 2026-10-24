package com.example

import com.example.calculator.engine.MathEvaluator
import com.example.calculator.engine.NumberBase
import com.example.calculator.engine.ProgrammerEngine
import com.example.calculator.engine.UnitCategory
import com.example.calculator.engine.UnitConverterEngine
import com.example.calculator.engine.WordSize
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

  @Test
  fun testBasicArithmetic() {
    val evaluator = MathEvaluator(isDegMode = true)
    val res1 = evaluator.evaluate("2 + 3 * 4")
    assertTrue(res1 is MathEvaluator.EvalResult.Success)
    assertEquals("14", (res1 as MathEvaluator.EvalResult.Success).formatted)

    val res2 = evaluator.evaluate("(10 - 2) / 4")
    assertTrue(res2 is MathEvaluator.EvalResult.Success)
    assertEquals("2", (res2 as MathEvaluator.EvalResult.Success).formatted)
  }

  @Test
  fun testScientificFunctionsDeg() {
    val evaluatorDeg = MathEvaluator(isDegMode = true)

    // sin(30) in degrees = 0.5
    val sin30 = evaluatorDeg.evaluate("sin(30)")
    assertTrue(sin30 is MathEvaluator.EvalResult.Success)
    assertEquals("0.5", (sin30 as MathEvaluator.EvalResult.Success).formatted)

    // cos(60) in degrees = 0.5
    val cos60 = evaluatorDeg.evaluate("cos(60)")
    assertTrue(cos60 is MathEvaluator.EvalResult.Success)
    assertEquals("0.5", (cos60 as MathEvaluator.EvalResult.Success).formatted)

    // tan(45) in degrees = 1
    val tan45 = evaluatorDeg.evaluate("tan(45)")
    assertTrue(tan45 is MathEvaluator.EvalResult.Success)
    assertEquals("1", (tan45 as MathEvaluator.EvalResult.Success).formatted)
  }

  @Test
  fun testFactorialAndPowers() {
    val evaluator = MathEvaluator()
    // 5! = 120
    val fact = evaluator.evaluate("5!")
    assertTrue(fact is MathEvaluator.EvalResult.Success)
    assertEquals("120", (fact as MathEvaluator.EvalResult.Success).formatted)

    // 2^10 = 1024
    val pwr = evaluator.evaluate("2^10")
    assertTrue(pwr is MathEvaluator.EvalResult.Success)
    assertEquals("1024", (pwr as MathEvaluator.EvalResult.Success).formatted)

    // sqrt(144) = 12
    val sq = evaluator.evaluate("sqrt(144)")
    assertTrue(sq is MathEvaluator.EvalResult.Success)
    assertEquals("12", (sq as MathEvaluator.EvalResult.Success).formatted)
  }

  @Test
  fun testConstantsAndImplicitMultiplication() {
    val evaluator = MathEvaluator(isDegMode = false)
    val piRes = evaluator.evaluate("2 * π")
    assertTrue(piRes is MathEvaluator.EvalResult.Success)
    assertTrue((piRes as MathEvaluator.EvalResult.Success).value > 6.28)
  }

  @Test
  fun testErrorHandling() {
    val evaluator = MathEvaluator()
    val divZero = evaluator.evaluate("5 / 0")
    assertTrue(divZero is MathEvaluator.EvalResult.Error)

    val sqrtNeg = evaluator.evaluate("sqrt(-4)")
    assertTrue(sqrtNeg is MathEvaluator.EvalResult.Error)
  }

  @Test
  fun testProgrammerBaseConversion() {
    val formattedHex = ProgrammerEngine.formatInBase(255L, NumberBase.HEX, WordSize.BYTE)
    assertEquals("FF", formattedHex)

    val formattedBin = ProgrammerEngine.formatInBase(15L, NumberBase.BIN, WordSize.BYTE)
    assertEquals("0000 1111", formattedBin)
  }

  @Test
  fun testUnitConversion() {
    val lengthUnits = UnitConverterEngine.getUnitsForCategory(UnitCategory.LENGTH)
    val meter = lengthUnits.first { it.id == "m" }
    val km = lengthUnits.first { it.id == "km" }

    val converted = UnitConverterEngine.convert(5000.0, meter, km)
    assertEquals(5.0, converted, 0.0001)
  }
}
