package org.neves.libraryapi.model.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.neves.libraryapi.model.entity.Book;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
public class BookRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BookRepository repository;

    @Test
    @DisplayName("Deve retornar verdadeiro quando existir livro com isbn informado")
    public void returnTrueWhenIsbnExists(){
        String isbn = "777";
        entityManager.persist(createValidBookWithIsbn(isbn));

        boolean exists = repository.existsByIsbn(isbn);

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Deve retornar false quando não existir livro com isbn informado")
    public void returnFalseWhenIsbnDoesNotExist(){
        String isbn = "777";
        entityManager.persist(createValidBookWithIsbn("888"));

        boolean exists = repository.existsByIsbn(isbn);

        assertThat(exists).isFalse();
    }

    private Book createValidBookWithIsbn(String isbn) {
        return Book.builder().title("Meu livro").author("Autor").isbn(isbn).build();
    }
}
