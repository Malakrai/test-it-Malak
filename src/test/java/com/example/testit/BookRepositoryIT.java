package com.example.testit;

import com.example.testit.model.Book;
import com.example.testit.model.Member;
import com.example.testit.model.Status;
import com.example.testit.repository.BookRepository;
import com.example.testit.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class BookRepositoryIT {

    @Autowired
    BookRepository bookRepository;

    @Autowired
    MemberRepository memberRepository;

    @Test
    void findByMemberAndStatusNeRenvoieQueLesLivresDuMembreAvecCeStatut() {
        //given 
        Member malak = memberRepository.save(new Member("Malak"));
        Member Nour = memberRepository.save(new Member("Nour"));

        Book book1 = new Book("Les Misérables", "Victor Hugo");
        book1.setBorrower(malak);
        book1.setStatus(Status.EMPRUNTE);
        bookRepository.save(book1);

        Book book2 = new Book("Le Petit Prince", "Saint-Exupéry");
        book2.setBorrower(malak);
        book2.setStatus(Status.RENDU);
        bookRepository.save(book2);

        Book book3 = new Book("L'Étranger", "Camus");
        book3.setBorrower(Nour);
        book3.setStatus(Status.EMPRUNTE);
        bookRepository.save(book3);

        //when 
        List<Book> resultat = bookRepository.findByMemberAndStatus(malak.getId(), Status.EMPRUNTE);

        //then 
        assertEquals(1, resultat.size());
        assertEquals("Les Misérables", resultat.get(0).getTitle());
    }
}