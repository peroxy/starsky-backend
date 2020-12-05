insert into public.user (name, email, password, job_title, phone_number, notification_type_id, user_role_id,
                         parent_user_id,
                         date_created, enabled)
values ('Janez Novak', 'manager@manager.net', '$2a$10$JVzlURf4Act0rCVPL1NB3urt9lf6V9FT5BTsZvwL/3NXI0UW7J.D2',
        'Hospital Manager',
        '+386 31 123 456', 1, 1, null, now(), true);

insert into public.user (name, email, password, job_title, phone_number, notification_type_id, user_role_id,
                         parent_user_id,
                         date_created, enabled)
values ('Patrik Ravnik', 'admin@admin.net', '$2a$10$JVzlURf4Act0rCVPL1NB3urt9lf6V9FT5BTsZvwL/3NXI0UW7J.D2',
        'IT Administrator',
        '+386 31 122 436', 1, 3, null, now(), true);

insert into public.user (name, email, password, job_title, phone_number, notification_type_id, user_role_id,
                         parent_user_id,
                         date_created, enabled)
values ('Marija Jenko', 'employee@employee.net', '$2a$10$JVzlURf4Act0rCVPL1NB3urt9lf6V9FT5BTsZvwL/3NXI0UW7J.D2',
        'Nurse',
        '+386 31 121 436', 1, 2, 1, now(), true);

insert into public.user (name, email, password, job_title, phone_number, notification_type_id, user_role_id,
                         parent_user_id,
                         date_created, enabled)
values ('Janko Kokodajs', 'employee2@employee.net', '$2a$10$JVzlURf4Act0rCVPL1NB3urt9lf6V9FT5BTsZvwL/3NXI0UW7J.D2',
        'Senior Nurse',
        '+386 31 121 456', 2, 2, 1, now(), true);

insert into team (name, owner_user_id)
values ('Ljubljana Hospital', 1);
insert into team (name, owner_user_id)
values ('Kranj Hospital', 1);

insert into public.team_member (team_id, user_id)
values (1, 3);
insert into public.team_member (team_id, user_id)
values (1, 4);
insert into public.team_member (team_id, user_id)
values (2, 3);
insert into public.team_member (team_id, user_id)
values (2, 4);