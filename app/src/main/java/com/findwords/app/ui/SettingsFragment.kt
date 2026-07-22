package com.findwords.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.Switch
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.findwords.app.R
import com.findwords.app.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupRecyclerView()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView() {
        val settings = listOf(
            SettingItem("Sound", "Enable game sounds", true) { enabled ->
                // Save sound preference
            },
            SettingItem("Vibration", "Enable haptic feedback", true) { enabled ->
                // Save vibration preference
            },
            SettingItem("Language", "English", false, true) { },
            SettingItem("Rate App", "Rate us on Play Store", false, true) { },
            SettingItem("Privacy Policy", "Read our privacy policy", false, true) { },
            SettingItem("Terms of Service", "Read terms of service", false, true) { },
            SettingItem("Version 1.0.0", "Build 1", false, false) { }
        )

        binding.settingsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.settingsRecyclerView.adapter = SettingsAdapter(settings)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

data class SettingItem(
    val title: String,
    val subtitle: String,
    val isToggle: Boolean = false,
    val isClickable: Boolean = false,
    val defaultValue: Boolean = true,
    val onToggle: (Boolean) -> Unit = {}
)

class SettingsAdapter(private val items: List<SettingItem>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val TYPE_TOGGLE = 0
        const val TYPE_CLICKABLE = 1
        const val TYPE_INFO = 2
    }

    override fun getItemViewType(position: Int): Int {
        val item = items[position]
        return when {
            item.isToggle -> TYPE_TOGGLE
            item.isClickable -> TYPE_CLICKABLE
            else -> TYPE_INFO
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_TOGGLE -> ToggleViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_setting_toggle, parent, false)
            )
            TYPE_CLICKABLE -> ClickableViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_setting_clickable, parent, false)
            )
            else -> InfoViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_setting_info, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        when (holder) {
            is ToggleViewHolder -> holder.bind(item)
            is ClickableViewHolder -> holder.bind(item)
            is InfoViewHolder -> holder.bind(item)
        }
    }

    override fun getItemCount() = items.size

    class ToggleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val title: TextView = view.findViewById(R.id.tvTitle)
        private val subtitle: TextView = view.findViewById(R.id.tvSubtitle)
        private val toggle: Switch = view.findViewById(R.id.switchToggle)

        fun bind(item: SettingItem) {
            title.text = item.title
            subtitle.text = item.subtitle
            toggle.isChecked = item.defaultValue
            toggle.setOnCheckedChangeListener { _, isChecked ->
                item.onToggle(isChecked)
            }
        }
    }

    class ClickableViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val title: TextView = view.findViewById(R.id.tvTitle)
        private val subtitle: TextView = view.findViewById(R.id.tvSubtitle)

        fun bind(item: SettingItem) {
            title.text = item.title
            subtitle.text = item.subtitle
            itemView.setOnClickListener {
                // Handle click
            }
        }
    }

    class InfoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val title: TextView = view.findViewById(R.id.tvTitle)
        private val subtitle: TextView = view.findViewById(R.id.tvSubtitle)

        fun bind(item: SettingItem) {
            title.text = item.title
            subtitle.text = item.subtitle
        }
    }
}