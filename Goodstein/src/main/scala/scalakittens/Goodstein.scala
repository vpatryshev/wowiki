package scalakittens

import Numbers._
import scala.util.parsing.combinator._

object Goodstein {
  
  class Parser extends JavaTokenParsers {
    def digits: Parser[BigInt] = """\d+""".r ^^ { n => BigInt(n) }

    /**
     * A single digit can denote a degree; it's not wrapped in curlies, e.g. k^7
     * @return a regex parser
     */
    def smallDegree: Parser[Int] = """\d""".r ^^ {(n: String) => n.toInt }

    /**
     * A BigInt number that serves as a constant term
     * @return
     */
    def constantTerm: Parser[GT] = digits ^^ { Const }

    def expr: Parser[GT] = repsep(term, "+") ^^ {
      terms => 
        val const = terms.map(_.constantTerm).fold(Big0)(_+_)
        GT(const, terms flatMap (_.terms))
    }

    def coefficient: Parser[BigInt] = (digits ~ "×") ^^ { 
      case n ~ _ => n
    }

    def power: Parser[GT] = ("k^{"~expr~"}" | "k^k" | ("k^"~smallDegree) | "k") ^^ {
      case "k^k" => Power(GT.K)
      case "k^{"~(expr:GT)~"}" => Power(expr)
      case "k^" ~ (digit: Int) => Power(Const(digit))
      case "k" => GT.K
    }
    
    def term: Parser[GT] = (power | (coefficient ~ power) | constantTerm) ^^ {
      case (q: BigInt) ~ (t:Term) => t * q
      case c: Const => c
      case t: Term => t
    }
    
    def apply(ex: String): GT = parseAll(expr, ex).get
  }

  // e.g. 3\(\times\)(132^{132}) + 3\(\times\)(132^3) + 2\(\times\)(132^2) + 3\(\times\)132 + 131
  def latex2code(latex: String): GT = {
    GT(latex.replace("""\(\times\)""", "×"))
  }
}
