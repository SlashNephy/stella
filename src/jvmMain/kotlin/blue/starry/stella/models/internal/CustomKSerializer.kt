package blue.starry.stella.models.internal

import kotlinx.serialization.KSerializer

interface CustomKSerializer<T: Any?, R: Any>: KSerializer<R> {
    fun serialize(value: R): T
    fun serializeOrNull(value: R): T? {
        return runCatching {
            serialize(value)
        }.getOrNull()
    }

    fun deserialize(value: T): R
    fun deserializeOrNull(value: T): R? {
        return runCatching {
            deserialize(value)
        }.getOrNull()
    }
}
