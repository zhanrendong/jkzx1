#!/bin/bash

# 文件备份后缀
bak_suffix_date='20191203'
# bct更新包名
bct_file=20191203111328.tar.gz
# ironman更新包名(不含后缀)
ironman_file=ironman-release-jkzx-OTMS-8988
# ironman更新包后缀
ironman_file_suffix=.tar.gz

# 是否复用配置文件bct.config.js,默认为true，复用
reuse_bct_config=true
# 是否复用配置文件default_config.ini,默认为true，复用
reuse_dags_config=true

# ironman解压到服务器文件夹名称
ironman_dir_name=ironman-master-prod-jkzx
AIRFLOW_HOME=/home/airflow/airflow
BCT_HOME=/home/tongyu/jkzx

if [ -d ./install ];then
    rm -rf install
fi
if [ -d ./${ironman_file} ];then
    rm -rf ${ironman_file}
fi

echo "${bct_file}开始解压--------------------"
tar -xf ${bct_file} -C ./
if [ $? == 0 ];then
    echo "${bct_file}解压成功"
else
    echo "${bct_file}解压失败"
    exit
fi
echo "${ironman_file}${ironman_file_suffix}开始解压-------------------"
if [ ${ironman_file_suffix} == ".tar.gz" ];then
    tar -xf ${ironman_file}${ironman_file_suffix} -C ./
else
    unzip ${ironman_file}${ironman_file_suffix} > /dev/null
fi
if [ $? == 0 ];then
    echo "${ironman_file}${ironman_file_suffix}解压成功"
else
    echo "${ironman_file}${ironman_file_suffix}解压失败"
    exit
fi

chown -R root:root install
chown -R root:root ${ironman_file}

if [ ${reuse_bct_config} == "true" ];then
    \cp ${BCT_HOME}/install/bct.config.js .
fi
if [ ${reuse_dags_config} == "true" ];then
    \cp ${AIRFLOW_HOME}/dags/dags/conf/default_config.ini .
fi

echo "开始备份文件-------------------"
if [ -d /etc/nginx/dist ];then
    if [ -d /etc/nginx/dist_${bak_suffix_date} ];then
        rm -rf /etc/nginx/dist_${bak_suffix_date}
    fi
    echo "开始备份dist文件"
    mv /etc/nginx/dist /etc/nginx/dist_${bak_suffix_date}
fi
if [ -d ${BCT_HOME}/install ];then
    if [ -d ${BCT_HOME}/install_${bak_suffix_date} ];then
        rm -rf ${BCT_HOME}/install_${bak_suffix_date}
    fi
    echo "开始备份install文件"
    mv ${BCT_HOME}/install ${BCT_HOME}/install_${bak_suffix_date}
fi
if [ -d ${BCT_HOME}/${ironman_dir_name} ];then
    if [ -d ${BCT_HOME}/${ironman_dir_name}_${bak_suffix_date} ];then
        rm -rf ${BCT_HOME}/${ironman_dir_name}_${bak_suffix_date}
    fi
    echo "开始备份${ironman_dir_name}文件"
    mv ${BCT_HOME}/${ironman_dir_name} ${BCT_HOME}/${ironman_dir_name}_${bak_suffix_date}
fi
if [ -d ${AIRFLOW_HOME}/dags ];then
    if [ -d ${AIRFLOW_HOME}/dags_${bak_suffix_date} ];then
        rm -rf ${AIRFLOW_HOME}/dags_${bak_suffix_date}
    fi
    echo "开始备份dags文件"
    mv ${AIRFLOW_HOME}/dags ${AIRFLOW_HOME}/dags_${bak_suffix_date}
fi

echo "---------------------开始copy升级包文件--------------------------------------"
cp -r install/dist /etc/nginx/dist
cp -r install ${BCT_HOME}
cp -r install/scripts/airflow ${AIRFLOW_HOME}/dags
cp -r ${ironman_file} ${BCT_HOME}/${ironman_dir_name}
rm -rf ${AIRFLOW_HOME}/dags/dags
cp -r ${ironman_file}/dags ${AIRFLOW_HOME}/dags/
cp -r ${ironman_file}/terminal ${AIRFLOW_HOME}/dags/
cp -r ${ironman_file}/eod_terminal_data_dag.py ${AIRFLOW_HOME}/dags/
cp -r ${ironman_file}/manual_terminal_date_dag.py ${AIRFLOW_HOME}/dags/
echo "---------------------升级包文件copy完成--------------------------------------"

if [ ${reuse_bct_config} == "true" ];then
    \cp bct.config.js ${BCT_HOME}/install/
fi
if [ ${reuse_dags_config} == "true" ];then
    \cp default_config.ini ${AIRFLOW_HOME}/dags/dags/conf/default_config.ini
fi

rm -rf install
rm -rf ${ironman_file}

echo "-----------------开始重启服务------------------------"
nginx -s reload

su - tongyu << EOF
pm2 delete all
cd ${BCT_HOME}/install/
pm2 start bct.config.js
EOF

su - airflow << EOF
cd ${AIRFLOW_HOME}
supervisorctl restart all
EOF

echo "update otc system over!"
