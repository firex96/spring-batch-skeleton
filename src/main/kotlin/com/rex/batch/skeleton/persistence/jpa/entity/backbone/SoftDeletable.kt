package com.rex.batch.skeleton.persistence.jpa.entity.backbone

import java.time.LocalDateTime

/**
 * 소프트 삭제를 지원하는 엔티티 인터페이스
 *
 * 물리적 삭제 대신 삭제 여부를 표시하는 필드를 사용합니다.
 */
interface SoftDeletable {
    /** 삭제 일시 (null이면 활성 상태) */
    var deletedAt: LocalDateTime?

    /** 삭제한 사용자 ID (선택적) */
    var deletedBy: Long?

    /** 삭제 여부 확인 */
    fun isDeleted(): Boolean = deletedAt != null

    /** Soft Delete 수행 */
    fun softDelete(deletedBy: Long? = null) {
        this.deletedAt = LocalDateTime.now()
        this.deletedBy = deletedBy
    }

    /** Soft Delete 복원 */
    fun restore() {
        this.deletedAt = null
        this.deletedBy = null
    }
}