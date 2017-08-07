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
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import name.alexkosarev.tutorials.helpdesk.entities.Ticket;
import name.alexkosarev.tutorials.helpdesk.exceptions.EntityNotFoundException;
import name.alexkosarev.tutorials.helpdesk.forms.TicketForm;
import name.alexkosarev.tutorials.helpdesk.repositories.TicketCommentRepository;
import name.alexkosarev.tutorials.helpdesk.repositories.TicketRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * Контроллер для управления списком заявок.
 *
 * @author Alexander Kosarev
 */
@Controller
@RequestMapping("tickets")
@AllArgsConstructor
public class TicketsController {

    private final TicketRepository repository;

    private final TicketCommentRepository ticketCommentRepository;

    /**
     * Отображение списка заявок.
     *
     * @param page информация о пейджинации и сортировке
     * @return модель и шаблон со списком заявок
     */
    @GetMapping
    public ModelAndView index(Pageable page) {
        ModelAndView modelAndView = new ModelAndView("tickets/index");
        modelAndView.addObject("page", repository.findAll(page));

        return modelAndView;
    }

    /**
     * Просмотр одной заявки.
     *
     * @param ticket проблема
     * @param pageable параметры пейджинации и сортировки
     * @return модель и шаблон с проблемой
     * @throws EntityNotFoundException если проблема не найдена
     */
    @GetMapping("{ticket:\\d+}")
    public ModelAndView viewOne(@PathVariable Ticket ticket, Pageable pageable) throws EntityNotFoundException {
        if (ticket == null) {
            throw new EntityNotFoundException("error.ticket.notFound");
        }

        ModelAndView modelAndView = new ModelAndView("tickets/viewOne");
        modelAndView.addObject("ticket", ticket);
        modelAndView.addObject("comments", ticketCommentRepository.findByTicket(ticket, pageable));

        return modelAndView;
    }

    /**
     * Страница создания заявки.
     *
     * @return модель и шаблон создания заявки
     */
    @GetMapping("create")
    public ModelAndView createPage() {
        return new ModelAndView("tickets/create");
    }

    /**
     * Сохранение новой заявки.
     *
     * @param form данные, отправленные пользователем
     * @param bindingResult результат валидации
     * @return перенаправление на страницу просмотра заявки в случае успешной валидации или модель и шаблон формы
     * создания заявки в случае ошибок валидации
     */
    @PostMapping("create")
    public ModelAndView create(@Valid TicketForm form, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, Object> model = new HashMap<>();
            model.put("ticket", form.toTicket());
            model.put("errors", bindingResult);
            return new ModelAndView("tickets/create", model, HttpStatus.BAD_REQUEST);
        }

        return new ModelAndView("redirect:/tickets/" + repository.save(form.toTicket()).getId());
    }

    /**
     * Страница редактирования заявки.
     *
     * @param ticket заявка
     * @return модель и шаблон с заявкой
     * @throws EntityNotFoundException если заявка не найдена
     */
    @GetMapping("{ticket:\\d+}/edit")
    public ModelAndView editPage(@PathVariable Ticket ticket) throws EntityNotFoundException {
        if (ticket == null) {
            throw new EntityNotFoundException("error.ticket.notFound");
        }

        return new ModelAndView("tickets/edit", Collections.singletonMap("ticket", ticket));
    }

    /**
     * Сохранение изменений в заявке.
     *
     * @param form данные, отправленные пользователем
     * @param bindingResult результат валидации
     * @param ticket зазявка, к которой применяются изменения
     * @return перенаправление на страницу просмотра заявки в случае успешной валидации или модель и шаблон формы
     * редактирования заявки в случае ошибок валидации
     * @throws EntityNotFoundException если заявка не найдена
     */
    @PostMapping("{ticket:\\d+}/edit")
    public ModelAndView edit(@Valid TicketForm form, BindingResult bindingResult, @PathVariable Ticket ticket) throws EntityNotFoundException {
        if (ticket == null) {
            throw new EntityNotFoundException("error.ticket.notFound");
        }

        if (bindingResult.hasErrors()) {
            Map<String, Object> model = new HashMap<>();
            model.put("ticket", form.toTicket(ticket));
            model.put("errors", bindingResult);
            return new ModelAndView("tickets/edit", model, HttpStatus.BAD_REQUEST);
        }

        return new ModelAndView("redirect:/tickets/" + repository.save(form.toTicket(ticket)).getId());
    }

    /**
     * Страница подтверждения удаления заявки.
     *
     * @param ticket заявка
     * @return модель и шаблон с заявкой
     * @throws EntityNotFoundException если заявка не найдена
     */
    @GetMapping("{ticket:\\d+}/delete")
    public ModelAndView deletePage(@PathVariable Ticket ticket) throws EntityNotFoundException {
        if (ticket == null) {
            throw new EntityNotFoundException("error.ticket.notFound");
        }

        return new ModelAndView("tickets/delete", Collections.singletonMap("ticket", ticket));
    }

    /**
     * Удаление заявка.
     *
     * @param ticket заявки
     * @return перенаправление на список заявок
     * @throws EntityNotFoundException если заявка не найдена
     */
    @PostMapping("{ticket:\\d+}/delete")
    public ModelAndView delete(@PathVariable Ticket ticket) throws EntityNotFoundException {
        if (ticket == null) {
            throw new EntityNotFoundException("error.ticket.notFound");
        }

        repository.delete(ticket);

        return new ModelAndView("redirect:/tickets");
    }
}
