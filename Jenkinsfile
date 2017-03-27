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
            publishHTML(target: [reportDir:'build/reports/codenarc', reportFiles: 'main.html', reportName: 'Codenarc Main'])
            publishHTML(target: [reportDir:'build/reports/codenarc', reportFiles: 'text.html', reportName: 'Codenarc Test'])


        stage 'Unit Tests'
            sh "./gradlew test"

            // publish JUnit results to Jenkins
            step([$class: 'JUnitResultArchiver', testResults: '**/build/test-results/*.xml'])

            // save coverage reports for being processed during code quality phase.
            stash includes: 'build/jacoco/*.exec', name: 'codeCoverage'

        stage 'Coverage'
            unstash 'source'
            unstash 'codeCoverage'
            sh "./gradlew jacocoTestReport"
            publishHTML(target: [reportDir:'build/jacocoHtml', reportFiles: 'index.html', reportName: 'Code Coverage'])

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
