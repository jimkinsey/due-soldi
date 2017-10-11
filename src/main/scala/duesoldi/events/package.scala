package duesoldi

package object events {
  type Emit = Event => Unit
  val noopEmit: Emit = Event => {}
}
