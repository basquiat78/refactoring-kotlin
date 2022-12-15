package io.basquiat.refactoring.musician.repository

import io.basquiat.refactoring.common.repository.BaseRepository
import io.basquiat.refactoring.musician.model.entity.Musician
import java.util.*

interface MusicianRepository: BaseRepository<Musician, UUID>