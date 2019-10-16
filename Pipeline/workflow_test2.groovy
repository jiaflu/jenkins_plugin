def slm_instance = "http://10.140.213.237:8090/slm"

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

try {
    parameters {
        //choice(name: 'request_type', choices: ['PoolExpansion','BuildPool'], description: 'Request Type ')
        string(name: 'request_id', defaultValue: '', description: '')
        //string(name: 'automate_tools', defaultValue: 'cmc,slim,gfc,mct', description: 'The CMSE tools which are under automation')
        //string(name: 'component', defaultValue: '', description: 'Request Component')
    }

    stages {
//        stage("Validate Request") {
//            steps {
//                script {
//                    validate_result = soa_validate_req_params(
//                            requestId: "${params.request_id}",
//                            slmUrl: slm_instance
//                    )
//                }
//            }
//        }
//
//        stage("Check Validate Result") {
//            when {
//                expression { validate_result != "Validation pass" }
//            }
//            steps {
//                println("Error Message: " + validate_result)
//                error validate_result
//            }
//        }
//
//        //*****register_pool started******
//
//        stage("Automatically RegisterPool") {
//            when {
//                expression { request_type == 'BuildPool' }
//            }
//            steps {
//                soaTask(
//                        task: "RegisterPool",
//                        ackRequired: "slim,cmc",
//                        requestId: request_id,
//                        instance: slm_instance,
//                        automate_tools: params.automate_tools
//                )
//            }
//        }


        //*****online end******

        stage("end workflow") {
            steps {
                def url = slm_instance + "/workflow/end?request_id=${params.request_id}"
                println(url)
                def response = httpRequest(
                        httpMode: "GET",
                        url: "${params['slm_instance']}/workflow/end?request_id=${params.request_id}",
                        contentType: "APPLICATION_JSON"
                )

                println("Status: "+response.status)
                println("Content: "+response.content)

//                soa_end_workflow(instance: slm_instance,
//                        requestId: "${params.request_id}")
            }
        }
    }
} catch (err) {
    throw err
}
