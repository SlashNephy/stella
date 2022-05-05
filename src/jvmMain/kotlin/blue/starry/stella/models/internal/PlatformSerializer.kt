package blue.starry.stella.models.internal

import blue.starry.stella.models.PicEntry
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object PlatformSerializer: CustomKSerializer<String, PicEntry.Platform> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Platform", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: PicEntry.Platform) {
        encoder.encodeString(serialize(value))
    }

    override fun serialize(value: PicEntry.Platform): String {
        return value.name
    }

    override fun deserialize(decoder: Decoder): PicEntry.Platform {
        return requireNotNull(deserializeOrNull(decoder.decodeString()))
    }

    override fun deserialize(value: String): PicEntry.Platform {
        return PicEntry.Platform.values().first { serialize(it).equals(value, true) }
    }
}
