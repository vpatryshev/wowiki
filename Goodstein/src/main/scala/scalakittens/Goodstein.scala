package scalakittens

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

    lazy val height: Int = if (terms.isEmpty) 0 else 1 + terms.head._1.height

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

    def eval(base: BigInt): BigInt = terms.foldLeft(constantTerm) {
      case (s, (t, c)) => s + c * pow(base, t.eval(base))
    }

    override def toString: String = {
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

      val termsString = reducedStrings.fold ("") {
        case ("", term) => term
        case (acc, term) => s"$term + $acc"
      }

      (termsString, constantTerm) match {
        case ("", x) => x.toString
        case (s, Big0)  => s
        case (s, x) => s"$s + $x"
      }
    }
    
    def toLatex = toString.replace("·", """\(\times\)""")

    override def compare(that: HereditaryNotation): Int = {
      (this.height, this.constantTerm).compare(that.height, that.constantTerm)
    }

    override def equals(obj: Any): Boolean = {
      obj match {
        case other: HereditaryNotation =>
          constantTerm == other.constantTerm && terms == other.terms
        case _ => false
      }
    }

    override lazy val hashCode: Int = constantTerm.hashCode() * 53 + terms.hashCode()
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
      val grouped: Map[HereditaryNotation, Seq[(HereditaryNotation, BigInt)]] = theContents.groupBy(_._1)
      val contentsMap: Map[HereditaryNotation, BigInt] = grouped map { case (term, seq) => term -> seq.map(_._2).foldLeft(Big0)(_ + _) }
      val correctContents = contentsMap.filter(_._2 > 0).toList.sorted

      new HereditaryNotation {
        override val constantTerm: BigInt = constant
        override val terms: List[(HereditaryNotation, BigInt)] = correctContents
      }
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
        val const = terms.map(_.constantTerm).fold(Big0)(_+_)
        HereditaryNotation(const, terms flatMap (_.terms))
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
    
    def apply(ex: String): HereditaryNotation = parseAll(expr, ex).get
  }

  // e.g. 3\(\times\)(132^{132}) + 3\(\times\)(132^3) + 2\(\times\)(132^2) + 3\(\times\)132 + 131
  def fromLatex(latex: String): HereditaryNotation = {
    HereditaryNotation(latex.replace("""\(\times\)""", "·"))
  }
}
