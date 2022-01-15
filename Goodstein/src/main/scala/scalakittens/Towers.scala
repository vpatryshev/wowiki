package scalakittens

import Numbers._

/**
 * @param tail the numeric value at the end of this collection of tower-represented numbers
 * @param contents components of the tower representation: another tower (degree) and the quotient for it
 * e.g. 2*n^{3*n^{n+1} + 5*n + 7} + 6*n^2 + 8 is represented as
 * TR(8, TR(6, TR(2)), TR(2, TR(3, TR(1,TR(1)))))
 */
case class Towers(tail: BigInt, contents:  (Towers, BigInt)*) {
  def eval(base: BigInt): BigInt = contents.foldLeft(tail) {
    case (s, (t, c)) => s + c * pow(base, t.eval(base))
  }
}
