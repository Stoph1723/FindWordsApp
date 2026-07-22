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
import com.findwords.app.databinding.FragmentPackSelectionBinding
import com.findwords.app.databinding.ItemPackBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PackSelectionFragment : Fragment() {

    private var _binding: FragmentPackSelectionBinding? = null
    private val binding get() = _binding!!
    private val repo: GameRepository by lazy { (requireActivity().application as App).repository }
    private val adapter = PackAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPackSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        loadPacks()
    }

    private fun setupRecyclerView() {
        binding.packsRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.packsRecyclerView.adapter = adapter
        adapter.onPackClick = { pack ->
            val action = PackSelectionFragmentDirections.actionPackSelectionToLevelSelection(pack.id)
            findNavController().navigate(action)
        }
    }

    private fun loadPacks() {
        lifecycleScope.launch(Dispatchers.IO) {
            val packs = repo.getPacks()
            requireActivity().runOnUiThread {
                adapter.submitList(packs)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class PackAdapter : androidx.recyclerview.widget.ListAdapter<GameRepository.PackInfo, PackAdapter.PackViewHolder>(
    androidx.recyclerview.widget.DiffUtil.ItemCallback<GameRepository.PackInfo>() {
        override fun areItemsTheSame(oldItem: GameRepository.PackInfo, newItem: GameRepository.PackInfo) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: GameRepository.PackInfo, newItem: GameRepository.PackInfo) = oldItem == newItem
    }
) {

    var onPackClick: ((GameRepository.PackInfo) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PackViewHolder {
        val binding = ItemPackBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PackViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PackViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PackViewHolder(private val binding: ItemPackBinding) : androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {
        fun bind(pack: GameRepository.PackInfo) {
            binding.packName.text = pack.name
            binding.packProgress.text = "${pack.completedLevels}/${pack.totalLevels}"
            binding.progressBar.progress = (pack.progress * 100).toInt()
            binding.difficultyStars.text = "★".repeat(pack.difficulty)
            binding.root.setOnClickListener { onPackClick?.invoke(pack) }

            val themeColor = when (pack.theme) {
                "animals" -> 0xFF8D6E63
                "food" -> 0xFFFF8A65
                "geography" -> 0xFF64B5F6
                "sports" -> 0xFF81C784
                "science" -> 0xFFBA68C8
                else -> 0xFF78909C
            }
            binding.cardView.setCardBackgroundColor(themeColor)
        }
    }
}