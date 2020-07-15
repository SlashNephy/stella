package blue.starry.stella.api

enum class SortOrder(internal val text: String) {
    AddedDescending("added_descending"), AddedAscending("added_ascending"),

    CreatedDescending("created_descending"), CreatedAscending("created_ascending"),

    ManualUpdatedDescending("manual_updated_descending"), ManualUpdatedAscending("manual_updated_ascending"),

    AutoUpdatedDescending("auto_updated_descending"), AutoUpdatedAscending("auto_updated_ascending"),

    TitleDescending("title_descending"), TitleAscending("title_ascending"),

    AuthorDescending("author_descending"), AuthorAscending("author_ascending"),

    RatingDescending("rating_descending"), RatingAscending("rating_ascending"),

    LikeDescending("like_descending"), LikeAscending("like_ascending"),

    BookmarkDescending("bookmark_descending"), BookmarkAscending("bookmark_ascending"),

    ViewDescending("view_descending"), ViewAscending("view_ascending"),

    RetweetDescending("retweet_descending"), RetweetAscending("retweet_ascending"),

    ReplyDescending("reply_descending"), ReplyAscending("reply_ascending"),

    Random("random");
}

fun String.toSortOrder(): SortOrder? {
    return SortOrder.values().find { equals(it.text, true) }
}
