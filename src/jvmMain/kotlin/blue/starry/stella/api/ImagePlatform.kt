package blue.starry.stella.api

enum class ImagePlatform {
   Twitter, Nijie, Pixiv
}

fun String.toImagePlatform(): ImagePlatform? {
   return ImagePlatform.values().find { equals(it.name, true) }
}
