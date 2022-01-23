package scalakittens

import scala.language.postfixOps
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
    /**
     * terms with their coefficients
     */
    val terms: List[Term]
    
    lazy val reverseTerms: List[Term] = terms.reverse
    
    lazy val height: Int = terms.lastOption map (_.height) getOrElse 0
    lazy val length: Int = terms.length

    /**
     * Decrease the expression by 1
     * Assuming that the current base is is `base`
     * @param base the base at which we decrease it
     * @return another expression in HereditaryNotation
     */
    def dec(base: BigInt): HereditaryNotation = {
      if (constantTerm > 0) {
        HereditaryNotation(constantTerm - 1, terms)
      } else if (terms.isEmpty) {
        throw new Done(base)
      } else {
        val lowestTerm = terms.head
        val termWithLessCoefficient = lowestTerm.decreaseCoefficient
        val newTerms = termWithLessCoefficient::terms.tail

        lowestTerm.degree.dec(base) match {
          case HereditaryNotation.Zero => HereditaryNotation(base-1, newTerms)
          case smallerDegree           =>
            HereditaryNotation(0, new Term(smallerDegree, base) :: newTerms).dec(base)
        }
      }
    }

    /**
     * Add a number to the constant term
     * @param n the number to add
     * @return a new HereditaryNotation expression
     */
    def +(n: BigInt): HereditaryNotation = HereditaryNotation(constantTerm + n, terms)

    /**
     * Evaluate an expression in HereditaryNotation for a given base
     * @param base the base
     * @return a big number, the value of this expression
     */
    def eval(base: BigInt): BigInt = terms.foldLeft(constantTerm) {
      case (value, t) => value + t.q * pow(base, t.degree.eval(base))
    }

    override lazy val toString: String = {
      val termsString = reverseTerms map { _.toString } mkString " + "

      (termsString, constantTerm) match {
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
          val pairs = reverseTerms zip that.reverseTerms
          val firstDiff = pairs find(x => x._1 != x._2)
          firstDiff match {
            case None =>
              (this.length, this.constantTerm) compare (that.length, that.constantTerm)
            case Some(t) => t._1 compare t._2
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
    override val terms: List[Term] = Nil

    def this(n: Int) = this(BigInt(n))
  }

  class Term(val degree: HereditaryNotation, val q: BigInt) extends HereditaryNotation {

    override val constantTerm: BigInt = Big0
    override lazy val height: Int = 1 + degree.height
    override lazy val length: Int = 1
    override lazy val terms: List[Term] = this::Nil

    def withCoefficient(p: BigInt) = new Term(degree, p)

    def decreaseCoefficient: Term = withCoefficient(q - 1)
    
    override lazy val toString: String = {
      val exp = degree.toString match {
        case "1"                  => ""
        case ts if ts.length == 1 => s"^$ts"
        case ts                   => s"^{$ts}"
      }
      s"${if (q == Big1) "" else s"$q·"}k$exp"
    }
    
    def compare(that: Term): Int = {
      (degree, q) compare (that.degree, that.q)
    }

    override def compare(that: HereditaryNotation): Int = {
      height compare that.height match {
        case not0 if not0 != 0 => not0
        case 0 =>
          that.terms.lastOption match {
            case None => 1
            case Some(term) =>
              compare(term) match {
                case not0 if not0 != 0 => not0
                case 0 => (1, Big0) compare (that.length, that.constantTerm)
              }
          }
      }
    }
  }

  case class Power(override val degree: HereditaryNotation) extends Term(degree, Big1)

  class Done(base: BigInt) extends Exception(s"This is the end of Goodstein sequence, at base $base")
  
  object HereditaryNotation extends Parser {
    /**
     * @param constant the numeric value at the end of this collection of tower-represented numbers
     * @param terms components of the tower representation: it's just a pair, degree and coefficient
     * e.g. 2*n^^{3*n^^{n+1} + 5*n + 7} + 6*n^^{2} + 8 is represented as
     * TR(8, TR(6, TR(2)), TR(2, TR(3, TR(1,TR(1)))))
     */
    def apply(constant: BigInt, terms: List[Term]): HereditaryNotation = {
      val grouped: Map[HereditaryNotation, List[Term]] = terms.groupBy(_.degree)
      val contentsMap: Map[HereditaryNotation, BigInt] = grouped map { case (t, seq) => t -> seq.map(_.q).sum }
      val sortedContents = contentsMap.filter(_._2 > 0).toList.sorted

      val result = new HereditaryNotation {
        override val constantTerm: BigInt = constant
        override val terms: List[Term] = sortedContents map { case (deg, q) => new Term(deg, q)}
      }
      result
    }

    def apply(n: Int): HereditaryNotation = {
      apply(intToHnText(n))
    }
    
    def intToHnText(n: Int): String = {
      val constTerm = if (n % 2 == 0) "0" else "1"
      val terms = (1 until 32) collect { case i if (n & (1 << i)) != 0 => s"k^{${intToHnText(i)}}" } toList

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
        val const = terms map(_.constantTerm) sum
        val newTerms = terms flatMap (_.terms)
        HereditaryNotation(const, newTerms)
    }

    def coefficient: Parser[BigInt] = digits <~ "·"

    def power: Parser[HereditaryNotation] = (("k^"~>smallDegree) | "k^{"~>expr<~"}" | "k^k" | "k") ^^ {
      case digit: Int => Power(Const(digit))
      case expr: HereditaryNotation => Power(expr)
      case "k^k" => Power(HereditaryNotation.K)
      case "k" => HereditaryNotation.K
    }
    
    def term: Parser[HereditaryNotation] = (power | (coefficient ~ power) | constantTerm) ^^ {
      case (q: BigInt) ~ (t:Term) => t withCoefficient q
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

  def main(args: Array[String]): Unit = {
    def gs(n: Int) = {
      LazyList.iterate((2, HereditaryNotation(n))) {
        case (base, expr) => (base+1, expr.dec(base + 1))
      }
    }

    val Mil = 1000000
    val start = System.currentTimeMillis()
    
    for {
      (i, n) <- gs(4) takeWhile (_._1 <= 10000 * Mil)
    } {
      if (i % (100*Mil) == 0 || (n.constantTerm + 1) % i < 3) {
        val x = toFloatingString(n.eval(i), 6)
        println(f" $i%7d |   $x%20s  | $n\t\t\t\t${System.currentTimeMillis - start}")
      }
    }
  }

}
