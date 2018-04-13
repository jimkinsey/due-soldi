package sommelier.routing

import sommelier.Response

trait Rejection
{
  def response: Response
}
object Rejection
{
  def apply(r: Response): Rejection = new Rejection {
    override def response: Response = r
  }
}