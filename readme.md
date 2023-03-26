# *CovBoy* - Coverage based SMT-solvers tester

## How to run?

### Coverage sampler

Run on all *.smt2 files in directory:

`gradle :CovBoy-runner:benchmarks-sampler -PsolverTimeoutMillis=1000 -PsamplerTimeoutMillis=60000 -PrewriteResults=false -Psolvers=Z3,Bitwuzla,Cvc5`

Options:

* `-PbenchmarksDir=/path/to/smtlibbenchmarks`
* `-PcoverageDir=/path/to/coverage/to/save` - internal structure of files saved as benchmarks dir
* `-PsolverTimeoutMillis=1000` - timeout on solver checkSat(), in millis
* `-PsamplerTimeoutMillis=60000` - timeout on coverage sampling, in millis
* `-PrewriteResults=false` - rewrite previously collected coverage on the input files, or continue
  collecting [true/false]
* `-Psolvers=Z3,Bitwuzla,Cvc5,Yices` - solvers, used to sample coverage on formulas. Possible
  solvers: [Z3, Bitwuzla, Cvc5, Yices] - currently supported in **ksmt**

### Coverage comparator

`gradle :CovBoy-runner:coverage-compare`

Options:

* `-PcoverageDir=/path/to/saved/coverage`
* `-PprimarySolver=Z3` - Solver, which coverage will be compared with others. Possible
  value: [Z3, Bitwuzla, Cvc5, Yices]

