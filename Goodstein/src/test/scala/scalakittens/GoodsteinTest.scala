package scalakittens
import org.scalatest.flatspec.AnyFlatSpec
import Numbers._
import org.scalatest.matchers.should.Matchers
import scalakittens.Goodstein.HereditaryNotation
import Goodstein._

import java.util.Date

class GoodsteinTest extends AnyFlatSpec with Matchers {
  lazy val _5: HereditaryNotation = Const(5)
  lazy val _8: HereditaryNotation = Const(8)
  lazy val twoByPowerOfThreeByTen: HereditaryNotation = HereditaryNotation(2, _8 -> BigInt(3) :: Nil)
  lazy val sample: HereditaryNotation = HereditaryNotation(1, List(_5 -> 3, twoByPowerOfThreeByTen -> 5))

  "we" should "eval" in {
      sample.eval(1) shouldEqual 9
      twoByPowerOfThreeByTen.eval(2) shouldEqual 770
      HereditaryNotation(3, List(_5->2, _8->6)).eval(2) shouldEqual 1603
      val bigOne = sample.eval(3)
      bigOne.bitLength shouldEqual 31203
      toFloatingString(bigOne, 5) shouldEqual """6.77437·10^{9392}"""
    }

  "we" should "toString" in {
    HereditaryNotation(0, List(HereditaryNotation.K -> 1)).toString shouldEqual "k^k"
    twoByPowerOfThreeByTen.toString shouldEqual "3·k^8 + 2"
    sample.toString shouldEqual "5·k^{3·k^8 + 2} + 3·k^5 + 1"
    HereditaryNotation.One.toString shouldEqual "1"
    HereditaryNotation.K.toString shouldEqual "k"
  }

  "we" should "toLatex" in {
    sample.toLatex shouldEqual """5\(\times\)k^{3\(\times\)k^8 + 2} + 3\(\times\)k^5 + 1"""
  }
  
  "we" should "height" in {
    def check(a: String, expected: Int): Unit = {
      val x = HereditaryNotation(a)
      val actual = x.height
      if (actual != expected) fail(s"expected hight $expected of $x, got $actual")
    }
    
    check("42", 0)
    check("k", 1)
    check("k + 5", 1)
    check("123·k", 1)
    check("123·k + 8", 1)
    check("123·k^{47} + 8", 1)
    check("123·k^{k+1} + 8", 2)
  }
  
  "we" should "compare" in {
    def check(a: String, b: String): Unit = {
      val rels="<=>"
      val x = HereditaryNotation(a)
      val y = HereditaryNotation(b)
      val cmp = x compare y
      val c = rels.charAt(1 + cmp)
      if (cmp != -1) fail(s"$x $c $y")
    }

    check("k^k + 1", "k^k + k")
    check("k^{k^k + 1}", "k^{k^k + k}")
    check("k", "2·k")
    check("k + 1", "2·k")
    check("2·k", "2·k + 1")
    check("k", "k^k")
    check("k^2", "k^k")
    check("k^k", "k^{k+1}")
    check("k^{k+1}", "k^{k+2}")
    check("k^{k+1}", "k^{2·k+1}")
  }
  
  "we" should "parse" in {
    def check(s: String, expected: String = null): Unit = {
      val sut = HereditaryNotation(s)
      val suts = sut.toString
      suts shouldEqual Option(expected).getOrElse(s)
    }
    check("k^{k^k + k} + k^{k^k + 1} + k + 2")
    check("k^{k^k + k} + k^{k^k + 1} + k^k")
    check("256")
    check("k^k")
    check("k^{k}", "k^k")
    check("22+k", "k + 22")
    check("123 + 321 + k + k + k^{k} + k^k + 5·k", "2·k^k + 7·k + 444")
    check("10·k^{1+2·k^{13·k}} + k^{k}", "10·k^{2·k^{13·k} + 1} + k^k")

    check("3·k^3 + 3·k^2 + 3·k + 3")
    check("k^{k} + k^k", "2·k^k")
    check("k")
    check("5·k^9")
    check("5·k^k")
    check("5·k")
    check("k^k")
    check("k^k + k^k", "2·k^k")
    check("k^{0 + k} + k^{k + 0}", "2·k^k")
    check("k^9")
    check("256")
    check("k^{k}", "k^k")
    check("22 + k", "k + 22")
    check("3·k^{2·k^{k + 1}} + 4·k^{42} + 5·k^9 + 7·k + 49")
    check("123 + 321 + k + k + k^{k} + k^k + 5·k", "2·k^k + 7·k + 444")
  }
//
  //  val n263 = 3 * pow(263, 263) + 3 * pow(263, 3) + 2 * pow(263, 2) + 30 * 263  
  "we" should "dec" in {
    val source1 = HereditaryNotation("k^k")
    val expected1 = HereditaryNotation("3·k^3 + 3·k^2 + 3·k + 3")
    val sample1 = source1.dec(4)
    sample1 shouldEqual expected1
    HereditaryNotation("k").dec(7) shouldEqual HereditaryNotation("6")
    HereditaryNotation("10·k").dec(10) shouldEqual HereditaryNotation("9·k + 9")
    HereditaryNotation("17").dec(10) shouldEqual HereditaryNotation("16")
    HereditaryNotation("5·k").dec(8) shouldEqual HereditaryNotation("4·k + 7")
    HereditaryNotation("3·k^2").dec(10) shouldEqual HereditaryNotation("2·k^2 + 9·k + 9")
    HereditaryNotation("3·k^2 + 8").dec(10) shouldEqual HereditaryNotation("3·k^2 + 7")
    HereditaryNotation("2·k^k").dec(3) shouldEqual HereditaryNotation("k^k + 2·k^2 + 2·k + 2")
  }
  
  "we" should "accept numbers" in {
    def check(n: Int, expected: String): Unit = {
      HereditaryNotation(n).toString shouldEqual expected
    }
    check(1, "1")
    check(2, "k")
    check(3, "k + 1")
    check(4, "k^k")
    check(5, "k^k + 1")
    check(6, "k^k + k")
    check(7, "k^k + k + 1")
    check(8, "k^{k + 1}")
    check(9, "k^{k + 1} + 1")
    check(10, "k^{k + 1} + k")
    
    for (i <- 1 until 100) {
      HereditaryNotation(i).eval(2) shouldEqual i
    }
  }
  
  "we" should "float" in {
    val sut = pow(10, 100)
    toFloatingString(sut, 4) shouldEqual("1.0·10^{100}")
  }
  
  "we" should "do some stuff" in {
    def gs(n: Int) = {
      LazyList.iterate((2, HereditaryNotation(n))){ case (base, expr) => 
        (base+1, expr.dec(base + 1))
      }
    } 
    
    def msfmt(ms: Long) =
      if (ms < 1500) s"${ms}ms" else
      if (ms < 90000) s"${ms/1000}sec" else
      if (ms < 6000000) s"${ms/60000}min" else 
        s"${ms/3600000}h"

    println(s"started ${new Date()}")
    println(" Base |\t          Value           |           Hereditary Notation     ")
    println(" ---- |\t------------------------- | ----------------------------------")
    var t = System.currentTimeMillis()
    for {
      (i, n) <- gs(100) take 100
    } {
      val t1 = System.currentTimeMillis()
      val dt1 = t1 - t
      val x = toFloatingString(n.eval(i), 6)
      val t2 = System.currentTimeMillis()
      val dt2 = t2 - t1
      val line = f"   $i%2d |   $x%22s  | $n\t\t\t\t(${msfmt(dt1)} and ${msfmt(dt2)})"
      t = t2
//      val html = line.
//        replace("}", "</sup>").
//        replace("^", "<sup>").
//        replace("{", "")

      println(line)
    }
  }
}
