package blue.starry.stella.endpoints

import blue.starry.stella.models.PicModel
import blue.starry.stella.models.SortOrder
import kotlinx.coroutines.flow.flow
import org.bson.conversions.Bson
import org.litote.kmongo.*

object GetQueryPipelines {
    fun sort(value: String?, count: Int) = flow {
        when (value?.toSortOrder() ?: SortOrder.ManualUpdatedDescending) {
            SortOrder.AddedDescending -> {
                emit(
                    sort(descending(PicModel::timestamp / PicModel.Timestamp::added))
                )
            }
            SortOrder.AddedAscending -> {
                emit(
                    sort(ascending(PicModel::timestamp / PicModel.Timestamp::added))
                )
            }
            SortOrder.CreatedDescending -> {
                emit(
                    sort(descending(PicModel::timestamp / PicModel.Timestamp::created))
                )
            }
            SortOrder.CreatedAscending -> {
                emit(
                    sort(ascending(PicModel::timestamp / PicModel.Timestamp::created))
                )
            }
            SortOrder.ManualUpdatedDescending -> {
                emit(
                    sort(descending(PicModel::timestamp / PicModel.Timestamp::manual_updated))
                )
            }
            SortOrder.ManualUpdatedAscending -> {
                emit(
                    sort(ascending(PicModel::timestamp / PicModel.Timestamp::manual_updated))
                )
            }
            SortOrder.AutoUpdatedDescending -> {
                emit(
                    sort(descending(PicModel::timestamp / PicModel.Timestamp::auto_updated))
                )
            }
            SortOrder.AutoUpdatedAscending -> {
                emit(
                    sort(ascending(PicModel::timestamp / PicModel.Timestamp::auto_updated))
                )
            }
            SortOrder.TitleDescending -> {
                emit(
                    sort(descending(PicModel::title))
                )
            }
            SortOrder.TitleAscending -> {
                emit(
                    sort(ascending(PicModel::title))
                )
            }
            SortOrder.AuthorDescending -> {
                emit(
                    sort(descending(PicModel::author / PicModel.Author::name))
                )
            }
            SortOrder.AuthorAscending -> {
                emit(
                    sort(ascending(PicModel::author / PicModel.Author::name))
                )
            }
            SortOrder.RatingDescending -> {
                emit(
                    sort(descending(PicModel::rating / PicModel.Rating::score))
                )
            }
            SortOrder.RatingAscending -> {
                emit(
                    sort(ascending(PicModel::rating / PicModel.Rating::score))
                )
            }
            SortOrder.LikeDescending -> {
                emit(
                    match(not(PicModel::popularity / PicModel.Popularity::like eq null))
                )
                emit(
                    sort(descending(PicModel::popularity / PicModel.Popularity::like))
                )
            }
            SortOrder.LikeAscending -> {
                emit(
                    match(not(PicModel::popularity / PicModel.Popularity::like eq null))
                )
                emit(
                    sort(ascending(PicModel::popularity / PicModel.Popularity::like))
                )
            }
            SortOrder.BookmarkDescending -> {
                emit(
                    match(not(PicModel::popularity / PicModel.Popularity::bookmark eq null))
                )
                emit(
                    sort(descending(PicModel::popularity / PicModel.Popularity::bookmark))
                )
            }
            SortOrder.BookmarkAscending -> {
                emit(
                    match(not(PicModel::popularity / PicModel.Popularity::bookmark eq null))
                )
                emit(
                    sort(ascending(PicModel::popularity / PicModel.Popularity::bookmark))
                )
            }
            SortOrder.ViewDescending -> {
                emit(
                    match(not(PicModel::popularity / PicModel.Popularity::view eq null))
                )
                emit(
                    sort(descending(PicModel::popularity / PicModel.Popularity::view))
                )
            }
            SortOrder.ViewAscending -> {
                emit(
                    match(not(PicModel::popularity / PicModel.Popularity::view eq null))
                )
                emit(
                    sort(ascending(PicModel::popularity / PicModel.Popularity::view))
                )
            }
            SortOrder.RetweetDescending -> {
                emit(
                    match(not(PicModel::popularity / PicModel.Popularity::retweet eq null))
                )
                emit(
                    sort(descending(PicModel::popularity / PicModel.Popularity::retweet))
                )
            }
            SortOrder.RetweetAscending -> {
                emit(
                    match(not(PicModel::popularity / PicModel.Popularity::retweet eq null))
                )
                emit(
                    sort(ascending(PicModel::popularity / PicModel.Popularity::retweet))
                )
            }
            SortOrder.ReplyDescending -> {
                emit(
                    match(not(PicModel::popularity / PicModel.Popularity::reply eq null))
                )
                emit(
                    sort(descending(PicModel::popularity / PicModel.Popularity::reply))
                )
            }
            SortOrder.ReplyAscending -> {
                emit(
                    match(not(PicModel::popularity / PicModel.Popularity::reply eq null))
                )
                emit(
                    sort(ascending(PicModel::popularity / PicModel.Popularity::reply))
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
