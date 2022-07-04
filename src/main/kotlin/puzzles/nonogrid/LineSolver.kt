package puzzles.nonogrid

private fun Iterable<Int>.product(): Int = this.reduce { product, element -> product * element }

private fun combinations(clues: List<Int>, size: Int): Int {
    val degrees = size - (clues.sum() + clues.size - 1)
    val dividers = clues.size

    val numerator = ((degrees + dividers) downTo (degrees + 1)).product()
    val denominator = (dividers downTo 1).product()
    return numerator / denominator
}

// TODO:
//  Future optimization:
//  We generate permutation candidates fully, then check if each one is valid.
//  We can potentially prune permutation branches if we check as the permutation is being generated.
// TODO:
//  It would be nice to be able to compute (or estimate) the number of permutations without explicitly counting them all.
//  This would let us optimize on which line to solve first.
/**
 * Computes all possible permutations, respecting [clues] and [line].
 */
private fun permutations(clues: List<Int>, line: List<Boolean?>) = sequence<List<Boolean>> {
    // This approach is based on a combinatorics-ish representation.
    // A line can be represented as a sequence of "X"s and "|"s, such as:
    //      "XXX|X|XXX"
    // where each "X" represents a blank cell and each "|" represents a run of filled cells.
    // Each "|" would then correspond to a clue. (It is essentially a flattened clue.)
    // There are always a fixed number of "X"s and "|"s, so the challenge is just permuting them.
    //
    // This default representation requires an "X" between each "|" to be valid, which makes combinatorics harder.
    // So we modify this approach, letting each "|" represent a clue and the required trailing blank cell
    // (except for the last clue, which does not require a trailing blank).
    // This reduces the number of "X" characters, and the new amount is equal to the degrees of freedom in a line.
    // The combinatorics are now just (total characters) choose (# of dividers).
    //
    // To actually generate these permutations, we just count through the binary representation of integers,
    // dropping any representations that don't match the expected number of "X"s and "|"s.

    val degrees = line.size - (clues.sum() + clues.size - 1)
    val dividers = clues.size

    for (stringPermutation in stringPermutations(mapOf("X" to degrees, "|" to dividers))) {
        var clueIndex = 0
        val permutation = mutableListOf<Boolean>()
        for (char in stringPermutation) {
            when (char) {
                'X' -> permutation.add(false)
                '|' -> {
                    repeat(clues[clueIndex]) {
                        permutation.add(true)
                    }
                    clueIndex++
                    permutation.add(false)
                }
                else -> throw Exception()
            }
        }
        permutation.dropLast(1)

        if (isValid(line, permutation)) {
            yield(permutation)
        }
    }
}

private fun stringPermutations(candidates: Map<String, Int>, prefix: String = ""): Sequence<String> = sequence {
    if (candidates.values.sum() == 0) {
        yield(prefix)
        return@sequence
    }

    candidates.forEach { (candidate, remaining) ->
        if (remaining == 0) {
            return@forEach
        }
        val newMap: MutableMap<String, Int> = candidates.toMutableMap()
        newMap[candidate] = newMap[candidate]!! - 1
        yieldAll(stringPermutations(newMap, "$prefix$candidate"))
    }
}

/**
 * Whether or not [permutation] is a permutation that matches [line].
 */
private fun isValid(line: List<Boolean?>, permutation: List<Boolean>): Boolean {
    line.forEachIndexed { index, value ->
        if (value == null) {
            return@forEachIndexed
        }
        if (value != permutation[index]) {
            return false
        }
    }
    return true
}

class LineSolver {
    companion object {
        /**
         * Solves [state] as much as possible.
         */
        fun solve(state: LineState): LineState {
            return state.markEmptyCandidates().markSolvedClues().markFixedCells()
        }

        private fun LineState.markEmptyCandidates(): LineState {
            val newLine = this.line.toMutableList()
            this.clueIndexOptions.forEachIndexed { index, options ->
                if (options.isEmpty()) {
                    newLine[index] = false
                }
            }
            return this.copy(line = newLine).normalize()
        }

        /**
         * For each clue, if the number of candidates remaining matches the clue value, those cells are all true.
         */
        private fun LineState.markSolvedClues(): LineState {
            val newLine = this.line.toMutableList()
            this.clues.forEachIndexed { clueIndex, clue ->
                if (this.clueCandidateIndices[clueIndex]!!.size == clue) {
                    this.clueCandidateIndices[clueIndex]!!.forEach {
                        newLine[it] = true
                    }
                }
            }
            return this.copy(line = newLine).normalize()
        }

        /**
         * Brute forces possible solutions, marking any cells that are fixed across permutations.
         */
        fun LineState.markFixedCells(): LineState {
            // Candidate fixed values.
            val candidates: List<MutableSet<Boolean>> =
                this.line.map { if (it == null) mutableSetOf(true, false) else mutableSetOf(it) }
            // Remaining indices to try to fix. Allows for short circuiting.
            val toCheck: MutableList<Boolean> = this.line.map { it == null }.toMutableList()

            for (permutation in permutations(this.clues, this.line)) {
                toCheck.forEachIndexed { index, shouldCheck ->
                    if (!shouldCheck) {
                        return@forEachIndexed
                    }

                    candidates[index].remove(!permutation[index])
                    if (candidates[index].isEmpty()) {
                        toCheck[index] = false
                    }
                }

                // Short circuit if there are no more indices to check.
                if (toCheck.none { it }) {
                    break
                }
            }

            val newLine: List<Boolean?> = candidates.map { if (it.size == 1) it.first() else null }
            return this.copy(line = newLine).normalize()
        }
    }
}
