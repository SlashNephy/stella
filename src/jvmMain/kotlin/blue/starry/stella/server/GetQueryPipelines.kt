package blue.starry.stella.server

import blue.starry.stella.models.PicEntry
import blue.starry.stella.models.SortOrder
import kotlinx.coroutines.flow.flow
import org.bson.conversions.Bson
import org.litote.kmongo.*

object GetQueryPipelines {
    fun sort(value: String?, count: Int) = flow {
        when (value?.toSortOrder() ?: SortOrder.ManualUpdatedDescending) {
            SortOrder.AddedDescending -> {
                emit(
                    sort(descending(PicEntry::timestamp / PicEntry.Timestamp::added))
                )
            }
            SortOrder.AddedAscending -> {
                emit(
                    sort(ascending(PicEntry::timestamp / PicEntry.Timestamp::added))
                )
            }
            SortOrder.CreatedDescending -> {
                emit(
                    sort(descending(PicEntry::timestamp / PicEntry.Timestamp::created))
                )
            }
            SortOrder.CreatedAscending -> {
                emit(
                    sort(ascending(PicEntry::timestamp / PicEntry.Timestamp::created))
                )
            }
            SortOrder.ManualUpdatedDescending -> {
                emit(
                    sort(descending(PicEntry::timestamp / PicEntry.Timestamp::manual_updated))
                )
            }
            SortOrder.ManualUpdatedAscending -> {
                emit(
                    sort(ascending(PicEntry::timestamp / PicEntry.Timestamp::manual_updated))
                )
            }
            SortOrder.AutoUpdatedDescending -> {
                emit(
                    sort(descending(PicEntry::timestamp / PicEntry.Timestamp::auto_updated))
                )
            }
            SortOrder.AutoUpdatedAscending -> {
                emit(
                    sort(ascending(PicEntry::timestamp / PicEntry.Timestamp::auto_updated))
                )
            }
            SortOrder.TitleDescending -> {
                emit(
                    sort(descending(PicEntry::title))
                )
            }
            SortOrder.TitleAscending -> {
                emit(
                    sort(ascending(PicEntry::title))
                )
            }
            SortOrder.AuthorDescending -> {
                emit(
                    sort(descending(PicEntry::author / PicEntry.Author::name))
                )
            }
            SortOrder.AuthorAscending -> {
                emit(
                    sort(ascending(PicEntry::author / PicEntry.Author::name))
                )
            }
            SortOrder.RatingDescending -> {
                emit(
                    sort(descending(PicEntry::rating / PicEntry.Rating::score))
                )
            }
            SortOrder.RatingAscending -> {
                emit(
                    sort(ascending(PicEntry::rating / PicEntry.Rating::score))
                )
            }
            SortOrder.LikeDescending -> {
                emit(
                    match(not(PicEntry::popularity / PicEntry.Popularity::like eq null))
                )
                emit(
                    sort(descending(PicEntry::popularity / PicEntry.Popularity::like))
                )
            }
            SortOrder.LikeAscending -> {
                emit(
                    match(not(PicEntry::popularity / PicEntry.Popularity::like eq null))
                )
                emit(
                    sort(ascending(PicEntry::popularity / PicEntry.Popularity::like))
                )
            }
            SortOrder.BookmarkDescending -> {
                emit(
                    match(not(PicEntry::popularity / PicEntry.Popularity::bookmark eq null))
                )
                emit(
                    sort(descending(PicEntry::popularity / PicEntry.Popularity::bookmark))
                )
            }
            SortOrder.BookmarkAscending -> {
                emit(
                    match(not(PicEntry::popularity / PicEntry.Popularity::bookmark eq null))
                )
                emit(
                    sort(ascending(PicEntry::popularity / PicEntry.Popularity::bookmark))
                )
            }
            SortOrder.ViewDescending -> {
                emit(
                    match(not(PicEntry::popularity / PicEntry.Popularity::view eq null))
                )
                emit(
                    sort(descending(PicEntry::popularity / PicEntry.Popularity::view))
                )
            }
            SortOrder.ViewAscending -> {
                emit(
                    match(not(PicEntry::popularity / PicEntry.Popularity::view eq null))
                )
                emit(
                    sort(ascending(PicEntry::popularity / PicEntry.Popularity::view))
                )
            }
            SortOrder.RetweetDescending -> {
                emit(
                    match(not(PicEntry::popularity / PicEntry.Popularity::retweet eq null))
                )
                emit(
                    sort(descending(PicEntry::popularity / PicEntry.Popularity::retweet))
                )
            }
            SortOrder.RetweetAscending -> {
                emit(
                    match(not(PicEntry::popularity / PicEntry.Popularity::retweet eq null))
                )
                emit(
                    sort(ascending(PicEntry::popularity / PicEntry.Popularity::retweet))
                )
            }
            SortOrder.ReplyDescending -> {
                emit(
                    match(not(PicEntry::popularity / PicEntry.Popularity::reply eq null))
                )
                emit(
                    sort(descending(PicEntry::popularity / PicEntry.Popularity::reply))
                )
            }
            SortOrder.ReplyAscending -> {
                emit(
                    match(not(PicEntry::popularity / PicEntry.Popularity::reply eq null))
                )
                emit(
                    sort(ascending(PicEntry::popularity / PicEntry.Popularity::reply))
                )
            }
            SortOrder.Random -> {
                emit(
                    sample(count)
                )
            }
        }
    }

    fun applyFilters(filters: List<Bson>) = flow {
        if (filters.isNotEmpty()) {
            emit(
                match(and(filters))
            )
        }
    }

    fun limit(order: String?, page: Int, count: Int) = flow {
        if (order?.toSortOrder() != SortOrder.Random) {
            emit(
                skip(page * count)
            )
            emit(
                limit(count)
            )
        }
    }

    private fun String.toSortOrder(): SortOrder? {
        return SortOrder.values().find { equals(it.text, true) }
    }
}
