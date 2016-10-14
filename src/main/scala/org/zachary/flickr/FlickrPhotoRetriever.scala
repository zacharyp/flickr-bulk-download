package org.zachary.flickr

import akka.actor.Actor
import akka.stream.{ActorMaterializer, ThrottleMode}
import akka.stream.scaladsl.{Sink, Source}
import com.flickr4java.flickr.RequestContext
import com.flickr4java.flickr.photos.{Photo, PhotoList}
import org.zachary.flickr.FlickrAuthorization.FlickrContext

import scala.collection.JavaConverters._
import scala.concurrent.Future
import scala.concurrent.duration._

object FlickrPhotoRetriever {

  case object RetrieveAll

}

class FlickrPhotoRetriever(flickrContext: FlickrContext, implicit val actorMaterializer: ActorMaterializer) extends Actor {

  import FlickrPhotoRetriever._

  import context.dispatcher

  lazy val photoSaver = new PhotoSaver

  def receive = {
    case RetrieveAll => {

      val photosInterface = flickrContext.flickr.getPhotosInterface

      val requestContext: RequestContext = RequestContext.getRequestContext
      requestContext.setAuth(flickrContext.auth)

      val photoList: PhotoList[Photo] = photosInterface.getNotInSet(5, 1)

      Source.fromIterator(() => photoList.iterator.asScala)
        .throttle(10, 1.seconds, 2, ThrottleMode.shaping)
        .map(photo => {
          FlickrPhoto(
            photo.getId,
            photo.getTitle,
            Option(photo.getDescription),
            photo.getOriginalFormat
          )
        })
        .mapAsync(5)(p => {
          photoSaver.savePhoto(p)
        })
        .runWith(Sink.ignore)
    }
    case _ => System.out.println("wrong!")
  }
}
