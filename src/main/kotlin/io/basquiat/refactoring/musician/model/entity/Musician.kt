package io.basquiat.refactoring.musician.model.entity

import io.basquiat.refactoring.musician.model.code.GenreCode
import javax.persistence.*

@Entity
@Table(name = "musician")
class Musician(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    var name: String,

    @Enumerated(value = EnumType.STRING)
    var genre: GenreCode? = GenreCode.ETC,

)