package com.example.newsapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.R
import com.google.android.material.chip.Chip
import com.example.newsapp.utils.ChipUiUtil

class SourcesAdapter(
    private val onSourceToggled: (String, Boolean) -> Unit
) : ListAdapter<String, SourcesAdapter.SourceViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
                return oldItem == newItem
            }
        }
    }

    private var selectedSources: Set<String> = emptySet()

    fun setSelectedSources(selected: Set<String>) {
        selectedSources = selected
        notifyDataSetChanged()
    }

    class SourceViewHolder(val chip: Chip) : RecyclerView.ViewHolder(chip)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SourceViewHolder {
        val chip = LayoutInflater.from(parent.context)
            .inflate(R.layout.source_chip, parent, false) as Chip
        return SourceViewHolder(chip)
    }

    override fun onBindViewHolder(holder: SourceViewHolder, position: Int) {
        val source = getItem(position)
        val chip = holder.chip

        chip.setOnCheckedChangeListener(null)

        chip.text = source
        val isSelected = selectedSources.contains(source)
        chip.isChecked = isSelected
        ChipUiUtil.updateChipVisual(chip, isSelected)

        chip.setOnCheckedChangeListener { _, checked ->
            onSourceToggled(source, checked)
        }
    }
}
