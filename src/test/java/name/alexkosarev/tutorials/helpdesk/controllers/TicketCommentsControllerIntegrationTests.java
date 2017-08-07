/*
 * Copyright 2017 Alexander Kosarev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package name.alexkosarev.tutorials.helpdesk.controllers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 *
 * @author Alexander Kosarev
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class TicketCommentsControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;
    
    @Test
    public void createWhenTicketExistsAndFormIsValidShouldSaveTicketCommentAndReturnRedirection() throws Exception {
        mockMvc.perform(post("/tickets/1/comments").param("comment", "Sample comment"))
                .andDo(print())
                .andExpect(view().name("redirect:/tickets/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tickets/1"));
    }
    
    @Test
    public void createWhenTicketExistsAndFormIsInvalidShouldDoNothingAndReturnModelAndViewWith400Status() throws Exception {
        mockMvc.perform(post("/tickets/1/comments"))
                .andDo(print())
                .andExpect(view().name("tickets/viewOne"))
                .andExpect(model().hasErrors())
                .andExpect(status().isBadRequest());
    }
    
    @Test
    public void createWhenTicketDoesNotExistShouldThrowException() throws Exception {
        mockMvc.perform(post("/tickets/100500/comments"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
    
    @Test
    public void deletePageWhenTicketAndCommentExistAndRelatedShouldReturnDeletePage() throws Exception {
        mockMvc.perform(get("/tickets/1/comments/1/delete"))
                .andDo(print())
                .andExpect(view().name("tickets/comments/delete"))
                .andExpect(model().attributeExists("comment"))
                .andExpect(status().isOk());
    }
    
    @Test
    public void deletePageWhenTicketDoesNotExistShouldThrowException() throws Exception {
        mockMvc.perform(get("/tickets/100500/comments/1/delete"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
    
    @Test
    public void deletePageWhenCommentDoesNotExistShouldThrowException() throws Exception {
        mockMvc.perform(get("/tickets/1/comments/100500/delete"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
    
    @Test
    public void deletePageWhenTicketAndCommentNotRelatedShouldThrowException() throws Exception {
        mockMvc.perform(get("/tickets/2/comments/1/delete"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
    
    @Test
    public void deleteWhenTicketAndCommentExistAndRelatedShouldDeleteCommentAndReturnRedirection() throws Exception {
        mockMvc.perform(post("/tickets/1/comments/3/delete"))
                .andDo(print())
                .andExpect(view().name("redirect:/tickets/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tickets/1"));
    }
    
    @Test
    public void deleteWhenTicketDoesNotExistShouldThrowException() throws Exception {
        mockMvc.perform(post("/tickets/100500/comments/3/delete"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
    
    @Test
    public void deleteWhenCommentDoesNotExistShouldThrowException() throws Exception {
        mockMvc.perform(post("/tickets/1/comments/100500/delete"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
    
    @Test
    public void deleteWhenTicketAndCommentNotRelatedShouldThrowException() throws Exception {
        mockMvc.perform(post("/tickets/1/comments/2/delete"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
}
