package com.findwords.app.engine

import com.google.gson.Gson
import java.util.Random

object WordSearchGenerator {

    private val directions = arrayOf(
        IntArray(2) { 0 },  // placeholder
        intArrayOf(0, 1),   // right
        intArrayOf(1, 0),   // down
        intArrayOf(1, 1),   // down-right
        intArrayOf(1, -1),  // down-left
        intArrayOf(0, -1),  // left
        intArrayOf(-1, 0),  // up
        intArrayOf(-1, 1),  // up-right
        intArrayOf(-1, -1)  // up-left
    )

    data class GenerationResult(
        val grid: Array<CharArray>,
        val words: List<WordPlacement>,
        val gridSize: Int
    )

    data class WordPlacement(
        val word: String,
        val startRow: Int,
        val startCol: Int,
        val direction: Int,
        var found: Boolean = false
    )

    fun generate(words: List<String>, gridSize: Int, maxAttempts: Int = 100): GenerationResult {
        val sortedWords = words.sortedByDescending { it.length }
        var bestResult: GenerationResult? = null
        var bestScore = Int.MAX_VALUE

        for (attempt in 0 until maxAttempts) {
            val result = tryGenerate(sortedWords, gridSize)
            if (result != null) {
                val emptyCells = countEmptyCells(result.grid)
                if (emptyCells < bestScore) {
                    bestScore = emptyCells
                    bestResult = result
                    if (emptyCells == 0) break
                }
            }
        }

        return bestResult ?: generateFallback(sortedWords, gridSize)
    }

    private fun tryGenerate(words: List<String>, gridSize: Int): GenerationResult? {
        val grid = Array(gridSize) { CharArray(gridSize) { ' ' } }
        val placedWords = mutableListOf<WordPlacement>()
        val random = Random()

        for (word in words) {
            val cleanWord = word.uppercase().replace(" ", "").filter { it.isLetter() }
            if (cleanWord.length < 3 || cleanWord.length > gridSize) continue

            val positions = findValidPositions(grid, cleanWord)
            if (positions.isEmpty()) return null

            val pos = positions.random(random)
            placeWord(grid, cleanWord, pos.row, pos.col, pos.dir)
            placedWords.add(WordPlacement(cleanWord, pos.row, pos.col, pos.dir))
        }

        fillEmptyCells(grid, random)
        return GenerationResult(grid, placedWords, gridSize)
    }

    private data class Position(val row: Int, val col: Int, val dir: Int)

    private fun findValidPositions(grid: Array<CharArray>, word: String): List<Position> {
        val positions = mutableListOf<Position>()
        val size = grid.size

        for (dir in 1..8) {
            val dRow = directions[dir][0]
            val dCol = directions[dir][1]

            for (row in 0 until size) {
                for (col in 0 until size) {
                    if (canPlace(grid, word, row, col, dRow, dCol)) {
                        positions.add(Position(row, col, dir))
                    }
                }
            }
        }
        return positions
    }

    private fun canPlace(grid: Array<CharArray>, word: String, startRow: Int, startCol: Int, dRow: Int, dCol: Int): Boolean {
        val size = grid.size
        var row = startRow
        var col = startCol

        for (i in 0 until word.length) {
            if (row < 0 || row >= size || col < 0 || col >= size) return false
            val cell = grid[row][col]
            if (cell != ' ' && cell != word[i]) return false
            row += dRow
            col += dCol
        }
        return true
    }

    private fun placeWord(grid: Array<CharArray>, word: String, startRow: Int, startCol: Int, dir: Int) {
        val dRow = directions[dir][0]
        val dCol = directions[dir][1]
        var row = startRow
        var col = startCol

        for (i in 0 until word.length) {
            grid[row][col] = word[i]
            row += dRow
            col += dCol
        }
    }

    private fun fillEmptyCells(grid: Array<CharArray>, random: Random) {
        val letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray()
        for (row in 0 until grid.size) {
            for (col in 0 until grid[0].size) {
                if (grid[row][col] == ' ') {
                    grid[row][col] = letters[random.nextInt(letters.size)]
                }
            }
        }
    }

    private fun countEmptyCells(grid: Array<CharArray>): Int {
        var count = 0
        for (row in grid) {
            for (c in row) if (c == ' ') count++
        }
        return count
    }

    private fun generateFallback(words: List<String>, gridSize: Int): GenerationResult {
        val grid = Array(gridSize) { CharArray(gridSize) { 'A' } }
        val placedWords = mutableListOf<WordPlacement>()
        return GenerationResult(grid, placedWords, gridSize)
    }

    fun gridToJson(grid: Array<CharArray>): String {
        return Gson().toJson(grid)
    }

    fun wordsToJson(words: List<WordPlacement>): String {
        return Gson().toJson(words)
    }

    fun generateDailyChallenge(gridSize: Int = 10): GenerationResult {
        val dailyWords = listOf(
            "DAILY", "CHALLENGE", "PUZZLE", "WORD", "SEARCH", "FIND", "HIDDEN", "LETTERS",
            "GRID", "SOLVE", "BRAIN", "GAME", "PLAY", "FUN", "EASY", "HARD", "LEVEL"
        )
        return generate(dailyWords.shuffled().take(10), gridSize)
    }
}