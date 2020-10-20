FROM postgres:13.0-alpine

ENV POSTGRES_DB starsky

COPY ./database/init.sql /docker-entrypoint-initdb.d/