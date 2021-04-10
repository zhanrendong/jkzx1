#!/bin/bash
ROOT_PATH=$(pwd)
SCRIPT_PATH="$ROOT_PATH/scripts"
INIT_PATH="$SCRIPT_PATH/initialize"
DB_PATH="$INIT_PATH/db"
AIRFLOW_PATH="$SCRIPT_PATH/airflow"
MINIMUM_PATH="$INIT_PATH/minimum"
cd "$DB_PATH" || exit
if [ "$1" == "local" ]
  then
    ./start_db_fresh.sh
    cat regression_trades.sql  | docker exec -i bct-postgresql psql -U bct
    cat regression_quotes.sql  | docker exec -i bct-postgresql psql -U bct
    cd "$ROOT_PATH" || exit
    pm2 start regression.config.js
  else
    echo "use set up db"
fi
for (( c=1; c<=45; c++ ))
do  
   echo "check server status: $c"
   pm2 list
   pm2 log --nostream
   sleep 1
done
export BCT_PORT="16016"
export PYTHONPATH="$MINIMUM_PATH"
python "$MINIMUM_PATH"/init_regression.py
export PYTHONPATH="$AIRFLOW_PATH"
export TERMINAL_ENV=regression
python "$AIRFLOW_PATH"/regression/f2b_regression.py
