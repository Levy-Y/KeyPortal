.PHONY: build native clean run test setup

# TODO: Add option to build into native image

# Builds the application into a runnable jar, but does not make a fat jar
build:
	@./mvnw package

native:
	@./mvnw clean package -Pnative

clean:
	@rm -rf ./node_modules ./target

run:
	@./mvnw quarkus:dev

test:
	@./mvnw test

# Used to setup the dev/prod environment
setup: clean native
	@sudo docker compose up -d