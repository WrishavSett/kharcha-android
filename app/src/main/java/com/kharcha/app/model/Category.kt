package com.kharcha.app.model

enum class Category(val id: String, val title: String, val label: String) {
    Food("food", "Food & dining", "Food"),
    Groceries("groceries", "Groceries", "Groceries"),
    Transport("transport", "Transport", "Transport"),
    Bills("bills", "Bills & rent", "Bills"),
    Shopping("shopping", "Shopping", "Shopping"),
    Health("health", "Health", "Health"),
    Fun("fun", "Entertainment", "Fun"),
    Other("other", "Other", "Other");

    companion object {
        fun of(id: String): Category = entries.firstOrNull { it.id == id } ?: Other
    }
}

enum class PayMethod(val label: String) {
    Upi("UPI"),
    Card("Card"),
    Cash("Cash");

    companion object {
        fun of(label: String?): PayMethod = entries.firstOrNull { it.label == label } ?: Upi
    }
}
