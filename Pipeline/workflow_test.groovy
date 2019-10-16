def slm_instance = "http://10.140.212.227:8090/slm"

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
                    validateUrl = slm_instance + "/request/input/validate?request_id=${params.request_id}"
                    validate_result = httpRequest(
                            httpMode: "GET",
                            url: validateUrl,
                            contentType: "APPLICATION_JSON"
                    )
                    println("soa_validate_req_params: \n" + validateUrl)

                    validate_contentJson = readJSON text: validate_result.content
                    println(validate_contentJson.message)
                }
            }
        }

        stage("Check Validate Result") {
            when {
                expression { validate_contentJson.message != "Validation pass" }
            }
            steps {
                println("Error Message: " + validate_contentJson.message)
                error validate_contentJson.message
            }
        }

        //*****register_pool started******

        stage("Automatically RegisterPool") {
            when {
                expression { request_type == 'BuildPool' }
            }
            steps {
                script {
                    task = "RegisterPool"
                    ackRequired = "slim,cmc"
                    //number="${currentBuild.number}"
                    //automate_tools: params.automate_tools
                    triggerTaskUrl = slm_instance + "/workflow/task?task=" + task + "&request_id=" + "${params.request_id}" + "&ack_required=" + ackRequired + "&automate_tools=" + "${params.automate_tools}" + "&build_number=" + "${currentBuild.number}"

                    httpRequest(
                            httpMode: "GET",
                            url: triggerTaskUrl,
                            contentType: "APPLICATION_JSON"
                    )

                    println("soaTask:\n" + triggerTaskUrl)
                }
//                soaTask(
//                        task: "RegisterPool",
//                        ackRequired: "slim,cmc",
//                        requestId: request_id,
//                        instance: slm_instance,
//                        automate_tools: params.automate_tools
//                )
            }
        }


        //*****online end******

        stage("end workflow") {
            steps {
                script {
                    endWorkflowUrl = slm_instance + "/workflow/end?request_id=${params.request_id}"
                    end_workflow_response = httpRequest(
                            httpMode: "GET",
                            url: endWorkflowUrl,
                            //url: "${params['slm_instance']}/workflow/end?request_id=${params.request_id}",
                            contentType: "APPLICATION_JSON"
                    )
                    println("soa_end_workflow:\n" + endWorkflowUrl)
                    //println("Status: "+end_workflow_response.status)
                    //println("Content: "+end_workflow_response.content)
                }
            }
        }
    }
}