package io.basquiat.refactoring.common.utils

import io.basquiat.refactoring.common.exception.EntityEmptyException
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * id로 조회된 entity가 없을 경우
 */
fun entityEmpty(): Nothing {
    throw EntityEmptyException()
}

/**
 * 메세지가 있는 경우
 */
fun entityEmpty(message: String?): Nothing {
    if(message == null) {
        entityEmpty()
    } else {
        throw EntityEmptyException(message)
    }
}

/**
 * 클래스별로 로거 설정할 수 있도록 Inline, reified를 통해 제너릭하게 사용해 보자.
 */
inline fun <reified T> logger(): Logger = LoggerFactory.getLogger(T::class.java)