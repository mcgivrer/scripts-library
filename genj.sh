#!/bin/bash

# Default values for optional parameters
DEFAULT_JAVA_VERSION="11"
DEFAULT_AUTHOR_NAME="Nom de l'Auteur"
DEFAULT_AUTHOR_EMAIL="email@example.com"
DEFAULT_PROJECT_VERSION="1.0.0"
DEFAULT_MAIN_CLASS_NAME="MainClass"
DEFAULT_BASE_PACKAGE="com.example"
DEFAULT_VENDOR_NAME="Your Company"
BUILD_SCRIPT_SOURCE="$HOME/scripts/templates/build.txt"  # Path to the build script

# Function to display usage instructions
usage() {
    echo "Usage: $0 --template <template_directory> --project-name <project_name> --java-version <java_version> --author-name <author_name> --author-email <author_email> --project-version <project_version> --main-class <main_class_name> --base-package <base_package>"
    echo ""
    echo "Parameters:"
    echo "  -t  | --template <template_directory>     Path to the directory containing template files."
    echo "  -p  | --project-name <project_name>       Name of the project to be created."
    echo "  -j  | --java-version <java_version>       Version of Java to use (default: $DEFAULT_JAVA_VERSION)."
    echo "  -a  | --author-name <author_name>         Name of the author (default: $DEFAULT_AUTHOR_NAME)."
    echo "  -e  |  --author-email <author_email>      Email of the author (default: $DEFAULT_AUTHOR_EMAIL)."
    echo "  -v  | --project-version <project_version> Version of the project (default: $DEFAULT_PROJECT_VERSION)."
    echo "  -mc | --main-class <main_class_name>      Name of the main class (default: $DEFAULT_MAIN_CLASS_NAME)."
    echo "  -pk | --base-package <base_package>       Base package for the project (default: $DEFAULT_BASE_PACKAGE)."
    echo "  -vn | --vendor-name <vendor_name>         Vendor name set in MANIFEST.MF file (default: $DEFAULT_VENDOR_NAME)."
    
    echo ""
    echo "Description:"
    echo "This script generates a Java project based on the specified template files."
    echo "It creates the necessary directory structure, copies the templates, replaces"
    echo "placeholders in the templates with the provided values, and generates additional"
    echo "files such as MANIFEST.MF and README.md."
    echo ""
    echo "The build script will be copied from the path: $BUILD_SCRIPT_SOURCE."
    echo "Ensure that the build script exists at this location."
    exit 1
}

# Check for parameters and assign them to variables
while [[ "$#" -gt 0 ]]; do
    case $1 in
        -t|--template) TEMPLATE_DIR="$2"; shift ;;  # Path to the template directory
        -p|--project-name) PROJECT_NAME="$2"; shift ;;  # Name of the project
        -j|--java-version) JAVA_VERSION="$2"; shift ;;  # Version of Java to use
        -a|--author-name) AUTHOR_NAME="$2"; shift ;;  # Author's name
        -e|--author-email) AUTHOR_EMAIL="$2"; shift ;;  # Author's email
        -v|--project-version) PROJECT_VERSION="$2"; shift ;;  # Version of the project
        -mc|--main-class) MAIN_CLASS_NAME="$2"; shift ;;  # Name of the main class
        -pk|--base-package) BASE_PACKAGE="$2"; shift ;;  # Base package for the project
        -vn|--vendor-name) VENDOR_NAME="$2"; shift ;;  # Vendor name for the project
        *) usage ;;  # Display usage if an unknown parameter is found
    esac
    shift
done

# Check for mandatory parameters
if [ -z "$TEMPLATE_DIR" ] || [ -z "$PROJECT_NAME" ] || [ -z "$BASE_PACKAGE" ]; then
    echo "Error: The parameters --template, --project-name, and --base-package are required."
    usage  # Display usage if mandatory parameters are missing
fi

# Initialize parameters with default values if not specified
JAVA_VERSION="${JAVA_VERSION:-$DEFAULT_JAVA_VERSION}"  # Use default Java version if not provided
AUTHOR_NAME="${AUTHOR_NAME:-$DEFAULT_AUTHOR_NAME}"  # Use default author name if not provided
AUTHOR_EMAIL="${AUTHOR_EMAIL:-$DEFAULT_AUTHOR_EMAIL}"  # Use default author email if not provided
PROJECT_VERSION="${PROJECT_VERSION:-$DEFAULT_PROJECT_VERSION}"  # Use default project version if not provided
MAIN_CLASS_NAME="${MAIN_CLASS_NAME:-$DEFAULT_MAIN_CLASS_NAME}"  # Use default main class name if not provided
BASE_PACKAGE="${BASE_PACKAGE:-$DEFAULT_BASE_PACKAGE}"  # Use default base package if not provided
VENDOR_NAME="${VENDOR_NAME:-$DEFAULT_VENDOR_NAME}"  # Use default vendor name if not provided

# Create the project directory
mkdir -p "$PROJECT_NAME"  # Create the main project directory
mkdir -p "$PROJECT_NAME/${BASE_PACKAGE//./\/}"  # Create the base package directory structure

# Function to replace placeholders in template files
replace_placeholders() {
    local file="$1"
    sed -i.bak -e "s/{{JAVA_VERSION}}/$JAVA_VERSION/g" \
               -e "s/{{AUTHOR_NAME}}/$AUTHOR_NAME/g" \
               -e "s/{{AUTHOR_EMAIL}}/$AUTHOR_EMAIL/g" \
               -e "s/{{PROJECT_NAME}}/$PROJECT_NAME/g" \
               -e "s/{{PROJECT_VERSION}}/$PROJECT_VERSION/g" \
               -e "s/{{MAIN_CLASS_NAME}}/$MAIN_CLASS_NAME/g" \
               -e "s/{{BASE_PACKAGE}}/$BASE_PACKAGE/g" \
               -e "s/{{VENDOR_NAME}}/$VENDOR_NAME/g" \
               "$file"
}

# Function to copy and process the template directory recursively
copy_and_process_templates() {
    local src="$1"
    local dest="$2"
    
    # Create the destination directory
    mkdir -p "$dest"
    
    # Loop through each item in the source directory
    for item in "$src/"*; do
        if [ -d "$item" ]; then
            # If it's a directory, call the function recursively
            local new_dir_name=$(basename "$item")
            new_dir_name=$(echo "$new_dir_name" | sed -e "s/{{JAVA_VERSION}}/$JAVA_VERSION/g" \
                                                      -e "s/{{AUTHOR_NAME}}/$AUTHOR_NAME/g" \
                                                      -e "s/{{AUTHOR_EMAIL}}/$AUTHOR_EMAIL/g" \
                                                      -e "s/{{PROJECT_NAME}}/$PROJECT_NAME/g" \
                                                      -e "s/{{PROJECT_VERSION}}/$PROJECT_VERSION/g" \
                                                      -e "s/{{MAIN_CLASS_NAME}}/$MAIN_CLASS_NAME/g" \
                                                      -e "s/{{BASE_PACKAGE}}/$BASE_PACKAGE/g" \
                                                      -e "s/{{VENDOR_NAME}}/$VENDOR_NAME/g")
            copy_and_process_templates "$item" "$dest/$new_dir_name"
        elif [ -f "$item" ]; then
            # If it's a file, copy it and replace placeholders in the filename
            local file_name=$(basename "$item")
            local new_file_name=$(echo "$file_name" | sed -e "s/{{JAVA_VERSION}}/$JAVA_VERSION/g" \
                                                           -e "s/{{AUTHOR_NAME}}/$AUTHOR_NAME/g" \
                                                           -e "s/{{AUTHOR_EMAIL}}/$AUTHOR_EMAIL/g" \
                                                           -e "s/{{PROJECT_NAME}}/$PROJECT_NAME/g" \
                                                           -e "s/{{PROJECT_VERSION}}/$PROJECT_VERSION/g" \
                                                           -e "s/{{MAIN_CLASS_NAME}}/$MAIN_CLASS_NAME/g" \
                                                           -e "s/{{BASE_PACKAGE}}/$BASE_PACKAGE/g" \
                                                           -e "s/{{VENDOR_NAME}}/$VENDOR_NAME/g")
            cp "$item" "${BASE_PACKAGE//./\/}/$new_file_name"
            replace_placeholders "$dest/$new_file_name"  # Replace placeholders in the file content
        fi
    done
}

# Start copying and processing templates
copy_and_process_templates "$TEMPLATE_DIR" "$PROJECT_NAME"

# Create the MANIFEST.MF file in the META-INF directory
MANIFEST_FILE="$PROJECT_NAME/META-INF/MANIFEST.MF"
mkdir -p "$PROJECT_NAME/META-INF"  # Create META-INF directory
cat <<EOF > "$MANIFEST_FILE"  # Write the manifest file
Manifest-Version: 1.0
Main-Class: ${BASE_PACKAGE}.${MAIN_CLASS_NAME}
Author: $AUTHOR_NAME
Email: $AUTHOR_EMAIL
Version: $PROJECT_VERSION
Java-Version: $JAVA_VERSION
Vendor-Name: $VENDOR_NAME
EOF

# Create a README.md file with basic project information
cat <<EOF > "$PROJECT_NAME/README.md"
# $PROJECT_NAME

## Author
$AUTHOR_NAME <$AUTHOR_EMAIL>

## Version
$PROJECT_VERSION

## Java Version
$JAVA_VERSION
EOF

# Copy the build script to the project directory
if [ -f "$BUILD_SCRIPT_SOURCE" ]; then
    cp "$BUILD_SCRIPT_SOURCE" "$PROJECT_NAME/build"
    # Replace placeholders in the build script
    sed -i.bak -e "s/{{JAVA_VERSION}}/$JAVA_VERSION/g" \
               -e "s/{{AUTHOR_NAME}}/$AUTHOR_NAME/g" \
               -e "s/{{AUTHOR_EMAIL}}/$AUTHOR_EMAIL/g" \
               -e "s/{{PROJECT_NAME}}/$PROJECT_NAME/g" \
               -e "s/{{PROJECT_VERSION}}/$PROJECT_VERSION/g" \
               -e "s/{{MAIN_CLASS_NAME}}/$MAIN_CLASS_NAME/g" \
               -e "s/{{BASE_PACKAGE}}/$BASE_PACKAGE/g" "$PROJECT_NAME/build"

    chmod +x "$PROJECT_NAME/build"  # Make the build script executable
    echo "Build script copied to the project directory."
else
    echo "Warning: Build script not found at $BUILD_SCRIPT_SOURCE. Skipping copy."
fi

# Print a success message indicating where the project has been generated
echo "Java project successfully generated in the directory: $PROJECT_NAME"
