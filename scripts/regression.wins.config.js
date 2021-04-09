module.exports = {
  apps: [{
      name: 'bct-server',
      script: 'java',
      args: ['-jar', '-Xms128m', '-Xmx8g', 'bin/bct-server-3.0.jar'],
      env: {
          FILE_LOCATION: 'document-service/'
      }
    }, {
           name: 'zuul-server',
           script: 'java',
           args: ['-jar', '-Xms128m', '-Xmx2g', 'bin/zuul-server-3.0.jar']
         }
    , {
      name: 'workflow-service',
      script: 'java',
      args: ['-jar', '-Xms128m', '-Xmx2g', 'bin/workflow-service-3.0.jar'],
      env: {
          FILE_LOCATION: 'workflow-service/'
      }
  },{
     name: 'ironman',
     script: 'python',
     args: ['scripts/airflow/terminal/run.py'],
     env: {
          	PYTHONPATH: "scripts/airflow/"
     }
  }]
};