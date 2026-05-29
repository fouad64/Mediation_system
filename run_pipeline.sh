#!/bin/bash

# 1. Check if containers are already running
RUNNING_CONTAINERS=$(docker compose ps --filter "status=running" -q)

if [ -z "$RUNNING_CONTAINERS" ]; then
    echo "--------------------------------------"
    echo "🚀 Starting SFTP Infrastructure..."
    echo "--------------------------------------"
    docker compose up -d

    echo "⏳ Waiting for containers to be ready (5s)..."
    sleep 5
else
    echo "--------------------------------------"
    echo "✅ SFTP Infrastructure is already running. Skipping startup."
    echo "--------------------------------------"
fi

echo "--------------------------------------"
echo "⚙️  Starting Automatic Mediation Scheduler (every 15s)"
echo "    Press Ctrl+C to stop"
echo "--------------------------------------"

while true; do
    echo "--------------------------------------"
    echo "⚙️  Running Mediation Pipeline (Time: $(date))..."
    echo "--------------------------------------"

    mvn compile exec:java

    echo "--------------------------------------"
    echo "✅ Pipeline Run Finished! Sleeping for 15 seconds..."
    echo "--------------------------------------"
    sleep 15
done
