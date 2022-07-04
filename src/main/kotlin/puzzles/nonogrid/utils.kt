package puzzles.nonogrid

/**
 * The degrees of freedom within a line.
 */
fun degreesOfFreedom(clues: List<Int>, size: Int) = size - (clues.sum() + clues.size - 1)