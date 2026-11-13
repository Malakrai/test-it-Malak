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
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    @Test
    void refuseLEmpruntSiLeLivreEstInconnu() {
        //given
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        //when
        Executable emprunt = () -> bookService.borrowBook(99L, 10L);

        //then
        assertThrows(IllegalArgumentException.class, emprunt);
    }

    @Test
    void refuseLEmpruntSiLeMembreEstInconnu() {
        //given
        Book book = new Book("Les Misérables", "Victor Hugo");
        book.setId(1L);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(memberRepository.findById(99L)).thenReturn(Optional.empty());

        //when
        Executable emprunt = () -> bookService.borrowBook(1L, 99L);

        //then
        assertThrows(IllegalArgumentException.class, emprunt);
    }

    @Test
    void refuseLEmpruntSiLeLivreEstDejaEmprunte() {
        //given
        Book book = new Book("Les Misérables", "Victor Hugo");
        book.setId(1L);
        book.setStatus(Status.EMPRUNTE);
        Member malak = new Member("Malak");
        malak.setId(10L);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(memberRepository.findById(10L)).thenReturn(Optional.of(malak));

        //when
        Executable emprunt = () -> bookService.borrowBook(1L, 10L);

        //then
        assertThrows(IllegalStateException.class, emprunt);
    }

    @Test
    void refuseLEmpruntSiLeMembreADejaUnLivreEnCours() {
        //given 
        Book book = new Book("Les Misérables", "Victor Hugo");
        book.setId(1L);
        Member malak = new Member("Malak");
        malak.setId(10L);
        Book livreEnCours = new Book("Le Petit Prince", "Saint-Exupéry");
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(memberRepository.findById(10L)).thenReturn(Optional.of(malak));
        when(bookRepository.findByMemberAndStatus(10L, Status.EMPRUNTE)).thenReturn(List.of(livreEnCours));

        //when
        Executable emprunt = () -> bookService.borrowBook(1L, 10L);

        //then
        assertThrows(IllegalStateException.class, emprunt);
    }

    @Test
    void refuseLeRetourParUnAutreMembreQueLEmprunteur() {
        //given 
        Member malak = new Member("Malak");
        malak.setId(10L);
        Book book = new Book("Les Misérables", "Victor Hugo");
        book.setId(1L);
        book.setStatus(Status.EMPRUNTE);
        book.setBorrower(malak);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        //when 
        Executable retour = () -> bookService.returnBook(1L, 20L);

        //then
        assertThrows(IllegalStateException.class, retour);
    }

    @Test
    void retourSansReferentPrevientSeulementLeMembre() {
        //given 
        Member malak = new Member("Malak");
        malak.setId(10L);
        Book book = new Book("Les Misérables", "Victor Hugo");
        book.setId(1L);
        book.setStatus(Status.EMPRUNTE);
        book.setBorrower(malak);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).thenAnswer(appel -> appel.getArgument(0));

        //when
        Book resultat = bookService.returnBook(1L, 10L);

        //then 
        assertEquals(Status.RENDU, resultat.getStatus());
        verify(mailService, times(1)).sendMail(any(), anyString(), anyString());
        verify(mailService).sendMail(eq(malak), anyString(), anyString());
    }

    @Test
    void retourAvecReferentPrevientLeMembreEtLeReferent() {
        //given 
        Member referente = new Member("Bibliothecaire");
        referente.setId(30L);
        Member malak = new Member("Malak");
        malak.setId(10L);
        malak.setReferent(referente);
        Book book = new Book("Les Misérables", "Victor Hugo");
        book.setId(1L);
        book.setStatus(Status.EMPRUNTE);
        book.setBorrower(malak);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).thenAnswer(appel -> appel.getArgument(0));

        //when
        bookService.returnBook(1L, 10L);

        //then
        verify(mailService).sendMail(eq(malak), anyString(), anyString());
        verify(mailService).sendMail(eq(referente), anyString(), anyString());
    }


    static class FakeMailService implements MailService {
        List<Member> destinataires = new ArrayList<>();

        @Override
        public void sendMail(Member member, String subject, String message) {
            destinataires.add(member);
        }

        @Override
        public void sendMailCloture(Member member) {
            destinataires.add(member);
        }
    }

    @Test
    void emprunterEnvoieUnMailAuMembreAvecUnFake() {
        //given 
        FakeMailService fakeMail = new FakeMailService();
        BookService serviceAvecFake = new BookService(bookRepository, memberRepository, fakeMail);
        Book book = new Book("Les Misérables", "Victor Hugo");
        book.setId(1L);
        Member malak = new Member("Malak");
        malak.setId(10L);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(memberRepository.findById(10L)).thenReturn(Optional.of(malak));
        when(bookRepository.save(any(Book.class))).thenAnswer(appel -> appel.getArgument(0));

        //when
        serviceAvecFake.borrowBook(1L, 10L);

        //then 
        assertEquals(List.of(malak), fakeMail.destinataires);
    }
}