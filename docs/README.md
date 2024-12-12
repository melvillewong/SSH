___

**Please visit [here](/docs) for details of each component**

___
## Prerequisites
- PostgreSQL 14.0 installation
- Java Environment
- Docker Installation
___
#### Simple Testing by running the automation shell script start_all.sh
- The shell script start_all.sh is simple script that does the following
  - Initiate the Docker containers, both “postgres_server’ and “flyway”
  - Compile necessary java files to the latest version
  - Start RecordsDatabaseServer.java and TerminalRecordsClient.java with the required drivers
  - Automate/Simulate the user inputs onto TerminalRecordsClient.java with first_name = “Anson” and last_name = “Lo”
  - Clean up, kill the processes and “docker-compose down”

#### Docker Installation Tutorial with migration implementation (Flyway)
1. Download Docker from the [official website](https://www.docker.com/products/docker-desktop/).
2. `git pull` for the latest version of the repository.
3. Head to the directory where the repository sits.

#### How do we start up the server?
- `docker-compose up`
  - This is to initialise the container that contains the image of "postgresql" and "flyway/flyway".
  - This will automatically migrate the latest version of the datbase using the SQL scripts within "/migrations/".
- `docker-compose down`
  - This is to terminate/shutdown the container.
___

#### How do we access the database using PostgreSQL CLI?
- Please use the command `psql -h localhost -p 5432 -U postgres -d ssh_smart_scheduling`

- How to view the histories of recent migrations?
  - `SELECT * FROM flyway_schema_history;`
___
#### Continous integration of PostgreSQL Database
- Within the "/migrations/" folder, there are files startign with "V1__...", "V2__...".
  - Please name the most recent changes or updates of the SQL script to the format of, "V'X'__..." where X is the version/number of the times that the database have been updated.
  - This is nesscery for "Flyway", the migration automation, to work smoothly.
  - Ensure that the message "Successfully applied 1 migration (execution time 00:00.XXXs)." is displayed for each migration, else please update the relevant files for it to run successfully.
___
#### Git Pull and Git Push
- Please be sure to `git pull` each time before starting any work on the database, as it retrieves the latest version of the repository.
- And please `git push` to prevent any versions to be lost in between each migration.
