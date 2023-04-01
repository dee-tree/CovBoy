# *CovBoy* - Coverage based SMT-solvers tester

## How to run?

### Coverage sampler

#### On *smtlib v2* benchmarks

Run on all *.smt2 files in directory:

`gradle :CovBoy-runner:benchmarks-sampler`

Example:

```
gradle :CovBoy-runner:benchmarks-sampler -PbenchmarksDir=./data/benchmarks/formulas -PcoverageDir=./data/benchmarks/coverage -PsamplerType=baseline -PsamplerParams=--solverTimeoutMillis=1000,--samplerTimeoutMillis=60000,--completeModels=true,Z3,Bitwuzla,Cvc5
```

Options:

* `-PbenchmarksDir=/path/to/smtlibbenchmarks`
* `-PcoverageDir=/path/to/coverage/to/save` - internal structure of files preserved as benchmarks dir
* `-PsamplerType=baseline` - type of coverage sampler. Possible
  values: `baseline` / `PredicatesPropagatingSampler` / `GroupingModelsSampler`. *baseline*
  is `PredicatesPropagatingSampler`
* `-PsamplerParams=samplerParams,separated,with,commas`:
  * `--solverTimeoutMillis=1000` - timeout on solver checkSat(), in millis
  * `--PsamplerTimeoutMillis=60000` - timeout on coverage sampling, in millis
  * `--rewrite` - rewrite previously collected coverage on the input files, or continue collecting. By default - do not
    rewrite
  * `--completeModels=true` - force solver to provide complete models (`true`), or compute coverage lazily if
    possible (`false`)
  * `Z3,Bitwuzla,Cvc5,Yices` - solvers, used to sample coverage on formulas. Possible
    solvers: [Z3, Bitwuzla, Cvc5, Yices] - currently supported in **ksmt**

### Coverage comparator

`gradle :CovBoy-runner:coverage-compare`

Example:
`gradle :CovBoy-runner:coverage-compare -PcoverageDir=./data/benchmarks/coverage -PprimarySolver=Z3`

Options:

* `-PcoverageDir=/path/to/sampled/coverage`
* `-PprimarySolver=Z3` - Solver, which coverage will be compared with others. Possible
  value: [Z3, Bitwuzla, Cvc5, Yices]

