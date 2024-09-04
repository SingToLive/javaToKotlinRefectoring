package com.group.libraryapp

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class JunitTest {

    // java로는 static인데 companion 안에 있으면 static 처리를 해주며 이때 @JvmStatic을 붙여줘야 한다.
    companion object {

        @BeforeAll
        @JvmStatic
        fun beforeAll() {
            println("before all test")
        }

        @AfterAll
        @JvmStatic
        fun afterAll() {
            println("after all test")
        }
    }

    @BeforeEach
    fun beforeEach() {
        println("before test")
    }

    @AfterEach
    fun afterEach() {
        println("after test")
    }

    @Test
    fun test1() {
        println("1")
    }

    @Test
    fun test2() {
        println("2")
    }

}