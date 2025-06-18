#!/bin/bash
# usage:  script.sh <project_name>
mkdir -p $1/{src/{{main,test}/{java,resources},docs},libs}
echo "#README" >$1/README.md
echo ".target/" >$1/.gitignore
echo "java=24-zulu">.sdkmanrc
# sources
mkdir -p $1/src/main/resources/i18n
curl -sL https://gist.githubusercontent.com/mcgivrer/01728ce9e04d389c6a08a065c10cb1b5/raw/7f15731d4b942a87444c65d78913d1d755b5be8d/App.java >$1/src/main/java/App.java
curl -sL https://gist.githubusercontent.com/mcgivrer/01728ce9e04d389c6a08a065c10cb1b5/raw/7f15731d4b942a87444c65d78913d1d755b5be8d/config.properties >$1/src/main/resources/config.properties
curl -sL https://gist.githubusercontent.com/mcgivrer/01728ce9e04d389c6a08a065c10cb1b5/raw/7f15731d4b942a87444c65d78913d1d755b5be8d/messages.properties >$1/src/main/resources/i18n/messages.properties
# dependencies
curl -sL https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/1.12.2/junit-platform-console-standalone-1.12.2.jar >$1/libs/junit-platform-console-standalone-1.12.2.jar
# build
curl -sL https://gist.githubusercontent.com/mcgivrer/b95bf476b8494c180c0a386a5ae11264/raw/0618c582240ec4972fad844c884d28df086ab674/build >$1/build
# git repo
git init -b main --quiet $1/
cd $1
git add .
git commit -m "Create Project $1"
