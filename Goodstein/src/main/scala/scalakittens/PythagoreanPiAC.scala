package scalakittens

import java.math.MathContext
import scala.Console.err
import scala.annotation.tailrec

// similar problem: https://x.com/AlexFleischer1/status/1796815019222368275
// find a Pythagorean triangle ABC, where C/A approximates Pi
object PythagoreanPiAC extends App {
  val PiString = "3.14159265358979323846264338327950"
  val Pi: BigDecimal = BigDecimal(PiString)
  val Q = sqrt((Pi - 1)/ (Pi + 1))
  val requiredPrecision = 20 /*digits*/
  require(precision(Q*Q*(Pi+1) + 1) > requiredPrecision)

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
    val b = (BigDecimal(a) * Q).toBigInt
    val hypotenuse = a*a + b*b
    val leg = a*a - b*b
    val (candidate: BigDecimal, p: Int) = if (leg <= 0) {
      (0, -2)
    } else {
      val candidate = BigDecimal(hypotenuse) / BigDecimal(leg)
      val p = precision(candidate)
      (candidate, p)
    }

    if (a == 0) {
      err.println("Wrong initial value")
    } else if (p >= requiredPrecision) {
      println(s"$hypotenuse/$leg ≅ $candidate with $p digits")
    } else {
      bestLeg((BigDecimal(a) / Q).toBigInt)
    }
  }

  bestLeg(BigInt(4))
}
