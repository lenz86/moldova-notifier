#!/bin/bash

# Check that path to directory is provided
if [ "$#" -ne 1 ]; then
    echo "Usage: $0 /path/to/directory"
    exit 1
fi

# Get directory from arguments
WORK_DIR="$1"

# Check that directory exists
if [ ! -d "$WORK_DIR" ]; then
    echo "Directory $WORK_DIR does not exist."
    exit 1
fi

echo "Starting containers in $WORK_DIR"

# Move to directory
cd "$WORK_DIR" || exit

docker compose -f ./docker-compose.yml down
docker compose -f ./docker-compose.yml up -d
