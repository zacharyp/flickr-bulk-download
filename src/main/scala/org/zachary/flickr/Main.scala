package org.zachary.flickr

import akka.actor.{ActorSystem, Props}
import akka.stream.ActorMaterializer

object Main extends App {
  private val main: Main = new Main

  main.start()
}

class Main {
  lazy implicit val actorSystem = ActorSystem("akka-http-demo")
  lazy implicit val actorMaterializer = ActorMaterializer()

  def start(): Unit = {
    val flickrContext = FlickrAuthorization.getFlickrContext

    val fpr = actorSystem.actorOf(
      Props(classOf[FlickrPhotoRetriever], flickrContext, actorMaterializer), "FlickrPhotoRetriever")

    fpr ! FlickrPhotoRetriever.RetrieveAll
  }
}