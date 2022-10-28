(set-info :smt-lib-version 2.6)
(set-logic LIA)
(set-info
  :source |
 Generated by PSyCO 0.1
 More info in N. P. Lopes and J. Monteiro. Weakest Precondition Synthesis for
 Compiler Optimizations, VMCAI'14.
|)
(set-info :category "industrial")
(set-info :status sat)
(declare-fun R_E1_V6 () Bool)
(declare-fun W_S1_V6 () Bool)
(declare-fun R_E1_V4 () Bool)
(declare-fun W_S1_V4 () Bool)
(declare-fun W_S1_V3 () Bool)
(declare-fun W_S1_V1 () Bool)
(declare-fun R_S1_V6 () Bool)
(declare-fun R_S1_V4 () Bool)
(declare-fun R_S1_V5 () Bool)
(declare-fun R_S1_V2 () Bool)
(declare-fun R_S1_V3 () Bool)
(declare-fun R_S1_V1 () Bool)
(declare-fun R_E2_V1 () Bool)
(declare-fun R_E1_V2 () Bool)
(declare-fun R_E1_V5 () Bool)
(declare-fun DISJ_W_S1_R_S1 () Bool)
(declare-fun R_E2_V6 () Bool)
(declare-fun R_E2_V4 () Bool)
(declare-fun R_E2_V5 () Bool)
(declare-fun DISJ_W_S1_R_E2 () Bool)
(declare-fun W_S1_V5 () Bool)
(declare-fun W_S1_V2 () Bool)
(declare-fun R_E1_V3 () Bool)
(declare-fun R_E1_V1 () Bool)
(declare-fun R_E2_V3 () Bool)
(declare-fun DISJ_W_S1_R_E1 () Bool)
(assert
 (let
 (($x61276
   (forall
    ((V2_0 Int) (V5_0 Int) 
     (V4_0 Int) (V6_0 Int) 
     (MW_S1_V1 Bool) (MW_S1_V3 Bool) 
     (MW_S1_V2 Bool) (MW_S1_V5 Bool) 
     (MW_S1_V4 Bool) (MW_S1_V6 Bool) 
     (S1_V1_!7162 Int) (S1_V1_!7168 Int) 
     (S1_V1_!7177 Int) (S1_V1_!7184 Int) 
     (S1_V3_!7163 Int) (S1_V3_!7169 Int) 
     (S1_V3_!7178 Int) (S1_V3_!7185 Int) 
     (S1_V2_!7164 Int) (S1_V2_!7170 Int) 
     (S1_V2_!7179 Int) (S1_V2_!7186 Int) 
     (E1_!7159 Int) (E1_!7174 Int) 
     (E1_!7176 Int) (E1_!7183 Int) 
     (S1_V5_!7165 Int) (S1_V5_!7171 Int) 
     (S1_V5_!7180 Int) (S1_V5_!7187 Int) 
     (E2_!7160 Int) (E2_!7161 Int) 
     (E2_!7175 Int) (S1_V4_!7166 Int) 
     (S1_V4_!7172 Int) (S1_V4_!7181 Int) 
     (S1_V4_!7188 Int) (S1_V6_!7167 Int) 
     (S1_V6_!7173 Int) (S1_V6_!7182 Int) 
     (S1_V6_!7189 Int))
    (let ((?x61126 (ite MW_S1_V3 S1_V3_!7178 E2_!7175)))
    (let ((?x60090 (+ 1 ?x61126)))
    (let ((?x62541 (ite MW_S1_V3 S1_V3_!7185 ?x60090)))
    (let ((?x62464 (ite MW_S1_V3 S1_V3_!7163 E2_!7161)))
    (let ((?x62740 (+ 1 ?x62464)))
    (let ((?x62994 (ite MW_S1_V3 S1_V3_!7169 ?x62740)))
    (let
    (($x62486
      (and
      (= (ite MW_S1_V1 S1_V1_!7168 E1_!7159)
      (ite MW_S1_V1 S1_V1_!7184 E1_!7183)) 
      (= ?x62994 ?x62541)
      (= (ite MW_S1_V2 S1_V2_!7170 V2_0) (ite MW_S1_V2 S1_V2_!7186 V2_0))
      (= (ite MW_S1_V5 S1_V5_!7171 V5_0) (ite MW_S1_V5 S1_V5_!7187 V5_0))
      (= (ite MW_S1_V4 S1_V4_!7172 V4_0) (ite MW_S1_V4 S1_V4_!7188 V4_0))
      (= (ite MW_S1_V6 S1_V6_!7173 V6_0) (ite MW_S1_V6 S1_V6_!7189 V6_0)))))
    (let
    (($x62663
      (and (not (<= V4_0 E2_!7160)) 
      (not (<= V2_0 E1_!7159)) 
      (not (<= V4_0 E2_!7161))
      (not (<= (ite MW_S1_V4 S1_V4_!7166 V4_0) ?x62740))
      (>= ?x62994 (+ (- 1) (ite MW_S1_V4 S1_V4_!7172 V4_0)))
      (>= (ite MW_S1_V1 S1_V1_!7168 E1_!7159)
      (+ (- 1) (ite MW_S1_V2 S1_V2_!7170 V2_0))) 
      (not (<= V2_0 E1_!7174)) 
      (not (<= V4_0 E2_!7175)) 
      (not (<= V2_0 E1_!7176))
      (>= (ite MW_S1_V1 S1_V1_!7177 E1_!7176)
      (+ (- 1) (ite MW_S1_V2 S1_V2_!7179 V2_0)))
      (not (<= (ite MW_S1_V4 S1_V4_!7181 V4_0) ?x60090))
      (not (<= (ite MW_S1_V2 S1_V2_!7179 V2_0) E1_!7183))
      (>= (ite MW_S1_V1 S1_V1_!7184 E1_!7183)
      (+ (- 1) (ite MW_S1_V2 S1_V2_!7186 V2_0)))
      (>= ?x62541 (+ (- 1) (ite MW_S1_V4 S1_V4_!7188 V4_0))))))
    (let
    (($x62560
      (and
      (or (not R_S1_V1) (= E1_!7183 (ite MW_S1_V1 S1_V1_!7162 E1_!7159)))
      (or (not R_S1_V3) (= ?x61126 ?x62464))
      (or (not R_S1_V2)
      (= (ite MW_S1_V2 S1_V2_!7179 V2_0) (ite MW_S1_V2 S1_V2_!7164 V2_0)))
      (or (not R_S1_V5)
      (= (ite MW_S1_V5 S1_V5_!7180 V5_0) (ite MW_S1_V5 S1_V5_!7165 V5_0)))
      (or (not R_S1_V4)
      (= (ite MW_S1_V4 S1_V4_!7181 V4_0) (ite MW_S1_V4 S1_V4_!7166 V4_0)))
      (or (not R_S1_V6)
      (= (ite MW_S1_V6 S1_V6_!7182 V6_0) (ite MW_S1_V6 S1_V6_!7167 V6_0))))))
    (let (($x61423 (not $x62560)))
    (let (($x207 (not R_S1_V6)))
    (let (($x61164 (or $x207 (= V6_0 (ite MW_S1_V6 S1_V6_!7182 V6_0)))))
    (let (($x205 (not R_S1_V4)))
    (let (($x62776 (or $x205 (= V4_0 (ite MW_S1_V4 S1_V4_!7181 V4_0)))))
    (let (($x203 (not R_S1_V5)))
    (let (($x59952 (or $x203 (= V5_0 (ite MW_S1_V5 S1_V5_!7180 V5_0)))))
    (let (($x201 (not R_S1_V2)))
    (let (($x61077 (or $x201 (= V2_0 (ite MW_S1_V2 S1_V2_!7179 V2_0)))))
    (let
    (($x60067
      (and (or (not R_S1_V1) (= E1_!7176 E1_!7183))
      (or (not R_S1_V3) (= E2_!7175 ?x60090)) $x61077 $x59952 $x62776
      $x61164)))
    (let (($x62412 (or $x207 (= V6_0 (ite MW_S1_V6 S1_V6_!7167 V6_0)))))
    (let (($x62713 (or $x205 (= V4_0 (ite MW_S1_V4 S1_V4_!7166 V4_0)))))
    (let (($x60185 (or $x203 (= V5_0 (ite MW_S1_V5 S1_V5_!7165 V5_0)))))
    (let (($x59862 (or $x201 (= V2_0 (ite MW_S1_V2 S1_V2_!7164 V2_0)))))
    (let
    (($x62261
      (and
      (or (not R_S1_V1) (= E1_!7176 (ite MW_S1_V1 S1_V1_!7162 E1_!7159)))
      (or (not R_S1_V3) (= E2_!7175 ?x62740)) $x59862 $x60185 $x62713
      $x62412)))
    (let (($x62471 (not $x62261)))
    (let
    (($x61267
      (and (or (not R_S1_V1) (= E1_!7159 E1_!7183))
      (or (not R_S1_V3) (= E2_!7161 ?x60090)) $x61077 $x59952 $x62776
      $x61164)))
    (let (($x63179 (not $x61267)))
    (let
    (($x61291
      (and (or (not R_S1_V1) (= E1_!7159 E1_!7176))
      (or (not R_S1_V3) (= E2_!7161 E2_!7175)))))
    (let (($x62606 (not $x61291)))
    (let
    (($x62692
      (and
      (or (not R_S1_V1) (= E1_!7159 (ite MW_S1_V1 S1_V1_!7162 E1_!7159)))
      (or (not R_S1_V3) (= E2_!7161 ?x62740)) $x59862 $x60185 $x62713
      $x62412)))
    (let (($x62278 (not $x62692)))
    (let (($x59901 (or $x207 (= (ite MW_S1_V6 S1_V6_!7182 V6_0) V6_0))))
    (let (($x63002 (or $x205 (= (ite MW_S1_V4 S1_V4_!7181 V4_0) V4_0))))
    (let (($x60959 (or $x203 (= (ite MW_S1_V5 S1_V5_!7180 V5_0) V5_0))))
    (let (($x60917 (or $x201 (= (ite MW_S1_V2 S1_V2_!7179 V2_0) V2_0))))
    (let
    (($x62651
      (and (or (not R_S1_V1) (= E1_!7183 E1_!7176))
      (or (not R_S1_V3) (= ?x61126 (+ (- 1) E2_!7175))) $x60917 $x60959
      $x63002 $x59901)))
    (let (($x63164 (not $x62651)))
    (let
    (($x62954
      (and (or (not R_S1_V1) (= E1_!7183 E1_!7159))
      (or (not R_S1_V3) (= ?x61126 (+ (- 1) E2_!7161))) $x60917 $x60959
      $x63002 $x59901)))
    (let (($x63180 (not $x62954)))
    (let
    (($x62394
      (and (or (not R_S1_V1) (= E1_!7176 E1_!7159))
      (or (not R_S1_V3) (= E2_!7175 E2_!7161)))))
    (let (($x62313 (or $x207 (= (ite MW_S1_V6 S1_V6_!7167 V6_0) V6_0))))
    (let (($x62622 (or $x205 (= (ite MW_S1_V4 S1_V4_!7166 V4_0) V4_0))))
    (let (($x62628 (or $x203 (= (ite MW_S1_V5 S1_V5_!7165 V5_0) V5_0))))
    (let (($x62846 (or $x201 (= (ite MW_S1_V2 S1_V2_!7164 V2_0) V2_0))))
    (let
    (($x62906
      (and
      (or (not R_S1_V1) (= (ite MW_S1_V1 S1_V1_!7162 E1_!7159) E1_!7159))
      (or (not R_S1_V3) (= ?x62464 (+ (- 1) E2_!7161))) $x62846 $x62628
      $x62622 $x62313)))
    (let
    (($x63142
      (and
      (or (not R_S1_V1) (= (ite MW_S1_V1 S1_V1_!7162 E1_!7159) E1_!7176))
      (or (not R_S1_V3) (= ?x62464 (+ (- 1) E2_!7175))) $x62846 $x62628
      $x62622 $x62313)))
    (let (($x61070 (not $x63142)))
    (let (($x62650 (= E1_!7183 E1_!7176)))
    (let
    (($x59863
      (and (or (not R_E1_V2) (= (ite MW_S1_V2 S1_V2_!7179 V2_0) V2_0))
      (or (not R_E1_V4) (= (ite MW_S1_V4 S1_V4_!7181 V4_0) V4_0))
      (or (not R_E1_V6) (= (ite MW_S1_V6 S1_V6_!7182 V6_0) V6_0)))))
    (let (($x61453 (= E1_!7159 E1_!7183)))
    (let
    (($x61325
      (and (or (not R_E1_V2) (= V2_0 (ite MW_S1_V2 S1_V2_!7179 V2_0)))
      (or (not R_E1_V4) (= V4_0 (ite MW_S1_V4 S1_V4_!7181 V4_0)))
      (or (not R_E1_V6) (= V6_0 (ite MW_S1_V6 S1_V6_!7182 V6_0))))))
    (let (($x62649 (= E1_!7159 E1_!7176)))
    (let
    (($x62786
      (and
      (or (not R_S1_V1) (= (ite MW_S1_V1 S1_V1_!7162 E1_!7159) E1_!7183))
      (or (not R_S1_V3) (= ?x62464 ?x61126))
      (or $x201
      (= (ite MW_S1_V2 S1_V2_!7164 V2_0) (ite MW_S1_V2 S1_V2_!7179 V2_0)))
      (or $x203
      (= (ite MW_S1_V5 S1_V5_!7165 V5_0) (ite MW_S1_V5 S1_V5_!7180 V5_0)))
      (or $x205
      (= (ite MW_S1_V4 S1_V4_!7166 V4_0) (ite MW_S1_V4 S1_V4_!7181 V4_0)))
      (or $x207
      (= (ite MW_S1_V6 S1_V6_!7167 V6_0) (ite MW_S1_V6 S1_V6_!7182 V6_0))))))
    (let
    (($x63037
      (and (or $x62278 (= S1_V1_!7162 S1_V1_!7168))
      (or $x63179 (= S1_V1_!7162 S1_V1_!7184))
      (or (not $x62394) (= S1_V1_!7177 S1_V1_!7162))
      (or $x62471 (= S1_V1_!7177 S1_V1_!7168))
      (or (not $x60067) (= S1_V1_!7177 S1_V1_!7184))
      (or $x61423 (= S1_V1_!7184 S1_V1_!7168))
      (or $x62278 (= S1_V3_!7163 S1_V3_!7169))
      (or $x62606 (= S1_V3_!7163 S1_V3_!7178))
      (or $x61070 (= S1_V3_!7169 S1_V3_!7178))
      (or $x63180 (= S1_V3_!7185 S1_V3_!7163))
      (or $x61423 (= S1_V3_!7185 S1_V3_!7169))
      (or $x63164 (= S1_V3_!7185 S1_V3_!7178))
      (or $x62278 (= S1_V2_!7164 S1_V2_!7170))
      (or $x62606 (= S1_V2_!7164 S1_V2_!7179))
      (or $x63179 (= S1_V2_!7164 S1_V2_!7186))
      (or $x61070 (= S1_V2_!7170 S1_V2_!7179))
      (or (not $x62786) (= S1_V2_!7170 S1_V2_!7186))
      (or $x63164 (= S1_V2_!7186 S1_V2_!7179)) 
      (= E1_!7159 E1_!7174) $x62649 
      (or (not $x61325) $x61453) 
      (= E1_!7174 E1_!7176) 
      (or (not $x59863) (= E1_!7183 E1_!7174)) 
      (or (not $x59863) $x62650) 
      (or $x62606 (= S1_V5_!7165 S1_V5_!7180))
      (or (not $x62906) (= S1_V5_!7171 S1_V5_!7165))
      (or $x61070 (= S1_V5_!7171 S1_V5_!7180))
      (or $x63180 (= S1_V5_!7187 S1_V5_!7165))
      (or $x61423 (= S1_V5_!7187 S1_V5_!7171))
      (or $x63164 (= S1_V5_!7187 S1_V5_!7180)) 
      (= E2_!7160 E2_!7161)
      (or (not (or (not R_E2_V1) (= E1_!7174 E1_!7159)))
      (= E2_!7175 E2_!7160))
      (or (not (or (not R_E2_V1) (= E1_!7174 E1_!7159)))
      (= E2_!7175 E2_!7161)) 
      (or (not $x62906) (= S1_V4_!7172 S1_V4_!7166))
      (or (not $x62394) (= S1_V4_!7181 S1_V4_!7166))
      (or $x62471 (= S1_V4_!7181 S1_V4_!7172))
      (or $x63180 (= S1_V4_!7188 S1_V4_!7166))
      (or $x61423 (= S1_V4_!7188 S1_V4_!7172))
      (or $x63164 (= S1_V4_!7188 S1_V4_!7181))
      (or $x62278 (= S1_V6_!7167 S1_V6_!7173))
      (or $x62606 (= S1_V6_!7167 S1_V6_!7182))
      (or $x63179 (= S1_V6_!7167 S1_V6_!7189))
      (or $x62471 (= S1_V6_!7182 S1_V6_!7173))
      (or (not $x60067) (= S1_V6_!7182 S1_V6_!7189))
      (or $x61423 (= S1_V6_!7189 S1_V6_!7173)) 
      (or (not MW_S1_V1) W_S1_V1) 
      (or (not MW_S1_V3) W_S1_V3) 
      (not MW_S1_V2) (or (not MW_S1_V4) W_S1_V4) 
      (or (not MW_S1_V6) W_S1_V6))))
    (or (not $x63037) (not $x62663) $x62486))))))))))))))))))))))))))))))))))))))))))))))))))))))))))
 (let (($x119 (not R_E1_V5)))
 (let (($x40 (and W_S1_V6 R_S1_V6)))
 (let (($x38 (and W_S1_V4 R_S1_V4)))
 (let (($x32 (and W_S1_V3 R_S1_V3)))
 (let (($x30 (and W_S1_V1 R_S1_V1)))
 (let (($x24 (and W_S1_V6 R_E2_V6)))
 (let (($x21 (and W_S1_V4 R_E2_V4)))
 (let (($x10 (and W_S1_V1 R_E2_V1)))
 (let (($x59788 (not W_S1_V1)))
 (let
 (($x64788
   (or DISJ_W_S1_R_S1 $x59788 
   (and (not W_S1_V3) (not W_S1_V4))
   (and (not R_S1_V1) (not R_S1_V3) (not W_S1_V4)))))
 (let (($x59796 (not W_S1_V2)))
 (let (($x115 (not R_E1_V3)))
 (let (($x113 (not R_E1_V1)))
 (let (($x130 (not R_E2_V3)))
 (and DISJ_W_S1_R_E1 $x130 $x113 $x115 $x59796 $x64788 W_S1_V5
 (= DISJ_W_S1_R_E2 (not (or $x10 R_E2_V5 $x21 $x24)))
 (= DISJ_W_S1_R_S1 (not (or $x30 $x32 R_S1_V5 $x38 $x40))) $x119 $x61276
 (not (and W_S1_V4 R_E1_V4)) 
 (not (and W_S1_V6 R_E1_V6)))))))))))))))))))
(assert (not DISJ_W_S1_R_S1))
(assert W_S1_V1)
(check-sat)
(exit)

