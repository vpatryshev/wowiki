# Goodstein Sequence Calculator

For reference, see [wolfram](https://mathworld.wolfram.com/GoodsteinSequence.html) and [wiki](https://en.wikipedia.org/wiki/Goodstein%27s_theorem)

This code represents big numbers as [Hereditary Notation](https://mathworld.wolfram.com/HereditaryRepresentation.html).
To produce Goodstein Sequence, one has to, at each step, increase the base by 1, and then decrease the number by 1. These two steps are implemented in one method, `dec(base)`.

Numbers are canonically represented in md-like format, but can be also converted to Latex.

## Examples
`2·k^k - 1`, for base=3, will be `k^k + 2·k^2 + 2·k + 2`.


