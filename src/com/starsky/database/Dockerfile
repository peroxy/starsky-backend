FROM postgres:13.0-alpine

ENV POSTGRES_DB starsky

COPY src/com/starsky/database/init.sql /docker-entrypoint-initdb.d/