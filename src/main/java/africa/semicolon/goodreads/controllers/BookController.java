package africa.semicolon.goodreads.controllers;

import africa.semicolon.goodreads.services.BookService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class BookController {
    private BookService bookService;
    private List<String> validFileExtension;
    private List<String> validImageExtension;

    public BookController(BookService bookService, List<String> validFileExtension, List<String> validImageExtension) {
        this.bookService = bookService;
        this.validFileExtension = new ArrayList<>();
        this.validImageExtension = new ArrayList<>();
    }


}
