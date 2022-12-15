package io.basquiat.refactoring.common.exception

import java.lang.RuntimeException

/**
 * EntityEmptyException 관련 에러 처리 exception
 * created by basquiat
 */
class EntityEmptyException(message: String? = "엔티티가 존재하지 않습니다.") : RuntimeException(message)