package io.basquiat.refactoring.musician.repository

import io.basquiat.refactoring.musician.model.code.GenreCode
import io.basquiat.refactoring.musician.model.entity.Musician
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.Rollback
import org.springframework.transaction.annotation.Transactional
import java.util.*

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
        musicianRepository.save(musician)
    }

}