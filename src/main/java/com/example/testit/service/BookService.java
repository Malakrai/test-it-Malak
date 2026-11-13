package com.example.testit.service;

import com.example.testit.adapter.mail.MailService;
import com.example.testit.model.Book;
import com.example.testit.model.Member;
import com.example.testit.model.Status;
import com.example.testit.repository.BookRepository;
import com.example.testit.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

// @Transactional : garde la session ouverte pendant toute la méthode (sinon LazyInitializationException)
@Service
@Transactional
public class BookService {

    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;
    private final MailService mailService;

    public BookService(BookRepository bookRepository, MemberRepository memberRepository, MailService mailService) {
        this.bookRepository = bookRepository;
        this.memberRepository = memberRepository;
        this.mailService = mailService;
    }

    public List<Book> findAll() {
        return bookRepository.findAll();
    }

    public Optional<Book> findById(Long id) {
        return bookRepository.findById(id);
    }

    public List<Book> findByMemberId(Long memberId) {
        return bookRepository.findByBorrowerId(memberId);
    }

    public Book createBook(String title, String author) {
        return bookRepository.save(new Book(title, author));
    }

    // on modifie seulement le titre et l'auteur, le statut et l'emprunteur sont gardés
    public Book updateBook(Book book) {
        if (book.getId() == null) {
            throw new IllegalArgumentException("Book not found");
        }
        Book existant = bookRepository.findById(book.getId())
                .orElseThrow(() -> new IllegalArgumentException("Book not found"));
        existant.setTitle(book.getTitle());
        existant.setAuthor(book.getAuthor());
        return bookRepository.save(existant);
    }

    public void deleteBook(Long id) {
        bookRepository.deleteById(id);
    }

    public Book borrowBook(Long bookId, Long memberId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found"));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));
        if (book.getStatus() == Status.EMPRUNTE) {
            throw new IllegalStateException("Book can only be borrowed if status is DISPONIBLE");
        }
        // EMPRUNTE au lieu de RENDU : on cherche les livres en cours du membre
        List<Book> ongoingLoans = bookRepository.findByMemberAndStatus(memberId, Status.EMPRUNTE);
        if (!ongoingLoans.isEmpty()) {
            throw new IllegalStateException("Member cannot borrow more than one book at a time");
        }
        book.setStatus(Status.EMPRUNTE);
        book.setBorrower(member);
        Book saved = bookRepository.save(book);
        // on ajoute cette ligne pour corriger le problème de non-envoi de mail lors de l'emprunt d'un livre
        mailService.sendMail(member, "Livre emprunté", "Vous avez emprunté '" + book.getTitle() + "'.");
        return saved;
    }

    public Book returnBook(Long bookId, Long memberId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found"));
        if (book.getStatus() != Status.EMPRUNTE) {
            throw new IllegalStateException("Book can only be returned if status is EMPRUNTE");
        }
        // seul l'emprunteur peut rendre le livre
        if (!book.getBorrower().getId().equals(memberId)) {
            throw new IllegalStateException("Only the borrower can return the book");
        }
        Member member = book.getBorrower();
        book.setStatus(Status.RENDU);
        Book saved = bookRepository.save(book);
        mailService.sendMail(member, "Livre rendu", "Merci d'avoir rendu '" + book.getTitle() + "'.");
        // Prévenir aussi le bibliothécaire référent si présent (mail au référent, pas au membre)
        if (member.getReferent() != null) {
            mailService.sendMail(member.getReferent(), "Livre rendu par un de vos membres",
                    "'" + book.getTitle() + "' a été rendu par " + member.getUsername() + ".");
        }
        return saved;
    }
}