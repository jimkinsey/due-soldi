package duesoldi.dependencies

import duesoldi.blog.dependencies.BlogDependencies
import duesoldi.debug.dependencies.AppDebugDependencies
import duesoldi.furniture.FurnitureCacheConfig
import duesoldi.gallery.GalleryDependencies
import duesoldi.metrics.dependencies.AccessRecordingDependencies

object DueSoldiDependencies
extends GalleryDependencies
with BlogDependencies
with AccessRecordingDependencies
with AppDebugDependencies
with FurnitureDependencies

trait FurnitureDependencies {
  import duesoldi.dependencies.Injection.Inject

  implicit val furnitureCacheConfig: Inject[FurnitureCacheConfig] = config => FurnitureCacheConfig(config.furnitureCacheDurationHours)
}