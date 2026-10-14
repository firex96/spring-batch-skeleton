-- skeleton_main 업무 DB에서 수동 실행한다. 런타임/Flyway가 자동 실행하지 않는다 (docker compose 초기화·Testcontainers 는 이 파일을 쓴다).
-- ============================================================
-- Sample Business Database Schema for MySQL
-- Database: skeleton_main (or your business database)
-- Version: 1.0.0
-- ============================================================

-- ------------------------------------------------------------
-- Sample Table for Batch Processing
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sample (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'PK',
    name VARCHAR(255) NOT NULL COMMENT '이름',
    description VARCHAR(500) NULL COMMENT '설명',
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING' COMMENT '상태 (PENDING, PROCESSING, COMPLETED, FAILED)',
    processed_at DATETIME(6) NULL COMMENT '처리 시간',
    created_by BIGINT NOT NULL DEFAULT 0 COMMENT '생성자',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시간',
    updated_by BIGINT NOT NULL DEFAULT 0 COMMENT '수정자',
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '수정 시간',
    INDEX IDX_SAMPLE_STATUS (status),
    INDEX IDX_SAMPLE_CREATED_AT (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
COMMENT='샘플 테이블 (배치 처리 대상)';

-- Indexes



-- ------------------------------------------------------------
-- Sample Test Data (Optional)
-- ------------------------------------------------------------
-- INSERT INTO sample (name, description, status) VALUES
-- ('Sample 1', 'First sample data', 'PENDING'),
-- ('Sample 2', 'Second sample data', 'PENDING'),
-- ('Sample 3', 'Third sample data', 'PENDING');
