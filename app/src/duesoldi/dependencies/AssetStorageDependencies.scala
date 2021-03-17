package duesoldi.dependencies

import duesoldi.assets.{S3AssetStore, StoreAsset}
import duesoldi.dependencies.Injection.Inject

trait AssetStorageDependencies {
  implicit lazy val storeAsset: Inject[StoreAsset] = config => S3AssetStore.storeAsset(config.assetBucket)
}
