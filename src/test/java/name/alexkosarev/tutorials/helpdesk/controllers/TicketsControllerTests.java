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

import java.util.Arrays;
import name.alexkosarev.tutorials.helpdesk.entities.Ticket;
import name.alexkosarev.tutorials.helpdesk.exceptions.EntityNotFoundException;
import name.alexkosarev.tutorials.helpdesk.forms.TicketForm;
import name.alexkosarev.tutorials.helpdesk.repositories.TicketCommentRepository;
import name.alexkosarev.tutorials.helpdesk.repositories.TicketRepository;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.notNull;
import org.mockito.Mock;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import static org.springframework.test.web.ModelAndViewAssert.assertAndReturnModelAttributeOfType;
import static org.springframework.test.web.ModelAndViewAssert.assertViewName;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;

/**
 * Модульные тесты для тестирования {@link TicketsController}
 *
 * @author Alexander Kosarev
 */
public class TicketsControllerTests {

    private TicketsController controller;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private TicketRepository repository;
    
    @Mock
    private TicketCommentRepository ticketCommentRepository;

    @Before
    public void setUp() {
        initMocks(this);

        controller = new TicketsController(repository, ticketCommentRepository);
    }

    /**
     * Просмотр списка заявок.
     *
     * Должна быть возвращена модель, содержащая поле page типа {@link Pageable} и шаблон tickets/index.
     */
    @Test
    public void indexShouldReturnModelAndView() {
        doReturn(new PageImpl<>(Arrays.asList(new Ticket(), new Ticket(), new Ticket()))).when(repository)
                .findAll(any(Pageable.class));

        ModelAndView index = controller.index(new PageRequest(0, 10));

        verify(repository).findAll(notNull(Pageable.class));

        assertViewName(index, "tickets/index");
        assertEquals(3, assertAndReturnModelAttributeOfType(index, "page", Page.class)
                .getNumberOfElements());
    }

    /**
     * Просмотр заявки, оптимистичный сценарий.
     *
     * Если заявка найдена, то должна быть возвращена модель, содержащая поле ticket типа {@link Ticket} и шаблон
     * tickets/viewOne.
     */
    @Test
    public void viewOneWhenTicketExistsShoudlReturnModelAndView() throws EntityNotFoundException {
        ModelAndView viewOne = controller.viewOne(new Ticket(), new PageRequest(0, 10));

        assertViewName(viewOne, "tickets/viewOne");
        assertAndReturnModelAttributeOfType(viewOne, "ticket", Ticket.class);
    }

    /**
     * Просмотр заявки, если заявка не существует.
     *
     * Если заявка не найдена должно быть выброшено исключение {@link EntityNotFoundException}.
     */
    @Test
    public void viewOneWhenTicketDoesNotExistShouldThrowException() throws EntityNotFoundException {
        expectedException.expect(EntityNotFoundException.class);

        controller.viewOne(null, null);
    }

    @Test
    public void createPageShouldReturnModelAndView() {
        ModelAndView createPage = controller.createPage();
        assertViewName(createPage, "tickets/create");
    }

    /**
     * Сохранение новой заявки, оптимистичный сценарий.
     *
     * Если переданные данные валидны, заявка должна быть сохранена в базе, а пользователь перенаправлен на страницу
     * просмотра заявки.
     */
    @Test
    public void createWhenFormIsValidShouldSaveTicketAndReturnRedirection() {
        BindingResult bindingResult = mock(BindingResult.class);
        doReturn(false).when(bindingResult)
                .hasErrors();
        doAnswer(invocation -> invocation.getArgumentAt(0, Ticket.class)).when(repository)
                .save(any(Ticket.class));

        ModelAndView create = controller.create(new TicketForm(), bindingResult);

        verify(bindingResult).hasErrors();
        verify(repository).save(notNull(Ticket.class));

        assertViewName(create, "redirect:/tickets/0");
    }

    /**
     * Сохранение новой заявки, если переданные данные невалидны.
     *
     * Если переданные данные невалидны, заявка не должна быть сохранена, должна быть возвращена модель с полем ticket,
     * содержащим введённые данные, полем errors, содержащим ошибки валидации, статусом 400 Bad Request и шаблон
     * tickets/create
     */
    @Test
    public void createWhenFormIsInvalidShouldDoNothingAndReturnModelAndViewWith400Status() {
        BindingResult bindingResult = mock(BindingResult.class);
        doReturn(true).when(bindingResult)
                .hasErrors();

        ModelAndView create = controller.create(new TicketForm(), bindingResult);

        verify(bindingResult).hasErrors();
        verify(repository, never()).save(notNull(Ticket.class));

        assertViewName(create, "tickets/create");
        assertAndReturnModelAttributeOfType(create, "ticket", Ticket.class);
        assertAndReturnModelAttributeOfType(create, "errors", Errors.class);
        assertEquals(HttpStatus.BAD_REQUEST, create.getStatus());
    }

    /**
     * Страница редактирования заявки, оптимистичный сценарий.
     *
     * Если запрашивается страница редактирования существующей заявки, должна быть возвращена модель с полем ticket и
     * шаблон tickets/edit.
     */
    @Test
    public void editPageWhenTicketExistsShouldReturnModelAndView() throws EntityNotFoundException {
        ModelAndView editPage = controller.editPage(new Ticket());

        assertViewName(editPage, "tickets/edit");
        assertAndReturnModelAttributeOfType(editPage, "ticket", Ticket.class);
    }

    /**
     * Страница редактирования заявки, если заявка не существует.
     *
     * Если запрашивается страница редактирования несуществующей заявки, должно быть выброшено исключение.
     */
    @Test
    public void editPageWhenTicketDoesNotExistShouldThrowException() throws EntityNotFoundException {
        expectedException.expect(EntityNotFoundException.class);

        controller.editPage(null);
    }

    /**
     * Сохранение изменений в заявке, оптимистичный сценарий.
     *
     * Если редактируемая заявка существует и переданные данные валидны, то изменения должны быть сохранены в базе и
     * должно быть возвращено перенаправление на страницу просмотра заявки.
     */
    @Test
    public void editWhenFormIsValidShouldSaveTicketAndReturnRedirection() throws EntityNotFoundException {
        BindingResult bindingResult = mock(BindingResult.class);
        doReturn(false).when(bindingResult)
                .hasErrors();
        doAnswer(invocation -> invocation.getArgumentAt(0, Ticket.class)).when(repository)
                .save(any(Ticket.class));

        ModelAndView edit = controller.edit(new TicketForm(), bindingResult, new Ticket());

        verify(bindingResult).hasErrors();
        verify(repository).save(notNull(Ticket.class));

        assertViewName(edit, "redirect:/tickets/0");
    }

    /**
     * Сохранение изменения зявки, если полученные данные невалидны.
     *
     * Если редактируемая заявка существует, но полученные данные невалидны, заявка не должна быть сохранена, должна
     * быть возвращена модель с полем ticket, содержащим введённые данные, полем errors, содержащим ошибки валидации,
     * статусом 400 Bad Request и шаблон tickets/edit
     */
    @Test
    public void editWhenFormIsInvalidShouldDoNothingAndReturnModelAndViewWith400Status() throws EntityNotFoundException {
        BindingResult bindingResult = mock(BindingResult.class);
        doReturn(true).when(bindingResult)
                .hasErrors();

        ModelAndView edit = controller.edit(new TicketForm(), bindingResult, new Ticket());

        verify(bindingResult).hasErrors();
        verify(repository, never()).save(notNull(Ticket.class));

        assertViewName(edit, "tickets/edit");
        assertAndReturnModelAttributeOfType(edit, "ticket", Ticket.class);
        assertAndReturnModelAttributeOfType(edit, "errors", Errors.class);
        assertEquals(HttpStatus.BAD_REQUEST, edit.getStatus());
    }

    /**
     * Сохранение изменений в заявке, если заявка не существует.
     *
     * Если редактируемая заявка не существует, должно быть выброшено исключение.
     */
    @Test
    public void editWhenTicketDoesNotExistShouldThrowException() throws EntityNotFoundException {
        expectedException.expect(EntityNotFoundException.class);

        controller.edit(null, null, null);
    }

    /**
     * Страница подтверждения удаления, оптимистичный сценарий.
     *
     * Если удаляемая заявка существует, должна быть возвращена модель с полем ticket типа Ticket и шаблон
     * tickets/delete.
     */
    @Test
    public void deletePageWhenTicketExistsShouldReturnModelAndView() throws EntityNotFoundException {
        ModelAndView deleteConfirmationPage = controller.deletePage(new Ticket());

        assertViewName(deleteConfirmationPage, "tickets/delete");
        assertAndReturnModelAttributeOfType(deleteConfirmationPage, "ticket", Ticket.class);
    }

    /**
     * Страница подтверждения удаления несуществующей заявки.
     *
     * Если удаляемая заявка не существует, должно быть выброшено исключение.
     */
    @Test
    public void deletePageWhenTicketDoesNotExistShouldThrowException() throws EntityNotFoundException {
        expectedException.expect(EntityNotFoundException.class);

        controller.deletePage(null);
    }

    /**
     * Удаление заявки, оптимистичный сценарий.
     *
     * Если удаляемая заявка существует, она должна быть удалена из базы, должно быть возвращено перенаправление на
     * страницу списка заявок.
     */
    @Test
    public void deleteWhenTicketExistsShouldDeleteTicketAndReturnRedirectionToIndex() throws EntityNotFoundException {
        ModelAndView deleteConfirmationPage = controller.delete(new Ticket());

        verify(repository).delete(notNull(Ticket.class));

        assertViewName(deleteConfirmationPage, "redirect:/tickets");
    }

    /**
     * Удаление несуществующей заявки.
     *
     * Если удаляемая заявка не существует, должно быть выброшено исключение.
     */
    @Test
    public void deleteWhenTicketDoesNotExistShouldThrowException() throws EntityNotFoundException {
        expectedException.expect(EntityNotFoundException.class);

        controller.delete(null);
    }
}
