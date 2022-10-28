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
(declare-fun W_S2_V1 () Bool)
(declare-fun W_S2_V3 () Bool)
(declare-fun W_S2_V2 () Bool)
(declare-fun W_S2_V5 () Bool)
(declare-fun W_S2_V6 () Bool)
(declare-fun W_S3_V1 () Bool)
(declare-fun W_S3_V3 () Bool)
(declare-fun W_S3_V2 () Bool)
(declare-fun W_S3_V4 () Bool)
(declare-fun W_S3_V6 () Bool)
(declare-fun W_S1_V1 () Bool)
(declare-fun W_S1_V2 () Bool)
(declare-fun W_S1_V5 () Bool)
(declare-fun W_S1_V4 () Bool)
(declare-fun W_S1_V6 () Bool)
(declare-fun R_S2_V1 () Bool)
(declare-fun R_S2_V3 () Bool)
(declare-fun R_S2_V2 () Bool)
(declare-fun R_S2_V5 () Bool)
(declare-fun R_S2_V4 () Bool)
(declare-fun R_S2_V6 () Bool)
(declare-fun R_E1_V1 () Bool)
(declare-fun R_E1_V3 () Bool)
(declare-fun R_E1_V2 () Bool)
(declare-fun R_E1_V5 () Bool)
(declare-fun R_E1_V4 () Bool)
(declare-fun R_E1_V6 () Bool)
(declare-fun DISJ_W_S2_W_S3 () Bool)
(declare-fun DISJ_W_S2_R_S2 () Bool)
(declare-fun DISJ_W_S2_R_E1 () Bool)
(declare-fun R_S3_V1 () Bool)
(declare-fun R_S3_V3 () Bool)
(declare-fun R_S3_V2 () Bool)
(declare-fun R_S3_V5 () Bool)
(declare-fun R_S3_V4 () Bool)
(declare-fun R_S3_V6 () Bool)
(declare-fun DISJ_W_S2_R_S3 () Bool)
(declare-fun R_S1_V1 () Bool)
(declare-fun R_S1_V3 () Bool)
(declare-fun R_S1_V2 () Bool)
(declare-fun R_S1_V5 () Bool)
(declare-fun R_S1_V4 () Bool)
(declare-fun R_S1_V6 () Bool)
(declare-fun DISJ_W_S2_R_S1 () Bool)
(declare-fun R_B1_V1 () Bool)
(declare-fun R_B1_V3 () Bool)
(declare-fun R_B1_V2 () Bool)
(declare-fun R_B1_V5 () Bool)
(declare-fun R_B1_V4 () Bool)
(declare-fun R_B1_V6 () Bool)
(declare-fun DISJ_W_S2_R_B1 () Bool)
(declare-fun DISJ_W_S3_R_S2 () Bool)
(declare-fun DISJ_W_S3_R_E1 () Bool)
(declare-fun DISJ_W_S3_R_S3 () Bool)
(declare-fun DISJ_W_S3_R_S1 () Bool)
(declare-fun DISJ_W_S3_R_B1 () Bool)
(declare-fun DISJ_W_S1_W_S2 () Bool)
(declare-fun DISJ_W_S1_W_S3 () Bool)
(declare-fun DISJ_W_S1_R_S2 () Bool)
(declare-fun DISJ_W_S1_R_E1 () Bool)
(declare-fun DISJ_W_S1_R_S3 () Bool)
(declare-fun DISJ_W_S1_R_S1 () Bool)
(declare-fun DISJ_W_S1_R_B1 () Bool)
(declare-fun W_S2_V4 () Bool)
(declare-fun W_S3_V5 () Bool)
(declare-fun W_S1_V3 () Bool)
(assert
 (let
 (($x1313
   (forall
    ((V6_0 Int) (V4_0 Int) 
     (V5_0 Int) (V2_0 Int) 
     (V3_0 Int) (V1_0 Int) 
     (MW_S1_V6 Bool) (MW_S1_V4 Bool) 
     (MW_S1_V5 Bool) (MW_S1_V2 Bool) 
     (MW_S1_V3 Bool) (MW_S1_V1 Bool) 
     (MW_S3_V6 Bool) (MW_S3_V4 Bool) 
     (MW_S3_V2 Bool) (MW_S3_V3 Bool) 
     (MW_S3_V1 Bool) (MW_S2_V6 Bool) 
     (MW_S2_V4 Bool) (MW_S2_V5 Bool) 
     (MW_S2_V2 Bool) (MW_S2_V3 Bool) 
     (MW_S2_V1 Bool) (S1_V4_!47 Int) 
     (S1_V4_!62 Int) (S2_V4_!54 Int) 
     (S2_V4_!69 Int) (S1_V6_!46 Int) 
     (S1_V6_!61 Int) (E1_!52 Int) 
     (E1_!59 Int) (E1_!67 Int) 
     (S1_V1_!51 Int) (S1_V1_!66 Int) 
     (S2_V6_!53 Int) (S2_V6_!68 Int) 
     (S1_V3_!50 Int) (S1_V3_!65 Int) 
     (S1_V2_!49 Int) (S1_V2_!64 Int) 
     (B1_!45 Bool) (B1_!60 Bool) 
     (S2_V1_!58 Int) (S2_V1_!73 Int) 
     (S2_V5_!55 Int) (S2_V5_!70 Int) 
     (S2_V2_!56 Int) (S2_V2_!71 Int) 
     (S2_V3_!57 Int) (S2_V3_!72 Int) 
     (S1_V5_!48 Int) (S1_V5_!63 Int))
    (let ((?x1221 (ite MW_S1_V3 S1_V3_!50 V3_0)))
    (let ((?x1240 (ite MW_S2_V3 S2_V3_!57 ?x1221)))
    (let ((?x1214 (ite MW_S1_V5 S1_V5_!48 V5_0)))
    (let ((?x1234 (ite MW_S2_V5 S2_V5_!55 ?x1214)))
    (let ((?x1210 (ite MW_S1_V4 S1_V4_!47 V4_0)))
    (let ((?x1231 (ite MW_S2_V4 S2_V4_!54 ?x1210)))
    (let ((?x1206 (ite MW_S1_V6 S1_V6_!46 V6_0)))
    (let ((?x1228 (ite MW_S2_V6 S2_V6_!53 ?x1206)))
    (let
    (($x1311
      (and (= ?x1228 (ite MW_S2_V6 S2_V6_!68 (ite MW_S1_V6 S1_V6_!61 V6_0)))
      (= ?x1231 (ite MW_S2_V4 S2_V4_!69 (ite MW_S1_V4 S1_V4_!62 V4_0)))
      (= ?x1234 (ite MW_S2_V5 S2_V5_!70 (ite MW_S1_V5 S1_V5_!63 V5_0)))
      (= E1_!59 (ite MW_S2_V2 S2_V2_!71 E1_!67))
      (= ?x1240 (ite MW_S2_V3 S2_V3_!72 (ite MW_S1_V3 S1_V3_!65 V3_0)))
      (= (ite MW_S2_V1 S2_V1_!58 E1_!52) (ite MW_S2_V1 S2_V1_!73 E1_!67)))))
    (let
    (($x1225
      (and (or (not R_S2_V6) (= ?x1206 (ite MW_S1_V6 S1_V6_!61 V6_0)))
      (or (not R_S2_V4) (= ?x1210 (ite MW_S1_V4 S1_V4_!62 V4_0)))
      (or (not R_S2_V5) (= ?x1214 (ite MW_S1_V5 S1_V5_!63 V5_0)))
      (or (not R_S2_V2) (= (ite MW_S1_V2 S1_V2_!49 V2_0) E1_!67))
      (or (not R_S2_V3) (= ?x1221 (ite MW_S1_V3 S1_V3_!65 V3_0)))
      (or (not R_S2_V1) (= E1_!52 E1_!67)))))
    (let (($x1226 (not $x1225)))
    (let
    (($x1287
      (and (or (not R_S2_V6) (= (ite MW_S1_V6 S1_V6_!61 V6_0) ?x1206))
      (or (not R_S2_V4) (= (ite MW_S1_V4 S1_V4_!62 V4_0) ?x1210))
      (or (not R_S2_V5) (= (ite MW_S1_V5 S1_V5_!63 V5_0) ?x1214))
      (or (not R_S2_V2) (= E1_!67 (ite MW_S1_V2 S1_V2_!49 V2_0)))
      (or (not R_S2_V3) (= (ite MW_S1_V3 S1_V3_!65 V3_0) ?x1221))
      (or (not R_S2_V1) (= E1_!67 E1_!52)))))
    (let (($x1288 (not $x1287)))
    (let
    (($x1278
      (and (or (not R_E1_V6) (= (ite MW_S1_V6 S1_V6_!61 V6_0) ?x1206))
      (or (not R_E1_V4) (= (ite MW_S1_V4 S1_V4_!62 V4_0) ?x1210))
      (or (not R_E1_V5) (= (ite MW_S1_V5 S1_V5_!63 V5_0) ?x1214))
      (or (not R_E1_V2)
      (= (ite MW_S1_V2 S1_V2_!64 V2_0) (ite MW_S1_V2 S1_V2_!49 V2_0)))
      (or (not R_E1_V3) (= (ite MW_S1_V3 S1_V3_!65 V3_0) ?x1221))
      (or (not R_E1_V1)
      (= (ite MW_S1_V1 S1_V1_!66 V1_0) (ite MW_S1_V1 S1_V1_!51 V1_0))))))
    (let (($x401 (not R_E1_V2)))
    (let
    (($x1257
      (or $x401
      (= (ite MW_S2_V2 S2_V2_!56 (ite MW_S1_V2 S1_V2_!49 V2_0))
      (ite MW_S1_V2 S1_V2_!64 V2_0)))))
    (let
    (($x1263
      (and (or (not R_E1_V6) (= ?x1228 (ite MW_S1_V6 S1_V6_!61 V6_0)))
      (or (not R_E1_V4) (= ?x1231 (ite MW_S1_V4 S1_V4_!62 V4_0)))
      (or (not R_E1_V5) (= ?x1234 (ite MW_S1_V5 S1_V5_!63 V5_0))) $x1257
      (or (not R_E1_V3) (= ?x1240 (ite MW_S1_V3 S1_V3_!65 V3_0)))
      (or (not R_E1_V1)
      (= (ite MW_S2_V1 S2_V1_!58 E1_!52) (ite MW_S1_V1 S1_V1_!66 V1_0))))))
    (let
    (($x1239
      (or $x401
      (= (ite MW_S2_V2 S2_V2_!56 (ite MW_S1_V2 S1_V2_!49 V2_0))
      (ite MW_S1_V2 S1_V2_!49 V2_0)))))
    (let
    (($x1246
      (and (or (not R_E1_V6) (= ?x1228 ?x1206))
      (or (not R_E1_V4) (= ?x1231 ?x1210))
      (or (not R_E1_V5) (= ?x1234 ?x1214)) $x1239
      (or (not R_E1_V3) (= ?x1240 ?x1221))
      (or (not R_E1_V1)
      (= (ite MW_S2_V1 S2_V1_!58 E1_!52) (ite MW_S1_V1 S1_V1_!51 V1_0))))))
    (let
    (($x1301
      (and (= S1_V4_!47 S1_V4_!62) 
      (or $x1226 (= S2_V4_!54 S2_V4_!69)) 
      (= S1_V6_!61 S1_V6_!46) 
      (or (not $x1246) (= E1_!59 E1_!52)) 
      (or (not $x1263) (= E1_!59 E1_!67)) 
      (or (not $x1278) (= E1_!67 E1_!52)) 
      (= S1_V1_!66 S1_V1_!51) 
      (or $x1288 (= S2_V6_!68 S2_V6_!53)) 
      (= S1_V3_!65 S1_V3_!50) 
      (= S1_V2_!64 S1_V2_!49) 
      (= B1_!60 B1_!45) (or $x1288 (= S2_V1_!73 S2_V1_!58))
      (or $x1288 (= S2_V5_!70 S2_V5_!55)) 
      (or $x1226 (= S2_V2_!56 S2_V2_!71)) 
      (or $x1226 (= S2_V3_!57 S2_V3_!72)) 
      (= S1_V5_!63 S1_V5_!48) 
      (or (not MW_S1_V6) W_S1_V6) 
      (or (not MW_S1_V4) W_S1_V4) 
      (or (not MW_S1_V5) W_S1_V5) 
      (or (not MW_S1_V2) W_S1_V2) 
      (or (not MW_S1_V1) W_S1_V1) 
      (or (not MW_S3_V6) W_S3_V6) 
      (or (not MW_S3_V4) W_S3_V4) 
      (or (not MW_S3_V2) W_S3_V2) 
      (or (not MW_S3_V3) W_S3_V3) 
      (or (not MW_S3_V1) W_S3_V1) 
      (or (not MW_S2_V6) W_S2_V6) 
      (or (not MW_S2_V5) W_S2_V5) 
      (or (not MW_S2_V2) W_S2_V2) 
      (or (not MW_S2_V3) W_S2_V3) 
      (or (not MW_S2_V1) W_S2_V1))))
    (or (not $x1301) (not (and B1_!45 B1_!60)) $x1311))))))))))))))))))))))))
 (let
 (($x289
   (or (and W_S2_V6 W_S3_V6) W_S3_V4 W_S2_V5 
   (and W_S2_V2 W_S3_V2) (and W_S2_V3 W_S3_V3) 
   (and W_S2_V1 W_S3_V1))))
 (let (($x291 (= DISJ_W_S2_W_S3 (not $x289))))
 (let
 (($x286
   (or (and W_S2_V6 R_S2_V6) R_S2_V4 
   (and W_S2_V5 R_S2_V5) (and W_S2_V2 R_S2_V2) 
   (and W_S2_V3 R_S2_V3) (and W_S2_V1 R_S2_V1))))
 (let (($x288 (= DISJ_W_S2_R_S2 (not $x286))))
 (let
 (($x283
   (or (and W_S2_V6 R_E1_V6) R_E1_V4 
   (and W_S2_V5 R_E1_V5) (and W_S2_V2 R_E1_V2) 
   (and W_S2_V3 R_E1_V3) (and W_S2_V1 R_E1_V1))))
 (let (($x285 (= DISJ_W_S2_R_E1 (not $x283))))
 (let
 (($x280
   (or (and W_S2_V6 R_S3_V6) R_S3_V4 
   (and W_S2_V5 R_S3_V5) (and W_S2_V2 R_S3_V2) 
   (and W_S2_V3 R_S3_V3) (and W_S2_V1 R_S3_V1))))
 (let (($x282 (= DISJ_W_S2_R_S3 (not $x280))))
 (let
 (($x277
   (or (and W_S2_V6 R_S1_V6) R_S1_V4 
   (and W_S2_V5 R_S1_V5) (and W_S2_V2 R_S1_V2) 
   (and W_S2_V3 R_S1_V3) (and W_S2_V1 R_S1_V1))))
 (let (($x279 (= DISJ_W_S2_R_S1 (not $x277))))
 (let
 (($x274
   (or (and W_S2_V6 R_B1_V6) R_B1_V4 
   (and W_S2_V5 R_B1_V5) (and W_S2_V2 R_B1_V2) 
   (and W_S2_V3 R_B1_V3) (and W_S2_V1 R_B1_V1))))
 (let (($x276 (= DISJ_W_S2_R_B1 (not $x274))))
 (let
 (($x271
   (or (and W_S3_V6 R_S2_V6) 
   (and W_S3_V4 R_S2_V4) R_S2_V5 
   (and W_S3_V2 R_S2_V2) (and W_S3_V3 R_S2_V3) 
   (and W_S3_V1 R_S2_V1))))
 (let (($x273 (= DISJ_W_S3_R_S2 (not $x271))))
 (let
 (($x268
   (or (and W_S3_V6 R_E1_V6) 
   (and W_S3_V4 R_E1_V4) R_E1_V5 
   (and W_S3_V2 R_E1_V2) (and W_S3_V3 R_E1_V3) 
   (and W_S3_V1 R_E1_V1))))
 (let (($x270 (= DISJ_W_S3_R_E1 (not $x268))))
 (let
 (($x265
   (or (and W_S3_V6 R_S3_V6) 
   (and W_S3_V4 R_S3_V4) R_S3_V5 
   (and W_S3_V2 R_S3_V2) (and W_S3_V3 R_S3_V3) 
   (and W_S3_V1 R_S3_V1))))
 (let (($x267 (= DISJ_W_S3_R_S3 (not $x265))))
 (let
 (($x262
   (or (and W_S3_V6 R_S1_V6) 
   (and W_S3_V4 R_S1_V4) R_S1_V5 
   (and W_S3_V2 R_S1_V2) (and W_S3_V3 R_S1_V3) 
   (and W_S3_V1 R_S1_V1))))
 (let (($x264 (= DISJ_W_S3_R_S1 (not $x262))))
 (let
 (($x259
   (or (and W_S3_V6 R_B1_V6) 
   (and W_S3_V4 R_B1_V4) R_B1_V5 
   (and W_S3_V2 R_B1_V2) (and W_S3_V3 R_B1_V3) 
   (and W_S3_V1 R_B1_V1))))
 (let (($x261 (= DISJ_W_S3_R_B1 (not $x259))))
 (let
 (($x256
   (or (and W_S1_V6 W_S2_V6) W_S1_V4 
   (and W_S1_V5 W_S2_V5) (and W_S1_V2 W_S2_V2) W_S2_V3 
   (and W_S1_V1 W_S2_V1))))
 (let (($x258 (= DISJ_W_S1_W_S2 (not $x256))))
 (let
 (($x253
   (or (and W_S1_V6 W_S3_V6) 
   (and W_S1_V4 W_S3_V4) W_S1_V5 
   (and W_S1_V2 W_S3_V2) W_S3_V3 
   (and W_S1_V1 W_S3_V1))))
 (let (($x255 (= DISJ_W_S1_W_S3 (not $x253))))
 (let
 (($x250
   (or (and W_S1_V6 R_S2_V6) 
   (and W_S1_V4 R_S2_V4) (and W_S1_V5 R_S2_V5) 
   (and W_S1_V2 R_S2_V2) R_S2_V3 
   (and W_S1_V1 R_S2_V1))))
 (let (($x252 (= DISJ_W_S1_R_S2 (not $x250))))
 (let
 (($x247
   (or (and W_S1_V6 R_E1_V6) 
   (and W_S1_V4 R_E1_V4) (and W_S1_V5 R_E1_V5) 
   (and W_S1_V2 R_E1_V2) R_E1_V3 
   (and W_S1_V1 R_E1_V1))))
 (let (($x249 (= DISJ_W_S1_R_E1 (not $x247))))
 (let
 (($x244
   (or (and W_S1_V6 R_S3_V6) 
   (and W_S1_V4 R_S3_V4) (and W_S1_V5 R_S3_V5) 
   (and W_S1_V2 R_S3_V2) R_S3_V3 
   (and W_S1_V1 R_S3_V1))))
 (let (($x246 (= DISJ_W_S1_R_S3 (not $x244))))
 (let
 (($x241
   (or (and W_S1_V6 R_S1_V6) 
   (and W_S1_V4 R_S1_V4) (and W_S1_V5 R_S1_V5) 
   (and W_S1_V2 R_S1_V2) R_S1_V3 
   (and W_S1_V1 R_S1_V1))))
 (let (($x243 (= DISJ_W_S1_R_S1 (not $x241))))
 (let
 (($x238
   (or (and W_S1_V6 R_B1_V6) 
   (and W_S1_V4 R_B1_V4) (and W_S1_V5 R_B1_V5) 
   (and W_S1_V2 R_B1_V2) R_B1_V3 
   (and W_S1_V1 R_B1_V1))))
 (let (($x240 (= DISJ_W_S1_R_B1 (not $x238))))
 (and W_S1_V3 W_S3_V5 W_S2_V4 $x240 $x243 $x246 $x249 $x252 $x255 $x258 $x261
 $x264 $x267 $x270 $x273 $x276 $x279 $x282 $x285 $x288 $x291 $x1313)))))))))))))))))))))))))))))))))))))))
(check-sat)
(exit)

