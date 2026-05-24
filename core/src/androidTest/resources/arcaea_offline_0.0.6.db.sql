BEGIN TRANSACTION;

INSERT INTO
  "android_metadata" ("locale")
VALUES
  ('zh_CN_#Hans');

INSERT INTO
  "charts_info" ("song_id", "rating_class", "constant", "notes")
VALUES
  ('inkarusi', 'PAST', 20, 276),
  ('inkarusi', 'PRESENT', 40, 326),
  ('inkarusi', 'FUTURE', 78, 463);

INSERT INTO
  "difficulties" (
    "song_id",
    "rating_class",
    "rating",
    "rating_plus",
    "chart_designer",
    "jacket_designer",
    "audio_override",
    "jacket_override",
    "jacket_night",
    "title",
    "artist",
    "bg",
    "bg_inverse",
    "bpm",
    "bpm_base",
    "version",
    "date"
  )
VALUES
  ('inkarusi', 'PAST', 2, 0, 'Kurorak', 'Ancy', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
  ('inkarusi', 'PRESENT', 4, 0, 'Kurorak', 'Ancy', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
  ('inkarusi', 'FUTURE', 7, 1, 'Kurorak', 'Ancy', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
  ('inkarusi', 'BEYOND', 9, 0, 'CERiNG', 'Ancy', 0, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '5.3', 1706140800);

INSERT INTO
  "packs" ("id", "name", "description")
VALUES
  ('single', 'Memory Archive', NULL),
  ('extend', 'Extend Archive 1', 'Lorem ipsum dolor sit amet.');

INSERT INTO
  "play_results" (
    "id",
    "uuid",
    "song_id",
    "rating_class",
    "score",
    "pure",
    "far",
    "lost",
    "date",
    "max_recall",
    "modifier",
    "clear_type",
    "comment"
  )
VALUES
  (1, X'86b68641c4f340f8943b102e9b4dd584', 'inkarusi', 'FUTURE', 10000433, 463, 0, 0, 1767225600000, 463, 'NORMAL', 'PURE_MEMORY', '');

INSERT INTO
  "properties" ("key", "value")
VALUES
  ('version', '4');

INSERT INTO
  "songs" (
    "idx",
    "id",
    "title",
    "artist",
    "set",
    "bpm",
    "bpm_base",
    "audio_preview",
    "audio_preview_end",
    "side",
    "version",
    "date",
    "bg",
    "bg_inverse",
    "bg_day",
    "bg_night",
    "source",
    "source_copyright"
  )
VALUES
  (81, 'inkarusi', 'inkar-usi', 'DIA', 'base', '102', 102.0, 58235, 87058, 0, '1.6', 1529539200, 'base_light', 'base_conflict', NULL, NULL, NULL, NULL);

COMMIT;
