import puzzles.nonogrid.NonogridSolver
import puzzles.nonogrid.parsers.BrainBashersNonogridParser

fun main(args: Array<String>) {
    // Learn more about running applications: https://www.jetbrains.com/help/idea/running-applications.html.
//    println("Program arguments: ${args.joinToString()}")

    val nonogridUrl = "https://www.brainbashers.com/shownonogrid.asp?date=0704&size=20"
    val nonogrid = BrainBashersNonogridParser.parse(nonogridUrl)
    println(NonogridSolver.solve(nonogrid))
}