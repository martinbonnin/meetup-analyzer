package net.mbonnin.paugmeetup

import net.mbonnin.paugmeetup.model.MAttendee
import net.mbonnin.paugmeetup.model.MEvent

data class PaugEvent(val event: MEvent, val attendeeList: List<MAttendee>)
