# Starsky backend

Starsky backend represents the API and database portion of starsky application for employee scheduling. 
It uses Java (Spring Boot) for REST API, PostgreSQL for data storage and Nginx for reverse proxy in production. 

Front-end React application is located in a repository called [starsky-frontend](https://github.com/peroxy/starsky-frontend).

It also uses the transactional email API that is located in a repository called [starsky-mail](https://github.com/peroxy/starsky-mail).

## Requirements

- [docker](https://docs.docker.com/get-docker/)
- [docker-compose](https://docs.docker.com/compose/install/) (at least 3.8 version support)

## Development

### Running with docker

Please note that this has only been tested with docker on Ubuntu 20.04.

1. Download source files and go to `docker` directory:

```shell script
git clone https://github.com/peroxy/starsky-backend.git
cd starsky-backend/docker
```

2. Build and run the entire stack:

```shell script
docker-compose up
```

3. You will now be able to access:

- API at http://localhost:8080/ and swagger-ui at http://localhost:8080/api/swagger-ui.html
- database at http://localhost:5432/

You can access the `starsky` database by using credentials (this can be changed in the `docker-compose.override.yml` file):
- username: `starsky`
- password: `starsky`

### Debugging
You can run the API locally using your favorite Java IDE:  
1. Run the database:

```shell script
cd starsky-backend/docker
docker-compose up database
```
2. Set Spring active profiles: `dev` and `local`
3. Run and debug the application 

### OpenAPI client
You can generate an OpenAPI client by running backend API locally, then running (for example TypeScript client):

```shell
docker run --rm --network host -v "${PWD}:/local" openapitools/openapi-generator-cli generate -i http://localhost:8080/api/v3/api-docs -g typescript -o /local/out/ts
```

## Deployment

We host entire infrastructure on Azure, specifically using Azure Virtual Machine.

### Server requirements

The server (in our case Azure VM) must have these installed:

- [docker](https://docs.docker.com/get-docker/),
- [docker-compose](https://docs.docker.com/compose/install/) (at least 3.8 version support).

#### Setup on Azure Virtual Machine:
1. Create Azure Virtual Machine with Ubuntu installed and setup your public SSH key. Ubuntu 18.04 was used at the time of writing this.
2. Enable SSH (port 22) and whitelist your IP.
3. Connect to your machine:

   ```shell script
   ssh username@ipAddress
   ```

4. [Install docker](https://docs.docker.com/get-docker/):

    ```shell script
    curl -fsSL https://get.docker.com -o get-docker.sh
    sudo usermod -aG docker <your-user>
    ```
   Log out and log back in to be able to use `docker` without `sudo`.


5. [Install docker-compose](https://docs.docker.com/compose/install/):

   ```shell script
    sudo curl -L "https://github.com/docker/compose/releases/download/1.27.4/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    sudo chmod +x /usr/local/bin/docker-compose
    ```

6. Generate a SSH key:bb

    ```shell script
    ssh-keygen -t rsa -b 4096 -c "starsky_deploy"
    ```

#### SSL Setup
Currently, SSL is done manually by using ZeroSSL and copying certificates to Azure VM.

1. Generate a certificate, you will get 3 files: certificate.crt, private.key, ca_bundle.crt.
2. Copy them to Azure VM with rsync:

```shell
cd ~/certs
rsync ./* user@host:~/certs
```

3. Restart Nginx on Azure VM.

### Repository secrets

These are the required secrets that should be stored inside Github repository secrets:

- Dockerhub:
   - `DOCKERHUB_USERNAME`
   - `DOCKERHUB_TOKEN` - see [Create an access token](https://docs.docker.com/docker-hub/access-tokens/#create-an-access-token) for more information
- PostgreSQL:
   - `POSTGRES_USER`
   - `POSTGRES_PASSWORD` - don't make it too long, there were some issues with authentication with a 128 character password, even though it should be supported in theory...
- Server host (Azure VM):
   - `REMOTE_HOST` - remote host IP address / domain to SSH into
   - `REMOTE_USER` - username to SSH with
   - `SERVER_SSH_KEY` - private SSH key (OpenSSH, for example the contents of your `~/.ssh/id_rsa` key) to connect to your server
- API:
   - `STARSKY_JWT_SECRET` - needed for generating JWT tokens, use a random 32 character secret
   - `STARSKY_FRONTEND_REGISTER_URL` - needed to send invite links with proper URL

### How to deploy

Push a tag `*.*.*` (e.g. `1.0.3`) to `master` branch and it will automatically deploy everything via Github workflow.
See `.github/main.yml` workflow for more info.

In short, it does this if it gets triggered by a new tag:

- Takes source code from `master` branch and extracts the newest version from tag.
- Configures environment variables used by docker containers from Github repository's secrets.
- Builds and pushes all apps as Docker images to DockerHub.
- Copies environment variables and docker-compose files to Azure VM.
- Stops `starsky-backend` containers on Azure VM, pulls the newest images and starts the containers again.