#!/bin/bash

echo "Starting the PostgreSQL Docker container..."
docker-compose down && docker-compose up -d
if [ $? -ne 0 ]; then
    echo "Failed to start the Docker container."
    exit 1
fi

echo "Waiting for PostgreSQL to be ready..."
sleep 2  # Adjust this time based on your database initialization time

echo "Compiling Java files..."
javac java-temp/*.java
if [ $? -ne 0 ]; then
    echo "Failed to compile Java files."
    exit 1
fi

echo "Starting RecordsDatabaseServer..."
java -cp "java-temp:java-temp/lib/postgresql-42.6.0.jar" RecordsDatabaseServer &
SERVER_PID=$!
echo "RecordsDatabaseServer started with PID $SERVER_PID"

echo "Automating input for TerminalRecordsClient..."
(
  echo "Anson"  # First Name
  sleep 1       # Pause to simulate real typing (optional)
  echo "Lo"     # Last Name
) | java -cp "java-temp:java-temp/lib/postgresql-42.6.0.jar" TerminalRecordsClient &
CLIENT_PID=$!
echo "TerminalRecordsClient started with PID $CLIENT_PID"
sleep 2

echo "Cleaning up..."
wait $CLIENT_PID
kill $SERVER_PID
docker-compose down

echo "Process complete!"