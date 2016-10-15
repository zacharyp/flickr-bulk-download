package org.zachary.flickr

import akka.actor.Actor
import akka.stream.{ActorMaterializer, ThrottleMode}
import akka.stream.scaladsl.{Sink, Source}
import com.flickr4java.flickr.RequestContext
import com.flickr4java.flickr.people.{PeopleInterface, User}
import com.flickr4java.flickr.photos.{Photo, PhotoList, SearchParameters}
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

  private val pageSize = 100

  def receive = {
    case RetrieveAll =>

      val photosInterface = flickrContext.flickr.getPhotosInterface
      val peopleInterface = flickrContext.flickr.getPeopleInterface

      val requestContext: RequestContext = RequestContext.getRequestContext
      requestContext.setAuth(flickrContext.auth)

      val userId: String = flickrContext.auth.getUser.getId
      val user: User = peopleInterface.getInfo(userId)

      println(s"photo count: ${user.getPhotosCount}")

      Source(1 to (user.getPhotosCount / pageSize + 1))
        .mapAsync(2)(page => Future.successful {
          val searchParameters = new SearchParameters
          searchParameters.setUserId(userId)
          photosInterface.search(searchParameters, pageSize, page)
        })
        .mapConcat(photoList => photoList.asScala.toList)
        //        .throttle(10, 1.seconds, 2, ThrottleMode.shaping)
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
}
