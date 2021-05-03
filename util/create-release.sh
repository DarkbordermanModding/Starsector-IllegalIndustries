#!/usr/bin/env bash

MOD=$(basename "$(pwd)")
VERSION=$(jq -r .version mod_info.json)
RELEASE=$MOD-$VERSION

# copy over mod to release folder
mkdir $MOD
cp -R data/ graphics/ sounds/ src/ $MOD/
cp mod_info.json ${MOD}.jar $MOD/

zip -r $RELEASE.zip $MOD
rm -rf $MOD
