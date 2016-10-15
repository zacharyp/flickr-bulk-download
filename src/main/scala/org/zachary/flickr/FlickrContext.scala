package org.zachary.flickr

import java.io.File

import com.flickr4java.flickr.auth.{Auth, Permission}
import com.flickr4java.flickr.people.PeopleInterface
import com.flickr4java.flickr.util.FileAuthStore
import com.flickr4java.flickr.{Flickr, REST}
import com.typesafe.config.ConfigFactory
import org.scribe.model.Verifier

import scala.io.StdIn

object FlickrContext {

  lazy val config = ConfigFactory.load("application.conf")
  lazy val key = config.getString("flickr.api.authorization.key")
  lazy val secret = config.getString("flickr.api.authorization.secret")

  lazy val directory = config.getString("flickr.save.location")

  lazy val flickr = new Flickr(key, secret, new REST())
  lazy val authInterface = flickr.getAuthInterface
  lazy val authToken = authInterface.getRequestToken

  lazy val authDir: String = System.getProperty("user.home") + File.separatorChar + ".flickrAuth"
  lazy val authStore = new FileAuthStore(new File(authDir))

  def getFlickrContext: FlickrContext = {

    val url = authInterface.getAuthorizationUrl(authToken, Permission.DELETE)

    System.out.println("What is your Flickr username: ")
    val flickrUsername = StdIn.readLine()

    val flickRAuth = Option(authStore.retrieve(flickrUsername)).getOrElse {
      System.out.println("Follow this URL to authorise yourself on Flickr")
      System.out.println(url)
      System.out.println("Paste in the token it gives you: ")
      val tokenKey = StdIn.readLine()

      val requestToken = authInterface.getAccessToken(authToken, new Verifier(tokenKey))
      val auth = authInterface.checkToken(requestToken)

      authStore.store(auth)
      auth
    }

    FlickrContext(flickr, flickRAuth, directory)
  }
}

// auth needs to be set in the Flickr RequestContext every time it is used, as it is stored in a thread local ಠ_ಠ
case class FlickrContext(
  flickr: Flickr,
  auth: Auth,
  directory: String
)