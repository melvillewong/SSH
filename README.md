# SSH

<h2>Let's get this done!</h2>
___
## Prerequisites
- PostgreSQL 14.0
- Docker Installation
___

### Docker Installation Tutorial with migration implementation (Flyway)
1. Download Docker from the official website (https://www.docker.com/products/docker-desktop/).
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
Please be sure to `git pull` each time before starting any work on the database, as it retrieves the latest version of the repository.
And please `git push` to prevent any versions to be lost in between each migration.
