package org.zachary.flickr

import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import org.json4s.JsonDSL._
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization.write

import scala.concurrent.Future

class PhotoSaver(flickrContext: FlickrContext)(implicit val actorMaterializer: ActorMaterializer) {

  implicit val formats = DefaultFormats

  lazy val photosInterface = flickrContext.flickr.getPhotosInterface

  def savePhoto(photo: FlickrPhoto): Future[FlickrPhoto] = {


    val photoJson: String = write(photo)
    println(s"$photoJson")

    Source.single(photo)
      .mapAsync(1)(p => {


        Future.successful(p)
      })
      .runWith(Sink.ignore)
      .map(_ => photo)
  }
}
