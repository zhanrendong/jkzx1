curl 'https://oapi.dingtalk.com/robot/send?access_token=310d4788c2227bcef34fbbb5f2779ec6a4b4ddc79f116677b950f94e4564e1fe' \
   -H 'Content-Type: application/json' \
   -d "{\"msgtype\": \"markdown\",
        \"markdown\": {
            \"title\": \"部署通知\",
            \"text\": \"### 部署通知\n > ${1} ${2}\n\n > ![screenshot](https://docs.gitlab.com/ee/ci/introduction/img/gitlab_workflow_example_11_9.png)\n > ##### GITLAB \n\"
        },
        \"at\": {
                \"isAtAll\": true
            }
      }"
