#!/bin/bash

lein uberjar
cp ./target/uberjar/muninn.jar ./dist/
chmod +x ./search.sh
