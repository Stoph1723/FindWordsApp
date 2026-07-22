package com.findwords.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.findwords.app.R
import com.findwords.app.data.GameRepository
import com.findwords.app.databinding.FragmentLevelSelectionBinding
import com.findwords.app.databinding.ItemLevelBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LevelSelectionFragment : Fragment() {

    private var _binding: FragmentLevelSelectionBinding? = null
    private val binding get() = _binding!!
    private val repo: GameRepository by lazy { (requireActivity().application as App).repository }
    private val adapter = LevelAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLevelSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val packId = LevelSelectionFragmentArgs.fromBundle(requireArguments()).packId

        setupToolbar()
        setupRecyclerView()
        loadLevels(packId)
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView() {
        binding.levelsRecyclerView.layoutManager = GridLayoutManager(requireContext(), 5)
        binding.levelsRecyclerView.adapter = adapter
        adapter.onLevelClick = { level ->
            val action = LevelSelectionFragmentDirections.actionLevelSelectionToGame(level.id)
            findNavController().navigate(action)
        }
    }

    private fun loadLevels(packId: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            val levels = repo.getLevelsForPack(packId)
            requireActivity().runOnUiThread {
                adapter.submitList(levels)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class LevelAdapter : androidx.recyclerview.widget.ListAdapter<GameRepository.LevelInfo, LevelAdapter.LevelViewHolder>(
    androidx.recyclerview.widget.DiffUtil.ItemCallback<GameRepository.LevelInfo>() {
        override fun areItemsTheSame(oldItem: GameRepository.LevelInfo, newItem: GameRepository.LevelInfo) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: GameRepository.LevelInfo, newItem: GameRepository.LevelInfo) = oldItem == newItem
    }
) {

    var onLevelClick: ((GameRepository.LevelInfo) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LevelViewHolder {
        val binding = ItemLevelBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LevelViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LevelViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class LevelViewHolder(private val binding: ItemLevelBinding) : androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {
        fun bind(level: GameRepository.LevelInfo) {
            binding.levelNumber.text = level.levelNumber.toString()
            binding.root.isEnabled = level.isUnlocked
            binding.root.alpha = if (level.isUnlocked) 1f else 0.5f

            when {
                level.starsEarned == 3 -> binding.starIcon.setImageResource(R.drawable.ic_star_filled)
                level.starsEarned > 0 -> binding.starIcon.setImageResource(R.drawable.ic_star_half)
                else -> binding.starIcon.setImageResource(R.drawable.ic_star_empty)
            }

            binding.root.setOnClickListener {
                if (level.isUnlocked) onLevelClick?.invoke(level)
            }
        }
    }
}