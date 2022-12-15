package io.basquiat.refactoring.album.repository

import io.basquiat.refactoring.album.domain.entity.Album
import io.basquiat.refactoring.musician.model.code.GenreCode
import io.basquiat.refactoring.musician.model.entity.Musician
import io.basquiat.refactoring.common.extensions.findByIdOrThrow
import io.basquiat.refactoring.musician.repository.MusicianRepository
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertTrue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.Rollback
import org.springframework.transaction.annotation.Transactional
import java.util.*
import javax.persistence.EntityManager

@SpringBootTest
@Transactional
@Rollback(false)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class AlbumRepositoryTest @Autowired constructor(
    private val em: EntityManager,
    private val albumRepository: AlbumRepository,
    private val musicianRepository: MusicianRepository,
) {

    lateinit var album: Album

    @BeforeEach
    fun init() {
        // given
        val musician = Musician(
            name = "Chet Baker",
            genre = GenreCode.JAZZ
        )

        val album = Album(
            title = "As Time Goes By",
            musician = musician
        )

        // then 저장전에는 true
        assertTrue(album.isNew)

        // when
        em.persist(album)
        em.flush()

        // then 저장이후에는 false
        assertTrue(!album.isNew)

        this.album = album
        em.detach(this.album)
    }

    @Test
    @Order(1)
    @DisplayName("findByIdOrThrow 테스트")
    fun findByIdOrThrow_Query_TEST() {
        // given
        val id: UUID = album.id

        // when
        val selected = albumRepository.findByIdOrThrow(id)

        // then 조회된 엔티티는 isNew가 false여야 한다.
        assertTrue(!selected.isNew)
        assertTrue(selected.title == album.title)

        // lazy loding
        println("============================= lazy loading =========================")
        // when
        val musician = selected.musician

        val selectedMusician = musicianRepository.findByIdOrThrow(musician.id)

        assertTrue(musician == selectedMusician)

        // then 조회된 엔티티므로 false
        assertTrue(!musician.isNew)
        assertTrue(musician.name == "Chet Baker")

    }

}