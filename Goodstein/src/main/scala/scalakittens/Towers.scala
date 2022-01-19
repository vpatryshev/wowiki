package scalakittens

import Numbers._
import scalakittens.Goodstein.T

/**
 * @param tail the numeric value at the end of this collection of tower-represented numbers
 * @param contents components of the tower representation: another tower (degree) and the quotient for it
 * e.g. 2*n^{3*n^{n+1} + 5*n + 7} + 6*n^{2} + 8 is represented as
 * TR(8, TR(6, TR(2)), TR(2, TR(3, TR(1,TR(1)))))
 */
case class Towers(tail: BigInt, contents:  (Towers, BigInt)*) extends Ordered[Towers] { expr =>
  lazy val height: Int = if (contents.isEmpty) 0 else 1 + contents.head._1.height
  
  /**
   * Decrease the value by 1
   * Assuming that the current base is k
   * @k the base at which we decrease it
   * @return another Towers representation
   */
  def dec(k: BigInt): Towers = {
    println(s"\n** doing dec($k) on $expr")
    if (tail > 0) {
      Towers(tail-1, contents: _*)
    }
    else {
      println(s"dec ($this, $k) with contents $contents")
      val (lowest, q) = contents.last
      println(s"will dec $q * k^$lowest")
      val allButLast = contents.dropRight(1).toList
      val newMember = lowest.dec(k)
      println(s"new member = $newMember")
      if (newMember.isZero) {
        val newMembers = if (q == Big1) allButLast else (lowest -> (q - 1))::allButLast
        new Towers(k-1, newMembers.sortBy(_._1).reverse:_*)
      } else {
        val decomposed = newMember -> k
        val newMembers = decomposed ::
          (if (q == 1) allButLast else (lowest -> (q-1))::allButLast)
        println(s"q=$q, lowest=$lowest => $decomposed; getting $newMembers")

        val toDecrease = new Towers(0, newMembers.sortBy(_._1).reverse: _*)
        toDecrease.dec(k)
      }
    }
  }
  
  lazy val isZero: Boolean = tail == 0 && contents.isEmpty
  
  def eval(base: BigInt): BigInt = contents.foldLeft(tail) {
    case (s, (t, c)) => s + c * pow(base, t.eval(base))
  }
  
  override def toString: String = {
    val members = contents map {
      case (t, c) =>
        val ts = t.toString
        val mult = if (c == Big1) "" else s"${c}×"
        val order = if(ts == "1") "" else "^" + (if (ts.length == 1) ts else s"{$ts}")
        s"${mult}k${order}"
    } mkString " + "
    (members, tail) match {
      case ("", x) => x.toString
      case (s, Big0)  => s
      case (s, x) => s"$s + $x"
    }
  }

  override def compare(that: Towers): Int = {
    (this.height, this.tail).compare((that.height), that.tail)
  }

  override def equals(obj: Any): Boolean = {
    obj match {
      case other: Towers =>
        tail == other.tail && contents == other.contents
      case wrong => false
    }
  }

  override def clone(): AnyRef = super.clone()
}

object Towers extends T