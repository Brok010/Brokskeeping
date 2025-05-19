import java.util.Date

data class SupplementedFeed(
    val id: Int,
    val hiveId: Int,
    val date: Date,
    val kilos: Double
)
