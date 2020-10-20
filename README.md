# Starsky backend
Starsky backend represents the API and database portion of starsky application for employee scheduling.

### Requirements 
- [docker](https://docs.docker.com/get-docker/) 
- [docker-compose](https://docs.docker.com/compose/install/) (at least 3.3 version support)

### Running
Please note that this has only been tested on Ubuntu 20.04.
1. Download source files:
 
    ```ssh
    git clone https://github.com/peroxy/starsky-backend.git
    ```
2. Go to `src` directory:
 
    ```ssh
    cd starsky-backend/src
    ```
3. You must specify PostgreSQL password for `starsky` user. Create a file called `.env`
 and specify environment variable `POSTGRES_PASSWORD`:
 
    ```ssh
    echo "POSTGRES_PASSWORD=password" > .env
    ```
4. Build and run the database and API:
 
    ```ssh
    docker-compose up
    ```

<!-- TODO finish readme
You are now running the API at http://localhost:61234/3fs/api/ and database at http://localhost:5432. 
If everything went well you should be able to login to your database `manage-db` with user `postgres`. The API should be returning 
empty dataset by running:
```ssh
curl -l http://localhost:61234/3fs/api/group
```

To review all REST operations you can view the OpenAPI documentation by running:
```ssh
swagger serve ~/dev/3fsTask/swagger/swagger.yaml
``` 

### Development
You can generate go-swagger server from your `swagger.yaml` file by running:  
```ssh
swagger generate server -f ~/dev/3fsTask/swagger/swagger.yaml
```
--!>


