# Starsky backend
Starsky backend represents the API and database portion of starsky application for employee scheduling. 
Front-end React application is located in a different repository called 
[starsky-frontend](https://github.com/peroxy/starsky-frontend). 

It uses Kotlin and OpenJDK for REST API and PostgreSQL for data storage.

## Requirements

#### Development 
- [docker](https://docs.docker.com/get-docker/) 
- [docker-compose](https://docs.docker.com/compose/install/) (at least 3.3 version support)

#### Deployment
- [heroku-cli](https://devcenter.heroku.com/articles/heroku-cli) (optional but nice)
- PostgreSQL client to execute a SQL script, e.g. [pgAdmin](https://www.pgadmin.org/)


## Local Development
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

    - (Optional) Set `STARSKY_ENVIRONMENT` environment variable, which can be either of those:
        - DEV (default value if no environment variable is found),
        - PROD.
    - (Optional) Set `STARSKY_FRONTEND_HOST` environment variable with front-end hostname for CORS access, e.g. `domain.com`.
    You can set this variable if you want to override default DEV front-end React application hosted at `localhost:3000`.
     This front-end origin will receive `AccessControlAllowOrigin` header.

Create an `.env` file and specify environment variables `POSTGRES_PASSWORD` and `STARSKY_JWT_SECRET`:
 
```shell script
echo "POSTGRES_PASSWORD=password" > .env
echo "STARSKY_JWT_SECRET=secret" >> .env

echo "STARSKY_ENVIRONMENT=DEV" >> .env  # optional
echo "STARSKY_FRONTEND_HOST=localhost:3000" >> .env  # optional
```
    
   JWT secret will be used to generate bearer tokens for clients. An easy way to generate a strong JWT secret is by using OpenSSL:
   
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
Database will be filled up with mock data from `mockData.sql` file. All mock users' passwords are set to `password`.


## Deployment
### Heroku
Starsky Backend can be deployed to Heroku's free dyno plan. 
Please check out Heroku's pricing [here](https://www.heroku.com/pricing) 
to be aware of limitations and features offered by the free plan.

#### Creating and configuring your app

**Note**: `heroku-cli` is not required, you can also use Heroku's website.

1. Login and create a Heroku app:
 
```shell script
heroku login
heroku create starsky-backend
```

2. Attach Heroku Postgres addon:
 
```shell script
heroku addons:attach heroku-postgresql
```

3. Configuring database: 
- Open the `starsky-backend` postgresql addon page .

```shell script
heroku addons:open heroku-postgresql -a starsky-backend
```

- Go to _Settings_ tab and click on _Database Credentials_ --> _View Credentials_.
- Copy your database's credentials and host:port info.
- Open your favorite PostgreSQL client and connect to your database.
- Open `init.sql` file located in `\starsky-backend\src\com\starsky\database\scripts`.
- Execute `init.sql` file.

4. Add Heroku config vars (see Local Development in README for details about environment variables):

```shell script
heroku config:set GRADLE_TASK="shadowJar" -a starsky-backend
heroku config:set STARSKY_ENVIRONMENT="PROD" -a starsky-backend
heroku config:set STARSKY_JWT_SECRET="JWT secret" -a starsky-backend
heroku config:set STARSKY_FRONTEND_HOST="domain.com" -a starsky-backend
```

5. Go to Heroku's website and login, open your app and connect this Github repo to your app.
 You can now manually deploy the app to Heroku. **Success!**
 
 #### Automatic deployment
 
 This GitHub repository uses Heroku's automatic deployment functionality; everything that gets pushed to `master` branch will be automatically deployed to production.
 
 You cannot push to `master` branch directly; you can only create pull requests that have to be manually approved. 
 
 