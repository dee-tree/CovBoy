(set-info :smt-lib-version 2.6)
(set-logic QF_ALIA)
(set-info :source |
Benchmarks from Leonardo de Moura <demoura@csl.sri.com>

This benchmark was automatically translated into SMT-LIB format from
CVC format using CVC Lite
|)
(set-info :category "industrial")
(set-info :status sat)
(declare-fun x_0 () Int)
(declare-fun x_1 () Int)
(declare-fun x_2 () (Array Int Int))
(declare-fun x_3 () Int)
(declare-fun x_4 () Int)
(declare-fun x_5 () Bool)
(declare-fun x_6 () (Array Int Int))
(declare-fun x_7 () (Array Int Int))
(declare-fun x_8 () Int)
(declare-fun x_9 () Bool)
(declare-fun x_10 () Int)
(declare-fun x_11 () Int)
(declare-fun x_12 () Int)
(declare-fun x_13 () Int)
(declare-fun x_14 () (Array Int Int))
(declare-fun x_15 () Int)
(declare-fun x_16 () Int)
(declare-fun x_17 () Int)
(declare-fun x_18 () Int)
(declare-fun x_19 () (Array Int Int))
(declare-fun x_20 () Int)
(declare-fun x_21 () Bool)
(declare-fun x_22 () Int)
(declare-fun x_23 () Int)
(declare-fun x_24 () Int)
(declare-fun x_25 () Int)
(declare-fun x_26 () (Array Int Int))
(declare-fun x_27 () Int)
(declare-fun x_28 () Int)
(declare-fun x_29 () Int)
(declare-fun x_30 () Int)
(declare-fun x_31 () (Array Int Int))
(declare-fun x_32 () Int)
(declare-fun x_33 () Bool)
(declare-fun x_34 () Int)
(declare-fun x_35 () Int)
(declare-fun x_36 () Int)
(declare-fun x_37 () Int)
(declare-fun x_38 () (Array Int Int))
(declare-fun x_39 () Int)
(declare-fun x_40 () Int)
(declare-fun x_41 () Int)
(declare-fun x_42 () Int)
(declare-fun x_43 () (Array Int Int))
(declare-fun x_44 () Int)
(declare-fun x_45 () Bool)
(declare-fun x_46 () Int)
(declare-fun x_47 () Int)
(declare-fun x_48 () Int)
(declare-fun x_49 () Int)
(declare-fun x_50 () (Array Int Int))
(declare-fun x_51 () Int)
(declare-fun x_52 () Int)
(declare-fun x_53 () Int)
(declare-fun x_54 () Int)
(declare-fun x_55 () (Array Int Int))
(declare-fun x_56 () Int)
(declare-fun x_57 () Bool)
(declare-fun x_58 () Int)
(declare-fun x_59 () Int)
(declare-fun x_60 () Int)
(declare-fun x_61 () Int)
(declare-fun x_62 () (Array Int Int))
(declare-fun x_63 () Int)
(declare-fun x_64 () Int)
(declare-fun x_65 () Int)
(declare-fun x_66 () Int)
(declare-fun x_67 () (Array Int Int))
(declare-fun x_68 () Int)
(declare-fun x_69 () Bool)
(declare-fun x_70 () Int)
(declare-fun x_71 () Int)
(declare-fun x_72 () Int)
(declare-fun x_73 () Int)
(declare-fun x_74 () (Array Int Int))
(declare-fun x_75 () Int)
(declare-fun x_76 () Int)
(declare-fun x_77 () Int)
(declare-fun x_78 () Int)
(declare-fun x_79 () (Array Int Int))
(declare-fun x_80 () Int)
(declare-fun x_81 () Bool)
(declare-fun x_82 () Int)
(declare-fun x_83 () Int)
(declare-fun x_84 () Int)
(declare-fun x_85 () Int)
(declare-fun x_86 () (Array Int Int))
(declare-fun x_87 () Int)
(declare-fun x_88 () Int)
(declare-fun x_89 () Int)
(declare-fun x_90 () Int)
(declare-fun x_91 () (Array Int Int))
(declare-fun x_92 () Int)
(declare-fun x_93 () Bool)
(declare-fun x_94 () Int)
(declare-fun x_95 () Int)
(declare-fun x_96 () Int)
(declare-fun x_97 () Int)
(declare-fun x_98 () (Array Int Int))
(declare-fun x_99 () Int)
(declare-fun x_100 () Int)
(declare-fun x_101 () Int)
(declare-fun x_102 () Int)
(declare-fun x_103 () (Array Int Int))
(declare-fun x_104 () Int)
(declare-fun x_105 () Bool)
(declare-fun x_106 () Int)
(declare-fun x_107 () Int)
(declare-fun x_108 () Int)
(declare-fun x_109 () Int)
(declare-fun x_110 () (Array Int Int))
(declare-fun x_111 () Int)
(declare-fun x_112 () Int)
(declare-fun x_113 () Int)
(declare-fun x_114 () Int)
(declare-fun x_115 () (Array Int Int))
(declare-fun x_116 () Int)
(declare-fun x_117 () Bool)
(declare-fun x_118 () Int)
(declare-fun x_119 () Int)
(declare-fun x_120 () Int)
(declare-fun x_121 () Int)
(declare-fun x_122 () (Array Int Int))
(declare-fun x_123 () Int)
(declare-fun x_124 () Int)
(declare-fun x_125 () Int)
(declare-fun x_126 () Int)
(declare-fun x_127 () (Array Int Int))
(declare-fun x_128 () Int)
(declare-fun x_129 () Bool)
(declare-fun x_130 () Int)
(declare-fun x_131 () Int)
(declare-fun x_132 () Int)
(declare-fun x_133 () Int)
(declare-fun x_134 () (Array Int Int))
(declare-fun x_135 () Int)
(declare-fun x_136 () Int)
(declare-fun x_137 () Int)
(declare-fun x_138 () Int)
(declare-fun x_139 () (Array Int Int))
(declare-fun x_140 () Int)
(declare-fun x_141 () Bool)
(declare-fun x_142 () Int)
(declare-fun x_143 () Int)
(declare-fun x_144 () Int)
(declare-fun x_145 () Int)
(declare-fun x_146 () (Array Int Int))
(declare-fun x_147 () Int)
(declare-fun x_148 () Int)
(declare-fun x_149 () Int)
(declare-fun x_150 () Int)
(declare-fun x_151 () (Array Int Int))
(declare-fun x_152 () Int)
(declare-fun x_153 () Bool)
(declare-fun x_154 () Int)
(declare-fun x_155 () Int)
(declare-fun x_156 () Int)
(declare-fun x_157 () Int)
(declare-fun x_158 () (Array Int Int))
(declare-fun x_159 () Int)
(declare-fun x_160 () Int)
(declare-fun x_161 () Int)
(declare-fun x_162 () Int)
(declare-fun x_163 () (Array Int Int))
(declare-fun x_164 () Int)
(declare-fun x_165 () Bool)
(declare-fun x_166 () Int)
(declare-fun x_167 () Int)
(declare-fun x_168 () Int)
(declare-fun x_169 () Int)
(declare-fun x_170 () (Array Int Int))
(declare-fun x_171 () Int)
(declare-fun x_172 () Int)
(declare-fun x_173 () Int)
(declare-fun x_174 () Int)
(declare-fun x_175 () (Array Int Int))
(declare-fun x_176 () Int)
(declare-fun x_177 () Bool)
(declare-fun x_178 () Int)
(declare-fun x_179 () Int)
(declare-fun x_180 () Int)
(declare-fun x_181 () Int)
(declare-fun x_182 () (Array Int Int))
(declare-fun x_183 () Int)
(declare-fun x_184 () Int)
(declare-fun x_185 () Int)
(declare-fun x_186 () Int)
(declare-fun x_187 () (Array Int Int))
(declare-fun x_188 () Int)
(declare-fun x_189 () Bool)
(declare-fun x_190 () Int)
(declare-fun x_191 () Int)
(declare-fun x_192 () Int)
(declare-fun x_193 () Int)
(declare-fun x_194 () (Array Int Int))
(declare-fun x_195 () Int)
(declare-fun x_196 () Int)
(declare-fun x_197 () Int)
(declare-fun x_198 () Int)
(declare-fun x_199 () (Array Int Int))
(declare-fun x_200 () Int)
(declare-fun x_201 () Bool)
(declare-fun x_202 () Int)
(declare-fun x_203 () Int)
(declare-fun x_204 () Int)
(declare-fun x_205 () Int)
(declare-fun x_206 () (Array Int Int))
(declare-fun x_207 () Int)
(declare-fun x_208 () Int)
(declare-fun x_209 () Int)
(declare-fun x_210 () Int)
(declare-fun x_211 () (Array Int Int))
(declare-fun x_212 () Int)
(declare-fun x_213 () Bool)
(declare-fun x_214 () Int)
(declare-fun x_215 () Int)
(declare-fun x_216 () Int)
(declare-fun x_217 () Int)
(declare-fun x_218 () (Array Int Int))
(declare-fun x_219 () Int)
(declare-fun x_220 () Int)
(declare-fun x_221 () Int)
(declare-fun x_222 () Int)
(declare-fun x_223 () (Array Int Int))
(declare-fun x_224 () Int)
(declare-fun x_225 () Bool)
(declare-fun x_226 () Int)
(declare-fun x_227 () Int)
(declare-fun x_228 () Int)
(declare-fun x_229 () Int)
(declare-fun x_230 () (Array Int Int))
(declare-fun x_231 () Int)
(declare-fun x_232 () Int)
(declare-fun x_233 () Int)
(declare-fun x_234 () Int)
(declare-fun x_235 () (Array Int Int))
(declare-fun x_236 () Int)
(declare-fun x_237 () Bool)
(declare-fun x_238 () Int)
(declare-fun x_239 () Int)
(declare-fun x_240 () Int)
(declare-fun x_241 () Int)
(declare-fun x_242 () (Array Int Int))
(declare-fun x_243 () Int)
(declare-fun x_244 () Int)
(declare-fun x_245 () Int)
(declare-fun x_246 () Int)
(declare-fun x_247 () Int)
(declare-fun x_248 () Int)
(declare-fun x_249 () Int)
(declare-fun x_250 () Int)
(declare-fun x_251 () Int)
(declare-fun x_252 () Int)
(declare-fun x_253 () Int)
(declare-fun x_254 () Int)
(declare-fun x_255 () Int)
(declare-fun x_256 () Int)
(declare-fun x_257 () Int)
(declare-fun x_258 () Int)
(declare-fun x_259 () Int)
(declare-fun x_260 () Int)
(declare-fun x_261 () Int)
(declare-fun x_262 () Int)
(declare-fun x_263 () Int)
(declare-fun x_264 () Int)
(declare-fun x_265 () Int)
(declare-fun x_266 () Int)
(declare-fun x_267 () Int)
(declare-fun x_268 () Int)
(declare-fun x_269 () Int)
(declare-fun x_270 () Int)
(declare-fun x_271 () Int)
(declare-fun x_272 () Int)
(declare-fun x_273 () Int)
(declare-fun x_274 () Int)
(declare-fun x_275 () Int)
(declare-fun x_276 () Int)
(declare-fun x_277 () Int)
(declare-fun x_278 () Int)
(declare-fun x_279 () Int)
(declare-fun x_280 () Int)
(declare-fun x_281 () Int)
(declare-fun x_282 () Int)
(declare-fun x_283 () Int)
(declare-fun x_284 () Int)
(declare-fun x_285 () Int)
(declare-fun x_286 () Int)
(declare-fun x_287 () Int)
(declare-fun x_288 () Int)
(declare-fun x_289 () Int)
(declare-fun x_290 () Int)
(declare-fun x_291 () Int)
(declare-fun x_292 () Int)
(declare-fun x_293 () Int)
(declare-fun x_294 () Int)
(declare-fun x_295 () Int)
(declare-fun x_296 () Int)
(declare-fun x_297 () Int)
(declare-fun x_298 () Int)
(declare-fun x_299 () Int)
(declare-fun x_300 () Int)
(declare-fun x_301 () Int)
(declare-fun x_302 () Int)
(declare-fun x_303 () Int)
(declare-fun x_304 () Int)
(declare-fun x_305 () Int)
(declare-fun x_306 () Int)
(declare-fun x_307 () Int)
(declare-fun x_308 () Int)
(declare-fun x_309 () Int)
(declare-fun x_310 () Int)
(declare-fun x_311 () Int)
(declare-fun x_312 () Int)
(declare-fun x_313 () Int)
(declare-fun x_314 () Int)
(declare-fun x_315 () Int)
(declare-fun x_316 () Int)
(declare-fun x_317 () Int)
(declare-fun x_318 () Int)
(declare-fun x_319 () Int)
(declare-fun x_320 () Int)
(declare-fun x_321 () Int)
(declare-fun x_322 () Int)
(declare-fun x_323 () Int)
(declare-fun x_324 () Int)
(declare-fun x_325 () Int)
(declare-fun x_326 () Int)
(declare-fun x_327 () Int)
(declare-fun x_328 () Int)
(declare-fun x_329 () Int)
(declare-fun x_330 () Int)
(declare-fun x_331 () Int)
(declare-fun x_332 () Int)
(declare-fun x_333 () Int)
(declare-fun x_334 () Int)
(declare-fun x_335 () Int)
(declare-fun x_336 () Int)
(declare-fun x_337 () Int)
(declare-fun x_338 () Int)
(declare-fun x_339 () Int)
(declare-fun x_340 () Int)
(declare-fun x_341 () Int)
(declare-fun x_342 () Int)
(declare-fun x_343 () Int)
(declare-fun x_344 () Int)
(declare-fun x_345 () Int)
(declare-fun x_346 () Int)
(declare-fun x_347 () Int)
(declare-fun x_348 () Int)
(declare-fun x_349 () Int)
(declare-fun x_350 () Int)
(declare-fun x_351 () Int)
(declare-fun x_352 () Int)
(declare-fun x_353 () Int)
(declare-fun x_354 () Int)
(declare-fun x_355 () Int)
(declare-fun x_356 () Int)
(declare-fun x_357 () Int)
(declare-fun x_358 () Int)
(declare-fun x_359 () Int)
(declare-fun x_360 () Int)
(declare-fun x_361 () Int)
(declare-fun x_362 () Int)
(declare-fun x_363 () Int)
(declare-fun x_364 () Int)
(declare-fun x_365 () Int)
(declare-fun x_366 () Int)
(declare-fun x_367 () Int)
(declare-fun x_368 () Int)
(declare-fun x_369 () Int)
(declare-fun x_370 () Int)
(assert (let ((?v_98 (= x_6 x_7)) (?v_95 (= x_8 x_0)) (?v_96 (= x_9 x_5)) (?v_99 (= x_10 x_1)) (?v_97 (not (<= x_1 x_0))) (?v_93 (= x_19 x_6)) (?v_90 (= x_20 x_8)) (?v_91 (= x_21 x_9)) (?v_94 (= x_22 x_10)) (?v_92 (not (<= x_10 x_8))) (?v_88 (= x_31 x_19)) (?v_85 (= x_32 x_20)) (?v_86 (= x_33 x_21)) (?v_89 (= x_34 x_22)) (?v_87 (not (<= x_22 x_20))) (?v_83 (= x_43 x_31)) (?v_80 (= x_44 x_32)) (?v_81 (= x_45 x_33)) (?v_84 (= x_46 x_34)) (?v_82 (not (<= x_34 x_32))) (?v_78 (= x_55 x_43)) (?v_75 (= x_56 x_44)) (?v_76 (= x_57 x_45)) (?v_79 (= x_58 x_46)) (?v_77 (not (<= x_46 x_44))) (?v_73 (= x_67 x_55)) (?v_70 (= x_68 x_56)) (?v_71 (= x_69 x_57)) (?v_74 (= x_70 x_58)) (?v_72 (not (<= x_58 x_56))) (?v_68 (= x_79 x_67)) (?v_65 (= x_80 x_68)) (?v_66 (= x_81 x_69)) (?v_69 (= x_82 x_70)) (?v_67 (not (<= x_70 x_68))) (?v_63 (= x_91 x_79)) (?v_60 (= x_92 x_80)) (?v_61 (= x_93 x_81)) (?v_64 (= x_94 x_82)) (?v_62 (not (<= x_82 x_80))) (?v_58 (= x_103 x_91)) (?v_55 (= x_104 x_92)) (?v_56 (= x_105 x_93)) (?v_59 (= x_106 x_94)) (?v_57 (not (<= x_94 x_92))) (?v_53 (= x_115 x_103)) (?v_50 (= x_116 x_104)) (?v_51 (= x_117 x_105)) (?v_54 (= x_118 x_106)) (?v_52 (not (<= x_106 x_104))) (?v_48 (= x_127 x_115)) (?v_45 (= x_128 x_116)) (?v_46 (= x_129 x_117)) (?v_49 (= x_130 x_118)) (?v_47 (not (<= x_118 x_116))) (?v_43 (= x_139 x_127)) (?v_40 (= x_140 x_128)) (?v_41 (= x_141 x_129)) (?v_44 (= x_142 x_130)) (?v_42 (not (<= x_130 x_128))) (?v_38 (= x_151 x_139)) (?v_35 (= x_152 x_140)) (?v_36 (= x_153 x_141)) (?v_39 (= x_154 x_142)) (?v_37 (not (<= x_142 x_140))) (?v_33 (= x_163 x_151)) (?v_30 (= x_164 x_152)) (?v_31 (= x_165 x_153)) (?v_34 (= x_166 x_154)) (?v_32 (not (<= x_154 x_152))) (?v_28 (= x_175 x_163)) (?v_25 (= x_176 x_164)) (?v_26 (= x_177 x_165)) (?v_29 (= x_178 x_166)) (?v_27 (not (<= x_166 x_164))) (?v_23 (= x_187 x_175)) (?v_20 (= x_188 x_176)) (?v_21 (= x_189 x_177)) (?v_24 (= x_190 x_178)) (?v_22 (not (<= x_178 x_176))) (?v_18 (= x_199 x_187)) (?v_15 (= x_200 x_188)) (?v_16 (= x_201 x_189)) (?v_19 (= x_202 x_190)) (?v_17 (not (<= x_190 x_188))) (?v_13 (= x_211 x_199)) (?v_10 (= x_212 x_200)) (?v_11 (= x_213 x_201)) (?v_14 (= x_214 x_202)) (?v_12 (not (<= x_202 x_200))) (?v_8 (= x_223 x_211)) (?v_5 (= x_224 x_212)) (?v_6 (= x_225 x_213)) (?v_9 (= x_226 x_214)) (?v_7 (not (<= x_214 x_212))) (?v_3 (= x_235 x_223)) (?v_0 (= x_236 x_224)) (?v_1 (= x_237 x_225)) (?v_4 (= x_238 x_226)) (?v_2 (not (<= x_226 x_224))) (?v_100 (select x_2 x_3)) (?v_101 (select x_2 x_4))) (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (not (= x_4 x_3)) (= x_0 0)) (= x_1 0)) (= x_247 ?v_100)) (= x_247 1)) (= x_248 ?v_101)) (= x_248 1)) x_5) (= x_249 (select x_230 x_240))) (= x_250 (select x_230 x_243))) (= x_251 (select x_223 x_224))) (= x_252 (select x_230 x_245))) (or (or (or (and (and (and (and (and (and (and (= x_239 0) (= x_238 (+ x_226 1))) ?v_0) ?v_1) (= x_241 x_240)) (= x_249 1)) (= x_242 (store x_230 x_240 2))) (= x_235 (store x_223 x_226 x_240))) (and (and (and (and (and (and (and (and (and (= x_239 1) ?v_2) ?v_0) ?v_1) ?v_3) ?v_4) (= x_244 x_243)) (= x_250 2)) (= x_251 x_243)) (= x_242 (store x_230 x_243 3)))) (and (and (and (and (and (and (and (and (= x_239 2) ?v_2) (= x_236 (+ x_224 1))) ?v_1) ?v_3) ?v_4) (= x_246 x_245)) (= x_252 3)) (= x_242 (store x_230 x_245 1)))) (and (and (and (and (and (= x_239 3) ?v_3) ?v_0) ?v_1) (= x_242 x_230)) ?v_4))) (= x_253 (select x_218 x_228))) (= x_254 (select x_218 x_231))) (= x_255 (select x_211 x_212))) (= x_256 (select x_218 x_233))) (or (or (or (and (and (and (and (and (and (and (= x_227 0) (= x_226 (+ x_214 1))) ?v_5) ?v_6) (= x_229 x_228)) (= x_253 1)) (= x_230 (store x_218 x_228 2))) (= x_223 (store x_211 x_214 x_228))) (and (and (and (and (and (and (and (and (and (= x_227 1) ?v_7) ?v_5) ?v_6) ?v_8) ?v_9) (= x_232 x_231)) (= x_254 2)) (= x_255 x_231)) (= x_230 (store x_218 x_231 3)))) (and (and (and (and (and (and (and (and (= x_227 2) ?v_7) (= x_224 (+ x_212 1))) ?v_6) ?v_8) ?v_9) (= x_234 x_233)) (= x_256 3)) (= x_230 (store x_218 x_233 1)))) (and (and (and (and (and (= x_227 3) ?v_8) ?v_5) ?v_6) (= x_230 x_218)) ?v_9))) (= x_257 (select x_206 x_216))) (= x_258 (select x_206 x_219))) (= x_259 (select x_199 x_200))) (= x_260 (select x_206 x_221))) (or (or (or (and (and (and (and (and (and (and (= x_215 0) (= x_214 (+ x_202 1))) ?v_10) ?v_11) (= x_217 x_216)) (= x_257 1)) (= x_218 (store x_206 x_216 2))) (= x_211 (store x_199 x_202 x_216))) (and (and (and (and (and (and (and (and (and (= x_215 1) ?v_12) ?v_10) ?v_11) ?v_13) ?v_14) (= x_220 x_219)) (= x_258 2)) (= x_259 x_219)) (= x_218 (store x_206 x_219 3)))) (and (and (and (and (and (and (and (and (= x_215 2) ?v_12) (= x_212 (+ x_200 1))) ?v_11) ?v_13) ?v_14) (= x_222 x_221)) (= x_260 3)) (= x_218 (store x_206 x_221 1)))) (and (and (and (and (and (= x_215 3) ?v_13) ?v_10) ?v_11) (= x_218 x_206)) ?v_14))) (= x_261 (select x_194 x_204))) (= x_262 (select x_194 x_207))) (= x_263 (select x_187 x_188))) (= x_264 (select x_194 x_209))) (or (or (or (and (and (and (and (and (and (and (= x_203 0) (= x_202 (+ x_190 1))) ?v_15) ?v_16) (= x_205 x_204)) (= x_261 1)) (= x_206 (store x_194 x_204 2))) (= x_199 (store x_187 x_190 x_204))) (and (and (and (and (and (and (and (and (and (= x_203 1) ?v_17) ?v_15) ?v_16) ?v_18) ?v_19) (= x_208 x_207)) (= x_262 2)) (= x_263 x_207)) (= x_206 (store x_194 x_207 3)))) (and (and (and (and (and (and (and (and (= x_203 2) ?v_17) (= x_200 (+ x_188 1))) ?v_16) ?v_18) ?v_19) (= x_210 x_209)) (= x_264 3)) (= x_206 (store x_194 x_209 1)))) (and (and (and (and (and (= x_203 3) ?v_18) ?v_15) ?v_16) (= x_206 x_194)) ?v_19))) (= x_265 (select x_182 x_192))) (= x_266 (select x_182 x_195))) (= x_267 (select x_175 x_176))) (= x_268 (select x_182 x_197))) (or (or (or (and (and (and (and (and (and (and (= x_191 0) (= x_190 (+ x_178 1))) ?v_20) ?v_21) (= x_193 x_192)) (= x_265 1)) (= x_194 (store x_182 x_192 2))) (= x_187 (store x_175 x_178 x_192))) (and (and (and (and (and (and (and (and (and (= x_191 1) ?v_22) ?v_20) ?v_21) ?v_23) ?v_24) (= x_196 x_195)) (= x_266 2)) (= x_267 x_195)) (= x_194 (store x_182 x_195 3)))) (and (and (and (and (and (and (and (and (= x_191 2) ?v_22) (= x_188 (+ x_176 1))) ?v_21) ?v_23) ?v_24) (= x_198 x_197)) (= x_268 3)) (= x_194 (store x_182 x_197 1)))) (and (and (and (and (and (= x_191 3) ?v_23) ?v_20) ?v_21) (= x_194 x_182)) ?v_24))) (= x_269 (select x_170 x_180))) (= x_270 (select x_170 x_183))) (= x_271 (select x_163 x_164))) (= x_272 (select x_170 x_185))) (or (or (or (and (and (and (and (and (and (and (= x_179 0) (= x_178 (+ x_166 1))) ?v_25) ?v_26) (= x_181 x_180)) (= x_269 1)) (= x_182 (store x_170 x_180 2))) (= x_175 (store x_163 x_166 x_180))) (and (and (and (and (and (and (and (and (and (= x_179 1) ?v_27) ?v_25) ?v_26) ?v_28) ?v_29) (= x_184 x_183)) (= x_270 2)) (= x_271 x_183)) (= x_182 (store x_170 x_183 3)))) (and (and (and (and (and (and (and (and (= x_179 2) ?v_27) (= x_176 (+ x_164 1))) ?v_26) ?v_28) ?v_29) (= x_186 x_185)) (= x_272 3)) (= x_182 (store x_170 x_185 1)))) (and (and (and (and (and (= x_179 3) ?v_28) ?v_25) ?v_26) (= x_182 x_170)) ?v_29))) (= x_273 (select x_158 x_168))) (= x_274 (select x_158 x_171))) (= x_275 (select x_151 x_152))) (= x_276 (select x_158 x_173))) (or (or (or (and (and (and (and (and (and (and (= x_167 0) (= x_166 (+ x_154 1))) ?v_30) ?v_31) (= x_169 x_168)) (= x_273 1)) (= x_170 (store x_158 x_168 2))) (= x_163 (store x_151 x_154 x_168))) (and (and (and (and (and (and (and (and (and (= x_167 1) ?v_32) ?v_30) ?v_31) ?v_33) ?v_34) (= x_172 x_171)) (= x_274 2)) (= x_275 x_171)) (= x_170 (store x_158 x_171 3)))) (and (and (and (and (and (and (and (and (= x_167 2) ?v_32) (= x_164 (+ x_152 1))) ?v_31) ?v_33) ?v_34) (= x_174 x_173)) (= x_276 3)) (= x_170 (store x_158 x_173 1)))) (and (and (and (and (and (= x_167 3) ?v_33) ?v_30) ?v_31) (= x_170 x_158)) ?v_34))) (= x_277 (select x_146 x_156))) (= x_278 (select x_146 x_159))) (= x_279 (select x_139 x_140))) (= x_280 (select x_146 x_161))) (or (or (or (and (and (and (and (and (and (and (= x_155 0) (= x_154 (+ x_142 1))) ?v_35) ?v_36) (= x_157 x_156)) (= x_277 1)) (= x_158 (store x_146 x_156 2))) (= x_151 (store x_139 x_142 x_156))) (and (and (and (and (and (and (and (and (and (= x_155 1) ?v_37) ?v_35) ?v_36) ?v_38) ?v_39) (= x_160 x_159)) (= x_278 2)) (= x_279 x_159)) (= x_158 (store x_146 x_159 3)))) (and (and (and (and (and (and (and (and (= x_155 2) ?v_37) (= x_152 (+ x_140 1))) ?v_36) ?v_38) ?v_39) (= x_162 x_161)) (= x_280 3)) (= x_158 (store x_146 x_161 1)))) (and (and (and (and (and (= x_155 3) ?v_38) ?v_35) ?v_36) (= x_158 x_146)) ?v_39))) (= x_281 (select x_134 x_144))) (= x_282 (select x_134 x_147))) (= x_283 (select x_127 x_128))) (= x_284 (select x_134 x_149))) (or (or (or (and (and (and (and (and (and (and (= x_143 0) (= x_142 (+ x_130 1))) ?v_40) ?v_41) (= x_145 x_144)) (= x_281 1)) (= x_146 (store x_134 x_144 2))) (= x_139 (store x_127 x_130 x_144))) (and (and (and (and (and (and (and (and (and (= x_143 1) ?v_42) ?v_40) ?v_41) ?v_43) ?v_44) (= x_148 x_147)) (= x_282 2)) (= x_283 x_147)) (= x_146 (store x_134 x_147 3)))) (and (and (and (and (and (and (and (and (= x_143 2) ?v_42) (= x_140 (+ x_128 1))) ?v_41) ?v_43) ?v_44) (= x_150 x_149)) (= x_284 3)) (= x_146 (store x_134 x_149 1)))) (and (and (and (and (and (= x_143 3) ?v_43) ?v_40) ?v_41) (= x_146 x_134)) ?v_44))) (= x_285 (select x_122 x_132))) (= x_286 (select x_122 x_135))) (= x_287 (select x_115 x_116))) (= x_288 (select x_122 x_137))) (or (or (or (and (and (and (and (and (and (and (= x_131 0) (= x_130 (+ x_118 1))) ?v_45) ?v_46) (= x_133 x_132)) (= x_285 1)) (= x_134 (store x_122 x_132 2))) (= x_127 (store x_115 x_118 x_132))) (and (and (and (and (and (and (and (and (and (= x_131 1) ?v_47) ?v_45) ?v_46) ?v_48) ?v_49) (= x_136 x_135)) (= x_286 2)) (= x_287 x_135)) (= x_134 (store x_122 x_135 3)))) (and (and (and (and (and (and (and (and (= x_131 2) ?v_47) (= x_128 (+ x_116 1))) ?v_46) ?v_48) ?v_49) (= x_138 x_137)) (= x_288 3)) (= x_134 (store x_122 x_137 1)))) (and (and (and (and (and (= x_131 3) ?v_48) ?v_45) ?v_46) (= x_134 x_122)) ?v_49))) (= x_289 (select x_110 x_120))) (= x_290 (select x_110 x_123))) (= x_291 (select x_103 x_104))) (= x_292 (select x_110 x_125))) (or (or (or (and (and (and (and (and (and (and (= x_119 0) (= x_118 (+ x_106 1))) ?v_50) ?v_51) (= x_121 x_120)) (= x_289 1)) (= x_122 (store x_110 x_120 2))) (= x_115 (store x_103 x_106 x_120))) (and (and (and (and (and (and (and (and (and (= x_119 1) ?v_52) ?v_50) ?v_51) ?v_53) ?v_54) (= x_124 x_123)) (= x_290 2)) (= x_291 x_123)) (= x_122 (store x_110 x_123 3)))) (and (and (and (and (and (and (and (and (= x_119 2) ?v_52) (= x_116 (+ x_104 1))) ?v_51) ?v_53) ?v_54) (= x_126 x_125)) (= x_292 3)) (= x_122 (store x_110 x_125 1)))) (and (and (and (and (and (= x_119 3) ?v_53) ?v_50) ?v_51) (= x_122 x_110)) ?v_54))) (= x_293 (select x_98 x_108))) (= x_294 (select x_98 x_111))) (= x_295 (select x_91 x_92))) (= x_296 (select x_98 x_113))) (or (or (or (and (and (and (and (and (and (and (= x_107 0) (= x_106 (+ x_94 1))) ?v_55) ?v_56) (= x_109 x_108)) (= x_293 1)) (= x_110 (store x_98 x_108 2))) (= x_103 (store x_91 x_94 x_108))) (and (and (and (and (and (and (and (and (and (= x_107 1) ?v_57) ?v_55) ?v_56) ?v_58) ?v_59) (= x_112 x_111)) (= x_294 2)) (= x_295 x_111)) (= x_110 (store x_98 x_111 3)))) (and (and (and (and (and (and (and (and (= x_107 2) ?v_57) (= x_104 (+ x_92 1))) ?v_56) ?v_58) ?v_59) (= x_114 x_113)) (= x_296 3)) (= x_110 (store x_98 x_113 1)))) (and (and (and (and (and (= x_107 3) ?v_58) ?v_55) ?v_56) (= x_110 x_98)) ?v_59))) (= x_297 (select x_86 x_96))) (= x_298 (select x_86 x_99))) (= x_299 (select x_79 x_80))) (= x_300 (select x_86 x_101))) (or (or (or (and (and (and (and (and (and (and (= x_95 0) (= x_94 (+ x_82 1))) ?v_60) ?v_61) (= x_97 x_96)) (= x_297 1)) (= x_98 (store x_86 x_96 2))) (= x_91 (store x_79 x_82 x_96))) (and (and (and (and (and (and (and (and (and (= x_95 1) ?v_62) ?v_60) ?v_61) ?v_63) ?v_64) (= x_100 x_99)) (= x_298 2)) (= x_299 x_99)) (= x_98 (store x_86 x_99 3)))) (and (and (and (and (and (and (and (and (= x_95 2) ?v_62) (= x_92 (+ x_80 1))) ?v_61) ?v_63) ?v_64) (= x_102 x_101)) (= x_300 3)) (= x_98 (store x_86 x_101 1)))) (and (and (and (and (and (= x_95 3) ?v_63) ?v_60) ?v_61) (= x_98 x_86)) ?v_64))) (= x_301 (select x_74 x_84))) (= x_302 (select x_74 x_87))) (= x_303 (select x_67 x_68))) (= x_304 (select x_74 x_89))) (or (or (or (and (and (and (and (and (and (and (= x_83 0) (= x_82 (+ x_70 1))) ?v_65) ?v_66) (= x_85 x_84)) (= x_301 1)) (= x_86 (store x_74 x_84 2))) (= x_79 (store x_67 x_70 x_84))) (and (and (and (and (and (and (and (and (and (= x_83 1) ?v_67) ?v_65) ?v_66) ?v_68) ?v_69) (= x_88 x_87)) (= x_302 2)) (= x_303 x_87)) (= x_86 (store x_74 x_87 3)))) (and (and (and (and (and (and (and (and (= x_83 2) ?v_67) (= x_80 (+ x_68 1))) ?v_66) ?v_68) ?v_69) (= x_90 x_89)) (= x_304 3)) (= x_86 (store x_74 x_89 1)))) (and (and (and (and (and (= x_83 3) ?v_68) ?v_65) ?v_66) (= x_86 x_74)) ?v_69))) (= x_305 (select x_62 x_72))) (= x_306 (select x_62 x_75))) (= x_307 (select x_55 x_56))) (= x_308 (select x_62 x_77))) (or (or (or (and (and (and (and (and (and (and (= x_71 0) (= x_70 (+ x_58 1))) ?v_70) ?v_71) (= x_73 x_72)) (= x_305 1)) (= x_74 (store x_62 x_72 2))) (= x_67 (store x_55 x_58 x_72))) (and (and (and (and (and (and (and (and (and (= x_71 1) ?v_72) ?v_70) ?v_71) ?v_73) ?v_74) (= x_76 x_75)) (= x_306 2)) (= x_307 x_75)) (= x_74 (store x_62 x_75 3)))) (and (and (and (and (and (and (and (and (= x_71 2) ?v_72) (= x_68 (+ x_56 1))) ?v_71) ?v_73) ?v_74) (= x_78 x_77)) (= x_308 3)) (= x_74 (store x_62 x_77 1)))) (and (and (and (and (and (= x_71 3) ?v_73) ?v_70) ?v_71) (= x_74 x_62)) ?v_74))) (= x_309 (select x_50 x_60))) (= x_310 (select x_50 x_63))) (= x_311 (select x_43 x_44))) (= x_312 (select x_50 x_65))) (or (or (or (and (and (and (and (and (and (and (= x_59 0) (= x_58 (+ x_46 1))) ?v_75) ?v_76) (= x_61 x_60)) (= x_309 1)) (= x_62 (store x_50 x_60 2))) (= x_55 (store x_43 x_46 x_60))) (and (and (and (and (and (and (and (and (and (= x_59 1) ?v_77) ?v_75) ?v_76) ?v_78) ?v_79) (= x_64 x_63)) (= x_310 2)) (= x_311 x_63)) (= x_62 (store x_50 x_63 3)))) (and (and (and (and (and (and (and (and (= x_59 2) ?v_77) (= x_56 (+ x_44 1))) ?v_76) ?v_78) ?v_79) (= x_66 x_65)) (= x_312 3)) (= x_62 (store x_50 x_65 1)))) (and (and (and (and (and (= x_59 3) ?v_78) ?v_75) ?v_76) (= x_62 x_50)) ?v_79))) (= x_313 (select x_38 x_48))) (= x_314 (select x_38 x_51))) (= x_315 (select x_31 x_32))) (= x_316 (select x_38 x_53))) (or (or (or (and (and (and (and (and (and (and (= x_47 0) (= x_46 (+ x_34 1))) ?v_80) ?v_81) (= x_49 x_48)) (= x_313 1)) (= x_50 (store x_38 x_48 2))) (= x_43 (store x_31 x_34 x_48))) (and (and (and (and (and (and (and (and (and (= x_47 1) ?v_82) ?v_80) ?v_81) ?v_83) ?v_84) (= x_52 x_51)) (= x_314 2)) (= x_315 x_51)) (= x_50 (store x_38 x_51 3)))) (and (and (and (and (and (and (and (and (= x_47 2) ?v_82) (= x_44 (+ x_32 1))) ?v_81) ?v_83) ?v_84) (= x_54 x_53)) (= x_316 3)) (= x_50 (store x_38 x_53 1)))) (and (and (and (and (and (= x_47 3) ?v_83) ?v_80) ?v_81) (= x_50 x_38)) ?v_84))) (= x_317 (select x_26 x_36))) (= x_318 (select x_26 x_39))) (= x_319 (select x_19 x_20))) (= x_320 (select x_26 x_41))) (or (or (or (and (and (and (and (and (and (and (= x_35 0) (= x_34 (+ x_22 1))) ?v_85) ?v_86) (= x_37 x_36)) (= x_317 1)) (= x_38 (store x_26 x_36 2))) (= x_31 (store x_19 x_22 x_36))) (and (and (and (and (and (and (and (and (and (= x_35 1) ?v_87) ?v_85) ?v_86) ?v_88) ?v_89) (= x_40 x_39)) (= x_318 2)) (= x_319 x_39)) (= x_38 (store x_26 x_39 3)))) (and (and (and (and (and (and (and (and (= x_35 2) ?v_87) (= x_32 (+ x_20 1))) ?v_86) ?v_88) ?v_89) (= x_42 x_41)) (= x_320 3)) (= x_38 (store x_26 x_41 1)))) (and (and (and (and (and (= x_35 3) ?v_88) ?v_85) ?v_86) (= x_38 x_26)) ?v_89))) (= x_321 (select x_14 x_24))) (= x_322 (select x_14 x_27))) (= x_323 (select x_6 x_8))) (= x_324 (select x_14 x_29))) (or (or (or (and (and (and (and (and (and (and (= x_23 0) (= x_22 (+ x_10 1))) ?v_90) ?v_91) (= x_25 x_24)) (= x_321 1)) (= x_26 (store x_14 x_24 2))) (= x_19 (store x_6 x_10 x_24))) (and (and (and (and (and (and (and (and (and (= x_23 1) ?v_92) ?v_90) ?v_91) ?v_93) ?v_94) (= x_28 x_27)) (= x_322 2)) (= x_323 x_27)) (= x_26 (store x_14 x_27 3)))) (and (and (and (and (and (and (and (and (= x_23 2) ?v_92) (= x_20 (+ x_8 1))) ?v_91) ?v_93) ?v_94) (= x_30 x_29)) (= x_324 3)) (= x_26 (store x_14 x_29 1)))) (and (and (and (and (and (= x_23 3) ?v_93) ?v_90) ?v_91) (= x_26 x_14)) ?v_94))) (= x_325 (select x_2 x_12))) (= x_326 (select x_2 x_15))) (= x_327 (select x_7 x_0))) (= x_328 (select x_2 x_17))) (or (or (or (and (and (and (and (and (and (and (= x_11 0) (= x_10 (+ x_1 1))) ?v_95) ?v_96) (= x_13 x_12)) (= x_325 1)) (= x_14 (store x_2 x_12 2))) (= x_6 (store x_7 x_1 x_12))) (and (and (and (and (and (and (and (and (and (= x_11 1) ?v_97) ?v_95) ?v_96) ?v_98) ?v_99) (= x_16 x_15)) (= x_326 2)) (= x_327 x_15)) (= x_14 (store x_2 x_15 3)))) (and (and (and (and (and (and (and (and (= x_11 2) ?v_97) (= x_8 (+ x_0 1))) ?v_96) ?v_98) ?v_99) (= x_18 x_17)) (= x_328 3)) (= x_14 (store x_2 x_17 1)))) (and (and (and (and (and (= x_11 3) ?v_98) ?v_95) ?v_96) (= x_14 x_2)) ?v_99))) (= x_329 (select x_242 x_3))) (= x_330 (select x_242 x_4))) (= x_331 (select x_230 x_3))) (= x_332 (select x_230 x_4))) (= x_333 (select x_218 x_3))) (= x_334 (select x_218 x_4))) (= x_335 (select x_206 x_3))) (= x_336 (select x_206 x_4))) (= x_337 (select x_194 x_3))) (= x_338 (select x_194 x_4))) (= x_339 (select x_182 x_3))) (= x_340 (select x_182 x_4))) (= x_341 (select x_170 x_3))) (= x_342 (select x_170 x_4))) (= x_343 (select x_158 x_3))) (= x_344 (select x_158 x_4))) (= x_345 (select x_146 x_3))) (= x_346 (select x_146 x_4))) (= x_347 (select x_134 x_3))) (= x_348 (select x_134 x_4))) (= x_349 (select x_122 x_3))) (= x_350 (select x_122 x_4))) (= x_351 (select x_110 x_3))) (= x_352 (select x_110 x_4))) (= x_353 (select x_98 x_3))) (= x_354 (select x_98 x_4))) (= x_355 (select x_86 x_3))) (= x_356 (select x_86 x_4))) (= x_357 (select x_74 x_3))) (= x_358 (select x_74 x_4))) (= x_359 (select x_62 x_3))) (= x_360 (select x_62 x_4))) (= x_361 (select x_50 x_3))) (= x_362 (select x_50 x_4))) (= x_363 (select x_38 x_3))) (= x_364 (select x_38 x_4))) (= x_365 (select x_26 x_3))) (= x_366 (select x_26 x_4))) (= x_367 (select x_14 x_3))) (= x_368 (select x_14 x_4))) (= x_369 ?v_100)) (= x_370 ?v_101)) (or (or (or (or (or (or (or (or (or (or (or (or (or (or (or (or (or (or (or (or (and (= x_329 3) (= x_330 3)) (and (= x_331 3) (= x_332 3))) (and (= x_333 3) (= x_334 3))) (and (= x_335 3) (= x_336 3))) (and (= x_337 3) (= x_338 3))) (and (= x_339 3) (= x_340 3))) (and (= x_341 3) (= x_342 3))) (and (= x_343 3) (= x_344 3))) (and (= x_345 3) (= x_346 3))) (and (= x_347 3) (= x_348 3))) (and (= x_349 3) (= x_350 3))) (and (= x_351 3) (= x_352 3))) (and (= x_353 3) (= x_354 3))) (and (= x_355 3) (= x_356 3))) (and (= x_357 3) (= x_358 3))) (and (= x_359 3) (= x_360 3))) (and (= x_361 3) (= x_362 3))) (and (= x_363 3) (= x_364 3))) (and (= x_365 3) (= x_366 3))) (and (= x_367 3) (= x_368 3))) (and (= x_369 3) (= x_370 3))))))
(check-sat)
(exit)
