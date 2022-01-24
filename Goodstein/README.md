# Goodstein Sequence Calculator

For reference, see [wolfram](https://mathworld.wolfram.com/GoodsteinSequence.html) and [wiki](https://en.wikipedia.org/wiki/Goodstein%27s_theorem)

This code represents big numbers as [Hereditary Notation](https://mathworld.wolfram.com/HereditaryRepresentation.html).
To produce Goodstein Sequence, one has to, at each step, increase the base by 1, and then decrease the number by 1. These two steps are implemented in one method, `dec(base)`.

Numbers are canonically represented in md-like format, but can be also converted to Latex.

## Examples
`2·k<sup>k</sup> - 1`, for base=3, will be `k<sup>k</sup> + 2·k<sup>2</sup> + 2·k + 2`.

### First 100 values

### First n values starting at 100

Base |	          Value           |           Hereditary Notation
 ---- |	-------------------------: | ----------------------------------
2 |                      100  | k<sup>k<sup>k</sup> + 1</sup> + k<sup>k<sup>k</sup> + k</sup> + k<sup>k</sup>
3 |         2.287679·10<sup>14</sup>  | k<sup>k<sup>k</sup> + 1</sup> + k<sup>k<sup>k</sup> + k</sup> + 2·k<sup>2</sup> + 2·k + 2
4 |         3.48603·10<sup>156</sup>  | k<sup>k<sup>k</sup> + 1</sup> + k<sup>k<sup>k</sup> + k</sup> + 2·k<sup>2</sup> + 2·k + 1
5 |       5.981469·10<sup>2187</sup>  | k<sup>k<sup>k</sup> + 1</sup> + k<sup>k<sup>k</sup> + k</sup> + 2·k<sup>2</sup> + 2·k
6 |      1.240798·10<sup>36310</sup>  | k<sup>k<sup>k</sup> + 1</sup> + k<sup>k<sup>k</sup> + k</sup> + 2·k<sup>2</sup> + k + 5
7 |     3.096402·10<sup>695980</sup>  | k<sup>k<sup>k</sup> + 1</sup> + k<sup>k<sup>k</sup> + k</sup> + 2·k<sup>2</sup> + k + 4
8 |   1.009069·10<sup>15151343</sup>  | k<sup>k<sup>k</sup> + 1</sup> + k<sup>k<sup>k</sup> + k</sup> + 2·k<sup>2</sup> + k + 3

# The Code

`Numbers#BiggerInt` adds functionality for BigInt

`HereditaryNotation` represents numbers in hereditary notation; `k` is the variable for base. A hereditary notation consists of `Term`s.

`Term` is a class representing a term; it has two parameters, coefficient `c` and degree `d`, so that the number represented is `c·k<sup>d</sup>.

`Parser` parses hereditary notation representations, see tests.

`main` function runs an example of calculations; you can use either `sequential` generator that produces values one by one, starting with `base=2`, or `accelerated` that skips most of the calculations, only demonstrating the values around where `constantTerm` is either very small or very large.

Your comments and/or cooperation welcome.

_Author: Vlad Patryshev, 01/23/2022_



