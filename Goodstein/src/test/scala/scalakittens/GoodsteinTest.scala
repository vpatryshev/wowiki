package scalakittens
import org.scalatest.flatspec.AnyFlatSpec
import Numbers._

import org.scalatest.matchers.should.Matchers

class GoodsteinTest extends AnyFlatSpec with Matchers {

  "Goodstein" should "eval tower" in {
    val _5 = Towers(5)
    val _10 = Towers(10)
    val twoByPowerOfThreeByTen = Towers(2, _10 -> BigInt(3))
    val sample = Towers(1, _5 -> 3, twoByPowerOfThreeByTen -> 5)
    val x = BigInt(7)
    val y = BigInt(8)
    sample.eval(1) shouldEqual 9
    twoByPowerOfThreeByTen.eval(2) shouldEqual 3074
    Towers(3, _5->2, _10->6).eval(2) shouldEqual 6211
    val bigOne = sample.eval(3)
    bigOne.bitLength shouldEqual 280777
    toDoubleLatexString(bigOne, 5) shouldEqual """1.78699\(\times\)10^{84522}"""
  }
}
