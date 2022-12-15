package io.basquiat.refactoring.album.repository

import io.basquiat.refactoring.album.domain.entity.Album
import io.basquiat.refactoring.common.repository.BaseRepository

/**
 * AlbumRepository
 */
interface AlbumRepository : BaseRepository<Album, Long>