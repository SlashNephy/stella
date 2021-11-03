package blue.starry.stella.models.internal

import blue.starry.stella.models.PicModel
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object PlatformSerializer: KSerializer<PicModel.Platform> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Platform", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: PicModel.Platform) {
        encoder.encodeString(serialize(value))
    }

    fun serialize(value: PicModel.Platform): String {
        return value.name
    }

    override fun deserialize(decoder: Decoder): PicModel.Platform {
        return requireNotNull(deserializeOrNull(decoder.decodeString()))
    }

    fun deserializeOrNull(value: String): PicModel.Platform? {
        return PicModel.Platform.values().find { serialize(it).equals(value, true) }
    }
}
