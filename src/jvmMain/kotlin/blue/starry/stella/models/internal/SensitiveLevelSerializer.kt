package blue.starry.stella.models.internal

import blue.starry.stella.models.PicEntry
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object SensitiveLevelSerializer: CustomKSerializer<Int, PicEntry.SensitiveLevel> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("SensitiveLevel", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: PicEntry.SensitiveLevel) {
        encoder.encodeInt(serialize(value))
    }

    override fun serialize(value: PicEntry.SensitiveLevel): Int {
        return value.ordinal
    }

    override fun deserialize(decoder: Decoder): PicEntry.SensitiveLevel {
        return requireNotNull(deserializeOrNull(decoder.decodeInt()))
    }

    override fun deserialize(value: Int): PicEntry.SensitiveLevel {
        return PicEntry.SensitiveLevel.values()[value]
    }
}
