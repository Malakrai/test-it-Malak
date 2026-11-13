package com.example.testit;

import com.example.testit.adapter.mail.MailService;
import com.example.testit.model.Book;
import com.example.testit.model.Member;
import com.example.testit.model.Status;
import com.example.testit.repository.BookRepository;
import com.example.testit.repository.MemberRepository;
import com.example.testit.service.BookService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    BookRepository bookRepository;

    @Mock
    MemberRepository memberRepository;

    @Mock
    MailService mailService;

    @InjectMocks
    BookService bookService;

    @Test
    void emprunterUnLivreDisponibleEtVerifieLeMail() {
        //given
        Book book = new Book("Les Misérables", "Victor Hugo");
        book.setId(1L);
        Member malak = new Member("Malak");
        malak.setId(10L);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(memberRepository.findById(10L)).thenReturn(Optional.of(malak));
        when(bookRepository.save(any(Book.class))).thenAnswer(appel -> appel.getArgument(0));

        //when
        Book resultat = bookService.borrowBook(1L, 10L);

        //then
        assertEquals(Status.EMPRUNTE, resultat.getStatus());
        assertEquals(malak, resultat.getBorrower());
        verify(mailService).sendMail(eq(malak), anyString(), anyString());
    }
}