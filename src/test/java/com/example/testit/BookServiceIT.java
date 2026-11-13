package com.example.testit;

import com.example.testit.model.Book;
import com.example.testit.model.Member;
import com.example.testit.model.Status;
import com.example.testit.repository.BookRepository;
import com.example.testit.repository.MemberRepository;
import com.example.testit.service.BookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class BookServiceIT {

    @Autowired
    BookService bookService;

    @Autowired
    BookRepository bookRepository;

    @Autowired
    MemberRepository memberRepository;

    @Test
    void emprunterPuisRendreUnLivreLeMetAuStatutRendu() {
        //given 
        Member malak = memberRepository.save(new Member("Malak-service"));
        Book book = bookService.createBook("Les Misérables", "Victor Hugo");

        //when
        bookService.borrowBook(book.getId(), malak.getId());
        bookService.returnBook(book.getId(), malak.getId());

        //then 
        Book relu = bookRepository.findById(book.getId()).orElseThrow();
        assertEquals(Status.RENDU, relu.getStatus());
    }
}