package scalakittens

import java.math.MathContext
import scala.Console.err
import scala.annotation.tailrec

// problem: https://x.com/AlexFleischer1/status/1796815019222368275

object PythagoreanPiAB extends App {
  val PiString = "3.14159265358979323846264338327950"
  val Pi: BigDecimal = BigDecimal(PiString)
  /*
  For A and B, the formulas for legs are A^2-A^B and 2*A*B.
  A good solution is
  A^2-B^2=2*Pi*A*B
  That is, (A-Pi*B) = sqrt(Pi^2+1)*B
  B = A/Q1 where Q1=Pi + sqrt(Pi^2+1) (approx. 2*Pi)
  */

  val Q = Pi + sqrt(Pi*Pi + 1)
  val requiredPrecision = 20 /*digits*/

  /**
   * Number of digits of x that coincide with digits of Pi
   * @param x the value to check
   * @return number of digits
   */
  def precision(x: BigDecimal): Int = {
    val s = x.toString()
    val size = math.min(PiString.length, s.length)
    ((2 to size) find (i => PiString(i) != s(i)) getOrElse 0) - 2
  }

  // credits: https://gist.github.com/flightonary/404a94791594d7f568f1
  def sqrt(a: BigDecimal): BigDecimal = {
    val x = BigDecimal( Math.sqrt(a.doubleValue), MathContext.DECIMAL128)
    x - (x * x - a) / (2 * x)
  }

  @tailrec def bestLeg(a: BigInt): Unit = {
    val q = Q
    val b = (BigDecimal(a) / q).toBigInt
    val leg1 = a*a - b*b
    val leg2 = 2*a*b

    val candidate = BigDecimal(leg1) / BigDecimal(leg2)
    val p = precision(candidate)

    if (a == 0) {
      err.println("Wrong initial value")
    } else if (p >= requiredPrecision) {
      println(s"$leg1/$leg2 ≅ $candidate with $p digits")
    } else {
      bestLeg((BigDecimal(a) * 2.71828).toBigInt)
    }
  }

  bestLeg(BigInt(10))
}
// leg1 = 13678 21762 96624 86073 88130 67156 30939 239213  // using sqrt(e), starting at 10
// leg2 = 4353911896894984964242566043405248453716
// leg1/leg2 ≅ 3.1415926535897932384673907612212019476959
//               12345678901234567890
//        Pi ≅ 3.14159265358979323846264338327950