package com.example.newsapp.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.R
import com.google.android.material.chip.Chip

class SourcesAdapter(
    private val onSelectionChanged: (List<String>) -> Unit
) : RecyclerView.Adapter<SourcesAdapter.SourceViewHolder>() {

    private val sourcesList = mutableListOf<String>()
    private val selectedSources = mutableSetOf<String>() // Track selection in adapter

    // Expose selected sources for external access
    fun getSelectedSources(): List<String> = selectedSources.toList()

    // Clear all selections
    fun clearSelections() {
        selectedSources.clear()
        notifyDataSetChanged()
    }

    inner class SourceViewHolder(val chip: Chip) : RecyclerView.ViewHolder(chip)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SourceViewHolder {
        val chip = LayoutInflater.from(parent.context)
            .inflate(R.layout.source_chip, parent, false) as Chip
        return SourceViewHolder(chip)
    }

    override fun getItemCount() = sourcesList.size

    override fun onBindViewHolder(holder: SourceViewHolder, position: Int) {
        val source = sourcesList[position]
        val chip = holder.chip

        chip.text = source
        chip.isChecked = selectedSources.contains(source)

        // Set initial visual state
        updateChipVisual(chip, selectedSources.contains(source))

        chip.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedSources.add(source)
            } else {
                selectedSources.remove(source)
            }
            updateChipVisual(chip, isChecked)
            onSelectionChanged(selectedSources.toList())
        }
    }

    private fun updateChipVisual(chip: Chip, isSelected: Boolean) {
        if (isSelected) {
            chip.setChipBackgroundColorResource(R.color.gray_light)
            chip.setTextColor(Color.WHITE)
            chip.chipStrokeColor = chip.context.resources.getColorStateList(R.color.white)
        } else {
            chip.setChipBackgroundColorResource(R.color.white)
            chip.setTextColor(Color.BLACK)
            chip.chipStrokeColor = chip.context.resources.getColorStateList(R.color.lightBlue)
        }
    }

    fun submitList(list: List<String>, selected: List<String>) {
        sourcesList.clear()
        sourcesList.addAll(list)

        selectedSources.clear()
        selectedSources.addAll(selected)

        notifyDataSetChanged()
    }
}
