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
insert into ticket (issue, issue_details, date_created) values ('Ticket #1 issue', 'Ticket #1 issue description', now()), ('Ticket #2 issue', 'Ticket #2 issue description', now()), ('Ticket #3 issue', 'Ticket #3 issue description', now()), ('Ticket #4 issue', 'Ticket #4 issue description', now());

insert into ticket_comment (comment, date_created, ticket_id) values ('Ticket #1 comment #1', now(), 1), ('Ticket #2 comment #2', now(), 2), ('Ticket #1 comment #3', now(), 1);