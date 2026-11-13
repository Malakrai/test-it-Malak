package com.example.testit.model;

import jakarta.persistence.*;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "members")
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
// on ajoute @JsonIgnore parcequ'il évite l'erreur 500 en ne mettant pas la liste des livres dans le JSON
    @JsonIgnore
    @OneToMany(mappedBy = "borrower")
    private List<Book> books;

    /** Bibliothécaire référent du membre, prévenu quand il rend un livre. */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "referent_id")
    private Member referent;

    public Member() {}

    public Member(String username) {
        this.username = username;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public List<Book> getBooks() { return books; }
    public void setBooks(List<Book> books) { this.books = books; }

    public Member getReferent() { return referent; }
    public void setReferent(Member referent) { this.referent = referent; }
}
