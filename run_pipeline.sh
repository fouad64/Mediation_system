#!/bin/bash

# 1. Check if containers are already running
RUNNING_CONTAINERS=$(docker compose ps --filter "status=running" -q)

if [ -z "$RUNNING_CONTAINERS" ]; then
    echo "--------------------------------------"
    echo "🚀 Starting SFTP Infrastructure..."
    echo "--------------------------------------"
    docker compose up -d

    # 2. Give the containers a few seconds to fully initialize
    echo "⏳ Waiting for containers to be ready (5s)..."
    sleep 5
else
    echo "--------------------------------------"
    echo "✅ SFTP Infrastructure is already running. Skipping startup."
    echo "--------------------------------------"
fi

# 3. Run the Java mediation pipeline using Maven
echo "--------------------------------------"
echo "⚙️  Running Mediation Pipeline..."
echo "--------------------------------------"
mvn clean compile exec:java

echo "--------------------------------------"
echo "✅ Pipeline Execution Finished!"
echo "--------------------------------------"
