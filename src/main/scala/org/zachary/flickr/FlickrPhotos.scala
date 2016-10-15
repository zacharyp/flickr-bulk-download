package org.zachary.flickr

import akka.actor.Actor
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import com.flickr4java.flickr.RequestContext
import com.flickr4java.flickr.people.User
import com.flickr4java.flickr.photos.SearchParameters

import scala.collection.JavaConverters._
import scala.concurrent.Future

object FlickrPhotos {

  case object RetrieveAll

}

class FlickrPhotos(flickrContext: FlickrContext, implicit val actorMaterializer: ActorMaterializer) extends Actor {

  import FlickrPhotos._
  import context.system

  lazy val photoSaver = new PhotoSaver(flickrContext)

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
          searchParameters.setExtras(Set("original_format").asJava)
          photosInterface.search(searchParameters, pageSize, page)
        })
        .mapConcat(photoList => photoList.asScala.toList)
        //        .throttle(10, 1.seconds, 2, ThrottleMode.shaping)
        .map(photo => {
        FlickrPhoto(
          photo,
          photo.getId,
          photo.getTitle,
          Option(photo.getDescription),
          photo.getOriginalFormat
        )
      })
        .mapAsync(1)(p => {
          photoSaver.savePhoto(p)
        })
        .runWith(Sink.ignore)
  }
}
