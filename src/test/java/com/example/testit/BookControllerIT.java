package com.example.testit;

import com.example.testit.adapter.member.CurrentMemberServiceFake;
import com.example.testit.model.Book;
import com.example.testit.model.Member;
import com.example.testit.model.Status;
import com.example.testit.repository.BookRepository;
import com.example.testit.repository.MemberRepository;
import com.example.testit.service.BookService;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BookControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    BookRepository bookRepository;

    @Autowired
    BookService bookService;

    @Autowired
    CurrentMemberServiceFake currentMember;

    @Test
    void creerUnLivrePuisLEmprunter() throws Exception {
        //given 
        Member malak = memberRepository.save(new Member("Malak-controller"));
        currentMember.setCurrent(malak.getId());

        //when 
        ResultActions creation = mockMvc.perform(post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\": \"Les Misérables\", \"author\": \"Victor Hugo\"}"));

        //then 
        creation.andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Les Misérables"))
                .andExpect(jsonPath("$.author").value("Victor Hugo"))
                .andExpect(jsonPath("$.status").value("DISPONIBLE"));

        String reponse = creation.andReturn().getResponse().getContentAsString();
        Integer id = JsonPath.read(reponse, "$.id");

        //when 
        ResultActions emprunt = mockMvc.perform(post("/books/" + id + "/borrow"));

        //then 
        emprunt.andExpect(status().isOk())
               .andExpect(jsonPath("$.status").value("EMPRUNTE"));
    }

 

    @Test
    void modifierUnLivreEmprunteGardeSonStatut() throws Exception {
        //given
        Member malak = memberRepository.save(new Member("Malak-put"));
        Book book = bookService.createBook("Les Misérables", "Victor Hugo");
        bookService.borrowBook(book.getId(), malak.getId());

        //when 
        ResultActions modification = mockMvc.perform(put("/books/" + book.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\": \"Les Misérables (poche)\", \"author\": \"Victor Hugo\"}"));

        //then 
        modification.andExpect(status().isOk());
        Book relu = bookRepository.findById(book.getId()).orElseThrow();
        assertEquals(Status.EMPRUNTE, relu.getStatus());
    }

    @Test
    void supprimerUnLivreQuiNExistePasRenvoie404() throws Exception {
        //when
        ResultActions suppression = mockMvc.perform(delete("/books/999999"));

        //then
        suppression.andExpect(status().isNotFound());
    }

    @Test
    void emprunterSansEtreConnecteRenvoie401() throws Exception {
        //given 
        Book book = bookService.createBook("Les Misérables", "Victor Hugo");
        currentMember.setCurrent(null);

        //when
        ResultActions emprunt = mockMvc.perform(post("/books/" + book.getId() + "/borrow"));

        //then 
        emprunt.andExpect(status().isUnauthorized());
    }

    @Test
    void creerUnLivreAvecUnTitreVideRenvoie400() throws Exception {
        //when
        ResultActions creation = mockMvc.perform(post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\": \"\", \"author\": \"Victor Hugo\"}"));

        //then
        creation.andExpect(status().isBadRequest());
    }

    @Test
    void creerUnLivreAvecUnAuteurVideRenvoie400() throws Exception {
        //when
        ResultActions creation = mockMvc.perform(post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\": \"Les Misérables\", \"author\": \"\"}"));

        //then
        creation.andExpect(status().isBadRequest());
    }
}