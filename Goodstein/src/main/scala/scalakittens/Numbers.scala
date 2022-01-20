package scalakittens

/**
 * Operations on BigInts that are missing in the library
 */
object Numbers {
  /**
   * 0 as bigint
   */
  val Big0 = BigInt(0)

  /**
   * 1 as bigint
   */
  val Big1 = BigInt(1)

  /**
   * power function, for a big number n and a big number am, calculates n^^m
   * @param n base
   * @param m exponent
   * @return n^^m
   */
  def pow(n: BigInt, m: BigInt): BigInt = m match {
    case Big0 => Big1
    case _ => // a better solution would be to have m as a sequence of bits, rtl. Or just use bytes.
      val q = pow(n, m / 2)
      if (m % 2 == Big0) q * q else n * q * q
  }

  /**
   * Calculate the decimal exponent of a big number
   * @param n the big number, must be nonnegative
   * @return its exponent
   */
  def exponent(n: BigInt): Int = {
    import scala.annotation.tailrec
    @tailrec def exp(n: BigInt, acc: Int): Int = {
      if (n < 10) acc
      else exp(n / 10, 1 + acc)
    }

    exp(n, 0)
  }

  /**
   * Represent a big number as a string with mantissa and exponent
   * @param n big number
   * @param nDigits precision in the floating point number representation
   * @return a string with floating point representation
   */
  def toFloatingString(n: BigInt, nDigits: Int): String = {
    val d = exponent(n.abs)
    val m = n.toString.take(nDigits + 2).toDouble / math.pow(10, nDigits + 1)
    val ms = m.toString.take(nDigits + 2)
    val sign = if (n < 0) "-" else ""
    s"$sign$ms·10^{$d}"
  }
}
