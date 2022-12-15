package io.basquiat.refactoring.album.repository

import io.basquiat.refactoring.album.domain.entity.Album
import io.basquiat.refactoring.common.repository.BaseRepository
import java.util.*

/**
 * AlbumRepository
 */
interface AlbumRepository: BaseRepository<Album, UUID>