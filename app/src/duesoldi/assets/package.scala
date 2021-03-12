package duesoldi

package object assets {
  type StoreAsset = (String, Stream[Byte]) => Either[String, Unit]
}
