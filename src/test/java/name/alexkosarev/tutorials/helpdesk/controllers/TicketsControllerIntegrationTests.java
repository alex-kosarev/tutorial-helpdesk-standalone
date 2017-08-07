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

import name.alexkosarev.tutorials.helpdesk.entities.Ticket;
import name.alexkosarev.tutorials.helpdesk.repositories.TicketRepository;
import static org.junit.Assert.assertFalse;
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
public class TicketsControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TicketRepository ticketRepository;

    /**
     * Просмотр списка заявок.
     *
     * Должна быть возвращена страница со списком заявок. На странице должна быть ссылка на создание завки.
     */
    @Test
    public void indexShouldReturnTicketsPage() throws Exception {
        mockMvc.perform(get("/tickets"))
                .andDo(print())
                .andExpect(model().attributeExists("page"))
                .andExpect(view().name("tickets/index"))
                .andExpect(status().isOk())
                .andExpect(xpath(".//a[@href='/tickets/create']").exists());
    }

    /**
     * Просмотр заявки, оптимистичный сценарий.
     *
     * Если заявка найдена, то должна быть возвращена страница просмотра заявки. На странице должны быть ссылки на
     * редактирование и удаление заявки.
     */
    @Test
    public void viewWhenTicketExistsShouldShowTicketPage() throws Exception {
        mockMvc.perform(get("/tickets/1"))
                .andDo(print())
                .andExpect(model().attributeExists("ticket"))
                .andExpect(view().name("tickets/viewOne"))
                .andExpect(status().isOk())
                .andExpect(xpath(".//a[@href='/tickets']").exists())
                .andExpect(xpath(".//a[@href='/tickets/1/edit']").exists())
                .andExpect(xpath(".//a[@href='/tickets/1/delete']").exists());
    }

    /**
     * Просмотр заявки, если заявка не существует.
     *
     * Если заявка не найдена, должен быть возвращён ответ со статусом 404 Not Found
     */
    @Test
    public void viewWhenTicketDoesNotExistShouldReturn404Status() throws Exception {
        mockMvc.perform(get("/tickets/100500"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    public void createPageShouldReturnCreatePage() throws Exception {
        mockMvc.perform(get("/tickets/create"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(xpath(".//form[@method='post' and @action='/tickets/create']//*[@name='issue']").exists())
                .andExpect(xpath(".//form[@method='post' and @action='/tickets/create']//*[@name='issueDetails']").exists())
                .andExpect(xpath(".//form[@method='post' and @action='/tickets/create']//*[@type='submit']").exists());
    }

    /**
     * Сохранение новой заявки, оптимистичный сценарий.
     *
     * Если переданные данные валидны, заявка должна быть сохранена в базе, а пользователь перенаправлен на страницу
     * просмотра заявки.
     */
    @Test
    public void createWhenFormIsValidShouldSaveTicketAndReturnRedirection() throws Exception {
        mockMvc.perform(post("/tickets/create").param("issue", "Some issue").param("issueDetails", "Some issue description"))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/tickets/*"));
    }

    /**
     * Сохранение новой заявки, если переданные данные невалидны.
     *
     * Если переданные данные невалидны, заявка не должна быть сохранена, а пользователю должна быть возвращена форма
     * создания заявки с заполненными данными и ошибками и статусом 400 Bad Request.
     */
    @Test
    public void createWhenFormIsInvalidShouldDoNothingAndReturnModelAndViewWith400Status() throws Exception {
        String issueDetails = "Some issue description";
        mockMvc.perform(post("/tickets/create").param("issueDetails", issueDetails))
                .andDo(print())
                .andExpect(model().attributeExists("ticket"))
                .andExpect(model().attributeExists("errors"))
                .andExpect(view().name("tickets/create"))
                .andExpect(status().isBadRequest())
                .andExpect(xpath(".//form[@method='post' and @action='/tickets/create']//*[@name='issueDetails']").string(issueDetails));
    }

    /**
     * Страница редактирования заявки, оптимистичный сценарий.
     *
     * Если запрашивается страница редактирования существующей заявки, должна быть возвращена страница редактирования
     * заявки с заполненными полями. На странице должна присутствовать форма создания заявки с полями issue и
     * issueDetails и кнопкой отправки, а также ссылки на заявку и список заявок.
     */
    @Test
    public void editPageWhenTicketExistsShouldReturnModelAndView() throws Exception {
        Ticket ticket = ticketRepository.findOne(1);

        mockMvc.perform(get("/tickets/1/edit"))
                .andDo(print())
                .andExpect(model().attributeExists("ticket"))
                .andExpect(view().name("tickets/edit"))
                .andExpect(status().isOk())
                .andExpect(xpath(".//form[@method='post' and @action='/tickets/1/edit']//*[@name='issue']/@value").string(ticket.getIssue()))
                .andExpect(xpath(".//form[@method='post' and @action='/tickets/1/edit']//*[@name='issueDetails']").string(ticket.getIssueDetails()))
                .andExpect(xpath(".//form[@method='post' and @action='/tickets/1/edit']//*[@type='submit']").exists())
                .andExpect(xpath(".//a[@href='/tickets']").exists())
                .andExpect(xpath(".//a[@href='/tickets/1']").exists());
    }

    /**
     * Страница редактирования заявки, если заявка не существует.
     *
     * Если запрашивается страница редактирования несуществующей заявки, должен быть возвращён ответ со статусом 404 Not
     * Found.
     */
    @Test
    public void editPageWhenTicketDoesNotExistShouldThrowException() throws Exception {
        mockMvc.perform(get("/tickets/100500/edit"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    /**
     * Сохранение изменений в заявке, оптимистичный сценарий.
     *
     * Если редактируемая заявка существует и переданные данные валидны, то изменения должны быть сохранены в базе и
     * должно быть возвращено перенаправление на страницу просмотра заявки.
     */
    @Test
    public void updateWhenFormIsValidShouldSaveTicketAndReturnRedirection() throws Exception {
        mockMvc.perform(post("/tickets/2/edit").param("issue", "Some edited issue").param("issueDetails", "Some edited issue description"))
                .andDo(print())
                .andExpect(redirectedUrl("/tickets/2"));
    }

    /**
     * Сохранение изменения зявки, если полученные данные невалидны.
     *
     * Если редактируемая заявка существует, но полученные данные невалидны, заявка не должна быть сохранена, должна
     * быть возвращена модель с полем ticket, содержащим введённые данные, полем errors, содержащим ошибки валидации,
     * статусом 400 Bad Request и шаблон tickets/edit
     */
    @Test
    public void updateWhenFormIsInvalidShouldDoNothingAndReturnModelAndViewWith400Status() throws Exception {
        mockMvc.perform(post("/tickets/2/edit").param("issueDetails", "Some edited issue description"))
                .andDo(print())
                .andExpect(model().attributeExists("ticket"))
                .andExpect(model().attributeExists("errors"))
                .andExpect(view().name("tickets/edit"))
                .andExpect(status().isBadRequest())
                .andExpect(xpath(".//form[@method='post' and @action='/tickets/2/edit']//*[@name='issue']/@value").string(""))
                .andExpect(xpath(".//form[@method='post' and @action='/tickets/2/edit']//*[@name='issueDetails']").string("Some edited issue description"));
    }

    /**
     * Сохранение изменений в заявке, если заявка не существует.
     *
     * Если редактируемая заявка не существует, должен быть возвращён ответ со статуом 404 Not Found.
     */
    @Test
    public void updateWhenTicketDoesNotExistShouldThrowException() throws Exception {
        mockMvc.perform(post("/tickets/100500/edit"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    /**
     * Страница подтверждения удаления, оптимистичный сценарий.
     *
     * Если удаляемая заявка существует, должна быть возвращена страница с запросом на подтверждение заявки. На странице
     * должна присутсвовать кнопка отправки и ссылки на заявку и список заявок.
     */
    @Test
    public void deleteConfirmationPageWhenTicketExistsShouldReturnModelAndView() throws Exception {
        mockMvc.perform(get("/tickets/1/delete"))
                .andDo(print())
                .andExpect(model().attributeExists("ticket"))
                .andExpect(view().name("tickets/delete"))
                .andExpect(status().isOk())
                .andExpect(xpath(".//form[@method='post' and @action='/tickets/1/delete']//*[@type='submit']").exists())
                .andExpect(xpath(".//a[@href='/tickets']").exists())
                .andExpect(xpath(".//a[@href='/tickets/1']").exists());
    }

    /**
     * Страница подтверждения удаления несуществующей заявки.
     *
     * Если удаляемая заявка не существует, должен быть возвращён ответ со статусом 404 Not Found.
     */
    @Test
    public void deleteConfirmationPageWhenTicketDoesNotExistShouldThrowException() throws Exception {
        mockMvc.perform(get("/tickets/100500/delete"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    /**
     * Удаление заявки, оптимистичный сценарий.
     *
     * Если удаляемая заявка существует, она должна быть удалена из базы, должно быть возвращено перенаправление на
     * страницу списка заявок.
     */
    @Test
    public void deleteWhenTicketExistsShouldDeleteTicketAndReturnRedirectionToIndex() throws Exception {
        mockMvc.perform(post("/tickets/3/delete"))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tickets"));

        assertFalse(ticketRepository.exists(3));
    }

    /**
     * Удаление несуществующей заявки.
     *
     * Если удаляемая заявка не существует, должен быть возвращёт ответ со статусом 404 Not Found.
     */
    @Test
    public void deleteWhenTicketDoesNotExistShouldThrowException() throws Exception {
        mockMvc.perform(post("/tickets/100500/delete"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
}
