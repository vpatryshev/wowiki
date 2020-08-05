# Algebraic Data Types, Categorically


## Algebraic Theories
You can look them up, or look up [Lawvere theory](https://en.wikipedia.org/wiki/Lawvere_theory).
An algebraic theory **T** consists of, in its simplest form, of one data type and a collection of operation symbols of finite arity.
In addition, an algebraic theory may have axioms of the form expression<sub>1</sub> = expression<sub>2</sub>.
Note also, that here we talk exclusively about finitary theories: each operation arity is a natural number. This eliminates arity problems like the one in Bauer's "What is algebraic..." paper.

The theory of monoids is an example: it has a nullary operation `Zero` and a binary operation `Op`, with axioms for `Op`'s associativity and `Zero` being a neutral element for `Op`.

A model of an algebraic theory in a CCC(cartesian-closed category) **C** is an object **X** and an arrow **X<sup>n</sup> → X**, defined for each operation of arity **n**. If the theory has axioms, these axioms should hold in the model (that is, a certain square should be commutative). We can represent such a model as just one arrow **P<sub>T</sub>[X] → X** where **P<sub>T</sub>[X]** is a union of **X<sup>n</sup> → X** for each operation of arity **n**, that is, essentially, a polynomial; e.g. for monoids (forgetting its axioms, that is, for *magmas*), the functor **P<sub>T</sub>[X]** is **1+X<sup>2</sup>**.

If a theory **T** does not have axioms, it's also known as language. It is the language of legitimate expressions of the theory, and it can be expressed in BNF. When we talk about types, we may not need axioms (probably, not sure, axioms introduce dependent types).

## Multisorted Algebraic Theories
A slightly more complicated kind of algebraic theory is the one where there's more than one data type (aka 'sort'). In this case operations have signatures that specify argument types and the result types.

As an example, take vector spaces over ℝ - we have operations on ℝ, operations (addition) for vectors, and multiplication of vectors by numbers; there are also some axioms. 

Or you can take a theory of matrices, where we have a distinct type of matrix for each pair of dimensions, <code>n</code> and <code>m</code>, and operations of addition and multiplications defined accordingly.

Modeling a multisorted algebra is a little bit trickier. Say, we do it in sets. We need a set for each data type, and we need, for each operation, a function from the Cartesian product of sets of the appropriate argument types to the set of result type. E.g. for matrices, for the operation of multiplying n×m matrices with m×k matrices, we need a map **M<sub>n,m</sub>(ℝ) × M<sub>m,k</sub>(ℝ) → M<sub>n,k</sub>(ℝ)**.

## Types

Keep in mind that there lurks an algebraic theory somewhere behind all those "types".

Suppose we are in a CCC **C**, as may functional programmers imagine themselves in. In the terms of such category, I'm trying to express the notions of ADT and GADT.

First, a couple of definitions.

### Algebras over a Functor
Given a functor **F** in a category, an algebra over **F** is an object **X** with an arrow **F[X]→X** - or just such an arrow, since **X** is its codomain. The category of **F**-algebras is denoted as **C<sup>F</sup>**. Arrows in **C<sup>F</sup>** are known as "structured morphisms" in Haskell.

### Polynomial Functor
Any functor **P[X]** that takes an object **X** and maps it to a polynomial object, **P[X] = Σ c<sub>i</sub>× X<sup>i</sup>**.

As you see, an algebraic theory (with no axioms) **T**, when modeled in a category **C**, gives us a polynomial functor **P<sub>T</sub>**,  built out of **T**'s signatures, and also a natural transformation from **P<sub>T</sub>[X]** to **X**. That is, a model of **T** is an algebra over the functor **P<sub>T</sub>**. We can also go into whether **P<sub>T</sub>** is a monad, but that's a different story.

### Fixed point of a functor **F**
Fixed point of **F** is an object **X** such that **F[X] ≅ X**. There may be more than one fixed point. The smallest one is an initial object in **C<sup>F</sup>**; the biggest one is a terminal object in the category of **F**-coalgebras, **C<sub>F</sub>** (the category of arrows of the form **X→F[X]**).

It's pretty obvious how an initial object **I** in the category of algebras over **F** is a fixed point of **F** (hint: look how algebras **F[I]→I** and **F[F[I]]→F[I]** can be mapped to each other).

*Catamorphism* for a given algebra **F[X]→X** is the unique arrow of algebras ("structured morphism" in Haskell) from **I** to **X**.

Now, for a given theory **T**, in the category of algebras over **P<sub>T</sub>** there *can* exist an initial object **P<sub>T</sub>[I] → I**, that is, the one that has a unique algebra morphism **f: I → X** to any algebra **P<sub>T</sub>[X] → X**. How can it be produced? Since **P<sub>T</sub>** is a polynomial, we can view such a solution as a root of a polynomial equation **P<sub>T</sub>[X] ≅ X**. If we are talking about models in the category of sets, we are looking for a minimal solution of **P<sub>T</sub>[X] ≅ X**. It can be build constructively; and it's called an "algebraic data type". Such an algebra, if we ignore axioms, is a *tree*.

## Algebraic Data Type (ADT) _aka_ Recursive Data Type
Given an algebraic theory **T** (with no axioms), the corresponding ADT (in Sets) is defined like this:

These types are the following:
- a "simple value" for each nullary operation of **T**
- an operation tag followed by a tuple of ADT values, for each operation of the theory.

By this definition, any "constant" (given by a nullary operation) belong to the type, and any expression built from expressions of our type using operations symbols, also belongs to the type. "Recursive" means that we can go ahead and build more complex expressions out of simpler ones.

This definition gives us an initial algebra in Sets for the theory **T**, or, in other words, a minimal fixed point **X** = P<sub>T</sub>[X]**. Intuitively, it is clear that we are talking about AST for expressions in theory **T**. These trees are exactly instances of an ADT for this theory.

E.g., given a theory with a nullary operation **a<sub>0</sub>** and a <i>set</i> of unary operations **A**, the functor **X → 1+A×X** is polynomial (it corresponds to a theory with one nullary operation and a set **A** of unary operations; its fixpoint, **X = 1+A×X**, is an ADT that represents the type of strings of alphabet **A**, or, if you like, the type of lists of values from set **A**.

Another example, *magma* with a unit. A [magma](https://en.wikipedia.org/wiki/Magma_(algebra)) theory consists of one binary operation, call it **m**. Accordingly an algebra for magma theory is a set **X** with an operation **X×X → X**. If we don't require a unit element, that is, a point **1 → X**, an initial algebra for magma theory is an empty set. If we add a point, we have a different story. Even better if we add a set **A** of points. Then an algebra for such theory will be a set **X** with an operation **A + X×X → X**. In such theory, a minimal fixpoint for such an operation (that is, an initial algebra) is a binary tree with values of **A** at leaves. You can view such trees as just expressions of the form **m(m(a,b),m(m(b,m(c)),m(d,a))**, where **a**,**b**,**c** etc are elements of **A**, that is, constants of the theory **Magma + A**.

## Generalized Algebraic Data Type (GADT)
For a "generalized" type, we extends our theory with more than one type. If you read a wiki page on GADT (in Haskell), they'll tell you, basically, that if you have just one "initial" type, it's not a GADT, it's an ADT. I think, if it's generalized, it should include, right? Let's think logically, if possible. On the other hand, in Haskell's GADT, there are prescriptions specifying types of arguments of expressions that belong to a given GADT - meaning that, in a sense, a GADT is less generalized than an ADT.

We can actually generalize ADT properly, introducing the same notion, but for a multisorted algebraic theory.

A multisorted algebra makes the task of representing expressions more difficult. We may have constants of a variety of types, e.g. _Boolean_ and _Integer_. For a regular algebraic theory we don't involve any extra types. So, in ADT, if there are types, they can be viewed as parameters. But for multisorted algebras we need to provide some information about the types involved. E.g. _Integer_ can be defined via Peano, as **s<sup>n</sup>(0)**, and _Boolean_ can be defined as two constants and three operations (called *connectives* in logic): ¬, ∧, ∨. Then, having some types predefined, we can build a type of mixed expressions, according to our multisorted algebraic theory.

So, keeping in mind a multisorted algebraic theory, we define a GADT like this:

These types are the following:
- a collection of predefined ("base") types (this could be `Bool`, `Int`, `Char`... whatever types are defined as "base" in your type theory);
- a value of a polynomial functor on GADT;
- a fixed point of a GADT.

Similarly to the case of ADT, we have trees, but this time we need to attribute a type to the base value at each node.

For instance, for a vector space **𝕍** over **ℝ**, we have a theory that has **0** and **1** in **ℝ**; multiplication and addition for **ℝ**, that is, ** 

Another example (see [GADT in wiki](https://en.wikipedia.org/wiki/Generalized_algebraic_data_type) is build out of _Integer_ and _Boolean_ types via comparator predicate `==`. This operation takes two integers and produces a boolean value. Since we can also use logical connectives (which is a part of "logical theory") and some integer operations (defined by "the theory of integers"), we can build complex expressions that ensure we have correct type checking. But the basis of all this is just a multisorted algebraic theory.

From a categorical point of view, we need to find a solution for representing a model of a multisorted algebra as an algebra.
The trick is the following. Suppose we have n sorts in our theory, **S<sub>1</sub>,...,S<sub>n</sub>**. A model of such a theory would involve n types (or, in a category, n objects) **X<sub>1</sub>,...,X<sub>n</sub>**, randomly participating in operations, e.g. the first operation is defined on **X<sub>1</sub> × X<sub>2</sub>**, another on **X<sub>3</sub> × X<sub>5</sub> × X<sub>8</sub>**, etc. We can think of all these operations as being defined on one or more copies of **X<sub>1</sub> × X<sub>1</sub> × ... × X<sub>n</sub>**, and taking values in **X<sub>1</sub> × X<sub>1</sub> × ... × X<sub>n</sub>**. Let's denote this product as **X**; it's a compound type consisting of all "primitive" types.

How can we transform our multisorted algebra operations to operations over **X**? First, a nullary operation, **() → X<sub>i</sub>** can be always represented as a constant unary operation **X<sub>i</sub> → X<sub>i</sub>**. Then, given a unary operation **X<sub>i</sub> → X<sub>j</sub>**, we can represent it as **X → X** by mapping i-th component of the product to **X<sub>j</sub>**, and all others mapping to themselves. Similarly, a binary operation **op: X<sub>i</sub>×X<sub>j</sub> → X<sub>k</sub>** can be represented as **X×X → X** by mapping the components participating in **op** using this operation, and more or less arbitrarily defining the operation on other components, by projecting the components. That is, we need to define **X×X → X<sub>m</sub>**, and we define it like this: for **m=k** it is defined by the operation **op: X<sub>i</sub>×X<sub>j</sub> → X<sub>k</sub>**, and for **m≠k** it is the projection of **m**-th component of **X = X<sub>1</sub> × X<sub>1</sub> × ... × X<sub>n</sub>**.

This way we can represent multisorted algebras as regular algebras. E.g. for a vector space **𝕍** over **ℝ** we will have operations on **ℝ** and **𝕍** and multiplication **ℝ×𝕍 → 𝕍**. The corresponding single-sorted algebra is an object (a set)
**ℝ×𝕍** with operations **ℝ×𝕍 × ℝ×𝕍 → ℝ×𝕍** that, for number addition and multiplication, ignore the vector arguments:
**plus(r1,v1,r2,v2) = (r1+r2, v1)**, **mult(r1,v1,r2,v2) = (r1*r2, v1)**, and for vector addition we ignore the number arguments:
**vplus(r1,v1,r2,v2) = (r1, v1+v2)**. Multiplication of a vector by a number is defined as **vmult(r1,v1,r2,v2) = (r1, r1*v1)**.

Having all this, we can easily build a functor for a given multisorted algebraic theory, and a category of algebras for such a functor. These algebras are (up to the unused components) will be a model of our theory.

Returning to the type-theoretical view, we have, as a representation, a global type **T<sub>0</sub>**, which is a product of all possible types, for starters. Then our functor gives us a way to build the type of expressions, which is a union of all possible expressions. Recursively we build a type of syntax trees, which is a fixed point (the smallest one) of our functor.

Also, from nlab: "A monad (T,μ,i) on the category Set of sets, is finitary (also called algebraic, although some people consider any monad to be an algebraic notion) if the underlying endofunctor T:Set→Set commutes with filtered colimits.

In other words, an algebraic monad is a monoid in the category of algebraic endofunctors on Set."

Same page, further down:
"A finitary monad (T,μ,i) is completely determined by its value on all finite ordinals n∈ℕ0 considered as standard finite sets. T(n) is then the set of n-ary operations. The notion of algebraic monad is hence similar to the notion of a nonsymmetric operad in Set, but it is not equivalent, because of the possibility of duplicating or discarding inputs.

More precisely, each finitary monad T defines a Lawvere theory Th<sub>T</sub>, namely Th<sub>T</sub>=Free<sup>op</sup><sub>fin</sub> where Free<sub>fin</sub> is the category of free algebras T(n) on finite sets (as a full subcategory of Alg<sub>T</sub>). In fact, the two notions are equivalent: the assignment

T↦Th<sub>T</sub>

defines an equivalence between the category of finitary monads on Set and the category of Lawvere theories. Moreover, the category of T-algebras is equivalent to the category of models of Th<sub>T</sub>. However, a technical advantage of Lawvere theories is that they can be interpreted in categories other than Set: a model of a Lawvere theory 𝒯 in a category with cartesian products C is just a product-preserving functor 𝒯→C."

Sources:
  - [Lawvere, "Algebraic Theories, Algebraic Categories, and Algebraic Functors"](https://github.com/mattearnshaw/lawvere/blob/master/pdfs/1965-algebraic-theories-algebraic-categories-and-algebraic-functors.pdf)
  - [Lawvere, "FUNCTORIAL SEMANTICS OF ALGEBRAIC THEORIES"](http://www.tac.mta.ca/tac/reprints/articles/5/tr5.pdf)
  - [Type System (wiki)](https://en.wikipedia.org/wiki/Type_system)
  - [ADT (wiki)](https://en.wikipedia.org/wiki/Algebraic_data_type)
  - [ADT explained on SO](https://stackoverflow.com/questions/16770/haskells-algebraic-data-types/5917133#5917133)
  - [GADT (wiki)](https://en.wikipedia.org/wiki/Generalized_algebraic_data_type)
  - [Cheney, Hinze, "First-Class Phantom Types"](https://ecommons.cornell.edu/bitstream/handle/1813/5614/TR2003-1901.pdf?sequence=1&isAllowed=y)
  - [Fritz Henglein, "Kleene meets Church: Regular expressions as types"](http://www.cs.ox.ac.uk/ralf.hinze/WG2.8/27/slides/fritz.pdf)
- [Gordon Plotkin and John Power, "Semantics for algebraic operations"](http://homepages.inf.ed.ac.uk/gdp/publications/sem_alg_ops.pdf)
- [Gordon Plotkin and John Power, "Algebraic Operations and Generic Effects"](http://homepages.inf.ed.ac.uk/gdp/publications/alg_ops_gen_effects.pdf)
- [Andrej Bauer, "What is algebraic about algebraic effects and handlers?"](https://arxiv.org/abs/1807.05923)
- [Andrej Bauer, "Parallelism and Concurrency" (youtube)](https://www.youtube.com/watch?v=atYp386EGo8)
- [effects bibliography (github)](https://github.com/yallop/effects-bibliography)
- [reference compendium by xacid.dreamwidth.org](https://xacid.dreamwidth.org)
- [Bartosz's explanations of all this, in Haskell](https://bartoszmilewski.com/2018/08/20/recursion-schemes-for-higher-algebras)
- [S.MacLane, I.Moerdijk, "Sheaves in Geometry and Logic: A First Introduction to Topos Theory"](https://www.amazon.com/Sheaves-Geometry-Logic-Introduction-Universitext/dp/0387977104/ref=sr_1_1?dchild=1&qid=1595807966&s=books&sr=1-1)
- [finitary monad, nLab](https://ncatlab.org/nlab/show/finitary+monad)
- [Eugenia Cheng, Distributive laws for Lawvere theories](https://www.youtube.com/watch?v=t4pwM8h8XyY)
- [Geometric Theory on nLab](https://ncatlab.org/nlab/show/geometric+theory)
- [Theory of Objects on nLab](https://ncatlab.org/nlab/show/theory+of+objects)
