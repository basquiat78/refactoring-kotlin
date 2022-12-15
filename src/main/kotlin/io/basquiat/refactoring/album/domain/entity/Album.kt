package io.basquiat.refactoring.album.domain.entity

import io.basquiat.refactoring.musician.model.entity.Musician
import org.hibernate.annotations.DynamicUpdate
import javax.persistence.*

@Entity
@DynamicUpdate
@Table(name = "album")
class Album(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    var title: String,

    @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @JoinColumn(name = "musician_id")
    var musician: Musician,

)