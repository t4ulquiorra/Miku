package dev.anilbeesetti.nextplayer.core.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = MediaLayoutModeSerializer::class)
enum class MediaLayoutMode {
    LIST,
    GRID,
    CRATE,
}

object MediaLayoutModeSerializer : KSerializer<MediaLayoutMode> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("dev.anilbeesetti.nextplayer.core.model.MediaLayoutMode", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: MediaLayoutMode) {
        encoder.encodeString(value.name)
    }

    override fun deserialize(decoder: Decoder): MediaLayoutMode {
        return try {
            val name = decoder.decodeString()
            MediaLayoutMode.entries.firstOrNull { it.name.equals(name, ignoreCase = true) }
                ?: MediaLayoutMode.CRATE
        } catch (_: Exception) {
            MediaLayoutMode.CRATE
        }
    }
}
