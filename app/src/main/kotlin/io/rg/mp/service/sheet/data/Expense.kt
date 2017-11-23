package io.rg.mp.service.sheet.data

import io.rg.mp.persistence.entity.Category

data class Expense(
        val date: String,
        val amount: Float,
        val description: String,
        val category: Category) {

    fun asCellsList() = listOf(
            date,
            amount,
            description,
            category.name
    )
}