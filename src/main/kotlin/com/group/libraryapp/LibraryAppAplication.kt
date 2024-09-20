package com.group.libraryapp

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class LibraryAppAplication

fun main(args: Array<String>) {
    runApplication<LibraryAppAplication>(*args)
}

