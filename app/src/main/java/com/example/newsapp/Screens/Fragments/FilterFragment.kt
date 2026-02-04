package com.example.newsapp.Screens.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.newsapp.ViewModels.NewsViewModel
import com.example.newsapp.databinding.FragmentFilterBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FilterFragment : Fragment() {

    private lateinit var binding: FragmentFilterBinding
    private val newsViewModel: NewsViewModel by viewModels({ requireActivity() })
    private var filterListener: FilterListener? = null

    interface FilterListener {
        fun onFiltersApplied()
        fun onCloseFilter()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCategoryChips()
        observeSources()
        setupButtons()
    }

    private fun setupCategoryChips() {
        val categories = listOf("Business", "Technology")

        // Clear existing chips
        binding.chipGroupCategory.removeAllViews()

        categories.forEach { category ->
            val chip = com.google.android.material.chip.Chip(requireContext()).apply {
                text = category
                isCheckable = true

                // Check if this category is selected using StateFlow value
                if (newsViewModel.selectedCategories.value.contains(category)) {
                    isChecked = true
                }

                setOnCheckedChangeListener { _, _ ->
                    applyFilters()
                }
            }
            binding.chipGroupCategory.addView(chip)
        }
    }

    private fun observeSources() {
        // Use lifecycleScope to collect StateFlow
        lifecycleScope.launch {
            newsViewModel.availableSources.collect { sources ->
                setupSourceChips(sources.toList())
            }
        }
    }

    private fun setupSourceChips(sources: List<String>) {
        // Clear existing chips
        binding.chipGroupSources.removeAllViews()

        val sortedSources = sources.sorted()

        sortedSources.forEach { source ->
            val chip = com.google.android.material.chip.Chip(requireContext()).apply {
                text = source
                isCheckable = true

                // Check if this source is selected using StateFlow value
                if (newsViewModel.selectedSources.value.contains(source)) {
                    isChecked = true
                }

                setOnCheckedChangeListener { _, _ ->
                    applyFilters()
                }
            }
            binding.chipGroupSources.addView(chip)
        }
    }

    private fun setupButtons() {
        binding.btnApply.setOnClickListener {
            applyFilters()
            filterListener?.onFiltersApplied()
            filterListener?.onCloseFilter()
        }

        binding.btnClear.setOnClickListener {
            clearAllSelections()
            filterListener?.onCloseFilter()
        }

        binding.btnClose.setOnClickListener {
            filterListener?.onCloseFilter()
        }
    }

    private fun getSelectedCategories(): List<String> {
        val selected = mutableListOf<String>()
        for (i in 0 until binding.chipGroupCategory.childCount) {
            val chip = binding.chipGroupCategory.getChildAt(i) as com.google.android.material.chip.Chip
            if (chip.isChecked) {
                selected.add(chip.text.toString())
            }
        }
        return selected
    }

    private fun getSelectedSources(): List<String> {
        val selected = mutableListOf<String>()
        for (i in 0 until binding.chipGroupSources.childCount) {
            val chip = binding.chipGroupSources.getChildAt(i) as com.google.android.material.chip.Chip
            if (chip.isChecked) {
                selected.add(chip.text.toString())
            }
        }
        return selected
    }

    private fun applyFilters() {
        val selectedCategories = getSelectedCategories()
        val selectedSources = getSelectedSources()

        // Update ViewModel state
        newsViewModel.applyFilters(selectedCategories, selectedSources)
        filterListener?.onCloseFilter()
    }

    private fun clearAllSelections() {
        // Clear all chips
        for (i in 0 until binding.chipGroupCategory.childCount) {
            (binding.chipGroupCategory.getChildAt(i) as com.google.android.material.chip.Chip).isChecked = false
        }
        for (i in 0 until binding.chipGroupSources.childCount) {
            (binding.chipGroupSources.getChildAt(i) as com.google.android.material.chip.Chip).isChecked = false
        }

        // Check Business and Technology by default
        (binding.chipGroupCategory.getChildAt(0) as? com.google.android.material.chip.Chip)?.isChecked = true
        (binding.chipGroupCategory.getChildAt(1) as? com.google.android.material.chip.Chip)?.isChecked = true

        applyFilters()
    }

    fun setFilterListener(listener: FilterListener) {
        this.filterListener = listener
    }
}