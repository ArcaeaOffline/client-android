package xyz.sevive.arcaeaoffline.core.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

private val ratingClassMap =
    mapOf("PAST" to 0, "PRESENT" to 1, "FUTURE" to 2, "BEYOND" to 3, "ETERNAL" to 4)

private val clearTypeMap = mapOf(
    "TRACK_LOST" to 0,
    "NORMAL_CLEAR" to 1,
    "FULL_RECALL" to 2,
    "PURE_MEMORY" to 3,
    "EASY_CLEAR" to 4,
    "HARD_CLEAR" to 5,
)

private val modifierMap = mapOf("NORMAL" to 0, "EASY" to 1, "HARD" to 2)

object Migration_7_8 : Migration(7, 8) {
    private fun applyValueMap(
        db: SupportSQLiteDatabase, map: Map<String, Int>, tableName: String, columnName: String
    ) {
        map.forEach {
            db.execSQL("UPDATE $tableName SET $columnName = '${it.value}' WHERE $columnName = '${it.key}'")
        }
    }

    private fun replaceRatingClass(db: SupportSQLiteDatabase) {
        listOf("charts_info", "difficulties", "difficulties_localized", "play_results").forEach {
            applyValueMap(db, ratingClassMap, it, "rating_class")
        }
    }

    private fun replaceClearType(db: SupportSQLiteDatabase) {
        applyValueMap(db, clearTypeMap, "play_results", "clear_type")
    }

    private fun replaceModifier(db: SupportSQLiteDatabase) {
        applyValueMap(db, modifierMap, "play_results", "modifier")
    }

    override fun migrate(db: SupportSQLiteDatabase) {
        // replace original string literal to numbers
        replaceRatingClass(db)
        replaceClearType(db)
        replaceModifier(db)

        // rename old tables (and drop related view & index)
        db.execSQL("DROP INDEX `index_difficulties_localized_song_id_rating_class_lang`")
        db.execSQL("DROP INDEX `index_difficulties_localized_lang`")
        db.execSQL("DROP INDEX `index_play_results_uuid`")
        db.execSQL("DROP VIEW IF EXISTS `charts`")
        db.execSQL("DROP VIEW IF EXISTS `play_results_calculated`")
        db.execSQL("DROP VIEW IF EXISTS `play_results_best`")
        db.execSQL("ALTER TABLE charts_info RENAME TO charts_info_old")
        db.execSQL("ALTER TABLE difficulties RENAME TO difficulties_old")
        db.execSQL("ALTER TABLE difficulties_localized RENAME TO difficulties_localized_old")
        db.execSQL("ALTER TABLE play_results RENAME TO play_results_old")

        // create new tables that match room's schema check
        db.execSQL("CREATE TABLE `difficulties` (`song_id` TEXT NOT NULL, `rating_class` INTEGER NOT NULL, `rating` INTEGER NOT NULL, `rating_plus` INTEGER NOT NULL, `chart_designer` TEXT, `jacket_designer` TEXT, `audio_override` INTEGER NOT NULL, `jacket_override` INTEGER NOT NULL, `jacket_night` TEXT, `title` TEXT, `artist` TEXT, `bg` TEXT, `bg_inverse` TEXT, `bpm` TEXT, `bpm_base` REAL, `version` TEXT, `date` INTEGER, PRIMARY KEY(`song_id`, `rating_class`))")
        db.execSQL("CREATE TABLE `difficulties_localized` (`song_id` TEXT NOT NULL, `rating_class` INTEGER NOT NULL, `lang` TEXT NOT NULL, `title` TEXT, `artist` TEXT, PRIMARY KEY(`song_id`, `rating_class`), FOREIGN KEY(`song_id`, `rating_class`) REFERENCES `difficulties`(`song_id`, `rating_class`) ON UPDATE CASCADE ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED)")
        db.execSQL("CREATE UNIQUE INDEX `index_difficulties_localized_song_id_rating_class_lang` ON `difficulties_localized` (`song_id`, `rating_class`, `lang`)")
        db.execSQL("CREATE INDEX `index_difficulties_localized_lang` ON `difficulties_localized` (`lang`)")
        db.execSQL("CREATE TABLE `charts_info` (`song_id` TEXT NOT NULL, `rating_class` INTEGER NOT NULL, `constant` INTEGER NOT NULL, `notes` INTEGER, PRIMARY KEY(`song_id`, `rating_class`))")
        db.execSQL("CREATE TABLE `play_results` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `uuid` BLOB NOT NULL, `song_id` TEXT NOT NULL, `rating_class` INTEGER NOT NULL, `score` INTEGER NOT NULL, `pure` INTEGER, `far` INTEGER, `lost` INTEGER, `date` INTEGER, `max_recall` INTEGER, `modifier` INTEGER, `clear_type` INTEGER, `comment` TEXT)")
        db.execSQL("CREATE UNIQUE INDEX `index_play_results_uuid` ON `play_results` (`uuid`)")
        db.execSQL("CREATE VIEW `charts` AS SELECT\n            s.idx AS song_idx, d.song_id, d.rating_class, d.rating, d.rating_plus,\n            COALESCE(d.title, s.title) AS title, COALESCE(d.artist, s.artist) AS artist,\n            s.`set`, COALESCE(d.bpm, s.bpm) AS bpm, COALESCE(d.bpm_base, s.bpm_base) AS bpm_base,\n            s.audio_preview, s.audio_preview_end, s.side,\n            COALESCE(d.version, s.version) AS version, COALESCE(d.date, s.date) AS date,\n            COALESCE(d.bg, s.bg) AS bg, COALESCE(d.bg_inverse, s.bg_inverse) AS bg_inverse,\n            s.bg_day, s.bg_night, s.source, s.source_copyright,\n            d.chart_designer, d.jacket_designer, d.audio_override, d.jacket_override,\n            d.jacket_night, ci.constant, ci.notes\n        FROM difficulties d\n        INNER JOIN charts_info ci ON d.song_id = ci.song_id AND d.rating_class = ci.rating_class\n        INNER JOIN songs s ON d.song_id = s.id")
        db.execSQL("CREATE VIEW `play_results_calculated` AS SELECT\n        pr.id, pr.uuid, d.song_id, d.rating_class, pr.score, pr.pure,\n        CASE\n            WHEN ci.notes IS NOT NULL AND pr.pure IS NOT NULL AND pr.far IS NOT NULL AND ci.notes <> 0\n            THEN pr.score - FLOOR((pr.pure * 10000000.0 / ci.notes) + (pr.far * 0.5 * 100000000.0 / ci.notes))\n            ELSE NULL\n        END AS shiny_pure,\n        pr.far, pr.lost, pr.date, pr.max_recall, pr.modifier, pr.clear_type,\n        CASE\n            WHEN pr.score >= 100000000 THEN ci.constant / 10.0 + 2\n            WHEN pr.score >= 9800000 THEN ci.constant / 10.0 + 1 + (pr.score - 9800000) / 200000.0\n            ELSE MAX(ci.constant / 10.0 + (pr.score - 9500000) / 300000.0, 0)\n        END AS potential,\n        pr.comment\n    FROM difficulties d\n    JOIN charts_info ci ON d.song_id = ci.song_id AND d.rating_class = ci.rating_class\n    JOIN play_results pr ON d.song_id = pr.song_id AND d.rating_class = pr.rating_class")
        db.execSQL("CREATE VIEW `play_results_best` AS SELECT\n            prc.id, prc.uuid, prc.song_id, prc.rating_class, prc.score,\n            prc.pure, prc.shiny_pure, prc.far, prc.lost,\n            prc.date, prc.max_recall, prc.modifier, prc.clear_type,\n            MAX(prc.potential) AS potential,\n            prc.comment\n        FROM play_results_calculated prc\n        GROUP BY prc.song_id, prc.rating_class\n        ORDER BY prc.potential DESC")

        // move existing data to new tables
        db.execSQL("INSERT INTO charts_info SELECT * FROM charts_info_old")
        db.execSQL("INSERT INTO difficulties SELECT * FROM difficulties_old")
        db.execSQL("INSERT INTO difficulties_localized SELECT * FROM difficulties_localized_old")
        db.execSQL("INSERT INTO play_results SELECT * FROM play_results_old")

        // drop old tables
        db.execSQL("DROP TABLE charts_info_old")
        db.execSQL("DROP TABLE difficulties_old")
        db.execSQL("DROP TABLE difficulties_localized_old")
        db.execSQL("DROP TABLE play_results_old")
    }
}
