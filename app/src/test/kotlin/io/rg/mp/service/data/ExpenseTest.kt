package io.rg.mp.service.data

import io.rg.mp.persistence.entity.Category
import org.junit.Assert
import org.junit.Test
import java.util.*

class ExpenseTest {
    @Test
    fun `list of properties should represent cells in a spreadsheet`() {
        //given
        val date = Date()
        val category = Category("category", "id")
        val expense = Expense(date, 1.0f, "desc", category)

        //when
        val result = expense.asCellsList()

        Assert.assertTrue(result[0] == date )
        Assert.assertTrue(result[1] == 1.0f)
        Assert.assertTrue(result[2] == "desc" )
        Assert.assertTrue(result[3] == category.name )
    }
}