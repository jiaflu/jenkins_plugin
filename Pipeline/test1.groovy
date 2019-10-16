
pipeline {

    agent any
    parameters {
        string(name: 'request_id', defaultValue: '', description: '')
        string(name: 'slm_instance', defaultValue: '', description: '')
        string(name: 'automate_tools', defaultValue: 'cmc,slim,gfc,mct', description: 'The CMSE tools which are under automation')
    }

    stages {
        stage('Automatically RegisterServer') {
            steps {
                soaTask(
                        task: "RegisterServer",
                        ackRequired: "slim,gfc,cmc,mct",
                        requestId: params.request_id,
                        instance: params.slm_instance,
                        automate_tools: params.automate_tools
                )
            }
        }
    }
}