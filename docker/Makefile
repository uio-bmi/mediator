ARGS=

.PHONY: help up ps down

help:
	@echo "Usage: make <target>\n"
	@echo "where <target> is: 'up' 'ps', 'down'\n"

up:
	@docker-compose up -d

ps:
	@docker-compose ps

down:
	@docker-compose down -v
