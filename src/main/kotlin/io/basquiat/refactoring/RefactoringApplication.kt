package io.basquiat.refactoring

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class RefactoringApplication

fun main(args: Array<String>) {
	runApplication<RefactoringApplication>(*args)
}
