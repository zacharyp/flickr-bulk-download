package org.zachary

package object flickr {

  case class FlickrPhoto(
    id: String,
    title: String,
    description: Option[String],
    originalFormat: String,
    photoContent: Option[Array[Byte]] = None
  )
}
