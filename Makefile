# Shortcuts for the commands used most often. A wrapper, not build config —
# neither package references the other, and both still build standalone.
#
#   make            list every target
#   make be         run the backend on the host (fastest loop)
#   make up         whole stack in Docker

.DEFAULT_GOAL := help
.PHONY: help up up-be down clean logs logs-be be be-jar test test-one lint fe fe-build db db-shell db-reset health api verify

BACKEND  := backend
FRONTEND := frontend

## help: list targets
help:
	@grep -E '^## ' $(MAKEFILE_LIST) | sed 's/## //' | awk -F': ' '{printf "  \033[36m%-12s\033[0m %s\n", $$1, $$2}'

# --- docker stack ---------------------------------------------------------

## up: build and start everything (backend :8081, frontend :5173)
up:
	docker compose up --build

## up-be: backend + MySQL only, no frontend
up-be:
	docker compose up --build backend

## down: stop the stack, keep the database
down:
	docker compose down

## clean: stop the stack and wipe the database volume
clean:
	docker compose down -v

## logs: follow all container logs
logs:
	docker compose logs -f

## logs-be: follow backend container logs
logs-be:
	docker compose logs -f backend

# --- backend on the host --------------------------------------------------
# Needs MySQL reachable on :3306 — `make db` starts just that container.

## be: run the backend from source on :8080 (no image rebuild)
be:
	cd $(BACKEND) && ./mvnw spring-boot:run

## be-jar: package to backend/target/backend.jar
be-jar:
	cd $(BACKEND) && ./mvnw clean package

## test: backend unit tests (no database needed)
test:
	cd $(BACKEND) && ./mvnw test

## test-one: single test, e.g. make test-one T=CartServiceImplTest
test-one:
	cd $(BACKEND) && ./mvnw test -Dtest=$(T)

## verify: module boundaries — fails on a cycle or an undeclared dependency
verify:
	cd $(BACKEND) && ./mvnw test -Dtest=ModularityTest

# --- frontend -------------------------------------------------------------

## fe: Vite dev server on :5173
fe:
	cd $(FRONTEND) && npm run dev

## fe-build: type-check and production build
fe-build:
	cd $(FRONTEND) && npm run build

## lint: oxlint over the frontend
lint:
	cd $(FRONTEND) && npm run lint

# --- database -------------------------------------------------------------

## db: start MySQL alone, for a host-run backend
db:
	docker compose up -d mysql

## db-shell: mysql prompt on the dev schema
db-shell:
	docker exec -it twistedmomos-mysql mysql -uroot -proot twisted_momos_dev

## db-reset: drop the volume and re-run every migration from scratch
db-reset:
	docker compose down -v
	docker compose up -d mysql

# --- checks ---------------------------------------------------------------

## health: actuator health for both the container and a host-run backend
health:
	@printf 'container :8081  '; curl -fsS http://localhost:8081/actuator/health || echo 'down'
	@printf '\nhost      :8080  '; curl -fsS http://localhost:8080/actuator/health || echo 'down'
	@echo

## api: open the kulala request collection
api:
	$${EDITOR:-nvim} ~/api-collections/twistedmomos/twistedmomos.http
