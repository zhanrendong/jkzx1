#!/bin/bash

echo "ensure the env var exist"
if [ 0"$AIRFLOW_HOME" = "0" ];then
    exit 2
else
    cd $AIRFLOW_HOME
fi
pwd
echo "remove the python scripts in dags"
if [ ! -d "./dags/" ];then
    mkdir dags
fi
cd dags
sudo rm -rf ./__pycache__
sudo rm -rf ./*
echo "copy dags to airflow dags"
cp -r /home/tongyu/bct/install/scripts/airflow/* ./
cp ../ip_list ./
