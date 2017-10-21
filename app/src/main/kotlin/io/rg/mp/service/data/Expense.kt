package io.rg.mp.service.data

import io.rg.mp.persistence.entity.Category
import java.util.*

data class Expense(
        val date: Date,
        val amount: Float,
        val description: String,
        val category: Category)