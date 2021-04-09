#!/bin/bash
# start a postgresql db afresh
# nothing from the last run will be kept
cd ../initialize/db
pwd
echo "recreate db and redis"
./start_db_fresh.sh
rc=$?; if [[ $rc != 0 ]]; then exit $rc; fi
cd ../../../
pwd
echo "restart bct"
pm2 delete all
pm2 start bct.config.js
sleep 120
cd scripts/initialize/db
pwd
echo "init system data"
bash init_system_data.sh
cd ../minimum/
pwd
echo "init system"
./init_min_system.sh
rc=$?; if [[ $rc != 0 ]]; then exit $rc; fi
cd ../../airflow
pwd
#echo "run report test"
#python3 report_test.py
#rc=$?; if [[ $rc != 0 ]]; then exit $rc; fi
#echo "run intraday script"
#python3 intraday.py
#rc=$?; if [[ $rc != 0 ]]; then exit $rc; fi
echo "run eod script"
python3 eod_pd.py
rc=$?; if [[ $rc != 0 ]]; then exit $rc; fi
