#!/bin/sh

VERSION_NUMBER=$1

set -eu

mkdir -p Fiji.app
cd Fiji.app

# copy plugins
mkdir -p plugins
cd plugins

echo "copying pointmark jar..."
echo "VERSION_NUMBER received: ${VERSION_NUMBER}"
echo "VERSION_NUMBER=${VERSION_NUMBER}" >> $GITHUB_ENV
cd ../../
cp pointmark-1.0.${VERSION_NUMBER}.jar ./pointmark-1.0.${VERSION_NUMBER}.jar

# https://figshare.com/articles/dataset/Custom_toolbars_and_mini_applications_with_Action_Bar/3397603
echo "downloading action_bar.jar..."
JAR_URL="https://figshare.com/ndownloader/files/42230733"
# ActionBar needs this specific folder location in order to load icons
mkdir -p ActionBar/icons
cd ActionBar/icons
wget -qO action_bar.jar $JAR_URL