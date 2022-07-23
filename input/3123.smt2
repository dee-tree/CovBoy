; https://github.com/Z3Prover/z3test/blob/master/regressions/smt2/3123.smt2
(set-option :smt.arith.solver 6)
(declare-fun a () Real)
(declare-fun b () Real)
(declare-fun c () Real)
(declare-fun d () Real)
(declare-fun g () Real)
(assert
        (and
            (forall ((e Real)) (or (= (* b d) 0.0) (and (< 0.0 (/ 2.0 e c)) (< (* e c) g))))
            (forall ((f Real)) (< 0.0 (* a c)))))
(check-sat) ; expected: sat