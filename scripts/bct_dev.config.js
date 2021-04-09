module.exports = {
  apps: [{
      name: 'bct-server',
      script: 'java',
      args: ['-jar', '-Xms128m', '-Xmx512m', '../servers/bct-server/target/bct-server-3.0.jar'],
      env: {
          FILE_LOCATION: '/home/tongyu/document-service'
      }
    }, {
      name: 'zuul-server',
      script: 'java',
      args: ['-jar', '-Xms128m', '-Xmx512m', '../servers/zuul-server/target/zuul-server-3.0.jar']
    }, {
      name: 'workflow-service',
      script: 'java',
      args: ['-jar', '-Xms128m', '-Xmx512m', '../workflow-service/target/workflow-service-3.0.jar'],
      env: {
          FILE_LOCATION: '/home/tongyu/workflow-service'
      }
  }]
};
