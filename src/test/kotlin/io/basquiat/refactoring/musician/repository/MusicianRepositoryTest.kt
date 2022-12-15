package io.basquiat.refactoring.musician.repository

import io.basquiat.refactoring.musician.model.code.GenreCode
import io.basquiat.refactoring.musician.model.entity.Musician
import io.basquiat.refactoring.common.extensions.findByIdOrThrow
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.Rollback
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
class MusicianRepositoryTest @Autowired constructor(
    private val musicianRepository: MusicianRepository,
) {

    @Test
    @DisplayName("save 테스트")
    @Transactional
    @Rollback(false)
    fun save_Query_TEST() {
        // given
        val musician = Musician(
            name = "John Coltrane",
            genre = GenreCode.JAZZ
        )

        // when
        val saved = musicianRepository.save(musician)
        val selected = musicianRepository.findByIdOrThrow(1L)

        //then
        assertTrue(selected?.id == saved.id)
    }

}