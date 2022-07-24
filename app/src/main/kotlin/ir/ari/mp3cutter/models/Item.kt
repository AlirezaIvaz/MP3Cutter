package ir.ari.mp3cutter.models

class Item(
    var icon: Int,
    var title: String,
    var onClick: ((Item) -> Unit)
)
