package duesoldi.dependencies

object ContextualDependencies {
  type ContextualDependency[DEP,CTX] = CTX => DEP
  class DependentBlock[DEP,CTX] {
    def apply[RES](block: DEP => RES)(implicit dependency: ContextualDependency[DEP,CTX], context: CTX): RES = {
      block(dependency(context))
    }
  }
  def withDependency[DEP,CTX]: DependentBlock[DEP,CTX] = new DependentBlock[DEP,CTX]
}
