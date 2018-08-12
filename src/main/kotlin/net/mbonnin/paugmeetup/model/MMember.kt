package net.mbonnin.paugmeetup.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MMember(val id: String, val name: String)