package com.example.newsapp.screens.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.newsapp.viewmodels.NewsViewModel
import com.example.newsapp.adapters.SourcesAdapter
import com.example.newsapp.databinding.FragmentFilterBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import com.example.newsapp.R

@AndroidEntryPoint
class FilterFragment : Fragment() {

    private lateinit var binding: FragmentFilterBinding
    private val newsViewModel: NewsViewModel by viewModels({ requireActivity() })
    private lateinit var sourcesAdapter: SourcesAdapter
    private var filterListener: FilterListener? = null

    interface FilterListener {
        fun onFiltersApplied()
        fun onCloseFilter()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCategoryChips()
        setupSourcesRecycler()
        observeSources()
        setupButtons()
    }

    // -------------------- SOURCES RECYCLER --------------------
    private fun setupSourcesRecycler() {
        sourcesAdapter = SourcesAdapter { selected ->
            // Immediately update ViewModel when selection changes
            newsViewModel.applyFilters(
                getSelectedCategories(), selected
            )
        }

        binding.rvSources.apply {
            layoutManager = GridLayoutManager(
                requireContext(), 3, // Show 3 chips per row for better visibility
                GridLayoutManager.HORIZONTAL, false
            )
            adapter = sourcesAdapter
        }
    }

    private fun observeSources() {
        lifecycleScope.launch {
            // Collect both available sources and selected sources
            newsViewModel.availableSources.collect { sources ->
                sourcesAdapter.submitList(
                    sources.sorted(), newsViewModel.selectedSources.value // Already a List
                )
            }
        }
    }

    // -------------------- CATEGORY CHIPS --------------------
    private fun setupCategoryChips() {
        val categories = listOf(getString(R.string.business), getString(R.string.technology))

        binding.chipGroupCategory.removeAllViews()
        categories.forEach { category ->
            val chip = com.google.android.material.chip.Chip(requireContext()).apply {
                text = category
                isCheckable = true
                val selectedCategories = newsViewModel.selectedCategories.value
                isChecked =
                    selectedCategories.contains(category) // Only highlight if user applied filter

                // Update visual state
                updateCategoryChipVisual(this, isChecked)

                setOnCheckedChangeListener { _, isChecked ->
                    updateCategoryChipVisual(this, isChecked)
                }
            }
            binding.chipGroupCategory.addView(chip)
        }
    }

    @SuppressLint("UseCompatLoadingForColorStateLists")
    private fun updateCategoryChipVisual(
        chip: com.google.android.material.chip.Chip, isSelected: Boolean
    ) {
        if (isSelected) {
            chip.chipBackgroundColor = resources.getColorStateList(R.color.blueMain)
            chip.setTextColor(resources.getColor(R.color.white))
        } else {
            chip.chipBackgroundColor = resources.getColorStateList(R.color.white)
            chip.setTextColor(resources.getColor(R.color.blueMain))
        }
        chip.chipStrokeColor = resources.getColorStateList(R.color.blueMain)
    }

    private fun getSelectedCategories(): List<String> {
        val selected = mutableListOf<String>()
        for (i in 0 until binding.chipGroupCategory.childCount) {
            val chip =
                binding.chipGroupCategory.getChildAt(i) as com.google.android.material.chip.Chip
            if (chip.isChecked) selected.add(chip.text.toString())
        }
        return selected
    }

    // -------------------- BUTTONS --------------------
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

    private fun applyFilters() {
        // Get selected categories
        val selectedCategories = getSelectedCategories()

        // Get selected sources from adapter
        val selectedSources = sourcesAdapter.getSelectedSources()

        // Apply filters to ViewModel
        newsViewModel.applyFilters(selectedCategories, selectedSources)
    }

    private fun clearAllSelections() {
        // Clear category chips
        for (i in 0 until binding.chipGroupCategory.childCount) {
            val chip =
                binding.chipGroupCategory.getChildAt(i) as com.google.android.material.chip.Chip
            chip.isChecked = false
            updateCategoryChipVisual(chip, false)
        }

        // Clear sources selection
        sourcesAdapter.clearSelections()

        // Apply cleared filters
        applyFilters()
    }

    fun setFilterListener(listener: FilterListener) {
        this.filterListener = listener
    }
}







