package io.basquiat.refactoring.album.repository

import io.basquiat.refactoring.album.domain.entity.Album
import io.basquiat.refactoring.musician.model.code.GenreCode
import io.basquiat.refactoring.musician.model.entity.Musician
import io.basquiat.refactoring.common.extensions.findByIdOrThrow
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertTrue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.Rollback
import org.springframework.transaction.annotation.Transactional

@Transactional
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class AlbumRepositoryTest @Autowired constructor(
    private val albumRepository: AlbumRepository,
) {

    @Test
    @Order(1)
    @DisplayName("save 테스트")
    @Rollback(false)
    fun save_Query_TEST() {
        // given
        val musician = Musician(
            name = "Chet Baker",
            genre = GenreCode.JAZZ
        )

        val album = Album(
            title = "As Time Goes By",
            musician = musician
        )

        // when
        val saved = albumRepository.save(album)

        // then
        assertTrue(saved.title == "As Time Goes By")
    }

    @Test
    @Order(2)
    @DisplayName("findByIdOrThrow 테스트")
    fun findByIdOrThrow_Query_TEST() {
        // given
        val id: Long = 1

        // when
        val album = albumRepository.findByIdOrThrow(id)

        // then
        assertTrue(album.title == "As Time Goes By")

        println("======================lazy loading===========================")
        // then musician.name == Chet Baker, when lazy loading
        assertTrue(album.musician.name == "Chet Baker")
    }

}