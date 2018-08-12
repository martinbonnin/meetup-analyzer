package net.mbonnin.paugmeetup.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MAttendee(val member: MMember, val status: String?, val rsvp: MRsvp?)