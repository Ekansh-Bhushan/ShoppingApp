package eu.ekansh.shoppinglist

data class ShoppingItem (
    val id : Int,
    var name : String,
    var quantity : Int,
    val isEdited : Boolean = false
)