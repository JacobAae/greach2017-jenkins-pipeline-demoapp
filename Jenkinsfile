node {
    stage "checkout"
        // get source code
        checkout scm

        // Respect ci-skip message
        def lastMessage = sh(returnStdout: true, script:'git log --format=%s -1')
        if (lastMessage.contains('ci-skip') ) {
            currentBuild.result = 'ABORTED'
            //error("Not building due to ci-skip")
        }
}

if( currentBuild.result == 'ABORTED') {
    return
}

node {

    try {
        stage 'Clean'
            sh "./gradlew clean"
            // save source code so we don't need to get it every time and also avoids conflicts
            stash excludes: 'build/', includes: '**', name: 'source'

        stage 'Static Code Analysis'
            unstash 'source'
            sh "./gradlew codenarcMain codenarcTest"
            publishHTML(target: [allowMissing: false, alwaysLinkToLastBuild: false, keepAll: true, reportDir:'build/reports/codenarc', reportFiles: 'main.html', reportName: 'Codenarc Main'])
            publishHTML(target: [allowMissing: false, alwaysLinkToLastBuild: false, keepAll: true, reportDir:'build/reports/codenarc', reportFiles: 'test.html', reportName: 'Codenarc Test'])


        stage 'Unit Tests'
            sh "./gradlew test"

            // publish JUnit results to Jenkins
            step([$class: 'JUnitResultArchiver', testResults: '**/build/test-results/test/*.xml'])

            // save coverage reports for being processed during code quality phase.
            stash includes: 'build/jacoco/*.exec', name: 'codeCoverage'

        stage 'Coverage'
            unstash 'source'
            unstash 'codeCoverage'
            sh "./gradlew jacocoTestReport"
            publishHTML(target: [reportDir:'build/reports/jacoco', reportFiles: 'index.html', reportName: 'Code Coverage'])

    } catch( Exception e) {
        currentBuild.result = "FAILURE"

        mail body: "Project build error: ${e}" ,
            from: 'jenkins@greachconf.com',
            replyTo: 'jenkins@greachconf.com',
            subject: "Initial test/coverage or codenarc failed for ${env.JOB_NAME} - Build # ${env.BUILD_NUMBER}",
            to: "${env.CHANGE_AUTHOR_EMAIL}"

        throw e
    }
}

stage 'Docker Release?'
def releaseType
timeout(time:2, unit:'HOURS') {
    inputValue = input message: 'Release Docker Image?', ok: 'Yes', submitterParameter: 'approver', parameters: [[$class: 'ChoiceParameterDefinition', choices: 'Minor\nPatch\nMajor', description: 'Major,Minor or Patch', name: 'releaseType']]
    echo "${inputValue.approver} ${inputValue.releaseType}"
}

node {
    stage 'Release and Push'
            unstash 'source'
            sh "git checkout ${env.BRANCH_NAME} && git pull" // Need to be on actual branch, not in deteached head state

            switch( releaseType ) {
                case 'Patch':
                    sh "./gradlew releasePatch"
                    break
                case 'Minor':
                    sh "./gradlew releaseMinor"
                    break
                case 'Major':
                    sh "./gradlew releaseMajor"
                    break
                default:
                    currentBuild.result = "FAILURE"
            }

    stage 'Deploy'
            sh "./gradlew release${releaseType}"

    stage 'Wait til ready'
            sh "./gradlew waitForDeploy"
            sleep 5 // Wait for application to startup after deployment

    stage 'Integration Tests'
        def integrationTestResult = build job: '/demoapp-integration-tests/master', wait: true, propagate: false
        echo "Result: ${integrationTestResult.result}"

    stage 'Finalize'
        if( integrationTestResult.result == 'SUCCESS' ) {
            echo "Tests passed - finishing upgrade"
            sh "./gradlew -i approveDeploy"
        } else {
            echo "Tests failed with status ${integrationTestResult.result}"
            sh "./gradlew -i rollbackDeploy"

            currentBuild.result = "FAILURE"
            // TODO Send error message
        }
}





