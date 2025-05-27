#!/bin/bash
# build script (c) 2025 Frederic Delorme
#
# Please adapt the `project_name`, `project_version` and `main_class` variables to fit your own project.
# The generated JARs will be named as target/build/[project_name]-[main_class]-[project_version].jar
# NOTE: `main_class` is a list of space-separated classes that generate as many JAR files as listed classes.
## Add external JAR dependencies (mostly in ./libs) by using the JARS variable.
# e.g.: JARS="./libs/flexmark-all-0.64.8-lib.jar ./libs/org.eclipse.jgit-7.2.0.202503040940-r.jar"
# to build your project:
#     build c b m j 
# Will clean (c), build (b), create a manifest (m), and create a jar(j).
#     build r
# will execute the create JAR.
#
project_name=APPLICATION_NAME
project_version=0.0.1
main_class=App
author_name=YOUR_NAME
vendor_name=YOUR_VENDOR_NAME
#
# List of paths to jar dependencies (space separated).
# e.g.: JARS="libs/junit-5.0.4.jar libs/moscito.jar"
# 
JARS=
#
#--- DO NOT CHANGE THE FOLLOWING LINES ---
#
GIT_COMMIT_ID=$(git rev-parse HEAD)
JAVA_BUILD=$(java --version | head -1 | cut -f2 -d' ')
echo "build project ' ${project_name}' version ${project_version}..."
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
    c | clear)
      echo "clean previous build..."
      rm -vrf target/
      mkdir -vp target/{build,classes}
      echo "done."
      echo ---
      ;;
    r | run | execute)
      echo "run ..."
      java -jar target/build/${project_name}-${main_class}-${project_version}.jar
      ;;
    b | build)
      echo "build ..."
      echo "sources files:"
      find src/main/java src/main/resources -type f -name *.java
      echo ---
      javac -d target/classes -cp ${JARS// //;}:src/main/java:src/main/resources $(find src/main/java src/main/resources -type f -name *.java)
      cp -vr src/main/resources/* target/classes/
      ;;
    m | manifest)
      echo "create manifest ..."
      echo """Manifest-Version: ${project_name}
Main-Class: ${main_class}
Class-Path: ${JARS}
Created-By: ${JAVA_BUILD}
Implementation-Title: ${project_name}
Implementation-Version: ${project_version}-build_${GIT_COMMIT_ID:0:12}
Implementation-Vendor: ${vendor_name}
Implementation-Author: ${author_name}
""" >>target/MANIFEST.MF
      ;;
    j | jar)
    echo "build jar..."
    for app in ${main_class}
    do
      echo ">> for ${project_name}.$app..."
      jar cvfm target/build/${project_name}-$app-${project_version}.jar target/MANIFEST.MF -C target/classes .
      mkdir -p target/build/libs
      cp -vr ./libs/ target/build/libs
      echo "done."
    done
  esac;
done


