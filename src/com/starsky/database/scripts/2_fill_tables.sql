-- user role and notification type gets mapped to enum in code with hardcoded ID
-- DO NOT change the order of inserts here, unless you also change the hardcoded IDs in code

insert into public.user_role (name)
values ('manager'),
       ('employee'),
       ('admin');
insert into public.notification_type (name)
values ('email'),
       ('text-message');