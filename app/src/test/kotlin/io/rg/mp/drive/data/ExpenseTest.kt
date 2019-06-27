package io.rg.mp.drive.data

import io.rg.mp.persistence.entity.Category
import org.junit.Assert
import org.junit.Test

class ExpenseTest {
    @Test
    fun `list of properties should represent cells in a spreadsheet`() {
        //given
        val date = "01/01/2017"
        val category = Category("category", "", "", "", 0, "id")
        val expense = Expense(date, 1.0f, "desc", category)

        //when
        val result = expense.asCellsList()

        Assert.assertTrue(result[0] == date)
        Assert.assertTrue(result[1] == 1.0f)
        Assert.assertTrue(result[2] == "desc")
        Assert.assertTrue(result[3] == category.name)
    }
}