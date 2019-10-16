def slm_instance = "http://10.224.162.189:8090/slm"

def RegisterPool_slim_result
def RegisterPool_cmc_result
def RegisterServer_cmc_result
def RegisterServer_slim_result
def RegisterServer_gfc_result
def RegisterServer_mct_result
def Deploy_cmc_result
def Deploy_mct_result
def RunDeployPostScript_cmc_result
def JoinLDAP_cmc_result
def RunFixit_cmc_result
def Online_mct_result
def Online_slim_result
def Online_cmc_result

pipeline {
    agent any
    parameters {
        choice(name: 'request_type', choices: ['PoolExpansion','BuildPool'], description: 'Request Type ')
        string(name: 'request_id', defaultValue: '', description: '')
        string(name: 'automate_tools', defaultValue: 'cmc,slim,gfc,mct', description: 'The CMSE tools which are under automation')
        string(name: 'component', defaultValue: '', description: 'Request Component')
    }
    stages {

        stage("Validate Request") {
            steps {
                script {
                    validate_result = soa_validate_req_params(
                            requestId: "${params.request_id}",
                            slmUrl: slm_instance
                    )
                }
            }
        }
        stage("Check Validate Result") {
            when {
                expression { validate_result != "Validation pass" }
            }
            steps {
                println("Error Message: " + validate_result)
                error validate_result
            }
        }


        //*****register_pool started******


        stage("Automatically RegisterPool") {
            when {
                expression { request_type == 'BuildPool' }
            }
            steps {
                soaTask(
                        task: "RegisterPool",
                        ackRequired: "slim,cmc",
                        requestId: request_id,
                        instance: slm_instance,
                        automate_tools: params.automate_tools
                )
            }
        }

        stage('RegisterPool Return') {
            failFast true
            when {
                expression { request_type == 'BuildPool' }
            }
            parallel {
                stage('register pool in slim') {
                    stages {
                        stage("register pool in slim feedback") {
                            when {
                                expression { params.automate_tools.contains("slim") }
                            }
                            steps {
                                script {
                                    def RegisterPool_slim = input(id: 'RegisterPool_slim', message: 'Automatically Register Pool In Slim Success?',
                                            parameters: [
                                                    [$class: 'ChoiceParameterDefinition', description: 'result status', name: 'status', choices: "Success\nFail"],
                                                    string(name:"message",defaultValue:'')
                                            ])
                                    RegisterPool_slim_result = RegisterPool_slim
                                    println(RegisterPool_slim_result)
                                }
                            }
                        }
                        stage("manually fix RegisterPool in slim") {
                            when {
                                allOf{
                                    expression { params.automate_tools.contains("slim") }
                                    expression { RegisterPool_slim_result ==~ /(fail|Fail)/ }
                                }
                            }
                            steps {
                                script {
                                    def RegisterPool_slim_manual = input(id: 'RegisterPool_slim_manual', message: 'Already manually Fixed RegisterPool In Slim?')
                                }
                            }
                        }
                    }
                }
                stage('register pool in cmc') {

                    stages {
                        stage("register pool in cmc feedback") {
                            when {
                                expression { params.automate_tools.contains("cmc") }
                            }
                            steps {
                                script {
                                    def RegisterPool_cmc = input(id: 'RegisterPool_cmc', message: 'Automatically Register Pool In CMC Success?',
                                            parameters: [
                                                    [$class: 'ChoiceParameterDefinition', description: 'result status', name: 'status', choices: "Success\nFail"],
                                                    string(name:"message",defaultValue:'')
                                            ])

                                    RegisterPool_cmc_result = RegisterPool_cmc
                                    println(RegisterPool_cmc_result)
                                }
                            }
                        }
                        stage("manually fix RegisterPool in cmc") {
                            when {
                                allOf {
                                    expression { params.automate_tools.contains("cmc") }
                                    expression { RegisterPool_cmc_result ==~ /(fail|Fail)/ }
                                }
                            }
                            steps {
                                script {
                                    def RegisterPool_cmc_manual = input(id: 'RegisterPool_cmc_manual', message: 'Already manually Fixed RegisterPool In CMC?')
                                }
                            }
                        }
                    }
                }

            }
        }
        //*****register_pool end******

        //*****register_server started******
        stage('Automatically RegisterServer') {
            steps {
                soaTask(
                        task: "RegisterServer",
                        ackRequired: "slim,gfc,cmc,mct",
                        requestId: request_id,
                        instance: slm_instance,
                        automate_tools: params.automate_tools
                )
            }
        }

        stage('RegisterServer Return') {
            failFast true
            parallel {
                stage('register server in slim feedback') {
                    stages {
                        stage("register server in slim") {
                            when {
                                expression { params.automate_tools.contains("slim") }
                            }
                            steps {
                                script {
                                    def RegisterServer_slim = input(id: 'RegisterServer_slim', message: 'Automatically Register Server In Slim Success?',
                                            parameters: [
                                                    [$class: 'ChoiceParameterDefinition', description: 'result status', name: 'status', choices: "Success\nFail"],
                                                    string(name:"message",defaultValue:'')
                                            ])

                                    RegisterServer_slim_result = RegisterServer_slim
                                    println(RegisterServer_slim_result)
                                }
                            }
                        }
                        stage("manually fix RegisterServer in slim") {
                            when {
                                expression { params.automate_tools.contains("slim") }
                                expression { RegisterServer_slim_result ==~ /(fail|Fail)/ }
                            }
                            steps {
                                script {
                                    def RegisterServer_slim_manual = input(id: 'RegisterServer_slim_manual', message: 'Already manually Fixed Register Server In Slim?')
                                }
                            }
                        }
                    }
                }
                stage("register server in gfc feedback") {
                    stages {
                        stage("register server in gfc") {
                            when {
                                expression { params.automate_tools.contains("gfc") }
                            }
                            steps {
                                script {
                                    def RegisterServer_gfc = input(id: 'RegisterServer_gfc', message: 'Automatically Register Server In GFC Success?',
                                            parameters: [
                                                    [$class: 'ChoiceParameterDefinition', description: 'result status', name: 'status', choices: "Success\nFail"],
                                                    string(name:"message",defaultValue:'')
                                            ])

                                    RegisterServer_gfc_result = RegisterServer_gfc
                                    println(RegisterServer_gfc_result)
                                }
                            }
                        }
                        stage("manually fix RegisterServer in gfc") {
                            when {
                                allOf {
                                    expression { params.automate_tools.contains("gfc") }
                                    expression { RegisterServer_gfc_result ==~ /(fail|Fail)/ }
                                }
                            }
                            steps {
                                script {
                                    def RegisterServer_gfc_manual = input(id: 'RegisterServer_gfc_manual', message: 'Already manually Fixed Register Server In GFC?')
                                }
                            }
                        }
                    }
                }
                stage('register server in cmc feedback') {

                    stages {
                        stage("register server in cmc") {
                            when {
                                expression { params.automate_tools.contains("cmc") }
                            }
                            steps {
                                script {
                                    def RegisterServer_cmc = input(id: 'RegisterServer_cmc', message: 'Automatically Register Server In CMC Success?',
                                            parameters: [
                                                    [$class: 'ChoiceParameterDefinition', description: 'result status', name: 'status', choices: "Success\nFail"],
                                                    string(name:"message",defaultValue:'')
                                            ])

                                    RegisterServer_cmc_result = RegisterServer_cmc
                                    println(RegisterServer_cmc_result)
                                }
                            }
                        }
                        stage("manually fix RegisterServer in cmc") {
                            when {
                                allOf {
                                    expression { params.automate_tools.contains("cmc") }
                                    expression { RegisterServer_cmc_result ==~ /(fail|Fail)/ }
                                }
                            }
                            steps {
                                script {
                                    def RegisterServer_cmc_manual = input(id: 'RegisterServer_cmc_manual', message: 'Already manually Fixed Register Server In CMC?')
                                }
                            }
                        }
                    }
                }
                stage('register server in mct feedback') {

                    stages {
                        stage("register server in mct") {
                            when {
                                expression { params.automate_tools.contains("mct") }
                            }
                            steps {
                                script {
                                    def RegisterServer_mct = input(id: 'RegisterServer_mct', message: 'Automatically Register Server In MCT Success?',
                                            parameters: [
                                                    [$class: 'ChoiceParameterDefinition', description: 'result status', name: 'status', choices: "Success\nFail"],
                                                    string(name:"message",defaultValue:'')
                                            ])

                                    RegisterServer_mct_result = RegisterServer_mct
                                    println(RegisterServer_mct_result)
                                }
                            }
                        }
                        stage("manually fix RegisterServer in mct") {
                            when {
                                allOf{
                                    expression { params.automate_tools.contains("mct") }
                                    expression { RegisterServer_mct_result ==~ /(fail|Fail)/ }
                                }
                            }
                            steps {
                                script {
                                    def RegisterServer_mct_manual = input(id: 'RegisterServer_mct_manual', message: 'Already manually Fixed Register Server In MCT?')
                                }
                            }
                        }
                    }
                }
            }
        }
        //*****register_server end******

        //*****deploy started******
        stage('Automatically Deploy') {
            steps {
                soaTask(
                        task: "Deploy",
                        ackRequired: "mct,cmc",
                        requestId: request_id,
                        instance: slm_instance,
                        automate_tools: params.automate_tools
                )
            }
        }

        stage('Deploy Return') {
            failFast true
            parallel {
                stage('Deploy task in mct feedback') {
                    stages {
                        stage("Deploy task in mct") {
                            when{
                                expression{params.automate_tools.contains("mct")}
                            }
                            steps {
                                script {
                                    def Deploy_mct = input(id: 'Deploy_mct', message: 'Automatically Deploy In MCT Success?',
                                            parameters: [
                                                    [$class: 'ChoiceParameterDefinition', description: 'result status', name: 'status', choices: "Success\nFail"],
                                                    string(name:"message",defaultValue:'')
                                            ])

                                    Deploy_mct_result = Deploy_mct
                                    println(Deploy_mct_result)
                                }
                            }
                        }
                        stage("manually fix Deploy in mct") {
                            when {
                                allOf{
                                    expression{params.automate_tools.contains("mct")}
                                    expression { Deploy_mct_result ==~ /(fail|Fail)/}
                                }

                            }
                            steps {
                                script {
                                    def Deploy_mct_manual = input(id: 'Deploy_mct_manual', message: 'Manually Deploy In MCT?')
                                }
                            }
                        }
                    }
                }
                stage('Deploy task in cmc feedback') {
                    stages {
                        stage("Deploy task in cmc") {
                            when {
                                expression{params.automate_tools.contains("cmc")}
                            }
                            steps {
                                script {
                                    def Deploy_cmc = input(id: 'Deploy_cmc', message: 'Automatically Deploy In CMC Success?',
                                            parameters: [
                                                    [$class: 'ChoiceParameterDefinition', description: 'result status', name: 'status', choices: "Success\nFail"],
                                                    string(name:"message",defaultValue:'')
                                            ])

                                    Deploy_cmc_result = Deploy_cmc
                                    println(Deploy_cmc_result)
                                }
                            }
                        }
                        stage("manually fix Deploy in cmc") {
                            when {
                                allOf {
                                    expression { params.automate_tools.contains("cmc") }
                                    expression { Deploy_cmc_result ==~ /(fail|Fail)/ }
                                }
                            }
                            steps {
                                script {
                                    def Deploy_cmc_manual = input(id: 'Deploy_cmc_manual', message: 'Manually Deploy In CMC?')
                                }
                            }
                        }
                    }
                }

            }
        }

        //*****deploy ended******

        //*****RunDeployPostScript JoinLDAP started******
        stage('RunDeployPostScript') {
            stages {
                stage("Automatically RunDeployPostScript for cmc") {
                    when {
                        expression { params.automate_tools.contains("cmc") }
                        expression { params.component == 'mmp' }
                    }

                    steps {
                        soaTask(
                                task: "RunDeployPostScript",
                                ackRequired: "cmc",
                                requestId: request_id,
                                instance: slm_instance,
                                automate_tools: params.automate_tools
                        )

                        script {
                            RunDeployPostScript_cmc_result = input(id: 'RunDeployPostScript_cmc', message: 'Automatically RunDeployPostScript For CMC Success?',
                                    parameters: [
                                            [$class: 'ChoiceParameterDefinition', description: 'result status', name: 'status', choices: "Success\nFail"],
                                            string(name: "message", defaultValue: '')
                                    ])

                            println(RunDeployPostScript_cmc_result)
                        }
                    }
                }
                stage("manually fix RunDeployPostScript in cmc") {
                    when {
                        allOf {
                            expression { params.automate_tools.contains("cmc") }
                            expression { params.component == 'mmp' }
                            expression { RunDeployPostScript_cmc_result ==~ /(fail|Fail)/ }
                        }
                    }
                    steps {
                        script {
                            def RunDeployPostScript_cmc_manual = input(id: 'RunDeployPostScript_cmc_manual', message: 'Manually RunDeployPostScript In CMC?')
                        }
                    }
                }
            }
        }

        stage('JoinLDAP') {
            stages {
                stage("Automatically JoinLDAP for cmc") {
                    when {
                        expression { params.automate_tools.contains("cmc") }
                    }

                    steps {
                        soaTask(
                                task: "JoinLDAP",
                                ackRequired: "cmc",
                                requestId: request_id,
                                instance: slm_instance,
                                automate_tools: params.automate_tools
                        )

                        script {
                            JoinLDAP_cmc_result = input(id: 'JoinLDAP_cmc', message: 'Automatically  JoinLDAP For CMC Success?',
                                    parameters: [
                                            [$class: 'ChoiceParameterDefinition', description: 'result status', name: 'status', choices: "Success\nFail"],
                                            string(name: "message", defaultValue: '')
                                    ])
                            println(JoinLDAP_cmc_result)
                        }
                    }
                }
                stage("manually fix JoinLDAP in cmc") {
                    when {
                        allOf {
                            expression { params.automate_tools.contains("cmc") }
                            expression { JoinLDAP_cmc_result ==~ /(fail|Fail)/ }
                        }
                    }
                    steps {
                        script {
                            def JoinLDAP_cmc_manual = input(id: 'JoinLDAP_cmc_manual', message: 'Manually JoinLDAP In CMC?')
                        }
                    }
                }
            }
        }

        stage('RunFixit') {
            stages {
                stage("Automatically RunFixit for cmc") {
                    when {
                        expression { params.automate_tools.contains("cmc") }
                    }

                    steps {
                        soaTask(
                                task: "RunFixit",
                                ackRequired: "cmc",
                                requestId: request_id,
                                instance: slm_instance,
                                automate_tools: params.automate_tools
                        )

                        script {
                            RunFixit_cmc_result = input(id: 'RunFixit_cmc', message: 'Automatically RunFixit For CMC Success?',
                                    parameters: [
                                            [$class: 'ChoiceParameterDefinition', description: 'result status', name: 'status', choices: "Success\nFail"],
                                            string(name: "message", defaultValue: '')
                                    ])
                            println(RunFixit_cmc_result)
                        }
                    }
                }
                stage("manually fix RunFixit in cmc") {
                    when {
                        allOf {
                            expression { params.automate_tools.contains("cmc") }
                            expression { RunFixit_cmc_result ==~ /(fail|Fail)/ }
                        }
                    }
                    steps {
                        script {
                            def RunFixit_cmc_manual = input(id: 'RunFixit_cmc_manual', message: 'Manually RunFixit In CMC?')
                        }
                    }
                }
            }
        }
        //*****fixscript end******

        //*****online started******
        stage('Automatically Online') {
            when {
                expression { params.component != 'mmp' }
            }
            steps {
                soaTask(
                        task: "Online",
                        ackRequired: "mct,slim,cmc",
                        requestId: request_id,
                        instance: slm_instance,
                        automate_tools: params.automate_tools
                )
            }
        }

        stage('Online Return') {
            when {
                expression { params.component != 'mmp' }
            }
            failFast true
            parallel {
                stage('Online task in mct feedback') {
                    stages {
                        stage("Online task in mct") {
                            when {
                                expression { params.automate_tools.contains("mct") }
                            }
                            steps {
                                script {
                                    def Online_mct = input(id: 'Online_mct', message: 'Automatically Online In MCT Success?',
                                            parameters: [
                                                    [$class: 'ChoiceParameterDefinition', description: 'result status', name: 'status', choices: "Success\nFail"],
                                                    string(name:"message",defaultValue:'')
                                            ])

                                    Online_mct_result = Online_mct
                                    println(Online_mct_result)
                                }
                            }
                        }
                        stage("manually fix Online in mct") {
                            when {
                                allOf{
                                    expression { params.automate_tools.contains("mct") }
                                    expression { Online_mct_result ==~ /(fail|Fail)/ }
                                }
                            }
                            steps {
                                script {
                                    def Online_mct_manual = input(id: 'Online_mct_manual', message: 'Already manually Online In MCT?')
                                }
                            }
                        }
                    }
                }
                stage('Online task in slim feedback') {
                    stages {
                        stage("Online task in slim") {
                            when {
                                expression { params.automate_tools.contains("slim") }
                            }
                            steps {
                                script {
                                    def Online_slim = input(id: 'Online_slim', message: 'Automatically Online In SLIM Success?',
                                            parameters: [
                                                    [$class: 'ChoiceParameterDefinition', description: 'result status', name: 'status', choices: "Success\nFail"],
                                                    string(name:"message",defaultValue:'')
                                            ])

                                    Online_slim_result = Online_slim
                                    println(Online_slim_result)
                                }
                            }
                        }
                        stage("manually fix Online in slim") {
                            when {
                                allOf{
                                    expression { params.automate_tools.contains("slim") }
                                    expression { Online_slim_result ==~ /(fail|Fail)/ }
                                }
                            }
                            steps {
                                script {
                                    def Online_slim_manual = input(id: 'Online_slim_manual', message: 'Already manually Online In SLIM?')
                                }
                            }
                        }
                    }
                }
                stage('Online task in cmc feedback') {
                    stages {
                        stage("Online task in cmc") {
                            when {
                                expression { params.automate_tools.contains("cmc") }
                            }
                            steps {
                                script {
                                    def Online_cmc = input(id: 'Online_cmc', message: 'Automatically Online In CMC Success?',
                                            parameters: [
                                                    [$class: 'ChoiceParameterDefinition', description: 'result status', name: 'status', choices: "Success\nFail"]
                                            ])

                                    Online_cmc_result = Online_cmc
                                    println(Online_cmc_result)
                                }
                            }
                        }
                        stage("manually fix Online in cmc") {
                            when {
                                allOf{
                                    expression { params.automate_tools.contains("cmc") }
                                    expression { Online_cmc_result ==~ /(fail|Fail)/ }
                                }
                            }
                            steps {
                                script {
                                    def Online_cmc_manual = input(id: 'Online_cmc_manual', message: 'Already manually Online In CMC?')
                                }
                            }
                        }
                    }
                }
            }
        }
        //*****online end******


        stage("end workflow") {
            steps {
                soa_end_workflow(instance: slm_instance,
                        requestId: "${params.request_id}")
            }
        }
    }
}


