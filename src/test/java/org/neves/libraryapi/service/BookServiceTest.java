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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
public class BookServiceTest {

    private BookService service;

    @MockBean
    private BookRepository repository;

    @BeforeEach
    public void setUp() {
        this.service = new BookServiceImpl(repository);
    }

    @Test
    @DisplayName("Deve salvar um livro")
    public void saveBookTest() {
        Book book = createValidBook(1L);
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
    public void shouldNotSaveBookWithDuplicateIsbnTest() {
        Book book = createValidBook(1L);
        Mockito.when(repository.existsByIsbn(book.getIsbn()))
                .thenReturn(true);

        Throwable exception = Assertions.catchThrowable(() -> service.save(book));

        assertThat(exception)
                .isInstanceOf(BusinessException.class)
                .hasMessage("Isbn já cadastrado.");
        Mockito.verify(repository, Mockito.never()).save(book);
    }

    @Test
    @DisplayName("Deve obter um livro pelo id")
    public void getByIdTest() {
        Long id = 1L;
        Book book = createValidBook(id);
        Mockito.when(repository.findById(id))
                .thenReturn(Optional.of(book));

        Optional<Book> optionalBook = service.getById(id);

        assertThat(optionalBook.isPresent()).isTrue();
        Book wantedBook = optionalBook.get();
        assertThat(wantedBook.getId()).isEqualTo(id);
        assertThat(wantedBook.getIsbn()).isEqualTo(book.getIsbn());
        assertThat(wantedBook.getAuthor()).isEqualTo(book.getAuthor());
        assertThat(wantedBook.getTitle()).isEqualTo(book.getTitle());
    }

    @Test
    @DisplayName("Deve retornar vazio ao obter um livro não existente")
    public void bookNotFoundByIdTest() {
        Long id = 1L;
        Mockito.when(repository.findById(id))
                .thenReturn(Optional.empty());

        Optional<Book> optionalBook = service.getById(id);

        assertThat(optionalBook.isPresent()).isFalse();
    }

    private Book createValidBook(Long id) {
        return Book.builder().id(id).title("Meu livro").author("Autor").isbn("777").build();
    }

}
