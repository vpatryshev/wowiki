package scalakittens

import Numbers._
import scala.util.parsing.combinator._

object Goodstein {
  def const(n: BigInt) = Towers(n)
  val Const1 = const(Big1)
  val ConstK = Towers(Big0, Const1 -> Big1)

  def single(expr: Towers) = Towers(Big0, expr -> Big1)
  
  def mult(q: BigInt, single: Towers) = Towers(Big0, single.contents.head._1 -> q)
  
  class T extends JavaTokenParsers {
    def tap[X](what: String, x: X) = {
//      println(s"got $what (${x.getClass}) $x")
      x
    }
    
    def digit: Parser[String] = """\d""".r

    def digits: Parser[String] = """\d+""".r
    
    def smallNumber: Parser[Int] = digit ^^ {(n: String) => tap("smallint", n.toInt)}

    def number: Parser[Towers] = digits ^^ { n => tap("bigint", Towers(BigInt(n))) }

    def expr: Parser[Towers] = repsep(term, "+") ^^ {
      case list => tap("for expr:", list);
        val const = list.map(_.tail).fold(Big0)(_+_)
        val grouped = list.flatMap(_.contents).groupBy(_._1)
        val withCounts = grouped.map {
          case (t, group) => t -> group.map(_._2).fold(Big0)(_+_)
        }.toList
        val sorted = withCounts.sortBy(_._1).reverse
//        println(s"Before and after sorting:\n$withCounts\n$sorted")
        val result = Towers(const, sorted :_*)
        tap("result: ", result)
    }

    def q: Parser[BigInt] = (digits ~ "×") ^^ { 
      case n ~ _ => tap("q", BigInt(n))
    }

    def power: Parser[Towers] = ("k^{"~expr~"}" | "k^k" | ("k^"~smallNumber) | "k") ^^ {
      case "k^k" => tap("pure", "k^k")
        Towers(0, ConstK -> Big1)
      case "k^{"~(expr:Towers)~"}" => tap("something", expr); tap("resulting: ", single(expr))
//      case "k^9" => tap("k9", single(Towers(BigInt(9))))
      case "k^" ~ (digit: Int) => tap("kdigit", single(Towers(BigInt(digit))))
      case "k" => tap("singleton", "k"); ConstK
    }
    def term: Parser[Towers] = (power | (q ~ power) | number) ^^ {
//      case number => tap("plain number", _)
      case (q: BigInt) ~ (power:Towers) =>
        tap("quotient", q); tap("power", power); tap("qx?", mult(q, power))
      case p: Towers => tap("a term", p)
    }

    def asTerm(ex: String): Towers =  {
      println(s"Trying <term> $ex")
      val parsed = parser.parseAll(parser.term, ex)
      println(parsed)
      println("-----------------------")
      parsed.get
    }
    
    def apply(ex: String): Towers = {
//      println(s"Running $ex")
      val parsed = parser.parseAll(parser.expr, ex)
//      println(parsed)
//      println("-----------------------")
      parsed.get
    }

  }


  val parser = new T
  
  def run(ex: String): Unit = {
    parser.apply(ex)
  }
  
  def main(args: Array[String]): Unit =
  {
    run("256")
    run("k^k")
    run("k^{k}")
    run("22 + k")
    run("123 + 321 + k + k + k^{k} + k^k + 5×k")
    run("10×k^{1+2×k^{13×k}} + k^{k}")
  }


  // e.g. 3\(\times\)(132^{132}) + 3\(\times\)(132^3) + 2\(\times\)(132^2) + 3\(\times\)132 + 131
  def latex2code(latex: String): Towers = {
    Towers(latex.replace("""\(\times\)""", "×"))
  }
}
