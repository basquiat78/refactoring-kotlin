package io.basquiat.refactoring.musician.model.code

/**
 * 장르 코드 정의 enum
 */
enum class GenreCode(val genre: String) {

    JAZZ("Jazz"),
    ROCK("Rock"),
    POP("Pop"),
    HIPHOP("Hiphop"),
    WORLD("World Music"),
    ETC("etc");

    companion object {
        /**
         * null이면 illegalArgumentException을 던지고 있지만 ETC를 던져도 상관없다.
         * @param genre
         * @return GenreCode
         */
        fun of(genre: String?): GenreCode = values().firstOrNull { genreEnum -> genreEnum.genre.equals(genre, ignoreCase = true) }
                                            ?: throw IllegalArgumentException("맞는 장르 코드가 없습니다. 장르 코드를 확인하세요.")
    }

}