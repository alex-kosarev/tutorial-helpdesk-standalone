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
package name.alexkosarev.tutorials.helpdesk.forms;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import name.alexkosarev.tutorials.helpdesk.entities.Ticket;
import name.alexkosarev.tutorials.helpdesk.entities.TicketComment;
import org.hibernate.validator.constraints.NotBlank;

/**
 *
 * @author Alexander Kosarev
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketCommentForm {

    @NotBlank
    private String comment;

    public TicketComment toTicketComment(Ticket ticket) {
        TicketComment ticketComment = new TicketComment();
        ticketComment.setComment(comment);
        ticketComment.setDateCreated(new Date());
        ticketComment.setTicket(ticket);

        return ticketComment;
    }
}
