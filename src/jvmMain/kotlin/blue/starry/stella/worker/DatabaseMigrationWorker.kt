package blue.starry.stella.worker

import blue.starry.stella.Env
import blue.starry.stella.Stella
import blue.starry.stella.models.PicEntry
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import org.bson.conversions.Bson
import org.litote.kmongo.*

class DatabaseMigrationWorker: Worker(null) {
    override suspend fun run() {
        if (!Env.ENABLE_DATABASE_MIGRATION) {
            return
        }

        check()
    }

    private data class Migration(val name: String, val filter: Bson, val update: Bson)

    private suspend fun check() = coroutineScope {
        @Suppress("DEPRECATION")
        val migrations = listOf(
            Migration(
                name = "timestamp.archived",
                filter = (PicEntry::timestamp / PicEntry.Timestamp::archived) eq null,
                update = setValue(PicEntry::timestamp / PicEntry.Timestamp::archived, false)
            ),
            Migration(
                name = "user",
                filter = PicEntry::user ne null,
                update = unset(PicEntry::user)
            ),
            Migration(
                name = "kind",
                filter = PicEntry::kind eq null,
                update = setValue(PicEntry::kind, PicEntry.Kind.Illust)
            ),
            Migration(
                name = "author.id",
                filter = PicEntry::author / PicEntry.Author::id eq null,
                update = setValue(PicEntry::author / PicEntry.Author::id, null)
            )
        )

        val jobs = migrations.map { migration ->
            launch {
                migrate(migration)
            }
        }
        jobs.joinAll()
    }

    private suspend fun migrate(migration: Migration) = coroutineScope {
        val entries = Stella.PicCollection.find(migration.filter).toFlow()
        val jobs = entries.map { entry ->
            launch {
                migrateEach(entry.url, migration)
            }
        }.toList()

        jobs.joinAll()
    }

    private suspend fun migrateEach(url: String, migration: Migration) {
        Stella.PicCollection.updateOne(
            PicEntry::url eq url,
            migration.update
        )

        logger.debug { "エントリー: \"$url\" のマイグレーション \"${migration.name}\" が完了しました。" }
    }
}
