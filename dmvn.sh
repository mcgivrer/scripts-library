#!/bin/bash
docker run -it --rm --name my-maven-project -v "$(pwd)":/usr/src/mymaven -w /usr/src/mymaven maven:3.9.0-openjdk-19 mvn clean install
