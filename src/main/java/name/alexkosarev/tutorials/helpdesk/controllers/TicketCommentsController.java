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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import name.alexkosarev.tutorials.helpdesk.entities.Ticket;
import name.alexkosarev.tutorials.helpdesk.entities.TicketComment;
import name.alexkosarev.tutorials.helpdesk.exceptions.EntityNotFoundException;
import name.alexkosarev.tutorials.helpdesk.forms.TicketCommentForm;
import name.alexkosarev.tutorials.helpdesk.repositories.TicketCommentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * Контроллер для управления списком комментариев заявки.
 *
 * @author Alexander Kosarev
 */
@Controller
@RequestMapping("tickets/{ticket:\\d+}/comments")
@AllArgsConstructor
public class TicketCommentsController {

    private final TicketCommentRepository repository;

    /**
     * Создание комментария.
     *
     * @param form данные, отправленные пользователем
     * @param bindingResult результат валидации
     * @param ticket заявка, к которой создаётся комментарий
     * @return перенаправление на страницу заявки, в случае успеха, либо модель и шаблон заявки с ошибками валидации
     * @throws EntityNotFoundException если заявка не найдена
     */
    @PostMapping
    public ModelAndView create(@Valid TicketCommentForm form, BindingResult bindingResult, @PathVariable Ticket ticket) throws EntityNotFoundException {
        if (ticket == null) {
            throw new EntityNotFoundException("error.ticket.notFound");
        }

        if (bindingResult.hasErrors()) {
            Map<String, Object> model = new HashMap<>();
            model.put("ticket", ticket);
            model.put("ticketComment", form.toTicketComment(ticket));
            model.put("errors", bindingResult);

            return new ModelAndView("tickets/viewOne", model, HttpStatus.BAD_REQUEST);
        }

        repository.save(form.toTicketComment(ticket));

        return new ModelAndView("redirect:/tickets/" + ticket.getId());
    }

    /**
     * Страница удаления комментария.
     *
     * @param ticket заявка, у которой удаляется комментарий
     * @param comment комментарий для удаления
     * @return модель и шаблон с комментарием
     * @throws EntityNotFoundException если комментарий или заявка не найдены
     */
    @GetMapping("{comment:\\d+}/delete")
    public ModelAndView deletePage(@PathVariable Ticket ticket, @PathVariable TicketComment comment) throws EntityNotFoundException {
        if (comment == null) {
            throw new EntityNotFoundException("error.ticketComment.notFound");
        }
        if (ticket == null || !Objects.equals(comment.getTicket(), ticket)) {
            throw new EntityNotFoundException("error.ticket.notFound");
        }

        return new ModelAndView("tickets/comments/delete", Collections.singletonMap("comment", comment));
    }

    /**
     * Удаление комментария.
     *
     * @param ticket заявка, у которой удаляется комментарий
     * @param comment комментарий для удаления
     * @return перенаправление на страницу заявки
     * @throws EntityNotFoundException если комментарий или заявка не найдены
     */
    @PostMapping("{comment:\\d+}/delete")
    public ModelAndView delete(@PathVariable Ticket ticket, @PathVariable TicketComment comment) throws EntityNotFoundException {
        if (comment == null) {
            throw new EntityNotFoundException("error.ticketComment.notFound");
        }
        if (ticket == null || !Objects.equals(comment.getTicket(), ticket)) {
            throw new EntityNotFoundException("error.ticket.notFound");
        }

        repository.delete(comment);

        return new ModelAndView("redirect:/tickets/" + comment.getTicket().getId());
    }
}
