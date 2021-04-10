module.exports = {
    apps: [{
        name: 'bct-server',
        script: 'java',
        args: ['-jar', '-Xms128m', '-Xmx8g', '-Djava.net.preferIPv4Stack=true', 'bin/bct-server-3.0.jar'],
        env: {
            FILE_LOCATION: 'document-service/'
        }
    }, {
        name: 'ironman',
        script: 'python3',
        args: ['scripts/airflow/terminal/run.py'],
        env: {
            PYTHONPATH: "scripts/airflow/"
        }
    }]
};
