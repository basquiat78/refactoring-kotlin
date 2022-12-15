package io.basquiat.refactoring.common.model.entity

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.time.LocalDateTime.now
import javax.persistence.Column
import javax.persistence.EntityListeners
import javax.persistence.MappedSuperclass
import javax.persistence.PreUpdate

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class BaseTimeOnlyPreUpdateEntity {

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = now()
        protected set

    @Column(name = "updated_at", nullable = true, updatable = true)
    lateinit var updatedAt: LocalDateTime
        protected set

    @PreUpdate
    fun updated() {
        updatedAt = now()
    }

}