package net.mbonnin.paugmeetup

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import net.mbonnin.paugmeetup.model.MAttendee
import net.mbonnin.paugmeetup.model.MEvent
import okio.Okio
import java.io.File

class PaugAnalyzer {

    data class Join(val event: MEvent, val attendee: MAttendee)
    fun run() {
        val type = Types.newParameterizedType(List::class.java, PaugEvent::class.java)
        val adapter = Moshi.Builder().build().adapter<List<PaugEvent>>(type)

        val buffer = Okio.buffer(Okio.source(File("paug.json")))
        val allEvents = adapter.fromJson(buffer)!!
        buffer.close()

        val flatList = allEvents.flatMap { event ->
            event.attendeeList.map { Join(event.event, it) }
        }

        val map = flatList.groupBy{ it.attendee.member.id }

        System.out.println("${map.size} unique attendees")

        val userIdToYes = flatList
                .filter {
                    it.attendee.rsvp?.response == "yes"
                }
                .groupBy { it.attendee.member.id }
                .toList()
                .sortedBy {
                    -it.second.size
                }
                .take(100)

        System.out.println("most yes:")
        userIdToYes.forEachIndexed { index, pair ->
            System.out.println("#$index: ${pair.second.first().attendee.member.name}: ${pair.second.size}")
        }



    }
}