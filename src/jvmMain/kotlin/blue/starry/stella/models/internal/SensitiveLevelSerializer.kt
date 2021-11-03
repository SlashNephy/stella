package blue.starry.stella.models.internal

import blue.starry.stella.models.PicModel
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object SensitiveLevelSerializer: KSerializer<PicModel.SensitiveLevel> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("SensitiveLevel", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: PicModel.SensitiveLevel) {
        encoder.encodeInt(serialize(value))
    }

    fun serialize(value: PicModel.SensitiveLevel): Int {
        return value.ordinal
    }

    override fun deserialize(decoder: Decoder): PicModel.SensitiveLevel {
        return requireNotNull(deserializeOrNull(decoder.decodeInt()))
    }

    fun deserializeOrNull(value: Int): PicModel.SensitiveLevel? {
        return PicModel.SensitiveLevel.values().getOrNull(value)
    }
}
