import it.skrape.core.htmlDocument
import it.skrape.fetcher.BrowserFetcher
import it.skrape.fetcher.extractBlocking
import it.skrape.fetcher.skrape
import it.skrape.selects.html5.td
import it.skrape.selects.html5.tr
import puzzles.nonogrid.NonogridSolver
import puzzles.nonogrid.NonogridState

fun main(args: Array<String>) {
    // Learn more about running applications: https://www.jetbrains.com/help/idea/running-applications.html.
//    println("Program arguments: ${args.joinToString()}")

    var x: NonogridState = skrape(BrowserFetcher) {
        request {
//            url = "https://www.brainbashers.com/shownonogrid.asp?date=0703&size=25"
            url = "https://www.brainbashers.com/shownonogrid.asp?date=0701&size=30"
        }

        extractBlocking<NonogridState> {
            lateinit var colClues: List<List<Int>>
            lateinit var rowClues: List<List<Int>>
            htmlDocument {
                "#puzzletable" {
                    findFirst {
                        // Extract column clues.
                        tr {
                            findFirst {
                                td {
                                    findAll {
                                        colClues =
                                            this.map { it.text }
                                                .drop(1)
                                                .map { clueString ->
                                                    clueString.split(" ").map { Integer.parseInt(it) }
                                                }
                                    }
                                }
                            }
                        }

                        // Extract row clues.
                        tr {
                            findAll {
                                rowClues =
                                    this.drop(1)
                                        .map {
                                            it.td {
                                                findFirst {
                                                    text
                                                }
                                            }
                                        }
                                        .map { clueString ->
                                            clueString.split(" ").map { Integer.parseInt(it) }
                                        }
                            }
                        }
                    }
                }
            }

            NonogridState(
                grid = List(rowClues.size) { List(colClues.size) { null } },
                colClues = colClues,
                rowClues = rowClues
            )
        }
    }

    println(x)

    var lastState: NonogridState = x;
    var iteration = 0
    while (true) {
        val nextState = NonogridSolver.step(lastState)
        println(iteration)
        println(nextState)

        if (lastState == nextState) {
            break
        }
        lastState = nextState
        iteration++
    }
}