package org.zachary

import com.flickr4java.flickr.photos.Photo

package object flickr {

  case class FlickrPhoto(
    photo: Photo,
    id: String,
    title: String,
    description: String,
    tags: List[String],
    originalFormat: String
  )
}
