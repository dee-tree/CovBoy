; https://github.com/Z3Prover/z3test/blob/master/regressions/smt2/3104.smt2
(set-option :model_validate true)
(declare-const x Real)
(assert (= x 0.0))
(assert (= (^ x (- 1.0)) (/ 1.0 x)))
(check-sat) ; expected: sat