package net.mbonnin.paugmeetup.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MEvent(val created: Long, val name: String, val id: String, val manual_attendance_count: Int?, val rsvp_limit: Int?, val yes_rsvp_count: Int)