package org.neves.libraryapi.service;

import org.neves.libraryapi.model.entity.Book;

import java.util.Optional;

public interface BookService {

    Book save(Book book);

    Optional<Book> getById(Long id);
}
