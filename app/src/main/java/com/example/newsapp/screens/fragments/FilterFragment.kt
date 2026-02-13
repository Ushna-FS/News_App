package com.example.newsapp.screens.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.viewmodels.NewsViewModel
import com.example.newsapp.adapters.SourcesAdapter
import com.example.newsapp.databinding.FragmentFilterBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import com.example.newsapp.R
import com.example.newsapp.utils.ChipUiUtil
import com.google.android.material.chip.Chip

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
        setupCategoryChips()
        setupSourcesRecycler()
        observeSources()
        setupButtons()
    }


    // ---------------- SOURCES ----------------
//
    private fun setupSourcesRecycler() {
        sourcesAdapter = SourcesAdapter { source, checked ->
            newsViewModel.toggleSource(source, checked)
        }

        binding.rvSources.apply {
            layoutManager = GridLayoutManager(requireContext(), 3) // 3 columns vertical grid
            adapter = sourcesAdapter


            // Scrollable with 2 rows visible initially
            post {
                val child = getChildAt(0)
                if (child != null) {
                    val chipHeight =
                        child.height + (child.layoutParams as RecyclerView.LayoutParams).topMargin + (child.layoutParams as RecyclerView.LayoutParams).bottomMargin
                    val maxHeight = chipHeight * 2
                    layoutParams.height = maxHeight
                    requestLayout()
                }
            }

            overScrollMode = RecyclerView.OVER_SCROLL_ALWAYS
        }
    }

    private fun observeSources() {

        lifecycleScope.launch {
            newsViewModel.availableSources.collect { sources ->
                sourcesAdapter.submitList(sources.toList())
            }
        }

        lifecycleScope.launch {
            newsViewModel.selectedSources.collect { selected ->
                sourcesAdapter.setSelectedSources(selected.toSet())
            }
        }
    }

    // ---------------- CATEGORY CHIPS ----------------

    private fun setupCategoryChips() {
        val categories = listOf(
            getString(R.string.business),
            getString(R.string.technology)
        )

        binding.chipGroupCategory.removeAllViews()

        categories.forEach { category ->
            val chip = Chip(requireContext()).apply {
                text = category
                isCheckable = true

                val selected = newsViewModel.selectedCategories.value
                isChecked = selected.contains(category)

                ChipUiUtil.updateChipVisual(this, isChecked)

                setOnCheckedChangeListener { _, checked ->
                    ChipUiUtil.updateChipVisual(this, checked)
                }
            }

            binding.chipGroupCategory.addView(chip)
        }
    }

    private fun getSelectedCategories(): List<String> {
        val selected = mutableListOf<String>()

        for (i in 0 until binding.chipGroupCategory.childCount) {
            val chip = binding.chipGroupCategory.getChildAt(i)
                    as Chip

            if (chip.isChecked) selected.add(chip.text.toString())
        }

        return selected
    }

    // ---------------- BUTTONS ----------------

    private fun setupButtons() {

        binding.btnApply.setOnClickListener {
            newsViewModel.applyFilters(
                getSelectedCategories(),
                newsViewModel.selectedSources.value
            )
            filterListener?.onFiltersApplied()
            filterListener?.onCloseFilter()
        }

        binding.btnClear.setOnClickListener {
            clearAllSelections()
        }

        binding.btnClose.setOnClickListener {
            filterListener?.onCloseFilter()
        }
    }

    private fun clearAllSelections() {

        // clear categories
        for (i in 0 until binding.chipGroupCategory.childCount) {
            val chip = binding.chipGroupCategory.getChildAt(i)
                    as Chip
            chip.isChecked = false
            ChipUiUtil.updateChipVisual(chip, false)

        }

        // clear sources via ViewModel
        newsViewModel.applyFilters(emptyList(), emptyList())

        filterListener?.onCloseFilter()
    }

    fun setFilterListener(listener: FilterListener) {
        filterListener = listener
    }
}