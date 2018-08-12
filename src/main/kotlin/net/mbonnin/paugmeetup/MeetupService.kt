package net.mbonnin.paugmeetup

import kotlinx.coroutines.experimental.Deferred
import net.mbonnin.paugmeetup.model.MAttendee
import net.mbonnin.paugmeetup.model.MEvent
import retrofit2.http.GET
import retrofit2.http.Path

interface MeetupService {
    @GET("/Android-Paris/events?status=past,upcoming&page=300")
    fun getEvents(): Deferred<List< MEvent>>

    @GET("/Android-Paris/events/{eventId}/attendance?page=300")
    fun getAttendance(@Path("eventId") eventId: String): Deferred<List<MAttendee>>
}