package sommelier

object JournalDataStore
{
  case class JournalData(name: String)

  def get(id: Int): Option[JournalData] = data.get(id)

  private lazy val data: Map[Int, JournalData] = Map(
    41305 -> JournalData("Feminist Review"),
    41265 -> JournalData("Journal of Information Technology"),
    41267 -> JournalData("Journal of International Business Studies")
  )
}