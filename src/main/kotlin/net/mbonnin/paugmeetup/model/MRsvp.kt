package net.mbonnin.paugmeetup.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MRsvp(val response: String)