package io.basquiat.refactoring.album.domain.entity

import io.basquiat.refactoring.common.model.entity.UUIDPrimaryKeyOnlyPreUpdateEntity
import io.basquiat.refactoring.musician.model.entity.Musician
import org.hibernate.annotations.DynamicUpdate
import java.util.*
import javax.persistence.*

@Entity
@DynamicUpdate
@Table(name = "album")
class Album(

    id: UUID? = null,
    title: String,
    musician: Musician,

): UUIDPrimaryKeyOnlyPreUpdateEntity(id) {

    @Column(name = "title", nullable = false)
    var title = title
        protected set

    @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    var musician = musician
        protected set

    fun modifyTitle(_title: String) {
        this.title = _title
    }

}