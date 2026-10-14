-- 스키마와 애플리케이션 계정을 만든다 (컨테이너 볼륨을 처음 만들 때만 실행된다).
-- 계정·비밀번호는 application-local.yml 의 논리 키 값과 짝이다. 로컬 전용 값이다.
--   skeleton_batch : 배치 메타·Quartz·커스텀 테이블 — Flyway(skeleton_admin)가 만든다
--   skeleton_main · skeleton_sub : 업무 스키마 — 02-business-tables.sh 가 db/manual/*.sql 로 만든다
CREATE DATABASE IF NOT EXISTS skeleton_batch CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE DATABASE IF NOT EXISTS skeleton_main CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE DATABASE IF NOT EXISTS skeleton_sub CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

CREATE USER IF NOT EXISTS 'skeleton_reader'@'%' IDENTIFIED BY 'skeleton_reader';
CREATE USER IF NOT EXISTS 'skeleton_writer'@'%' IDENTIFIED BY 'skeleton_writer';
CREATE USER IF NOT EXISTS 'skeleton_admin'@'%' IDENTIFIED BY 'skeleton_admin';

GRANT SELECT ON skeleton_batch.* TO 'skeleton_reader'@'%';
GRANT SELECT ON skeleton_main.* TO 'skeleton_reader'@'%';
GRANT SELECT ON skeleton_sub.* TO 'skeleton_reader'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON skeleton_batch.* TO 'skeleton_writer'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON skeleton_main.* TO 'skeleton_writer'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON skeleton_sub.* TO 'skeleton_writer'@'%';
-- Flyway 전용 — skeleton_batch 만 DDL 권한을 갖는다
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, DROP, INDEX, REFERENCES ON skeleton_batch.* TO 'skeleton_admin'@'%';
FLUSH PRIVILEGES;
