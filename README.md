# Starsky backend
Starsky backend represents the API and database portion of starsky application for employee scheduling.

It uses Kotlin and OpenJDK for REST API and PostgreSQL for data storage.

### Requirements 
- [docker](https://docs.docker.com/get-docker/) 
- [docker-compose](https://docs.docker.com/compose/install/) (at least 3.3 version support)

### Development
Please note that this has only been tested with docker on Ubuntu 20.04.
1. Download source files:
 
```shell script
git clone https://github.com/peroxy/starsky-backend.git
```

2. Go to root directory:
 
```shell script
cd starsky-backend
```

3. You must specify PostgreSQL password for `starsky` user and JWT secret for API authentication. 

(Optional) You can also setup `STARSKY_ENVIRONMENT` environment variable, which can be either of those:
- DEV (default value if no environment variable is found),
- STAGE,
- PROD.

Create a `.env` file and specify environment variables `POSTGRES_PASSWORD` and `STARSKY_JWT_SECRET`:
 
```shell script
echo "POSTGRES_PASSWORD=password" > .env
echo "STARSKY_JWT_SECRET=secret" >> .env

echo "STARSKY_ENVIRONMENT=DEV" >> .env  #optional
```
    
   JWT secret will be used to generate bearer tokens for clients. Easy way to generate a strong JWT secret by using OpenSSL:
   
```shell script
openssl rand --base64 64
```
 
   Environment variables specified in `.env` file will be automatically used by `docker-compose`.
4. Build and run the database and API:
 
```shell script
docker-compose up
```
   
5. You will now be able to access:
- API at http://localhost:8080/
- database at http://localhost:5432/ 

 
You can login to `starsky` database with username `starsky` and password specified in `.env` file.
Database will be filled up with mock data from `mockData.sql` file. All users' passwords are set to `password`.
