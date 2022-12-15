package io.basquiat.refactoring.common.model.entity

import org.hibernate.proxy.HibernateProxy
import org.springframework.data.domain.Persistable
import java.io.Serializable
import java.util.*
import javax.persistence.*

@MappedSuperclass
abstract class UUIDPrimaryKeyOnlyPreUpdateEntity(
    id: UUID? = UUID.randomUUID(),
): Persistable<UUID>, BaseTimeOnlyPreUpdateEntity() {

    @Id
    @Column(length = 16, unique = true, nullable = false)
    private val id: UUID = id ?: UUID.randomUUID()

    @Transient
    private var persisted: Boolean = id != null
    override fun getId(): UUID = id

    override fun isNew() = !persisted

    override fun hashCode(): Int = id.hashCode()

    override fun equals(other: Any?): Boolean {
        return when {
            this === other -> true
            other == null -> false
            other !is HibernateProxy -> false
            other !is UUIDPrimaryKeyOnlyPreUpdateEntity -> false
            else -> getId() == this.anyIdentifier(other)
        }
    }

    private fun anyIdentifier(other: Any?): Serializable {
        return if (other is HibernateProxy) {
            other.hibernateLazyInitializer.identifier
        } else {
            (other as UUIDPrimaryKeyOnlyPreUpdateEntity).id
        }
    }

    @PostPersist
    @PostLoad
    protected fun load() {
        persisted = true
    }

}