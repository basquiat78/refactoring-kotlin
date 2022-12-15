package io.basquiat.refactoring.musician.model.entity

import io.basquiat.refactoring.common.model.entity.UUIDPrimaryKeyOnlyPreUpdateEntity
import io.basquiat.refactoring.musician.model.code.GenreCode
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "musician")
class Musician(

    id: UUID? = null,
    name: String,
    genre: GenreCode? = GenreCode.ETC,

): UUIDPrimaryKeyOnlyPreUpdateEntity(id) {

    @Column(name = "name", nullable = false)
    var name = name
        protected set

    @Enumerated(value = EnumType.STRING)
    var genre = genre
        protected set

    fun modifyName(_name: String) {
        this.name = _name
    }

    fun modifyGenre(_genre: GenreCode) {
        this.genre = _genre
    }

}