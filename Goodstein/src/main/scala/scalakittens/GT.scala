package scalakittens

import Numbers._
import scalakittens.Goodstein.Parser

trait GT extends Ordered[GT] { expr =>
  val constantTerm: BigInt
  val terms: List[(GT, BigInt)]
  
  lazy val height: Int = if (terms.isEmpty) 0 else 1 + terms.head._1.height
  
  /**
   * Decrease the value by 1
   * Assuming that the current base is k
   * @param k the base at which we decrease it
   * @return another Towers representation
   */
  def dec(k: BigInt): GT = {
    if (constantTerm > 0) {
      GT(constantTerm-1, terms)
    } else {
      val (lowest, q) = terms.head
      val allButLast = terms.tail
      val newMember = lowest.dec(k)
      val newSeq = lowest->(q-1) :: allButLast // for q=1 we'll have a coefficient 0, which gets eliminated in constructor
      newMember match {
        case GT.Zero => GT(k-1, newSeq)
        case _       => GT(0, newMember -> k :: newSeq).dec(k)
      }
    }
  }
  
  def eval(base: BigInt): BigInt = terms.foldLeft(constantTerm) {
    case (s, (t, c)) => s + c * pow(base, t.eval(base))
  }
  
  override def toString: String = {
    val termStrings = terms map {
      case (t, c) =>
        val ts = t.toString
        val mult = if (c == Big1) "" else s"$c×"
        val order = if(ts == "1") "" else "^" + (if (ts.length == 1) ts else s"{$ts}")
        s"${mult}k$order"
    }
    
    val termsString = termStrings.fold ("") {
      case ("", term) => term
      case (acc, term) => s"$term + $acc"
    }
          
    (termsString, constantTerm) match {
      case ("", x) => x.toString
      case (s, Big0)  => s
      case (s, x) => s"$s + $x"
    }
  }

  override def compare(that: GT): Int = {
    (this.height, this.constantTerm).compare(that.height, that.constantTerm)
  }

  override def equals(obj: Any): Boolean = {
    obj match {
      case other: GT =>
        constantTerm == other.constantTerm && terms == other.terms
      case _ => false
    }
  }

  override def clone(): AnyRef = super.clone()

  override def hashCode(): Int = constantTerm.hashCode() * 53 + terms.hashCode()
}

case class Const(value: BigInt) extends GT {
  override val constantTerm: BigInt = value
  override val terms: List[(GT, BigInt)] = Nil
  
  def this(n: Int) = this(BigInt(n))
}

class Term(degree: GT, q: BigInt) extends GT {
  override val constantTerm: BigInt = Big0
  override val terms: List[(GT, BigInt)] = degree -> q :: Nil

  def *(p: BigInt) = new Term(degree, q*p)
}

case class Power(degree: GT) extends Term(degree, Big1)

object GT extends Parser { 
  /**
   * @param constant the numeric value at the end of this collection of tower-represented numbers
   * @param theContents components of the tower representation: another tower (degree) and the quotient for it
   * e.g. 2*n^{3*n^{n+1} + 5*n + 7} + 6*n^{2} + 8 is represented as
   * TR(8, TR(6, TR(2)), TR(2, TR(3, TR(1,TR(1)))))
   */
  def apply(constant: BigInt, theContents:  List[(GT, BigInt)]): GT = {
    val grouped: Map[GT, Seq[(GT, BigInt)]] = theContents.groupBy(_._1)
    val contentsMap: Map[GT, BigInt] = grouped map { case (term, seq) => term -> seq.map(_._2).foldLeft(Big0)(_ + _) }
    val correctContents = contentsMap.filter(_._2 > 0).toList.sorted
    
    new GT {
      override val constantTerm: BigInt = constant
      override val terms: List[(GT, BigInt)] = correctContents
    }
  }

  val Zero: Const = Const(Big0)
  val Const_1: Const = Const(Big1)
  val K: Power = Power(Const_1)
} 