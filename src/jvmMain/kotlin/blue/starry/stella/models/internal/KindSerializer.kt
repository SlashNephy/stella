package blue.starry.stella.models.internal

import blue.starry.stella.models.PicEntry
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object KindSerializer: CustomKSerializer<Int, PicEntry.Kind> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Kind", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: PicEntry.Kind) {
        encoder.encodeInt(serialize(value))
    }

    override fun serialize(value: PicEntry.Kind): Int {
        return value.ordinal
    }

    override fun deserialize(decoder: Decoder): PicEntry.Kind {
        return requireNotNull(deserializeOrNull(decoder.decodeInt()))
    }

    override fun deserialize(value: Int): PicEntry.Kind {
        return PicEntry.Kind.values()[value]
    }
}
