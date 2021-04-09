#! /bin/bash

for i in $(find /home/tongyu/ -name workflow-service);

do
  echo $i
  mv $i/files/* /home/tongyu/workflow-service;
done
