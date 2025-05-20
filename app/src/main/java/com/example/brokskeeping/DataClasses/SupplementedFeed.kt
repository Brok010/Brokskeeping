import java.util.Date

data class SupplementedFeed(
    val id: Int? = -1,
    val hiveId: Int? = -1,
    val date: Date? = Date(0),
    val kilos: Double? = -1.0
)
