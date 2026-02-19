package com.example.newsapp.utils


import androidx.core.content.ContextCompat
import com.google.android.material.chip.Chip
import com.google.android.material.color.MaterialColors
import com.example.newsapp.R

object ChipUiUtil {

    fun updateChipVisual(chip: Chip, selected: Boolean) {

        if (selected) {
            chip.chipBackgroundColor =
                ContextCompat.getColorStateList(chip.context, R.color.chip_selected_bg)

            chip.setTextColor(
                ContextCompat.getColor(chip.context, R.color.chip_selected_text)
            )
        } else {
            chip.chipBackgroundColor =
                ContextCompat.getColorStateList(chip.context, R.color.chip_unselected_bg)

            chip.setTextColor(
                MaterialColors.getColor(
                    chip,
                    com.google.android.material.R.attr.colorOnSurface
                )
            )
        }

        chip.chipStrokeColor =
            ContextCompat.getColorStateList(chip.context, R.color.blueMain)
    }
}
