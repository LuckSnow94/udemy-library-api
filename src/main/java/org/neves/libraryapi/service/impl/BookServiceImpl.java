package org.neves.libraryapi.service.impl;

import org.neves.libraryapi.model.entity.Book;
import org.neves.libraryapi.model.repository.BookRepository;
import org.neves.libraryapi.service.BookService;
import org.springframework.stereotype.Service;

@Service
public class BookServiceImpl implements BookService {

    private BookRepository repository;

    public BookServiceImpl(BookRepository repository){
        this.repository = repository;
    }

    @Override
    public Book save(Book book) {
        return repository.save(book);
    }

}
