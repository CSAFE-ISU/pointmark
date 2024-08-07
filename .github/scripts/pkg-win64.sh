#!/bin/sh
set -eux

FIJI_FILENAME="fiji-win64.zip"

wget https://downloads.imagej.net/fiji/latest/fiji-win64.zip -qO "$FIJI_FILENAME"

zip --delete "$FIJI_FILENAME" 'Fiji.app/macros/*'
zip --delete "$FIJI_FILENAME" 'Fiji.app/plugins/*'
zip --delete "$FIJI_FILENAME" 'Fiji.app/scripts/*'
zip --delete "$FIJI_FILENAME" 'Fiji.app/images/about/*'

zip -r "$FIJI_FILENAME" Fiji.app
if unzip -vl "$FIJI_FILENAME" | grep -i "pointmark"; then
  echo "Found 'pointmark' in the zip file."
else
  echo "'pointmark' not found in the zip file."
fi
