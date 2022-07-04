package puzzles.nonogrid.parsers

import it.skrape.core.htmlDocument
import it.skrape.fetcher.BrowserFetcher
import it.skrape.fetcher.extractBlocking
import it.skrape.fetcher.skrape
import it.skrape.selects.html5.td
import it.skrape.selects.html5.tr
import puzzles.nonogrid.NonogridState

class BrainBashersNonogridParser {
    companion object {
        fun parse(url: String): NonogridState {
            return skrape(BrowserFetcher) {
                request {
                    this.url = url
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
        }
    }
}