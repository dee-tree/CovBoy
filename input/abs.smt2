(declare-fun abs (Int) Int)
(declare-const a Int)

; ((a < 0) and abs(a) = -a) or ((a >= 0) and (abs(a) = a))
(assert (or (and (< a 0) (= (abs a) (- a))) (and (>= a 0) (= (abs a) a))))