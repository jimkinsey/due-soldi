package duesoldi

package object events {
  type Emit = Any => Unit
  val noopEmit: Emit = _ => {}
}
