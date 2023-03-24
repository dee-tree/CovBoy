package com.sokolov.covboy.coverage

import org.ksmt.KContext
import org.ksmt.sort.KBoolSort
import java.io.InputStream

class CoverageComparator {

    companion object {

        @JvmStatic
        fun compare(
            primaryCoverageInputStream: InputStream,
            secondaryCoverageInputStream: InputStream,
            ctx: KContext = KContext(),
            onResult: (CoverageCompareStatus) -> Unit
        ) {
            val primaryInputStream = primaryCoverageInputStream.buffered()
            val secondaryInputStream = secondaryCoverageInputStream.buffered()

            val serializer = PredicatesCoverageSerializer(ctx)

            val isPrimaryCoverageComplete = serializer.isCompleteCoverage(primaryInputStream)
            val isSecondaryCoverageComplete = serializer.isCompleteCoverage(secondaryInputStream)

            if (!isPrimaryCoverageComplete || !isSecondaryCoverageComplete) {
                onResult(CoverageCompareStatus.SAMPLING_ERROR)
                return
            }

            val primaryCoverage = PredicatesCoverage.deserialize<KBoolSort>(ctx, primaryInputStream)
            val secondaryCoverage = PredicatesCoverage.deserialize<KBoolSort>(ctx, secondaryInputStream)

            when (primaryCoverage.equalsCoverage(secondaryCoverage)) {
                true -> onResult(CoverageCompareStatus.EQUAL)
                false -> onResult(CoverageCompareStatus.UNEQUAL)
            }
        }
    }

    enum class CoverageCompareStatus {
        EQUAL, UNEQUAL, SAMPLING_ERROR
    }
}