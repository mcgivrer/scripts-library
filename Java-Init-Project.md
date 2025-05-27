# README

The java init project commend line script creates a new java project with some build helpers (no maven/gradle).

```bash
$USER/scripts/java-init-project.sh
```

## Usage

Create a java project into a git-flow repository following the maven file structure
(but without maven command file)

```bash
$USER/scripts/java-init-project.sh \
 -m [project_name] \
 -a [author_name] \
 -e [author_email] \
 -v [project_version] \
 -j [java_version] \
 -p [package_name] \
 -k [JDK flavor version]
```

where:

- m) `[project_name]` name of the project directory to be generated, will be used capitalized as application name and main class (default is ),
- v) `[project_version]` the version for the project to be initialized (default is ),
- p) `[package_name]` the root java package name for your main application class (default is ),
- j) `[java_version]` the targeted compatible java version to be use as default one for the project (default is ).
- a) `[author_name]` name of the author (used as git commiter),
- e) `[author_email]` email of the author (used as git commiter),
- k) `[JDK flavor version]` you may define the sdkman jdk flavor and version (default is ).

## Examples

1. Create a named project

```bash
  $USER/scripts/java-init-project.sh -m MyProject
```

This will create a project named MyProject.

2. Create a project with a specific package:

```bash
  $USER/scripts/java-init-project.sh -m MyProject -p com.my.package
```

This will create a project MyProject with root package as com.my.package.app and the main class MyProject.

3. Create a project with a minimum JDK version

```bash
$USER/scripts/java-init-project.sh -m MyProject -j 21
```

This will create a JDK with a compatibility jdk version set to 21

4. Defining the specific JDK flavor (for sdkman usage)

```bash
  $USER/scripts/java-init-project.sh -m MyProject -j 21 -k 21.fx-zulu
```

This will require the Zulu JDK version 21 with JavaFX.
