package puzzles.nonogrid

import kotlin.math.max
import kotlin.math.min

data class LineState(
    val clues: List<Int>,
    val line: List<Boolean?>,
    val clueIndexOptions: List<List<Int>>
) {
    val solved = line.none { it == null }

    /**
     * For each clue index, the line indices where that clue may be present.
     *
     * Convenience map for finding clues in [clueIndexOptions].
     */
    val clueCandidateIndices: Map<Int, Set<Int>> by lazy {
        val map = mutableMapOf<Int, MutableSet<Int>>()
        this.clueIndexOptions.forEachIndexed { index, options ->
            options.forEach {
                map.computeIfAbsent(it) {
                    mutableSetOf()
                }.add(index)
            }
        }
        map
    }

    /**
     * The length of the run containing the target index.
     *
     * Only considers strictly true/solved values.
     */
    val runLengthsAtIndex: Map<Int, Int> by lazy {
        val lengths = mutableMapOf<Int, Int>()

        var currentLength = 0
        line.forEachIndexed { index, value ->
            // Increment run length.
            if (value == true) {
                currentLength++
                return@forEachIndexed
            }

            // Run broken.
            for (i in index - currentLength until index) {
                lengths[i] = currentLength
            }
            currentLength = 0
        }

        lengths
    }

    /**
     * Returns this `LineState` with an updated [line].
     *
     * This doesn't do too much, but the hope is to save a little bit of recomputation when calling normalize.
     */
    fun updateLine(line: List<Boolean?>): LineState {
        return copy(line = line).normalize()
    }

    /**
     * Recomputes metadata.
     */
    fun normalize(): LineState {
        val newClueIndexOptions =
            clueIndexOptions.map { it.toMutableList() }.apply {
                // Clear out all clue options for false cells.
                line.forEachIndexed { index, value -> if (value == false) this[index].clear() }

                // NOTE:
                //  I think the earliest/latest possible handling will deal with the 1-gap buffer?
                // Remove candidates if the run length at that index is already too long.
                runLengthsAtIndex.forEach { (index, runLength) ->
                    this[index].removeIf {
                        clues[it] < runLength
                    }
                }

                // Eliminate candidates if the max length is too short.
                for (clueIndex in clues.indices) {
                    var runLength = 0

                    this.forEachIndexed { index, options ->
                        if (options.contains(clueIndex)) {
                            runLength++
                            return@forEachIndexed
                        }

                        // Run is broken.
                        // If the run wasn't long enough, clear those candidates.
                        if (runLength < clues[clueIndex]) {
                            for (i in index - runLength until index) {
                                this[i].remove(clueIndex)
                            }
                        }
                        runLength = 0
                    }
                }

                // For each true cell, if it's clear which clue it corresponds to, eliminate all candidates that won't
                // overlap that cell.
                line.forEachIndexed { index, value ->
                    if (value != true) {
                        return@forEachIndexed
                    }
                    if (this[index].size != 1) {
                        return@forEachIndexed
                    }

                    val clueIndex = this[index][0]
                    val clue = clues[clueIndex]
                    val min = index - clue + 1
                    val max = index + clue - 1
                    for (i in 0 until min) {
                        this[i].remove(clueIndex)
                    }
                    for (i in max + 1 until size) {
                        this[i].remove(clueIndex)
                    }
                }

                // Update earliest possible clue indices.
                // Be aware that each loop iteration affects the next.
                for (i in clues.indices) {
                    val firstIndex = this.indexOfFirst { it.contains(i) }
                    for (j in firstIndex..min(firstIndex + clues[i], size - 1)) {
                        this[j].removeIf { it > i }
                    }
                }

                // Update latest possible clue indices.
                // Be aware that each loop iteration affects the next.
                for (i in clues.indices.reversed()) {
                    val lastIndex = this.indexOfLast { it.contains(i) }
                    for (j in max(lastIndex - clues[i], 0)..lastIndex) {
                        this[j].removeIf { it < i }
                    }
                }
            }

        return copy(
            clueIndexOptions = newClueIndexOptions
        )
    }

    companion object {
        fun initialState(clues: List<Int>, size: Int): LineState {
            val line = List(size) { null }
            val clueIndexOptions: List<List<Int>> = List(size) { clues.indices.toList() }
            return LineState(
                clues = clues,
                line = line,
                clueIndexOptions = clueIndexOptions
            ).normalize()
        }

        fun of(clues: List<Int>, line: List<Boolean?>): LineState {
            val clueIndexOptions: List<List<Int>> = List(line.size) { clues.indices.toList() }
            return LineState(
                clues = clues,
                line = line,
                clueIndexOptions = clueIndexOptions
            ).normalize()
        }
    }
}
