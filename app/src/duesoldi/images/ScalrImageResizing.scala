package duesoldi.images

import org.imgscalr.Scalr

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import javax.imageio.ImageIO

object ScalrImageResizing {

  def imageResize(): ImageResize = { (data, newWidth) =>
    try {
      val bais = new ByteArrayInputStream(data.toArray)
      val original = ImageIO.read(bais)

      println(s"Resizing image to width $newWidth px...")
      val resized = Scalr.resize(original, Scalr.Method.ULTRA_QUALITY, newWidth)

      val baos = new ByteArrayOutputStream
      ImageIO.write(resized, "jpg", baos)

      Right(baos.toByteArray.toStream)
    }
    catch { case t: Throwable =>
      t.printStackTrace()
      Left(t.getMessage)
    }
  }

}
