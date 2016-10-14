##Download all the images from flickr for a user

### Setup
To use this library, you will need a flickr api key (https://www.flickr.com/services/api/misc.api_keys.html).  Then, modify the file src/main/resource/authorization.conf with your key/secret:

```
flickr.api.authorization {
  key = "your-key-here"
  secret = "your-secret-here"
}
```

### Use

Requires Scala and SBT to be installed.

To run the program, type at the prompt:

- `sbt run`

