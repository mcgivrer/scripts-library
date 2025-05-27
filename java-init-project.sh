#!/bin/bash
#---- Display help message ---------------------------------------
export PROJECT_NAME=MyNewJavaProject
export APP_VERSION=1.0.0
export APP_NAME=${PROJECT_NAME[@]^}
export APP_EXE=${PROJECT_NAME}
# Retrieve git author name if exists at global level
export AUTHOR_NAME=$(git config --global user.name)
export AUTHOR_EMAIL=$(git config --global user.email)
#package name
export PACKAGE_NAME="my.${PROJECT_NAME,,}.app"
export JDK_VERSION=20.0.2-zulu
export JAVA_VERSION=20
export JUNIT_VERSION=1.10.1
export CHECKSTYLE_VERSION=10.12.3

export APP_PACKAGE_NAME=${PACKAGE_NAME}

# root path for application class & build templates
export GIST_ROOT=https://gist.githubusercontent.com/mcgivrer/c4a65916809f07915c5b7cfcb93422ed/raw/73c8d5d0873523292734c3ef5743ebffe12987ef
# https://gist.githubusercontent.com/mcgivrer/c4a65916809f07915c5b7cfcb93422ed/raw/73c8d5d0873523292734c3ef5743ebffe12987ef/template_batch_app_class.java

if [ "$#" -eq 0 ]; then
    echo -e " 
Command line: $0
---
Usage:

Create a java project into a git-flow repository following the maven file structure
(but without maven command file)

  $0 \\
    -m [project_name] \\
    -a [author_name] \\
    -e [author_email] \\
    -v [project_version] \\
    -j [java_version] \\
    -p [package_name] \\
    -k [JDK flavor version]

where:

  - m) [project_name] name of the project directory to be generated, will be used capitalized as application name and main class (default is $(APP_NAME)),
  - v) [project_version] the version for the project to be initialized (default is $(APP_VERSION)),
  - p) [package_name] the root java package name for your main application class (default is $(PACKAGE_NAME)),
  - j) [java_version] the targeted compatible java version to be use as default one for the project (default is $(JAVA_VERSION)).
  - a) [author_name] name of the author (used as git commiter),
  - e) [author_email] email of the author (used as git commiter),
  - k) [JDK flavor version] you may define the sdkman jdk flavor and version (default is $(JDK_VERSION)).
---
Examples:

1. Create a named project

  $0 -m MyProject

  This will create a project named "MyProject".

2. Create a project with a specific package:

  $0 -m MyProject -p com.my.package

  This will create a project "MyProject" with root package as com.my.package.app and the main class MyProject.

3. Create a project with a minimum JDK version

  $0 -m MyProject -j 21

  This will create a JDK with a compatibility jdk version set to 21

4. Defining the specific JDK flavor (for sdkman usage)

  $0 -m MyProject -j 21 -k 21.fx-zulu

  This will require the Zulu JDK version 21 with JavaFX.
---
"
else

    #---- set variables for all files ---------------------------------------

    while getopts ":m:a:e:v:j:p:k:" opt; do
        case $opt in
        m)
           export PROJECT_NAME="$OPTARG"
           export  APP_EXE=${PROJECT_NAME}
           export  APP_NAME=${PROJECT_NAME}
           export  APP_NAME=(${APP_NAME[@]^})
            ;;
        a)
           export AUTHOR="$OPTARG"
           export  AUTHOR_NAME=${AUTHOR}
            ;;
        e)
           export EMAIL="$OPTARG"
           export  AUTHOR_EMAIL=${EMAIL}
            ;;
        v)
           export VERSION="$OPTARG"
           export  APP_VERSION=${VERSION}
            ;;
        j)
           export JVERSION="$OPTARG"
           export  JAVA_VERSION=${JVERSION}
            ;;
        p)
           export PACKAGE_NAME="$OPTARG"
           export  APP_PACKAGE_NAME=${PACKAGE_NAME}
            ;;

        k)
           export  JDK_VERSION="$OPTARG"
            ;;

        \?)
            echo "Invalid option -$OPTARG" >&2
            exit 1
            ;;
        esac
        case $OPTARG in
        -*)
            echo "$opt need valid argument"
            exit 1
            ;;
        esac
    done
    # set parameters
    export LIBS=./lib

    #---- prepare project file structure ---------------------------------------
    echo "|"
    echo "|_ Create project directory structure"
    mkdir $PROJECT_NAME
    cd $PROJECT_NAME
    mkdir -p ./src/{main,test}/{java,resources}
    mkdir -p $LIBS/test
    mkdir -p ./scripts/

    echo "|"
    echo "|_ Create Readme"
    #---- start of README.md file ---------------------------------------
    cat >README.md <<EOL
# README
   
## Context

This is the readme file for $APP_NAME ($APP_VERSION)

## Build
To build the project, just execute the following command line :

\`\`\`bash
$> build.sh a
\`\`\`
## Run

To execute the build project, just run it with :

\`\`\`bash
$> build.sh r
\`\`\`

or you can execute the command line :

\`\`\`bash
$> java -jar target/$APP_NAME-$APP_VERSION.jar
\`\`\`

Enjoy !
tree
$AUTHOR_NAME.

EOL
    #---- start sdkman file ---------------------------------------

    echo "|"
    echo "|_ Create sdkman env file"
    cat >.sdkmanrc <<EOL
# Project $APP_NAME by $AUTHOR_NAME<$AUTHOR_EMAIL>
java=${JDK_VERSION}

EOL
    #---- end of README.md file ---------------------------------------

    echo "|"
    echo "|_ Create build file and required dependencies"
    mkdir ./lib/{dep,test,tools}

    #---- start of build.sh file ---------------------------------------
    ## download build scripts
    curl -s https://gist.githubusercontent.com/mcgivrer/3fe8a25a2815bca3a1a7f333f6944665/raw/dbba2a94e1595a236bdd6d76533cbb2cc8e4d61a/build.properties -o ./build.properties
    curl -s https://gist.githubusercontent.com/mcgivrer/3fe8a25a2815bca3a1a7f333f6944665/raw/dbba2a94e1595a236bdd6d76533cbb2cc8e4d61a/build.readme.md -o ./build.readme.md
    curl -s https://gist.githubusercontent.com/mcgivrer/3fe8a25a2815bca3a1a7f333f6944665/raw/dbba2a94e1595a236bdd6d76533cbb2cc8e4d61a/build.sh -o ./build.sh
    curl -s https://gist.githubusercontent.com/mcgivrer/3fe8a25a2815bca3a1a7f333f6944665/raw/dbba2a94e1595a236bdd6d76533cbb2cc8e4d61a/lib_stub.sh -o ./lib/stub.sh
    mkdir ./temp
    curl -s $GIST_ROOT/template_batch_app_class.java -o ./temp/template_batch_app_class.java
    curl -s $GIST_ROOT/template_build.properties -o ./temp/template_build.properties
    ## download jar dependencies
    curl https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/${JUNIT_VERSION}/junit-platform-console-standalone-${JUNIT_VERSION}.jar -o ./lib/test/junit-platform-console-standalone-${JUNIT_VERSION}.jar
    curl -s https://github.com/checkstyle/checkstyle/releases/download/checkstyle-${CHECKSTYLE_VERSION}/checkstyle-${CHECKSTYLE_VERSION}-all.jar -o ./lib/tools/checkstyle-${CHECKSTYLE_VERSION}-all.jar

    # create Build.properties file
    envsubst <./temp/template_build.properties >build.properties

    chmod +x ./build.sh
    ln -s build.sh build
    #---- end of build.sh file ---------------------------------------

    #---- sart of .gitignore file ---------------------------------------
    echo "|"
    echo "|_ Create .gitignore file"
    cat >.gitignore <<EOL
/.settings
/.vscode
/.idea
*.iml
.classpath
.project
/target
**/*.class
EOL
    #---- end of .gitignore file ---------------------------------------
    echo "|"
    echo "|_ Create app ${APP_NAME} main class in package ${APP_PACKAGE_NAME//.//}"
    mkdir -p src/main/java/${APP_PACKAGE_NAME//.//}

    #---- start of main java class file ---------------------------------------
    envsubst <./temp/template_batch_app_class.java >src/main/java/${APP_PACKAGE_NAME//.//}/${APP_NAME}App.java
    #---- end of main java class file ---------------------------------------
    echo "|"
    echo "|_ Project ${APP_NAME} created."

    mkdir -p src/main/resources

    #---- start of config.properties file ---------------------------------------
    cat >src/main/resources/config.properties <<EOL
app.exit=true
EOL
    #---- end of config.properties file ---------------------------------------
    mkdir -p src/main/resources/i18n

    #---- start of default i18n/messages.properties file ---------------------------------------
    cat >src/main/resources/i18n/messages.properties <<EOL
app.name=${APP_NAME}
app.version=${APP_VERSION}
EOL
    #---- end of default i18n/messages.properties file ---------------------------------------

    #---- create git repository ---------------------------------------
    echo "|"
    echo "|_ Initialize git repository and commit files with ${AUTHOR_NAME}<${AUTHOR_EMAIL}>"
    rm -rf ./temp
    git init
    git config --local user.name "${AUTHOR_NAME}"
    git config --local user.email ${AUTHOR_EMAIL}
    git add .
    git commit -m "Create project ${APP_NAME}"
    git flow init -d --feature feature/ --bugfix bugfix/ --release release/ --hotfix hotfix/ --support support/ -t ''
    echo "|_ git & git flow project initialized"
    echo "|"
    echo "|_ Project ${APP_NAME} ${APP_VERSION} on Java ${JDK_VERSION} is now ready to use !"
    echo "Thanks for using the '${0}' script to create you Java project."
    echo "---"
fi
