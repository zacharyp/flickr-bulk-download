##Download all the images from flickr for a user
Downloads all images for a user, putting any images currently in a Set into a subdirectory named by the name of the Set

### Setup
To use this library, you will need a flickr api key (https://www.flickr.com/services/api/misc.api_keys.html).  Then, modify the file src/main/resources/application.conf with your key/secret:

```
flickr.api.authorization {
  key = "your-key-here"
  secret = "your-secret-here"
}
```

You should also change the directory path in which you want the photos to save:

```
flickr.save.location = "/Users/<insert-user-name-here>/Pictures/flickr-download"
```


### Use

Requires Scala and SBT to be installed.

To run the program, type at the prompt:

- `sbt run`

To run the program with the required varaible set, they can be set on the command line instead, changing the FOOBAR values for your key and secrets values, and the save directory to your save directory:

- `sbt -DLOCAL_SAVE_DIR=/Users/zachary/Pictures/flickr-download -DFLICKR_KEY=FOOBAR -DFLICKR_SECRET=FOOBAR run`
