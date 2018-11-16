
#!/usr/bin/env bash
docker-compose down
docker network rm mediator_remote
docker network rm mediator_public
docker network rm mediator_private

docker-compose ps

docker network create mediator_remote
docker network create mediator_public
docker network create mediator_private
docker-compose up -d

docker-compose ps