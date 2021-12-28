package blue.starry.stella.models.internal

import blue.starry.stella.models.PicEntry
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object PlatformSerializer: KSerializer<PicEntry.Platform> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Platform", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: PicEntry.Platform) {
        encoder.encodeString(serialize(value))
    }

    fun serialize(value: PicEntry.Platform): String {
        return value.name
    }

    override fun deserialize(decoder: Decoder): PicEntry.Platform {
        return requireNotNull(deserializeOrNull(decoder.decodeString()))
    }

    fun deserializeOrNull(value: String): PicEntry.Platform? {
        return PicEntry.Platform.values().find { serialize(it).equals(value, true) }
    }
}
