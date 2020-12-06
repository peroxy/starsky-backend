CREATE TABLE public.user_role
(
    id   serial PRIMARY KEY,
    name text NOT NULL UNIQUE
);

ALTER TABLE public.user_role
    OWNER to starsky;



CREATE TABLE public.notification_type
(
    id   serial PRIMARY KEY,
    name text NOT NULL UNIQUE
);

ALTER TABLE public.notification_type
    OWNER to starsky;

CREATE TABLE public.user
(
    id                   serial PRIMARY KEY,
    name                 text      NOT NULL,
    email                text      NOT NULL,
    password             text      NOT NULL,
    job_title            text      NOT NULL,
    phone_number         text NULL,
    notification_type_id integer   NOT NULL, --user's notification preference - currently can only be notified by 1 channel
    user_role_id         integer   NOT NULL,
    parent_user_id       integer NULL,       --e.g. an employee will have manager's user ID set here
    date_created         timestamp NOT NULL,
    enabled              boolean   NOT NULL,
    CONSTRAINT fk_notification_type
        FOREIGN KEY (notification_type_id)
            REFERENCES notification_type (id),
    CONSTRAINT fk_user_role
        FOREIGN KEY (user_role_id)
            REFERENCES user_role (id)
);

ALTER TABLE public.user
    ADD CONSTRAINT fk_parent_user
        FOREIGN KEY (parent_user_id)
            REFERENCES public.user (id);

CREATE
UNIQUE INDEX user_email_enabled_constraint ON public.user (email, enabled)
    WHERE enabled;

ALTER TABLE public.user
    OWNER to starsky;

CREATE TABLE public.team
(
    id            serial PRIMARY KEY,
    name          text    NOT NULL,
    owner_user_id integer NOT NULL,
    UNIQUE (name, owner_user_id),
    CONSTRAINT fk_user
        FOREIGN KEY (owner_user_id)
            REFERENCES public.user (id)
);

ALTER TABLE public.team
    OWNER to starsky;

CREATE TABLE public.team_member
(
    id      serial PRIMARY KEY,
    team_id integer NOT NULL,
    user_id integer NOT NULL,
    UNIQUE (team_id, user_id),
    CONSTRAINT fk_user
        FOREIGN KEY (user_id)
            REFERENCES public.user (id),
    CONSTRAINT fk_team
        FOREIGN KEY (team_id)
            REFERENCES public.team (id)
);

ALTER TABLE public.team_member
    OWNER to starsky;

