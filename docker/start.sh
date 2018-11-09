#!/usr/bin/env bash
docker network create mediator_remote
docker network create mediator_public
docker network create mediator_private
docker-compose up -d