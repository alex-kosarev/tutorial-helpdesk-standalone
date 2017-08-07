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
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import name.alexkosarev.tutorials.helpdesk.entities.Ticket;
import org.hibernate.validator.constraints.NotBlank;

/**
 * Форма заявки.
 *
 * @author Alexander Kosarev
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketForm {

    @Size(max = 255, message = "ticketForm.issue.errors.tooLong")
    @NotBlank(message = "ticketForm.issue.errors.blank")
    private String issue;

    private String issueDetails;

    public Ticket toTicket() {
        return toTicket(new Ticket(0, issue, issueDetails, new Date()));
    }

    public Ticket toTicket(Ticket ticket) {
        ticket.setIssue(issue);
        ticket.setIssueDetails(issueDetails);

        return ticket;
    }
}
