# Makefile
.DEFAULT_GOAL := build-run

run-dist: #  Запуск исполняемого файла
	./build/install/app/bin/app 'src/test/resources/file1.json' 'src/test/resources/file2.json'

setup:
	./gradlew wrapper --gradle-version 8.5

clean:
	./gradlew clean

build:
	./gradlew clean build

install:
	./gradlew clean install

run:
	./gradlew run

bootRunDev:
	./gradlew bootRun --args='--spring.profiles.active=development'

test:
	./gradlew test

report:
	./gradlew jacocoTestReport

lint:
	./gradlew checkstyleMain checkstyleTest

check-deps:
	./gradlew dependencyUpdates -Drevision=release


build-run: build run

.PHONY: build
