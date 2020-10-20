CREATE TABLE public.user
(
    id serial NOT NULL,
    name text NOT NULL,
    email text NOT NULL,
    password text NOT NULL,
    date_created timestamp NOT NULL
);

ALTER TABLE public.user
    OWNER to starsky;