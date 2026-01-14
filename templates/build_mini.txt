#!/bin/bash
# mini build script v1.2 (c) 2025 Frederic Delorme
#
# Dependencies : git, Java JDK
#
# Please adapt the `PROJECT_NAME`, `PROJECT_VERSION` and `MAIN_CLASS` variables to fit your own project.
# The generated JARs will be named as target/build/[PROJECT_NAME]-[MAIN_CLASS]-[PROJECT_VERSION].jar
#
# NOTE: `MAIN_CLASS` is a list of space-separated classes that a specific JAR will be generated for each.
#
# To create your own application
# ==============================
# you can :
# - Add external JAR dependencies (mostly in ./libs) by using the JARS variable.
# e.g.: JARS="./libs/flexmark-all-0.64.8-lib.jar ./libs/org.eclipse.jgit-7.2.0.202503040940-r.jar"
# - Set some Java compilation options into COMPILATION_OPTS
# - Define the Java targeted version SOURCE_VERSION variable
# - Set default source encoding into SOURCE_ENCODING (default is UTF-8)
#
# To build your project
# =====================
# <> Example 1:
#     build
# will automatically execute by default the following steps
# - (c) clean,
# - (b) build,
# - (m) create a manifest,
# - (j) create a jar to `/target/build`,
#
# <> Example 2:
#     build c b m j t d s
# Will execute the required steps
# - (c) clean,
# - (b) build,
# - (m) create a manifest ,
# - (j) create a jar to `/target/build`,
# - (t) execute unit tests  from `src/test`,
# - (d) generate javadoc to `/target`,
# - (s) generate sources jar  to `/target`.
#
# <> Example 3:
#     build r
# will execute this single step
# - (r) run the created JAR in `/target/build/[app_name]-[main_class]-[app_version].jar`.
#

PROJECT_NAME=${APP_NAME:myapp}
PROJECT_VERSION=${APP_VERSION:0.0.1}
MAIN_CLASS=${APP_MAIN_CLASS:tutorials.core.App}
AUTHOR_NAME=${GIT_AUTHOR_NAME:"Frederic Delorme<frederic.delorme@gmail.com>"}
VENDOR_NAME=${GIT_VENDOR_NAME:SnapGames}

# List of paths to jar dependencies (space separated).
# e.g.: JARS="libs/jinput-2.0.8.jar libs/my-mandatory-lib-0.0.1.jar"
JARS=
# Compilation options
COMPILATION_OPTS=
RUNTIME_OPTS=
# align the JDK version in the below SOURCE_VERSION variable
SOURCE_VERSION=24
# default source encoding
SOURCE_ENCODING=UTF-8
# Javadoc: define the root package
JAVADOC_GROUPS="-group \"Core\" tutorials.core -group \"demo\" tutorials.demo"
#
#--------------------- DO NOT CHANGE THE FOLLOWING LINES -----------------------
#
GIT_COMMIT_ID=$(git rev-parse HEAD)
JAVA_BUILD=$(java --version | head -1 | cut -f2 -d' ')
# declare general script variables
SRC=./src
LIBS=./libs
TARGET=./target
BUILD=${TARGET}/build
CLASSES=${TARGET}/classes
RESOURCES=${SRC}/main/resources

if [[ "$OSTYPE" == "linux"* ]]; then
  FS=":"
else
  FS=";"
fi
# define colors
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m'

function generateJavadoc() {
  JAVADOC_GROUPS="$PROJECT_NAME"
  JAR_JAVADOC_NAME=$PROJECT_NAME-$PROJECT_VERSION-javadoc.jar
  echo -e "|_ ${BLUE}4. Generate Javadoc ${NC}..."
  echo "> from : $SRC"
  echo "> to   : $TARGET/javadoc"
  # prepare $TARGET
  mkdir -p $TARGET/javadoc
  # Compile class files
  rm -Rf $TARGET/javadoc/*
  # uncomment to generate a overview file from project README.md
  mkdir -p $SRC/main/javadoc
  java -jar ./$LIBS/tools/markdown2html-0.3.1.jar <README.md >$SRC/main/javadoc/overview.html
  javadoc -author -use -version \
    -doctitle "$PROGRAM_NAME" \
    -d $TARGET/javadoc \
    -sourcepath "${SRC}/main/java${FS}${SRC}/main/javadoc" \
    -subpackages "${JAVADOC_SUBPACKAGES}" \
    -cp ".;$JARS" \
    -overview $SRC/main/javadoc/overview.html \
    $JAVADOC_GROUPS
  cd $TARGET/javadoc
  jar cvf ../$JAR_JAVADOC_NAME *
  cd ../../
  echo -e "   |_ ${GREEN}done$NC"
  echo "- build javadoc $JAR_JAVADOC_NAME" >>$TARGET/build.log
}
#
function generateSourceJar() {
  echo -e "|_ ${BLUE}5. Generate JAR sources $TARGET/${PROJECT_NAME}-sources-${PROJECT_VERSION}.jar${NC}..."
  echo "> from : $SRC"
  echo "> to   : $TARGET/"
  jar cvf ${TARGET}/${PROJECT_NAME}-${PROJECT_VERSION}-sources.jar -C $SRC .
  echo -e "   |_ ${GREEN}done$NC"
  echo "- create JAR sources ${PROJECT_NAME}-${PROJECT_VERSION}-sources.jar" >>$TARGET/build.log
}
#
function executeTests() {
  TEST_CLASSES=$TARGET/test-classes
  TEST_RESOURCES=$SRC/test/resources
  LIB_TEST=$LIBS/test/junit-platform-console-standalone-1.12.2.jar
  echo -e "|_ ${BLUE}6. Execute tests${NC}..."
  echo "> from : $SRC/test"
  echo "> to   : $TARGET/test-classes"
  mkdir -p $TARGET/test-classes
  echo "copy test resources"
  #cp -r ./$RESOURCES/* $TEST_CLASSES
  cp -r ./$TEST_RESOURCES/* $TEST_CLASSES
  echo "compile test classes"
  #list test sources
  find $SRC/main -name '*.java' >$TARGET/sources.lst
  find $SRC/test -name '*.java' >$TARGET/test-sources.lst
  javac -source $SOURCE_VERSION -encoding $SOURCE_ENCODING $COMPILATION_OPTS -cp ".${FS}$LIB_TEST${FS}${EXTERNAL_JARS}" -d $TEST_CLASSES @$TARGET/sources.lst @$TARGET/test-sources.lst
  echo "execute tests through JUnit"
  java $JAR_OPTS -jar $LIB_TEST --cp "${JARS}${FS}${CLASSES}${FS}${TEST_CLASSES}${FS}." --scan-class-path
  echo -e "   |_ ${GREEN}done$NC"
  echo "- execute tests through JUnit $SRC/test." >>target/build.log
  ## TODO Integrate Cucumber tests execution
  ## e.g. 'java -cp "path/to/cucumber-core.jar:path/to/cucumber-java.jar:path/to/cucumber-junit.jar:path/to/other/dependencies/*:path/to/your/classes" cucumber.api.cli.Main --glue com.your.step.definitions path/to/your/features'
}
echo "build project ' ${PROJECT_NAME}' version ${PROJECT_VERSION}..."
# Check if no arguments were passed
if [ $# -eq 0 ]; then
    echo "No options provided. Defaulting to 'c b m j'."
    args=("c" "b" "m" "j")
else
    args=("$@")
fi

for arg in "${args[@]}"
do
	echo "execute command $arg:";
  case $arg in
    b | build)
      echo "build ..."
      echo "sources files:"
      find src/main/java src/main/resources -type f -name *.java
      echo ---
      javac -d target/classes -cp ${JARS// //;}:src/main/java:src/main/resources $(find src/main/java src/main/resources -type f -name *.java)
      cp -vr src/main/resources/* target/classes/
      ;;
    c | clear)
      echo "clean previous build..."
      rm -vrf target/
      mkdir -vp target/{build,classes}
      echo "done."
      echo ---
      ;;
    d | doc)
      generateJavadoc
      ;;
    j | jar)
    echo "build jar..."
      mkdir -p target/build/libs
      cp -vr ./libs/ target/build/libs
      for app in ${MAIN_CLASS}
      do
        echo ">> for ${PROJECT_NAME}.$app..."
        jar cvfm target/build/${PROJECT_NAME}-$app-${PROJECT_VERSION}.jar target/MANIFEST.MF -C target/classes .
      done
      echo "done."
      ;;
    m | manifest)
      echo "create manifest ..."
      echo """Manifest-Version: ${PROJECT_NAME}
Main-Class: ${MAIN_CLASS}
Class-Path: ${JARS}
Created-By: ${JAVA_BUILD}
Implementation-Title: ${PROJECT_NAME}
Implementation-Version: ${PROJECT_VERSION}-build_${GIT_COMMIT_ID:0:12}
Implementation-Vendor: ${VENDOR_NAME}
Implementation-Author: ${AUTHOR_NAME}
""" >>target/MANIFEST.MF
      ;;
   r | run | execute)
        echo "run ..."
        # Récupérer tous les arguments après l'option "r"
        shift # Supprime le premier argument (r)
        if [ -n "${JARS}"]
        then
        	java $RUNTIME_OPTS -jar target/build/${PROJECT_NAME}-${MAIN_CLASS}-${PROJECT_VERSION}.jar "$@"
        else
        	java -cp ${JARS// //${FS}} $RUNTIME_OPTS -jar target/build/${PROJECT_NAME}-${MAIN_CLASS}-${PROJECT_VERSION}.jar "$@"
        fi
        ;;
    s | src)
      generateSourceJar
      ;;
    t | test)
      executeTests
      ;;
  esac;
done
