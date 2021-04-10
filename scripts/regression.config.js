module.exports = {
    apps: [{
        name: 'zuul-server',
        script: 'java',
        args: ['-jar', '-Xms128m', '-Xmx2g', 'bin/zuul-server-3.0.jar']
    },{
        name: 'ironman',
        script: 'python3',
        args: ['scripts/airflow/terminal/run.py'],
        env: {
            PYTHONPATH: "scripts/airflow/"
        }
    }]
};
