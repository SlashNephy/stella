package blue.starry.stella.worker

import blue.starry.stella.Stella
import blue.starry.stella.models.PicEntry
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import org.bson.conversions.Bson
import org.litote.kmongo.div
import org.litote.kmongo.eq
import org.litote.kmongo.setValue

class DatabaseMigrationWorker: Worker(null) {
    override suspend fun run() {
        check()
    }

    private data class Migration(val filter: Bson, val update: Bson)

    private suspend fun check() {
        val migrations = listOf(
            // timestamp.archived
            Migration(
                filter = (PicEntry::timestamp / PicEntry.Timestamp::archived) eq null,
                update = setValue(PicEntry::timestamp / PicEntry.Timestamp::archived, false)
            )
        )

        val jobs = migrations.map { migration ->
            launch {
                migrate(migration)
            }
        }
        jobs.joinAll()
    }

    private suspend fun migrate(migration: Migration) {
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
    }
}
