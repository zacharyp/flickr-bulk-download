package org.zachary.flickr

import akka.stream.ActorMaterializer
import org.json4s.JsonDSL._
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization.write

import scala.concurrent.Future

class PhotoSaver()(implicit val actorMaterializer: ActorMaterializer) {

  implicit val formats = DefaultFormats

  def savePhoto(photo: FlickrPhoto): Future[FlickrPhoto] = {

    println(s"${write(photo)}")

    Future.successful(photo)
  }
}
