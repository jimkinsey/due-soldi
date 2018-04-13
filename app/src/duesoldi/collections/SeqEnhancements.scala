package duesoldi.collections

object SeqEnhancements
{
  implicit class EnhancedSeq[T](seq: Seq[T])
  {
    def asSeqOf[X](implicit tToX: Coercion[T,X]): Seq[X] = seq.flatMap(tToX.coerce)
  }
}
