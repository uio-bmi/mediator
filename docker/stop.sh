#!/usr/bin/env bash
docker-compose down
docker network rm mediator_remote
docker network rm mediator_public
docker network rm mediator_private
