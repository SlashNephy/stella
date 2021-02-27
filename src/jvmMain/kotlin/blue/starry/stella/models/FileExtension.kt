package blue.starry.stella.models

enum class FileExtension(internal val text: String, vararg val exts: String) {
    Image("image", "jpg", "jpeg", "png"),
    Jpg("jpg", "jpg", "jpeg"),
    Png("png", "png"),

    Video("video", "gif", "mp4", "m3u8"),
    Gif("gif", "gif"),
    MP4("mp4", "mp4"),
    M3U8("m3u8", "m3u8")
}
