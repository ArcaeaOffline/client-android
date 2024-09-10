package xyz.sevive.arcaeaoffline.core.database.extensions

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import org.threeten.bp.Instant
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultClearType
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultModifier
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult
import java.util.UUID


object PlayResultSerializer : KSerializer<PlayResult> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("PlayResult") {
        element<Long>("id")  // 0
        element<String>("uuid")
        element<String>("songId")  // 2
        element<Int>("ratingClass")
        element<Int>("score")  // 4
        element<Int?>("pure")
        element<Int?>("far")  // 6
        element<Int?>("lost")
        element<Long?>("date")  // 8
        element<Int?>("maxRecall")
        element<Int?>("modifier")  // 10
        element<Int?>("clearType")
        element<String?>("comment")
    }

    override fun serialize(encoder: Encoder, value: PlayResult) =
        encoder.encodeStructure(descriptor) {
            encodeLongElement(descriptor, 0, value.id)
            encodeStringElement(descriptor, 1, value.uuid.toString())
            encodeStringElement(descriptor, 2, value.songId)
            encodeIntElement(descriptor, 3, value.ratingClass.value)
            encodeIntElement(descriptor, 4, value.score)
            value.pure?.let { encodeIntElement(descriptor, 5, it) }
            value.far?.let { encodeIntElement(descriptor, 6, it) }
            value.lost?.let { encodeIntElement(descriptor, 7, it) }
            value.date?.let { encodeLongElement(descriptor, 8, it.toEpochMilli()) }
            value.maxRecall?.let { encodeIntElement(descriptor, 9, it) }
            value.modifier?.let { encodeIntElement(descriptor, 10, it.value) }
            value.clearType?.let { encodeIntElement(descriptor, 11, it.value) }
            value.comment?.let { encodeStringElement(descriptor, 12, it) }
        }

    override fun deserialize(decoder: Decoder): PlayResult = decoder.decodeStructure(descriptor) {
        var cls = PlayResult(songId = "", ratingClass = ArcaeaRatingClass.FUTURE, score = 0)

        while (true) {
            when (val index = decodeElementIndex(descriptor)) {
                0 -> cls = cls.copy(id = decodeLongElement(descriptor, 0))
                1 -> cls = cls.copy(uuid = UUID.fromString(decodeStringElement(descriptor, 1)))
                2 -> cls = cls.copy(songId = decodeStringElement(descriptor, 2))
                3 -> cls = cls.copy(
                    ratingClass = ArcaeaRatingClass.fromInt(decodeIntElement(descriptor, 3))
                )

                4 -> cls = cls.copy(score = decodeIntElement(descriptor, 4))
                5 -> cls = cls.copy(pure = decodeIntElement(descriptor, 5))
                6 -> cls = cls.copy(far = decodeIntElement(descriptor, 6))
                7 -> cls = cls.copy(lost = decodeIntElement(descriptor, 7))
                8 -> cls = cls.copy(date = decodeLongElement(
                    descriptor, 8
                ).let { Instant.ofEpochMilli(it) })

                9 -> cls = cls.copy(maxRecall = decodeIntElement(descriptor, 9))
                10 -> cls = cls.copy(modifier = decodeIntElement(
                    descriptor, 10
                ).let { if (it == 0) null else ArcaeaPlayResultModifier.fromInt(it) })

                11 -> cls = cls.copy(clearType = decodeIntElement(
                    descriptor, 11
                ).let { if (it == 0) null else ArcaeaPlayResultClearType.fromInt(it) })

                12 -> cls = cls.copy(comment = decodeStringElement(descriptor, 12))

                CompositeDecoder.DECODE_DONE -> break
                else -> error("Unexpected index: $index")
            }
        }

        return@decodeStructure cls
    }
}
