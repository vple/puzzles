package puzzles.nonogrid

class NonogridSolver {
    companion object {
        fun step(state: NonogridState): NonogridState {
            var newState = state;

            for (rowIndex in 0 until state.numRows) {
                var row = newState.rowLine(rowIndex)
                if (row.solved) continue
                row = LineSolver.solve(row)
                newState = newState.updateRow(rowIndex, row.line)
            }

            for (colIndex in 0 until state.numCols) {
                var col = newState.colLine(colIndex)
                if (col.solved) continue
                col = LineSolver.solve(col)
                newState = newState.updateCol(colIndex, col.line)
            }

            return newState
        }

        fun solve(state: NonogridState): NonogridState {
            var currentState = state
            while (true) {
                val nextState = step(currentState)
                if (nextState == currentState) {
                    return currentState
                }
                currentState = nextState
            }
        }
    }
}