-- Seed data for ArcaeaSt3PlayResultImporterTest.
-- Statements are split on ';', so keep semicolons out of string values.
-- Columns the importer does not read (health, ct, etc.) are filled with 0.
CREATE TABLE scores (
  id INTEGER PRIMARY key autoincrement NOT NULL,
  version INT,
  score INT,
  shinyPerfectCount INT,
  perfectCount INT,
  nearCount INT,
  missCount INT,
  "date" INT,
  songId text,
  songDifficulty INT,
  modifier INT,
  health INT,
  ct INT DEFAULT 0
);

CREATE TABLE cleartypes (
  id INTEGER PRIMARY key autoincrement NOT NULL,
  songId text,
  songDifficulty INT,
  clearType INT,
  ct INT DEFAULT 0
);

CREATE TABLE schemaversion (appliedVersion INT UNIQUE PRIMARY key NOT NULL);

INSERT INTO schemaversion (appliedVersion) VALUES (4);

-- FULL RECALL with a self-consistent row: max recall derived
INSERT INTO scores (version, score, shinyPerfectCount, perfectCount, nearCount, missCount, "date", songId, songDifficulty, modifier, health, ct)
VALUES (1, 9900000, 100, 900, 50, 0, 1788385000, 'a', 1, 0, 0, 0);
INSERT INTO cleartypes (songId, songDifficulty, clearType, ct) VALUES ('a', 1, 2, 0);

-- FULL RECALL with lost > 0 (the best-score play is not the FR play):
-- max recall stays null
INSERT INTO scores (version, score, shinyPerfectCount, perfectCount, nearCount, missCount, "date", songId, songDifficulty, modifier, health, ct)
VALUES (1, 9800000, 90, 800, 99, 1, 1670283375, 'b', 1, 2, 0, 0);
INSERT INTO cleartypes (songId, songDifficulty, clearType, ct) VALUES ('b', 1, 2, 0);

-- PURE MEMORY, self-consistent: max recall derived; truncated date padded
INSERT INTO scores (version, score, shinyPerfectCount, perfectCount, nearCount, missCount, "date", songId, songDifficulty, modifier, health, ct)
VALUES (1, 10000000, 20, 1000, 0, 0, 167028, 'c', 2, 1, 0, 0);
INSERT INTO cleartypes (songId, songDifficulty, clearType, ct) VALUES ('c', 2, 3, 0);

-- PURE MEMORY with far > 0: max recall stays null; zero date dropped
INSERT INTO scores (version, score, shinyPerfectCount, perfectCount, nearCount, missCount, "date", songId, songDifficulty, modifier, health, ct)
VALUES (1, 9700000, 80, 900, 95, 5, 0, 'd', 2, 0, 0, 0);
INSERT INTO cleartypes (songId, songDifficulty, clearType, ct) VALUES ('d', 2, 3, 0);

-- Nullable play counts pass through as null
INSERT INTO scores (version, score, shinyPerfectCount, perfectCount, nearCount, missCount, "date", songId, songDifficulty, modifier, health, ct)
VALUES (1, 8000000, 0, NULL, 100, 50, 0, 'e', 0, 0, 0, 0);
INSERT INTO cleartypes (songId, songDifficulty, clearType, ct) VALUES ('e', 0, 1, 0);

-- No cleartypes row: the score is kept, clearType falls to null;
-- a full-precision date passes through untouched
INSERT INTO scores (version, score, shinyPerfectCount, perfectCount, nearCount, missCount, "date", songId, songDifficulty, modifier, health, ct)
VALUES (1, 7000000, 0, 700, 0, 0, 1789000000, 'f', 3, 0, 0, 0);

-- TRACK LOST; 4-digit date padded
INSERT INTO scores (version, score, shinyPerfectCount, perfectCount, nearCount, missCount, "date", songId, songDifficulty, modifier, health, ct)
VALUES (1, 6000000, 0, 600, 30, 20, 1566, 'g', 3, 0, 0, 0);
INSERT INTO cleartypes (songId, songDifficulty, clearType, ct) VALUES ('g', 3, 0, 0);
