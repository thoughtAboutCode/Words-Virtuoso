package wordsvirtuoso

import java.io.File
import kotlin.random.Random

fun main(args: Array<String>) {
    if (args.size != 2) {
        println("Error: Wrong number of arguments.")
        return
    }

    val mainFile = File(args.first().trim())
    val candidateFile = File(args.last().trim())
    if (!mainFile.exists()) {
        println("Error: The words file ${args.first().trim()} doesn't exist.")
        return
    }
    if (!candidateFile.exists()) {
        println("Error: The candidate words file ${args.last().trim()} doesn't exist.")
        return
    }

    if (!checkFile(mainFile) || !checkFile(candidateFile) || !checkCandidateInclusion(mainFile, candidateFile)) {
        return
    }

    println("Words Virtuoso")

    val candidateWords = candidateFile.linesContent()
    val mainWords = mainFile.linesContent()
    var retry = 0
    var start: Long = 0
    var duration: Long
    val previousTries = mutableListOf<String>()
    val wrongChar = mutableSetOf<Char>()
    mainLoop@ do {
        val selectedCandidate = candidateWords.random(Random(Random.nextInt()))
        do {
            retry++
            println("Input a 5-letter word:").also {
                if (retry == 1) {
                    start = System.currentTimeMillis()
                }
            }
            val userInput = readln().trim().lowercase()
            if ((userInput == selectedCandidate).also {
                    if (it) {
                        if (retry > 1)
                            previousTries.forEach(::println)
                        userInput.uppercase().forEach { char ->
                            print("\u001B[48:5:10m${char}\u001B[0m")
                        }
                        println()
                        println("Correct!").also {
                            duration = (System.currentTimeMillis() - start) / 1000
                        }
                        if (retry > 1) {
                            println("The solution was found after $retry tries in $duration seconds.")
                        } else {
                            println("Amazing luck! The solution was found at once.")
                        }
                    }
                } || (userInput == "exit").also {
                    if (it) println("The game is over.")
                }) break@mainLoop

            if (!isValid(userInput, withErrorOutput = true) || (userInput !in mainWords).also {
                    if (it) println("The input word isn't included in my words list.")
                }) {
                continue@mainLoop
            }

            previousTries.add(
                userInput.mapIndexed { index, c ->
                    if (selectedCandidate.contains(c)) {
                        if (selectedCandidate[index] == c) "\u001B[48:5:10m${c.uppercase()}\u001B[0m" else "\u001B[48:5:11m${c.uppercase()}\u001B[0m"
                    } else "\u001B[48:5:7m${c.uppercase()}\u001B[0m".also { wrongChar.add(c.uppercaseChar()) }
                }.joinToString(separator = "")
            )
            previousTries.forEach(::println)
            println("\u001B[48:5:14m${wrongChar.sorted().joinToString(separator = "")}\u001B[0m")
        } while (true)
    } while (true)
}

fun File.linesContent(): List<String> = readLines().map {
    it.run {
        trim()
        lowercase()
    }
}

fun checkCandidateInclusion(words: File, candidate: File): Boolean {
    val wordsContent = words.readLines().map {
        it.run {
            trim()
            uppercase()
        }
    }
    val unInclude = candidate.readLines().map(String::trim).count { it.uppercase() !in wordsContent }
    return if (unInclude != 0) {
        println("Error: $unInclude candidate words are not included in the ${words.name} file.")
        false
    } else true
}

fun checkFile(file: File): Boolean {
    val invalidity = file.readLines().map(String::trim).count { !isValid(it) }
    return if (invalidity != 0) {
        println("Error: $invalidity invalid words were found in the ${file.name} file.")
        false
    } else true
}

fun isValid(input: String, withErrorOutput: Boolean = false): Boolean = when {
    (input.length != 5).also {
        if (it && withErrorOutput) println("The input isn't a 5-letter word.")
    } || with(input.uppercase()) {
        (any { it !in 'A'..'Z' }).also {
            if (it && withErrorOutput) println("One or more letters of the input aren't valid.")
        } || (run { firstOrNull { char -> count { it == char } > 1 } } != null).also {
            if (it && withErrorOutput) println("The input has duplicate letters.")
        }
    } -> false

    else -> true
}
