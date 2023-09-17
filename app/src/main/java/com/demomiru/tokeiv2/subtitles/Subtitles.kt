package com.demomiru.tokeiv2.subtitles



data class Subtitle(
    val data: List<Sub>
)

data class Sub(
    val id : String,
    val type:String,
    val attributes: Attribute
)

data class Attribute(
    val ratings:String,
    val files: List<SubFile>
)
data class SubFile(
    val file_id:String
)
