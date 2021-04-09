#!/bin/bash
cat init_system_data.sql | docker exec -i bct-postgresql psql -U bct
