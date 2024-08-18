#!/bin/sh
#
# Entrypoint for start notifier
#

echo "Starting embassy-notifier node on host: " $HOSTNAME
java -jar moldova-notifier.jar
