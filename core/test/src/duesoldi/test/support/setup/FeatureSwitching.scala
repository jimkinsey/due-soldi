package duesoldi.test.support.setup

object FeatureSwitching
{
  def featureEnabled(name: String): SyncSetup = _ => Map(s"FEATURE_$name" -> "on")
  def featureDisabled(name: String): SyncSetup = _ => Map(s"FEATURE_$name" -> "off")
}
