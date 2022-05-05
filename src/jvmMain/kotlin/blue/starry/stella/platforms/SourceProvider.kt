package blue.starry.stella.platforms

interface SourceProvider<ID: Any, DATA: Any> {
    suspend fun registerByUrl(url: String, auto: Boolean): Boolean
    suspend fun registerById(id: ID, auto: Boolean): Boolean
    suspend fun register(data: DATA, auto: Boolean): Boolean
}
