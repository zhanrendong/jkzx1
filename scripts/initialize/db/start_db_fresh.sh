#!/bin/bash
# start a postgresql db afresh
# nothing from the last run will be kept 
docker stop bct-postgresql
sleep 3
docker run --rm --name bct-postgresql -e POSTGRES_USER=bct -e POSTGRES_PASSWORD=kEaLJ9ZERLLN! -e POSTGRES_DB=bct -p 5432:5432 -d postgres:9-alpine -c max_connections=300
#sleep 5
docker stop bct-redis
docker run --rm --name bct-redis -p 6379:6379 -d redis:alpine
sleep 10
cat schemas.sql | docker exec -i bct-postgresql psql -U bct
#docker stop bct-oracle
#sleep 5
#docker run --rm --name bct-oracle -p 1521:1521 -d sath89/oracle-xe-11g
#sleep 120
#cat create_user_schema.sql | docker exec -i bct-oracle sqlplus system/oracle@localhost:1521/xe
