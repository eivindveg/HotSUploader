CREATE TABLE IF NOT EXISTS `ReplayFile` (
  `id`       BIGINT AUTO_INCREMENT,
  `fileName` VARCHAR(1023),
  UNIQUE (`fileName`),
  PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `UploadStatus` (
  `id`            BIGINT AUTO_INCREMENT,
  `host`          VARCHAR(15),
  `status`        VARCHAR(100),
  `replayFile_id` BIGINT,
  PRIMARY KEY (`id`)
)
