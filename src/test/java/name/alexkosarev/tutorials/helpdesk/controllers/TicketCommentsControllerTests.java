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

import java.util.Date;
import name.alexkosarev.tutorials.helpdesk.entities.Ticket;
import name.alexkosarev.tutorials.helpdesk.entities.TicketComment;
import name.alexkosarev.tutorials.helpdesk.exceptions.EntityNotFoundException;
import name.alexkosarev.tutorials.helpdesk.forms.TicketCommentForm;
import name.alexkosarev.tutorials.helpdesk.repositories.TicketCommentRepository;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import static org.mockito.Matchers.notNull;
import org.mockito.Mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;
import org.springframework.http.HttpStatus;
import static org.springframework.test.web.ModelAndViewAssert.assertAndReturnModelAttributeOfType;
import static org.springframework.test.web.ModelAndViewAssert.assertViewName;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;

/**
 * Модульные тесты для тестирования {@link TicketCommentsController}
 *
 * @author Alexander Kosarev
 */
public class TicketCommentsControllerTests {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private TicketCommentRepository repository;

    private TicketCommentsController controller;

    @Before
    public void setUp() {
        initMocks(this);

        controller = new TicketCommentsController(repository);
    }

    @Test
    public void createWhenTicketExistsAndFormIsValidShouldSaveCommentAndReturnRedirection() throws EntityNotFoundException {
        BindingResult bindingResult = mock(BindingResult.class);
        doReturn(false).when(bindingResult)
                .hasErrors();

        ModelAndView create = controller.create(new TicketCommentForm(), bindingResult, new Ticket());

        verify(bindingResult).hasErrors();
        verify(repository).save(notNull(TicketComment.class));

        assertViewName(create, "redirect:/tickets/0");
    }

    @Test
    public void createWhenTicketExistsAndFormIsInvalidShouldDoNothingAndReturnModelAndViewWith400Status() throws EntityNotFoundException {
        BindingResult bindingResult = mock(BindingResult.class);
        doReturn(true).when(bindingResult)
                .hasErrors();

        ModelAndView create = controller.create(new TicketCommentForm(), bindingResult, new Ticket());

        verify(bindingResult).hasErrors();
        verify(repository, never()).save(notNull(TicketComment.class));

        assertEquals(HttpStatus.BAD_REQUEST, create.getStatus());
    }

    @Test
    public void createWhenTicketDoesNotExistsShouldThrowException() throws EntityNotFoundException {
        expectedException.expect(EntityNotFoundException.class);

        controller.create(new TicketCommentForm(), null, null);
    }

    @Test
    public void deletePageWhenTicketAndCommentExistAndRelatedShouldReturnModelAndView() throws EntityNotFoundException {
        Ticket ticket = new Ticket();
        ModelAndView deletePage = controller.deletePage(ticket, new TicketComment(0, "", new Date(), ticket));

        assertViewName(deletePage, "tickets/comments/delete");
        assertAndReturnModelAttributeOfType(deletePage, "comment", TicketComment.class);
    }

    @Test
    public void deletePageWhenTicketDoesNotExistsShouldThrowException() throws EntityNotFoundException {
        expectedException.expect(EntityNotFoundException.class);

        controller.deletePage(null, new TicketComment());
    }

    @Test
    public void deletePageWhenCommentDoesNotExistsShouldThrowException() throws EntityNotFoundException {
        expectedException.expect(EntityNotFoundException.class);

        controller.deletePage(new Ticket(), null);
    }

    @Test
    public void deletePageWhenTicketAndCommentNotRelatedShouldThrowException() throws EntityNotFoundException {
        expectedException.expect(EntityNotFoundException.class);

        controller.deletePage(new Ticket(), new TicketComment());
    }

    @Test
    public void deleteWhenTicketAndCommentExistShouldDeleteCommentAndReturnRedirection() throws EntityNotFoundException {
        Ticket ticket = new Ticket();
        ModelAndView deletePage = controller.delete(ticket, new TicketComment(0, "", new Date(), ticket));

        verify(repository).delete(notNull(TicketComment.class));

        assertViewName(deletePage, "redirect:/tickets/0");
    }

    @Test
    public void deleteWhenTicketDoesNotExistsShouldThrowException() throws EntityNotFoundException {
        expectedException.expect(EntityNotFoundException.class);

        controller.delete(null, new TicketComment());
    }

    @Test
    public void deleteWhenCommentDoesNotExistsShouldThrowException() throws EntityNotFoundException {
        expectedException.expect(EntityNotFoundException.class);

        controller.delete(new Ticket(), null);
    }

    @Test
    public void deleteWhenTicketAndCommentNotRelatedShouldThrowException() throws EntityNotFoundException {
        expectedException.expect(EntityNotFoundException.class);

        controller.delete(new Ticket(), new TicketComment());
    }
}
