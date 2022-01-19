package scalakittens

object Numbers {
  val Big0 = BigInt(0)
  val Big1 = BigInt(1)

  def pow(n: BigInt, m: BigInt): BigInt = m match {
    case Big0 => Big1
    case Big1 => n
    case _ =>
      val r = m % 2
      val k = m / 2
      val q = pow(n, k)
      if (r == Big0) q * q else n * q * q
  }

  def degree(n: BigInt): Int = {
    import scala.annotation.tailrec
    @tailrec def deg(n: BigInt, acc: Int): Int = {
      if (n < 10) acc
      else deg(n / 10, 1 + acc)
    }

    deg(n.abs, 0)
  }

  def toDoubleString(n: BigInt, nDigits: Int): String = {
    val nab = n.abs
    val d = degree(nab)
    val m = n.toString.take(nDigits + 2).toDouble / math.pow(10, nDigits + 1)
    val ms = m.toString.take(nDigits + 2)
    val sign = if (n < 0) "-" else ""
    s"$sign$ms×10^{$d}"
  }

}
