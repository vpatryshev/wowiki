package scalakittens

import scala.language.postfixOps
import scala.util.Sorting
import scala.util.parsing.combinator._

/**
 * @see https://mathworld.wolfram.com/GoodsteinSequence.html
 * @see 
 */
object Goodstein {
  import Numbers._

  /**
   * @see https://mathworld.wolfram.com/HereditaryRepresentation.html
   */
  trait HereditaryNotation extends Ordered[HereditaryNotation] { expr =>
    val constantTerm: BigInt
    val terms: List[(HereditaryNotation, BigInt)]

    lazy val height: Int = terms.lastOption map (1 + _._1.height) getOrElse 0
    lazy val length: Int = terms.length

    /**
     * Decrease the value by 1
     * Assuming that the current base is is `base`
     * @param base the base at which we decrease it
     * @return another number in hereditary representation
     */
    def dec(base: BigInt): HereditaryNotation = {
      if (constantTerm > 0) {
        HereditaryNotation(constantTerm-1, terms)
      } else {
        val (lowest, q) = terms.head
        val allButLast = terms.tail
        val newMember = lowest.dec(base)
        val newSeq = lowest->(q-1) :: allButLast // for q=1 we'll have a coefficient 0, which gets eliminated in constructor
        newMember match {
          case HereditaryNotation.Zero => HereditaryNotation(base-1, newSeq)
          case _       => HereditaryNotation(0, newMember -> base :: newSeq).dec(base)
        }
      }
    }
    
    def +(n: BigInt): HereditaryNotation = new HereditaryNotation {
      override val constantTerm: BigInt = expr.constantTerm + n
      override val terms: List[(HereditaryNotation, BigInt)] = expr.terms
    }

    def eval(base: BigInt): BigInt = terms.foldLeft(constantTerm) {
      case (s, (t, c)) => s + c * pow(base, t.eval(base))
    }

    override lazy val toString: String = {
      val termStrings = terms map {
        case (t,c) => (t.toString, c.toString)
      }
      
      val reducedStrings = termStrings map {
          case ("1", "1") => "k"
          case ("1", c)    => s"$c·k"
          case (ts, "1")   =>
            val exponent = if (ts.length == 1) ts else s"{$ts}"
            s"k^$exponent"
          case (ts, c) =>
            val exponent = if (ts.length == 1) ts else s"{$ts}"
            s"$c·k^$exponent"
        }

      val stringOfTerms = reducedStrings.fold ("") {
        case ("", term) => term
        case (acc, term) => s"$term + $acc"
      }

      (stringOfTerms, constantTerm) match {
        case ("", x) => x.toString
        case (s, Big0)  => s
        case (s, x) => s"$s + $x"
      }
    }
    
    def toLatex: String = toString.replace("·", """\(\times\)""")

    override def compare(that: HereditaryNotation): Int = {
      this.height compare that.height match {
        case other if other != 0 => other
        case 0 =>
          val pairs = this.terms.reverse zip that.terms.reverse
          val firstDiff = pairs find(x => x._1 != x._2)
          firstDiff match {
            case None =>
              val c = (this.terms.length, this.constantTerm) compare (that.terms.length, that.constantTerm)
              c
            case Some(t) =>
             val c = t._1 compare t._2
              val c1 = t._1._1 compare t._2._1
              val c2 = t._1._2 compare t._2._2
              c
          }
      }
    }

    override def equals(obj: Any): Boolean = {
      obj match {
        case other: HereditaryNotation => toString == other.toString
        case _ => false
      }
    }

    override lazy val hashCode: Int = toString.hashCode()
  }

  case class Const(value: BigInt) extends HereditaryNotation {
    override val constantTerm: BigInt = value
    override val terms: List[(HereditaryNotation, BigInt)] = Nil

    def this(n: Int) = this(BigInt(n))
  }

  class Term(degree: HereditaryNotation, q: BigInt) extends HereditaryNotation {
    override val constantTerm: BigInt = Big0
    override val terms: List[(HereditaryNotation, BigInt)] = degree -> q :: Nil

    def *(p: BigInt) = new Term(degree, q*p)
  }

  case class Power(degree: HereditaryNotation) extends Term(degree, Big1)

  object HereditaryNotation extends Parser {
    /**
     * @param constant the numeric value at the end of this collection of tower-represented numbers
     * @param theContents components of the tower representation: another tower (degree) and the quotient for it
     * e.g. 2*n^{3*n^{n+1} + 5*n + 7} + 6*n^^{2} + 8 is represented as
     * TR(8, TR(6, TR(2)), TR(2, TR(3, TR(1,TR(1)))))
     */
    def apply(constant: BigInt, theContents:  List[(HereditaryNotation, BigInt)]): HereditaryNotation = {
      val grouped = theContents.groupBy(_._1)
      val contentsMap =  grouped map { case (term, seq) => term -> seq.map(_._2).sum }
      
      val sortedContents = contentsMap.filter(_._2 > 0).toList.sorted

      val result = new HereditaryNotation {
        override val constantTerm: BigInt = constant
        override val terms: List[(HereditaryNotation, BigInt)] = sortedContents
      }
      result
    }

    def apply(n: Int): HereditaryNotation = {
      apply(intToHNtext(n))
    }
    
    def intToHNtext(n: Int): String = {
      val constTerm = if (n % 2 == 0) "0" else "1"
      val terms = (1 until 32) collect { case i if (n & (1 << i)) != 0 => s"k^{${intToHNtext(i)}}" } toList

      (constTerm :: terms) mkString "+"
    }
    
    val Zero: Const = Const(Big0)
    val One: Const = Const(Big1)
    val K: Power = Power(One)
  }
  
  class Parser extends JavaTokenParsers {
    def digits: Parser[BigInt] = """\d+""".r ^^ { n => BigInt(n) }

    /**
     * A single digit can denote an exponent; it's not wrapped in curlies, e.g. k^^7
     * @return a regex parser
     */
    def smallDegree: Parser[Int] = """\d""".r ^^ {(n: String) => n.toInt }

    /**
     * A BigInt number that serves as a constant term
     * @return
     */
    def constantTerm: Parser[HereditaryNotation] = digits ^^ { Const }

    def expr: Parser[HereditaryNotation] = repsep(term, "+") ^^ {
      terms => 
        HereditaryNotation(terms map(_.constantTerm) sum, terms flatMap (_.terms))
    }

    def coefficient: Parser[BigInt] = (digits ~ "·") ^^ { 
      case n ~ _ => n
    }

    def power: Parser[HereditaryNotation] = ("k^{"~expr~"}" | "k^k" | ("k^"~smallDegree) | "k") ^^ {
      case "k^k" => Power(HereditaryNotation.K)
      case "k^{"~(expr:HereditaryNotation)~"}" => Power(expr)
      case "k^" ~ (digit: Int) => Power(Const(digit))
      case "k" => HereditaryNotation.K
    }
    
    def term: Parser[HereditaryNotation] = (power | (coefficient ~ power) | constantTerm) ^^ {
      case (q: BigInt) ~ (t:Term) => t * q
      case c: Const => c
      case t: Term => t
    }
    
    def apply(ex: String): HereditaryNotation = parseAll(expr, ex) match {
      case Success(result, _) => result
      case NoSuccess(msg: String, _) => throw new IllegalArgumentException(s"$msg on $ex")
    }
  }
  
  // e.g. 3\(\times\)(132^{132}) + 3\(\times\)(132^3) + 2\(\times\)(132^2) + 3\(\times\)132 + 131
  def fromLatex(latex: String): HereditaryNotation = {
    HereditaryNotation(latex.replace("""\(\times\)""", "·"))
  }
}
