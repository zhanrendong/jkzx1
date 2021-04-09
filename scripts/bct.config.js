module.exports = {
  apps: [{
      name: 'bct-server',
      script: 'java',
      args: ['-jar', '-Xms128m', '-Xmx8g', 'bin/bct-server-3.0.jar'],
      env: {
          FILE_LOCATION: '/home/tongyu/document-service'
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
          FILE_LOCATION: '/home/tongyu/workflow-service'
      }
  },{
     name: 'ironman',
     script: 'python3',
     args: ['/home/tongyu/jkzx/ironman-release-jkzx-OTMS-7018/terminal/run.py']
  }]
};