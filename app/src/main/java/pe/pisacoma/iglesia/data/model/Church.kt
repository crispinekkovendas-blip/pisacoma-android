package pe.pisacoma.iglesia.data.model

import kotlinx.serialization.Serializable

/** The site's src/data/church.ts, exported to church.json. */
@Serializable
data class ScheduleItem(val day: String, val time: String, val suffix: String, val name: String)

@Serializable
data class Location(val lat: Double, val lng: Double, val district: String, val province: String, val region: String, val altitude: String)

@Serializable
data class Mission(val name: String, val address: String, val city: String)

@Serializable
data class HierarchyItem(val name: String, val place: String)

@Serializable
data class TimelineItem(val year: String, val text: String)

@Serializable
data class Pioneer(val name: String, val role: String, val bio: String)

@Serializable
data class Church(
    val schedule: List<ScheduleItem>,
    val location: Location,
    val mission: Mission,
    val hierarchy: List<HierarchyItem>,
    val timeline: List<TimelineItem>,
    val pioneers: List<Pioneer>,
)
