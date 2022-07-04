package puzzles.nonogrid

data class NonogridState(
    val grid: List<List<Boolean?>>,
    val colClues: List<List<Int>>,
    val rowClues: List<List<Int>>
) {
    val numRows = grid.size
    val numCols = grid[0].size

    /**
     * Returns the [n]th row in the grid.
     */
    fun row(n: Int): List<Boolean?> = grid[n]

    fun rowLine(n: Int): LineState = LineState.of(rowClues[n], row(n))

    /**
     * Returns the [n]th col in the grid.
     */
    fun col(n: Int): List<Boolean?> = grid.map { it[n] }

    fun colLine(n: Int): LineState = LineState.of(colClues[n], col(n))

    fun updateRow(n: Int, row: List<Boolean?>): NonogridState {
        val newGrid: List<List<Boolean?>> = List(numRows) { if (it == n) row else grid[it] }
        return copy(grid = newGrid)
    }

    fun updateCol(n: Int, col: List<Boolean?>): NonogridState {
        val newGrid: List<List<Boolean?>> = List(numRows) { rowIndex ->
            val row = row(rowIndex).toMutableList()
            row[n] = col[rowIndex]
            row
        }
        return copy(grid = newGrid)
    }

    override fun toString(): String {
        val rows: MutableList<String> = mutableListOf()
        grid.forEachIndexed { rowIndex, row ->
            if (rowIndex % 5 == 0) {
                rows.add("-".repeat(2 * numCols + 1))
            }
            val stringRow =
                row.mapIndexed { index, b -> "${if (index % 5 == 0) "|" else " "}${if (b == null) " " else if (b) "X" else "."}" }
                    .joinToString("")
            rows.add("$stringRow|")
        }
        rows.add("-".repeat(2 * numCols + 1))

        return rows.joinToString("\n")
    }
}
