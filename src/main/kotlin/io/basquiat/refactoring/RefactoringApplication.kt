package io.basquiat.refactoring

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@EnableJpaAuditing
@SpringBootApplication
class RefactoringApplication

fun main(args: Array<String>) {
	runApplication<RefactoringApplication>(*args)
}
