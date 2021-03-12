package duesoldi.assets

import com.amazonaws.services.s3.AmazonS3ClientBuilder

import java.io.{BufferedOutputStream, File, FileOutputStream}
import java.util.UUID

object S3AssetStore {

  def storeAsset(bucket: String): StoreAsset = (path, data) => {
    val key = path.dropWhile(_ == '/')
    try {
      val tempFile = new File(s"${UUID.randomUUID()}.tmp")
      val outputStream = new BufferedOutputStream(new FileOutputStream(tempFile))
      outputStream.write(data.toArray)
      outputStream.close()

      val s3 = AmazonS3ClientBuilder.standard.build
      try {
        println(s"Uploading data with key [$key] to bucket [$bucket]...")
        s3.putObject(bucket, key, tempFile)
      } finally {
        s3.shutdown()
        tempFile.delete()
      }
      Right({})
    }
    catch {
      case e: Throwable =>
        System.err.println(e.getMessage)
        e.printStackTrace()
        Left(s"Failed to store asset for key [$key] in S3 bucket [$bucket] - ${e.getMessage}")
    }
  }

}
