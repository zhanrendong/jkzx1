#!/bin/bash

./gradlew sonarqube \
 -Dsonar.projectKey=cloud3_master_staging \
 -Dsonar.host.url=http://10.1.5.11:9000 \
 -Dsonar.login=f50a4f87d45109335c2942a34a62788ca3762546
