package blue.starry.stella.common

enum class ImagePlatform {
   Twitter, Nijie, Pixiv
}

fun String.toImagePlatform(): ImagePlatform? {
   return ImagePlatform.values().find { equals(it.name, true) }
}
