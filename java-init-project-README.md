# Java init 1.4

The Java init script gives the opportunity to create simple java project with some build script.

UPDATE: add some example usages in help and fix anoying bug.

The created project 

- is a git repository, initialized by default, with global git configured username and email,
- has its java environment maintained through the [sdkman](https://sdkman.io/) installation tool with a `.sdkmanrc` file,
- has default IDEA file excluded from the git repo tracking `.gitignore` file,
- provides a default JUNIT environement to executes unit tests with the all included JAR (see JUNIT_VERSION variable),
- provides a code quality check with [Checktyle](https://checkstyle.sourceforge.io/) tool (see CHECKSTYLE_VERSION variable),
- creates a `build.properties` file used by the [build script](https://gist.github.com/mcgivrer/a31510019029eba73edf5721a93c3dec) script to compile, run, test, produce javadoc, etc...


The create java application provide a basic class with a `config.properties` file and the standard java internationalization file `messages<_locale><_country>.properties`.

## Usage

```bash
java-init-project -m <project_name> -v <project_version> -j <jdk_version> -p <package_name> -a <author_name> -e <author_email>
```
where:

  - `project_name` name of the project directory to be generated (will be used capitalized as application name and main class),
  - `project_version` the version for the project to be initialized,
  - `package_name` the root java package name for your main applicaiton class,
  - `jdk_version` the targeted compatible JDK to be use as default one for the project.
  - `author_name` name of the author (used as git commiter),
  - `author_email` email of the author (used as git commiter).
  
McG!