package cicerone.test.support

object CustomMatchers
{
  implicit class EitherAssertions[L,R](either: Either[L,R])
  {
    def isLeftOf(value: L): Boolean = either.left.toOption.contains(value)
    def isRightOf(value: R): Boolean = either.toOption.contains(value)
    def isRightWhere(pred: R => Boolean): Boolean = either.toOption.exists(pred)
  }
}
