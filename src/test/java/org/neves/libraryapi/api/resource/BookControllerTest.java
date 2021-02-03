package org.neves.libraryapi.api.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.neves.libraryapi.api.dto.BookDTO;
import org.neves.libraryapi.exception.BusinessException;
import org.neves.libraryapi.model.entity.Book;
import org.neves.libraryapi.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
public class BookControllerTest {

    private static final String BOOK_API = "/api/books";

    @Autowired
    MockMvc mvc;

    @MockBean
    BookService service;

    @Test
    @DisplayName("Deve criar um livro com sucesso")
    public void createBookTest() throws Exception {

        BookDTO dto = createNewBookDTO();
        String json = instanceNewBookToJson(dto);

        Book savedBook = Book.builder().title("Meu livro").author("Autor").isbn("777").id(1L).build();
        BDDMockito.given(service.save(Mockito.any(Book.class))).willReturn(savedBook);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);
        mvc
                .perform(request)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").value(savedBook.getId()))
                .andExpect(jsonPath("title").value(savedBook.getTitle()))
                .andExpect(jsonPath("author").value(savedBook.getAuthor()))
                .andExpect(jsonPath("isbn").value(savedBook.getIsbn()));
    }

    @Test
    @DisplayName("Deve lançar erro de validação quando faltar algum dado para criar um livro")
    public void createInvalidBookTest() throws Exception {
        String json = instanceNewBookToJson(new BookDTO());
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);
        mvc
                .perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", hasSize(3)));
    }

    private String instanceNewBookToJson(BookDTO bookDTO) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(bookDTO);
    }

    @Test
    @DisplayName("Deve lançar erro ao tentar cadastrar livro com isbn duplicado")
    public void createBookWithDuplicatedIsbn() throws Exception {
        BookDTO dto = createNewBookDTO();
        String json = instanceNewBookToJson(dto);
        String expectedError = "Isbn já cadastrado.";

        BDDMockito.given(service.save(Mockito.any(Book.class))).willThrow(new BusinessException(expectedError));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mvc
                .perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", hasSize(1)))
                .andExpect(jsonPath("errors[0]").value(expectedError));
    }

    @Test
    @DisplayName("Deve obter informações de um livro")
    public void getBookDetailsTest() throws Exception {
        Long id = 1L;
        Book book = createValidBookWithId(id);
        BDDMockito.given(service.getById(id)).willReturn(Optional.of(book));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(BOOK_API.concat("/" + id))
                .accept(MediaType.APPLICATION_JSON);

        mvc
                .perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(id))
                .andExpect(jsonPath("title").value(book.getTitle()))
                .andExpect(jsonPath("author").value(book.getAuthor()))
                .andExpect(jsonPath("isbn").value(book.getIsbn()));
    }

    @Test
    @DisplayName("Deve retornar mensagem que não encontrou o livro buscado")
    public void bookNotFoundTest() throws Exception {
        BDDMockito.given(service.getById(anyLong())).willReturn(Optional.empty());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(BOOK_API.concat("/" + 1L))
                .accept(MediaType.APPLICATION_JSON);

        mvc
                .perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve deletar um livro")
    public void deleteBookTest() throws Exception {
        Long id = 1L;
        BDDMockito.given(service.getById(id)).willReturn(Optional.of(Book.builder().id(id).build()));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .delete(BOOK_API.concat("/" + id));

        mvc
                .perform(request)
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Deve retornar resource not found quando não encontrar um livro para deletar")
    public void deleteInexistentBookTest() throws Exception {
        BDDMockito.given(service.getById(anyLong())).willReturn(Optional.empty());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .delete(BOOK_API.concat("/" + 1L));

        mvc
                .perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve atualizar um livro")
    public void updateBookTest() throws Exception {
        Long id = 1L;
        BookDTO dto = createNewBookDTO();
        String json = instanceNewBookToJson(dto);

        Book updatingBook = createValidBookWithId(id);
        BDDMockito
                .given(service.getById(id))
                .willReturn(Optional.of(updatingBook));

        Book updatedBook = Book.builder().title("Meu livro").author("Jounin").isbn("777").id(id).build();
        updatedBook.setTitle(dto.getTitle());
        updatedBook.setAuthor(dto.getAuthor());
        BDDMockito
                .given(service.update(updatingBook))
                .willReturn(updatedBook);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put(BOOK_API.concat("/"+id))
                .content(json)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON);

        mvc
                .perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(id))
                .andExpect(jsonPath("title").value(dto.getTitle()))
                .andExpect(jsonPath("author").value(dto.getAuthor()))
                .andExpect(jsonPath("isbn").value(dto.getIsbn()));
    }

    @Test
    @DisplayName("Deve retornar resource not found ao tentar atualizar um livro")
    public void updateInexistentBookTest() throws Exception {
        String json = instanceNewBookToJson(createNewBookDTO());

        BDDMockito
                .given(service.getById(anyLong()))
                .willReturn(Optional.empty());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put(BOOK_API.concat("/"+1L))
                .content(json)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON);

        mvc
                .perform(request)
                .andExpect(status().isNotFound());
    }

    private BookDTO createNewBookDTO() {
        return BookDTO.builder().title("Novo livro").author("Genin").isbn("777").build();
    }

    private Book createValidBookWithId(Long id) {
        return Book.builder().title("Meu livro").author("Jounin").isbn("777").id(id).build();
    }
}

