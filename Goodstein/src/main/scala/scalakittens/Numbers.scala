package scalakittens

/**
 * Operations on BigInts that are missing in the library
 */
object Numbers {

  val Big0 = BigInt(0)

  val Big1 = BigInt(1)
  
  val Big10 = BigInt(10)

  implicit class BiggerInt(val n: BigInt) {

    /**
     * power function, for a big number n and a big number am, calculates n^^m
     * @param m exponent
     * @return n^^m
     */
    def ^^(m: BigInt): BigInt =  m match {
      case Big0 => Big1
      case _ => // a better solution would be to have m as a sequence of bits, rtl. Or just use bytes.
        val q = n ^^ (m / 2)
        if (m % 2 == Big0) q * q else n * q * q
    }

    /**
     * Calculate the decimal exponent of a big positive number
     * @return its exponent
     */
    lazy val exponent: Int = {
      import scala.annotation.tailrec
      @tailrec def exp(n: BigInt, acc: Int): Int = {
        if (n < 10) acc
        else exp(n / 10, 1 + acc)
      }

      val estimate = math.max(n.bitLength * 3 / 10 - 1, 0)
      try {
        val big10 = Big10 ^^ (estimate / 2)
        val n1 = n / big10
        val n2 = n1 / big10
        estimate + exp(n2, 0)
//      } catch {
//        case oom: OutOfMemoryError =>
//          throw new IllegalArgumentException(s"Degree too big: $estimate", oom)
      }
    }

    /**
     * Represent a big number as a string with mantissa and exponent
     * @param nDigits precision in the floating point number representation
     * @return a string with floating point representation
     */
    def toFloatingString(nDigits: Int): String = {
      val d = n.abs.exponent
      if (d < nDigits + 8) n.toString else {
        val m = n.toString.take(nDigits + 2).toDouble / math.pow(10, nDigits + 1)
        val ms = m.toString.take(nDigits + 2).takeWhile('E' != _)
        val sign = if (n < 0) "-" else ""
        val exp = if (d<10) s"$d" else s"{$d}"
        s"$sign$ms·10^$exp"
      }
    }
  }
}
