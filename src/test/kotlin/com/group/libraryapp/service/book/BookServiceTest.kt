package com.group.libraryapp.service.book

import com.group.libraryapp.domain.book.Book
import com.group.libraryapp.domain.book.BookRepository
import com.group.libraryapp.domain.book.BookType
import com.group.libraryapp.domain.user.User
import com.group.libraryapp.domain.user.UserRepository
import com.group.libraryapp.domain.user.loanhistory.UserLoanHistory
import com.group.libraryapp.domain.user.loanhistory.UserLoanHistoryRepository
import com.group.libraryapp.domain.user.loanhistory.UserLoanStatus
import com.group.libraryapp.dto.book.request.BookLoanRequest
import com.group.libraryapp.dto.book.request.BookRequest
import com.group.libraryapp.dto.book.request.BookReturnRequest
import com.group.libraryapp.dto.book.response.BookStatResponse
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class BookServiceTest @Autowired constructor(
    private val bookService: BookService,
    private val bookRepository: BookRepository,
    private val userRepository: UserRepository,
    private val userLoanHistoryRepository: UserLoanHistoryRepository,
){
    @AfterEach
    fun clean() {
        bookRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    @DisplayName("save Book successfully work")
    fun saveBookTest() {
        // given
        val request = BookRequest("이상한 나라의 엘리스", BookType.COMPUTER)

        // when
        bookService.saveBook(request)

        // then
        val books = bookRepository.findAll()
        assertThat(books).hasSize(1)
        assertThat(books[0].name).isEqualTo("이상한 나라의 엘리스")
        assertThat(books[0].type).isEqualTo(BookType.COMPUTER)
    }

    @Test
    @DisplayName("loan Book successfully work")
    fun loanBookTest() {
        // given
        bookRepository.save(Book.fixture("이상한 나라의 엘리스"))
        val savedUser = userRepository.save(User("최태현", null))
        val request = BookLoanRequest("최태현", "이상한 나라의 엘리스")

        // when
        bookService.loanBook(request)

        // then
        val results = userLoanHistoryRepository.findAll()
        assertThat(results).hasSize(1)
        assertThat(results[0].bookName).isEqualTo("이상한 나라의 엘리스")
        assertThat(results[0].user.id).isEqualTo(savedUser.id)
        assertThat(results[0].status).isEqualTo(UserLoanStatus.LOANED)
    }

    @Test
    @DisplayName("if book is already loan then new request fail")
    fun loanBookFail() {
        // given
        bookRepository.save(Book.fixture("이상한 나라의 엘리스"))
        val savedUser = userRepository.save(User("최태현", null))
        userLoanHistoryRepository.save(UserLoanHistory.fixture(savedUser, "이상한 나라의 엘리스"))
        val request = BookLoanRequest("최태현", "이상한 나라의 엘리스")

        // when & then
        assertThrows<IllegalArgumentException> {
            bookService.loanBook(request)
        }.apply {
            assertThat(message).isEqualTo("진작 대출되어 있는 책입니다")
        }
    }

    @Test
    @DisplayName("책 반납이 정상 동작")
    fun returnBookTest() {
        // given
        bookRepository.save(Book.fixture("이상한 나라의 엘리스"))
        val savedUser = userRepository.save(User("최태현", null))
        userLoanHistoryRepository.save(UserLoanHistory.fixture(savedUser, "이상한 나라의 엘리스", UserLoanStatus.RETUREND))
        val request = BookReturnRequest("최태현", "이상한 나라의 엘리스")

        // when
        bookService.returnBook(request)

        // then
        val results = userLoanHistoryRepository.findAll()
        assertThat(results).hasSize(1)
        assertThat(results[0].status).isEqualTo(UserLoanStatus.RETUREND)
    }

    @Test
    @DisplayName("책 대여 권수를 정상 확인한다")
    fun countLoanedBookTest() {
        // given
        val savedUser = userRepository.save(User("최태현", null))
        userLoanHistoryRepository.saveAll(listOf(
            UserLoanHistory.fixture(savedUser, "A"),
            UserLoanHistory.fixture(savedUser, "B", UserLoanStatus.RETUREND)
        ))

        // when
        val result = bookService.countLoanedBook()

        // then
        assertThat(result).isEqualTo(1)
    }

    @Test
    @DisplayName("분야별 책 권수를 정상 확인한다")
    fun getBookStatisticsTest() {
        // given
        bookRepository.saveAll(listOf(
            Book.fixture("A", BookType.COMPUTER),
            Book.fixture("B", BookType.COMPUTER),
            Book.fixture("C", BookType.SCIENCE)
        ))

        // when
        val results = bookService.getBookStatistics()

        // then
        assertThat(results).hasSize(2)
        assertCount(results, BookType.COMPUTER, 2L)
        assertCount(results, BookType.SCIENCE, 1L)
//        val computerDto = results.first { results -> results.type == BookType.COMPUTER}
//        assertThat(computerDto.count).isEqualTo(2)
//
//        val scienceDto = results.first { results -> results.type == BookType.SCIENCE }
//        assertThat(scienceDto.count).isEqualTo(1)
    }
}

private fun assertCount(results: List<BookStatResponse>, type: BookType, count: Long) {
    assertThat(results.first { result -> result.type == type }.count).isEqualTo(count)
}