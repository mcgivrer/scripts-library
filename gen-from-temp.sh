#!/bin/bash

# Définir les variables
export GIT_AUTHOR_NAME="Frédéric Delorme"
export GIT_AUTHOR_EMAIL=frederic.delorme@merckgroup.com
export APP_VERSION=0.0.1

# Générer le fichier en utilisant envsubst
envsubst < ~/scripts/templates/app_template.java > ./App.java
