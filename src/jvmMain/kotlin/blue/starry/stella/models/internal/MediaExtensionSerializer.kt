package blue.starry.stella.models.internal

import blue.starry.stella.models.PicEntry
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object MediaExtensionSerializer: CustomKSerializer<String, PicEntry.MediaExtension> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("MediaExtension", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: PicEntry.MediaExtension) {
        encoder.encodeString(serialize(value))
    }

    override fun serialize(value: PicEntry.MediaExtension): String {
        return value.name.lowercase()
    }

    override fun deserialize(decoder: Decoder): PicEntry.MediaExtension {
        return deserialize(decoder.decodeString())
    }

    override fun deserialize(value: String): PicEntry.MediaExtension {
        return PicEntry.MediaExtension.values().first { it.name.equals(value, true) }
    }

    fun deserializeFromUrl(url: String): PicEntry.MediaExtension {
        val value = url.split(".").last().split("?").first()
        return deserialize(value)
    }
}
