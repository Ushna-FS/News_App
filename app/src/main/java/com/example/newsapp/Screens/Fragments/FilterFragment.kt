//package com.example.newsapp.Screens.Fragments
//
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.fragment.app.Fragment
//import com.example.newsapp.R
//import com.example.newsapp.databinding.FragmentFilterBinding
//
//class FilterFragment : Fragment() {
//
//    private lateinit var binding: FragmentFilterBinding
//    private var filterListener: FilterListener? = null
//
//    interface FilterListener {
//        fun onCategorySelected(category: String)
//        fun onSourceSelected(sources: List<String>)
//        fun onApplyFilters(categories: List<String>, sources: List<String>)
//        fun onCloseFilter()
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        binding = FragmentFilterBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        setupCategoryChips()
//        setupSourceChips()
//        setupButtons()
//    }
//
//    private fun setupCategoryChips() {
//        val categories = listOf("Business", "Technology")
//
//        categories.forEach { category ->
//            val chip = com.google.android.material.chip.Chip(requireContext()).apply {
//                text = category
//                isCheckable = true
//                isChecked = category == "Business" || category == "Technology"
//                setOnCheckedChangeListener { _, isChecked ->
//                    // Handle category selection
//                }
//            }
//            binding.chipGroupCategory.addView(chip)
//        }
//    }
//
//    private fun setupSourceChips() {
//        val sources = listOf(
//            "CNN", "BBC News", "TechCrunch", "Bloomberg",
//            "The Wall Street Journal", "Reuters", "CNBC"
//        )
//
//        sources.forEach { source ->
//            val chip = com.google.android.material.chip.Chip(requireContext()).apply {
//                text = source
//                isCheckable = true
//                setOnCheckedChangeListener { _, isChecked ->
//                    // Handle source selection
//                }
//            }
//            binding.chipGroupSources.addView(chip)
//        }
//    }
//
//    private fun setupButtons() {
//        binding.btnApply.setOnClickListener {
//            val selectedCategories = getSelectedCategories()
//            val selectedSources = getSelectedSources()
//            filterListener?.onApplyFilters(selectedCategories, selectedSources)
//        }
//
//        binding.btnClear.setOnClickListener {
//            clearAllSelections()
//        }
//
//        binding.btnClose.setOnClickListener {
//            filterListener?.onCloseFilter()
//        }
//    }
//
//    private fun getSelectedCategories(): List<String> {
//        val selected = mutableListOf<String>()
//        for (i in 0 until binding.chipGroupCategory.childCount) {
//            val chip = binding.chipGroupCategory.getChildAt(i) as com.google.android.material.chip.Chip
//            if (chip.isChecked) {
//                selected.add(chip.text.toString())
//            }
//        }
//        return selected
//    }
//
//    private fun getSelectedSources(): List<String> {
//        val selected = mutableListOf<String>()
//        for (i in 0 until binding.chipGroupSources.childCount) {
//            val chip = binding.chipGroupSources.getChildAt(i) as com.google.android.material.chip.Chip
//            if (chip.isChecked) {
//                selected.add(chip.text.toString())
//            }
//        }
//        return selected
//    }
//
//    private fun clearAllSelections() {
//        for (i in 0 until binding.chipGroupCategory.childCount) {
//            (binding.chipGroupCategory.getChildAt(i) as com.google.android.material.chip.Chip).isChecked = false
//        }
//        for (i in 0 until binding.chipGroupSources.childCount) {
//            (binding.chipGroupSources.getChildAt(i) as com.google.android.material.chip.Chip).isChecked = false
//        }
//
//        // Re-check Business and Technology by default
//        (binding.chipGroupCategory.getChildAt(0) as? com.google.android.material.chip.Chip)?.isChecked = true
//        (binding.chipGroupCategory.getChildAt(1) as? com.google.android.material.chip.Chip)?.isChecked = true
//    }
//
//    fun setFilterListener(listener: FilterListener) {
//        this.filterListener = listener
//    }
//}




package com.example.newsapp.Screens.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.newsapp.ViewModels.NewsViewModel
import com.example.newsapp.databinding.FragmentFilterBinding

class FilterFragment : Fragment() {

    private lateinit var binding: FragmentFilterBinding
    private lateinit var newsViewModel: NewsViewModel  // FIXED
    private var filterListener: FilterListener? = null

    interface FilterListener {
        fun onFiltersApplied()
        fun onCloseFilter()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize ViewModel using requireActivity()
        newsViewModel = ViewModelProvider(requireActivity()).get(NewsViewModel::class.java)
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
                // Check if this category is selected
                newsViewModel.selectedCategories.value?.let { selectedCats ->
                    isChecked = selectedCats.contains(category)
                }
                setOnCheckedChangeListener { _, _ ->
                    applyFilters()
                }
            }
            binding.chipGroupCategory.addView(chip)
        }
    }

    private fun observeSources() {
        newsViewModel.availableSources.observe(viewLifecycleOwner, Observer { sources ->
            setupSourceChips(sources.toList())
        })
    }

    private fun setupSourceChips(sources: List<String>) {
        // Clear existing chips
        binding.chipGroupSources.removeAllViews()

        val sortedSources = sources.sorted()

        sortedSources.forEach { source ->
            val chip = com.google.android.material.chip.Chip(requireContext()).apply {
                text = source
                isCheckable = true
                // Check if this source is selected
                newsViewModel.selectedSources.value?.let { selectedSrcs ->
                    isChecked = selectedSrcs.contains(source)
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
            filterListener?.onCloseFilter()  // ADD THIS LINE
        }

        binding.btnClear.setOnClickListener {
            clearAllSelections()
            filterListener?.onCloseFilter()  // ADD THIS LINE
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
        newsViewModel.applyFilters(selectedCategories, selectedSources)
        filterListener?.onCloseFilter()  // Auto-close after applying
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