package scalakittens

import java.util.Date
import scala.language.postfixOps
import scala.util.parsing.combinator._

/**
 * @see https://mathworld.wolfram.com/GoodsteinSequence.html
 * Also see wikipedia
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

        lowestTerm.d.dec(base) match {
          case HereditaryNotation.Zero => HereditaryNotation(base-1, newTerms)
          case smallerDegree           =>
            HereditaryNotation(0, Term(base, smallerDegree) :: newTerms).dec(base)
        }
      }
    }
    
    def withConstantTerm(n: BigInt): HereditaryNotation = HereditaryNotation(n, terms)

    /**
     * Add a number to the constant term
     * @param n the number to add
     * @return a new HereditaryNotation expression
     */
    def +(n: BigInt): HereditaryNotation = withConstantTerm(constantTerm + n)
    
    /**
     * Evaluate an expression in HereditaryNotation for a given base
     * @param base the base
     * @return a big number, the value of this expression
     */
    def eval(base: BigInt): BigInt = terms.foldLeft(constantTerm) {
      case (value, t) => value + t.c * (base ^^ t.d.eval(base))
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

  case class Term(c: BigInt, d: HereditaryNotation) extends HereditaryNotation {

    override val constantTerm: BigInt = Big0
    override lazy val height: Int = 1 + d.height
    override lazy val length: Int = 1
    override lazy val terms: List[Term] = this::Nil

    def withCoefficient(a: BigInt): Term = Term(a, d)

    def decreaseCoefficient: Term = withCoefficient(c - 1)
    
    override lazy val toString: String = {
      val exp = d.toString match {
        case "1"                  => ""
        case ts if ts.length == 1 => s"^$ts"
        case ts                   => s"^{$ts}"
      }
      s"${if (c == Big1) "" else s"$c·"}k$exp"
    }
    
    def compare(that: Term): Int = {
      (d, c) compare (that.d, that.c)
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

  def Power(d: HereditaryNotation): Term = Term(Big1, d)

  class Done(base: BigInt) extends Exception(s"This is the end of Goodstein sequence, at base $base")
  
  object HereditaryNotation extends Parser {
    /**
     * @param constant the constant term
     * @param terms components of the tower representation: each one is just a pair, degree and coefficient
     * e.g. 2*n^^{3*n^^{n+1} + 5*n + 7} + 6*n^^{2} + 8 is represented as
     * HN(8, HN(6, TR(2)), HN(2, HN(3, HN(1,HN(1)))))
     */
    def apply(constant: BigInt, terms: List[Term]): HereditaryNotation = {
      val sortedTerms = terms filter (_.c > 0) sorted
      
      val result = new HereditaryNotation {
        override val constantTerm: BigInt = constant
        override val terms: List[Term] = sortedTerms
      }
      result
    }

    def apply(n: Int): HereditaryNotation = {
      def intToHnText(n: Int): String = {
        val constTerm = if (n % 2 == 0) "0" else "1"
        val terms = (1 until 32) collect { case i if (n & (1 << i)) != 0 => s"k^{${intToHnText(i)}}" } toList

        (constTerm :: terms) mkString "+"
      }

      apply(intToHnText(n))
    }
    
    val Zero: Const = Const(Big0)
    val One: Const = Const(Big1)
    val K: Term = Power(One)
  }

  /**
   * Parser for hereditary expressions.
   * See examples in the test.
   */
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
    private def constantTerm: Parser[HereditaryNotation] = digits ^^ { Const }

    private def expr: Parser[HereditaryNotation] = repsep(term, "+") ^^ {
      terms => 
        val const = terms map(_.constantTerm) sum
        val newTerms = terms flatMap (_.terms)
        HereditaryNotation(const, newTerms)
    }

    private def coefficient: Parser[BigInt] = digits <~ "·"

    private def power: Parser[HereditaryNotation] =
      (("k^"~>smallDegree) | "k^{"~>expr<~"}" | "k^k" | "k") ^^ {
        case digit: Int => Power(Const(digit))
        case expr: HereditaryNotation => Power(expr)
        case "k^k" => Power(HereditaryNotation.K)
        case "k" => HereditaryNotation.K
    }
    
    private def term: Parser[HereditaryNotation] = (power | (coefficient ~ power) | constantTerm) ^^ {
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

  def sequential(n: Int): LazyList[(Int, BigInt, HereditaryNotation)] = {
    LazyList.iterate((1, BigInt(2), HereditaryNotation(n))) {
      case (i, base, expr) => (i+1, base+1, expr.dec(base + 1))
    }
  }

  def accelerated(n: Int): LazyList[(Int, BigInt, HereditaryNotation)] = {
    LazyList.iterate((1, BigInt(2), HereditaryNotation(n))) {
      case (i, base, expr) =>
        if (expr.constantTerm <= 2 || base - expr.constantTerm > 2) {
          val newBase = base + 1
          (i+1, newBase, expr.dec(newBase))
        } else {
          val jump: BigInt = expr.constantTerm - 1
          require(jump > 0)
          val newBase: BigInt = base + jump
          (i+1, newBase, expr.withConstantTerm(2).dec(newBase))
        }
    }
  }
  
  def main(args: Array[String]): Unit = {

    val Mil = 1000000L
    
    val stream = sequential(4)
    println(s"started ${new Date()}")
    println("   Step   |   Base   |          Value          |         Hereditary Notation     ")
    println(" -------- | -------- | ----------------------- | --------------------------------")
    
    for {
      (i, base, n) <- stream takeWhile (_._1 <= Mil)
    } {
      val worthPrinting = ((n.constantTerm + 1) % base < 3) && (i < 100 || (i > 549600 && i % 300 < 40))
      if (worthPrinting) {
        val b = base.toFloatingString(4)
        val x = n.eval(base).toFloatingString(6)
        println(f" $i%8d | $b%8s |   $x%20s  | $n")
      }
    }
  }

}
