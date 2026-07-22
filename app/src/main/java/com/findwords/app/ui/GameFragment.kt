package com.findwords.app.ui

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.findwords.app.R
import com.findwords.app.data.GameRepository
import com.findwords.app.engine.WordSearchGenerator
import com.findwords.app.model.Level
import com.findwords.app.model.WordPlacement
import com.findwords.app.databinding.FragmentGameBinding
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GameFragment : Fragment() {

    private var _binding: FragmentGameBinding? = null
    private val binding get() = _binding!!
    private lateinit var repository: GameRepository
    private var currentLevel: Level? = null
    private var words: List<WordPlacement> = emptyList()
    private var grid: Array<CharArray> = Array(0) { CharArray(0) }
    private var selectedCells = mutableListOf<Pair<Int, Int>>()
    private var foundWords = mutableSetOf<String>()
    private var startTime = System.currentTimeMillis()
    private var hintsUsed = 0
    private var timer: CountDownTimer? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentGameBinding.inflate(inflater, container, false)
        repository = GameRepository(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val levelId = arguments?.getInt("levelId") ?: 1
        loadLevel(levelId)

        binding.btnHint.setOnClickListener { useHint() }
        binding.btnShuffle.setOnClickListener { shuffleGrid() }

        setupToolbar()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun loadLevel(levelId: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            val level = repository.getLevelById(levelId)
            withContext(Dispatchers.Main) {
                level?.let {
                    currentLevel = it
                    setupLevel(it)
                } ?: run {
                    Toast.makeText(requireContext(), "Level not found", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
            }
        }
    }

    private fun setupLevel(level: Level) {
        binding.tvPackName.text = "${level.packName} - Level ${level.levelIndex}"
        updateCoins()

        words = Gson().fromJson(level.words, object : com.google.gson.reflect.TypeToken<List<WordPlacement>>() {}.type)
        grid = level.gridArray
        foundWords.clear()
        selectedCells.clear()
        hintsUsed = 0
        startTime = System.currentTimeMillis()

        setupGrid()
        setupWordList()
    }

    private fun setupGrid() {
        val gridSize = grid.size
        binding.gridContainer.removeAllViews()

        val gridLayout = android.widget.GridLayout(requireContext()).apply {
            columnCount = gridSize
            rowCount = gridSize
            layoutParams = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams(
                androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.MATCH_PARENT,
                androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.MATCH_PARENT
            )
        }

        for (row in 0 until gridSize) {
            for (col in 0 until gridSize) {
                val cell = TextView(requireContext()).apply {
                    text = grid[row][col].toString()
                    textSize = 20f
                    gravity = android.view.Gravity.CENTER
                    setBackgroundResource(R.drawable.cell_background)
                    setTextColor(android.graphics.Color.BLACK)
                    layoutParams = android.widget.GridLayout.LayoutParams().apply {
                        width = 0
                        height = 0
                        columnSpec = android.widget.GridLayout.spec(col, 1f)
                        rowSpec = android.widget.GridLayout.spec(row, 1f)
                        setMargins(2, 2, 2, 2)
                    }
                    setOnClickListener { onCellClick(row, col) }
                }
                gridLayout.addView(cell)
            }
        }

        binding.gridContainer.addView(gridLayout)
    }

    private fun setupWordList() {
        val wordList = words.map { it.word }.sortedBy { it.length }.thenBy { it }
        binding.rvWords.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvWords.adapter = WordListAdapter(wordList, foundWords)
    }

    private fun onCellClick(row: Int, col: Int) {
        val pair = row to col
        if (selectedCells.contains(pair)) {
            selectedCells.remove(pair)
            updateCellBackground(row, col, false)
        } else {
            if (selectedCells.isEmpty() || isAdjacent(selectedCells.last(), pair)) {
                selectedCells.add(pair)
                updateCellBackground(row, col, true)
                checkWord()
            }
        }
    }

    private fun isAdjacent(a: Pair<Int, Int>, b: Pair<Int, Int>): Boolean {
        val dr = kotlin.math.abs(a.first - b.first)
        val dc = kotlin.math.abs(a.second - b.second)
        return dr <= 1 && dc <= 1 && (dr != 0 || dc != 0)
    }

    private fun checkWord() {
        if (selectedCells.size < 3) return

        val wordBuilder = StringBuilder()
        for ((r, c) in selectedCells) {
            wordBuilder.append(grid[r][c])
        }
        val word = wordBuilder.toString()
        val reversed = word.reversed()

        val matched = words.find { !foundWords.contains(it.word) && (it.word == word || it.word == reversed) }
        if (matched != null) {
            foundWords.add(matched.word)
            for ((r, c) in selectedCells) {
                markCellFound(r, c)
            }
            selectedCells.clear()
            updateWordList()
            checkLevelComplete()
        }
    }

    private fun markCellFound(row: Int, col: Int) {
        val gridLayout = binding.gridContainer.getChildAt(0) as? android.widget.GridLayout
        val index = row * grid.size + col
        gridLayout?.getChildAt(index)?.let { cell ->
            cell.setBackgroundResource(R.drawable.cell_found)
            (cell as TextView).setTextColor(android.graphics.Color.WHITE)
        }
    }

    private fun updateCellBackground(row: Int, col: Int, selected: Boolean) {
        val gridLayout = binding.gridContainer.getChildAt(0) as? android.widget.GridLayout
        val index = row * grid.size + col
        gridLayout?.getChildAt(index)?.let { cell ->
            if (selected) {
                cell.setBackgroundResource(R.drawable.cell_selected)
            } else {
                cell.setBackgroundResource(R.drawable.cell_background)
            }
        }
    }

    private fun updateWordList() {
        (binding.rvWords.adapter as? WordListAdapter)?.notifyDataSetChanged()
    }

    private fun checkLevelComplete() {
        if (foundWords.size == words.size) {
            val timeElapsed = System.currentTimeMillis() - startTime
            completeLevel(timeElapsed)
        }
    }

    private fun completeLevel(timeElapsed: Long) {
        currentLevel?.let { level ->
            repository.completeLevel(level.id, timeElapsed, hintsUsed)
            Toast.makeText(requireContext(), "Level Complete! +${when { hintsUsed == 0 && timeElapsed < 60000 -> 50; hintsUsed <= 1 -> 30; else -> 10 }} coins", Toast.LENGTH_LONG).show()
            findNavController().navigateUp()
        }
    }

    private fun useHint() {
        if (!repository.spendCoins(10)) {
            Toast.makeText(requireContext(), "Not enough coins!", Toast.LENGTH_SHORT).show()
            return
        }

        val unfound = words.find { !foundWords.contains(it.word) }
        unfound?.let { word ->
            hintsUsed++
            val (sr, sc, dir) = word.startRow to word.startCol to word.direction
            val dr = WordSearchGenerator.directions[dir][0]
            val dc = WordSearchGenerator.directions[dir][1]

            var r = sr
            var c = sc
            for (i in 0 until word.word.length) {
                markCellFound(r, c)
                r += dr
                c += dc
            }
            foundWords.add(word.word)
            updateWordList()
            updateCoins()
            checkLevelComplete()
        }
    }

    private fun shuffleGrid() {
        // Shuffle remaining letters in grid (visual only)
        Toast.makeText(requireContext(), "Grid shuffled!", Toast.LENGTH_SHORT).show()
    }

    private fun updateCoins() {
        binding.tvCoins.text = "🪙 ${repository.gameState.value.coins}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        timer?.cancel()
        _binding = null
    }
}

class WordListAdapter(
    private val words: List<String>,
    private val foundWords: Set<String>
) : RecyclerView.Adapter<WordListAdapter.WordViewHolder>() {

    class WordViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvWord: TextView = view.findViewById(R.id.tvWord)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_word, parent, false)
        return WordViewHolder(view)
    }

    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        val word = words[position]
        val found = foundWords.contains(word)
        holder.tvWord.text = if (found) "✓ $word" else word
        holder.tvWord.alpha = if (found) 0.5f else 1f
        holder.tvWord.paintFlags = if (found) holder.tvWord.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG else holder.tvWord.paintFlags and android.graphics.Paint.STRIKE_THRU_TEXT_FLAG.inv()
    }

    override fun getItemCount() = words.size
}