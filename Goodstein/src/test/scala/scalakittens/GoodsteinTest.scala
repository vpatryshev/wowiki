package scalakittens
import org.scalatest.flatspec.AnyFlatSpec
import Numbers._
import org.scalatest.matchers.should.Matchers
import scalakittens.Goodstein._

class GoodsteinTest extends AnyFlatSpec with Matchers {
  val _5: Towers = Towers(5)
  val _8: Towers = Towers(8)
  val twoByPowerOfThreeByTen: Towers = Towers(2, _8 -> BigInt(3))
  val sample: Towers = Towers(1, _5 -> 3, twoByPowerOfThreeByTen -> 5)

  "tower" should "eval" in {
      sample.eval(1) shouldEqual 9
      twoByPowerOfThreeByTen.eval(2) shouldEqual 770
      Towers(3, _5->2, _8->6).eval(2) shouldEqual 1603
      val bigOne = sample.eval(3)
      bigOne.bitLength shouldEqual 31203
      toDoubleString(bigOne, 5) shouldEqual """6.77437×10^{9392}"""
    }
  "tower" should "toString" in {
    Towers(0, ConstK -> 1).toString shouldEqual "k^k"
    twoByPowerOfThreeByTen.toString shouldEqual "3×k^8 + 2"
    sample.toString shouldEqual "3×k^5 + 5×k^{3×k^8 + 2} + 1"
    Const1.toString shouldEqual "1"
    ConstK.toString shouldEqual "k"
  }
  
  "tower" should "parse" in {
    def check(s: String, expected: String = null): Unit = {
      Towers(s).toString shouldEqual Option(expected).getOrElse(s)
    }

    check("k")
    check("5×k^9")
    check("5×k^k")
    check("5×k")
    check("k^k")
    check("k^9")
    check("256")
    check("k^{k}", "k^k")
    check("22 + k", "k + 22")
    check("3×k^{2×k^{k + 1}} + 4×k^{42} + 5×k^9 + 7×k + 49")
    check("123 + 321 + k + k + k^{k} + k^k + 5×k", "2×k^k + 7×k + 444")
  }
//
  //  val n263 = 3 * pow(263, 263) + 3 * pow(263, 3) + 2 * pow(263, 2) + 30 * 263  
  "tower" should "dec" in {
    Towers("k^k").dec(4) shouldEqual Towers("3×k^3 + 3×k^2 + 3×k + 3")
    Towers("k").dec(7) shouldEqual Towers("6")
    Towers("10×k").dec(10) shouldEqual(Towers("9×k + 9"))
    Towers("17").dec(10) shouldEqual(Towers("16"))
    Towers("5×k").dec(8) shouldEqual Towers("4×k + 7")
    Towers("3×k^2").dec(10) shouldEqual Towers("2×k^2 + 9×k + 9")
    Towers("3×k^2 + 8").dec(10) shouldEqual Towers("3×k^2 + 7")
    Towers("2×k^k").dec(3) shouldEqual Towers("k^k + 2×k^2 + 2×k + 2")
  }
}
