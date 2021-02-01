package org.neves.libraryapi.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.neves.libraryapi.exception.BusinessException;
import org.neves.libraryapi.model.entity.Book;
import org.neves.libraryapi.model.repository.BookRepository;
import org.neves.libraryapi.service.impl.BookServiceImpl;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
public class BookServiceTest {

    private BookService service;

    @MockBean
    private BookRepository repository;

    @BeforeEach
    public void setUp(){
        this.service = new BookServiceImpl(repository);
    }

    @Test
    @DisplayName("Deve salvar um livro")
    public void saveBookTest(){
        Book book = createValidBook();
        Mockito.when(repository.existsByIsbn(Mockito.anyString()))
                .thenReturn(false);
        Mockito.when(repository.save(book))
                .thenReturn(Book.builder()
                        .id(1L)
                        .isbn(book.getIsbn())
                        .title(book.getTitle())
                        .author(book.getAuthor()).build());
        Book savedBook = service.save(book);
        assertThat(savedBook.getId()).isNotNull();
        assertThat(savedBook.getIsbn()).isEqualTo(book.getIsbn());
        assertThat(savedBook.getAuthor()).isEqualTo(book.getAuthor());
        assertThat(savedBook.getTitle()).isEqualTo(book.getTitle());
    }

    @Test
    @DisplayName("Deve lançar erro de negócio ao tentar salvar livro com isbn duplicado")
    public void shouldNotSaveBookWithDuplicateIsbn(){
        Book book = createValidBook();
        Mockito.when(repository.existsByIsbn(book.getIsbn()))
                .thenReturn(true);

        Throwable exception = Assertions.catchThrowable(() -> service.save(book));

        assertThat(exception)
                .isInstanceOf(BusinessException.class)
                .hasMessage("Isbn já cadastrado.");
        Mockito.verify(repository, Mockito.never()).save(book);
    }

    private Book createValidBook() {
        return Book.builder().title("Meu livro").author("Autor").isbn("777").build();
    }

}
