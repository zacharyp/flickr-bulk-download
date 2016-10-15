package org.zachary.flickr

import java.io.{File, InputStream}
import java.nio.file.{Files, Path, Paths}

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import com.flickr4java.flickr.photos.{PhotoAllContext, Size}
import org.apache.commons.io.IOUtils
import org.json4s._
import org.json4s.jackson.Serialization.write

import scala.collection.JavaConverters._
import scala.concurrent.Future
import scala.util.control.NonFatal

object PhotoSaver {

  object PhotoJson {
    def apply(flickrPhoto: FlickrPhoto): PhotoJson = {
      PhotoJson(
        flickrPhoto.id,
        flickrPhoto.title,
        flickrPhoto.description,
        flickrPhoto.tags,
        flickrPhoto.originalFormat
      )
    }
  }

  case class PhotoJson(
    id: String,
    title: String,
    description: String,
    tags: List[String],
    originalFormat: String
  )

}

class PhotoSaver(flickrContext: FlickrContext)(implicit val actorM: ActorMaterializer, actorSystem: ActorSystem) {

  import PhotoSaver._
  import actorSystem.dispatcher

  implicit val formats = DefaultFormats

  lazy val photosInterface = flickrContext.flickr.getPhotosInterface

  private val logger: LoggingAdapter = Logging.getLogger(actorSystem, this)

  def savePhoto(photo: FlickrPhoto): Future[FlickrPhoto] = {

    Source.single(photo)
      .mapAsync(1)(p => {

        Future.successful {
          val photoAllContext: PhotoAllContext = photosInterface.getAllContexts(photo.id)

          val setTitle: String =
            photoAllContext.getPhotoSetList.asScala.map(s => s.getTitle).headOption.map(_ + "/").getOrElse("")

          try {
            val inputStream: InputStream = photosInterface.getImageAsStream(p.photo, Size.ORIGINAL)

            val byteArray: Array[Byte] = IOUtils.toByteArray(inputStream)
            inputStream.close()

            val directoryFile: File = Paths.get(flickrContext.directory, setTitle).toFile

            if (!directoryFile.exists()) {
              directoryFile.mkdirs()
            }

            val imageName: String = s"${photo.id}.${photo.originalFormat}"
            val path: Path = Paths.get(flickrContext.directory, setTitle, imageName)
            Files.write(path, byteArray)
            println(s"Wrote image to: ${path.toString}")

            val jsonPath: Path = Paths.get(flickrContext.directory, setTitle, imageName + ".json")
            Files.write(jsonPath, write(PhotoJson(photo)).getBytes)
            println(s"Wrote image meta data to: ${jsonPath.toString}")
          } catch {
            case NonFatal(ex) => logger.warning(ex.getMessage, ex)
          }
        }.map(_ => p)
      })
      .runWith(Sink.ignore)
      .map(_ => photo)
  }
}
