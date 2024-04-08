package scalakittens

import scala.collection.mutable
import scala.math.log

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
      case Big1 => n
      case _ => // a better solution would be to have m as a sequence of bits, rtl. Or just use bytes.
        val q = n ^^ (m >> 1)
        if ((m & 1) == Big0) q * q else n * q * q
    }

    private val degreesOf10: mutable.Map[BigInt, BigInt] = new mutable.HashMap[BigInt, BigInt]()
    
    def degreeOf10(m: BigInt): BigInt = degreesOf10.getOrElseUpdate(m, Big10 ^^ m)
    
    /**
     * Calculate the decimal exponent of a big positive number
     * @return its exponent
     */
    lazy val exponent: Int = {
      import scala.annotation.tailrec
      @tailrec def exp(n: BigInt, acc: Int): Int = {
        if (n <    10) acc     else
        if (n <   100) acc + 1 else
        if (n <  1000) acc + 2 else
        if (n < 10000) acc + 3 else
        exp(n / 10000, acc + 4)
      }

      val estimate = math.max((n.bitLength * 0.30103).toInt - 1, 0)
      val result = if (estimate < 33) exp(n, 0) else {
        val roughEstimate = 1 << ((log(estimate)/log(2)).toInt - 1)
        val big10 = degreeOf10(roughEstimate) // A bigger power may cause an OOM
        val n1 = n / big10 / big10
        roughEstimate * 2 + n1.exponent
      }
      result
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
        s"$sign${ms}·10^$exp"
      }
    }
  }
}
