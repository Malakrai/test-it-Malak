package com.example.testit;

import com.example.testit.adapter.member.CurrentMemberServiceFake;
import com.example.testit.model.Member;
import com.example.testit.repository.MemberRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    CurrentMemberServiceFake currentMember;

    @Test
    void creerUnLivrePuisLEmprunter() throws Exception {
        //given 
        Member malak = memberRepository.save(new Member("Malak-controller"));
        currentMember.setCurrent(malak.getId());

        //when 
        String reponse = mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"Les Misérables\", \"author\": \"Victor Hugo\"}"))
            
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Les Misérables"))
                .andExpect(jsonPath("$.author").value("Victor Hugo"))
                .andExpect(jsonPath("$.status").value("DISPONIBLE"))
                .andReturn().getResponse().getContentAsString();

    
        Integer id = JsonPath.read(reponse, "$.id");

        //then
        mockMvc.perform(post("/books/" + id + "/borrow"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EMPRUNTE"));
    }
}