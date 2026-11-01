pipeline {
    agent any

    options {
        disableConcurrentBuilds()
        skipDefaultCheckout(true)
        timestamps()
        timeout(time: 20, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    stages {
        stage('Checkout GitHub') {
            steps {
                deleteDir()
                script {
                    def source = checkout scm
                    env.SOURCE_COMMIT = source.GIT_COMMIT
                    currentBuild.description = "GitHub main: ${env.SOURCE_COMMIT.take(12)}"
                    writeFile file: 'source-commit.txt', text: env.SOURCE_COMMIT + '\n'
                }
                sh 'java -version'
                sh 'git log -1 --format=%H'
            }
        }

        stage('Test and build WAR') {
            steps {
                sh 'sh ./gradlew --no-daemon --max-workers=4 clean test bootWar'
            }
        }

        stage('Deploy to Ubuntu Tomcat') {
            steps {
                withCredentials([sshUserPrivateKey(
                    credentialsId: 'ubuntu-lab-tomcat',
                    keyFileVariable: 'TOMCAT_SSH_KEY',
                    usernameVariable: 'TOMCAT_SSH_USER'
                )]) {
                    sh '''
                        set +x
                        set -eu
                        scp -o BatchMode=yes -o StrictHostKeyChecking=yes \
                            -o UserKnownHostsFile=/var/jenkins_home/.ssh/ubuntu-lab-known-hosts \
                            -i "$TOMCAT_SSH_KEY" build/libs/admin-core.war \
                            "$TOMCAT_SSH_USER@ubuntu-lab:/opt/tomcat/webapps/admin-core.war.upload"
                        ssh -o BatchMode=yes -o StrictHostKeyChecking=yes \
                            -o UserKnownHostsFile=/var/jenkins_home/.ssh/ubuntu-lab-known-hosts \
                            -i "$TOMCAT_SSH_KEY" "$TOMCAT_SSH_USER@ubuntu-lab" '
                                set -eu
                                /usr/local/bin/stop-tomcat-lab
                                mv /opt/tomcat/webapps/admin-core.war.upload /opt/tomcat/webapps/admin-core.war
                                /usr/local/bin/start-tomcat-lab
                            '
                    '''
                }
            }
        }

        stage('Verify application') {
            steps {
                sh '''
                    set -eu
                    attempt=0
                    until curl --fail --silent --show-error --max-time 5 \
                        http://ubuntu-lab:8080/admin-core/actuator/health \
                        > build/tomcat-health.json 2>/dev/null; do
                        attempt=$((attempt + 1))
                        if [ "$attempt" -ge 60 ]; then
                            printf 'Application health did not become ready.\\n' >&2
                            cat build/tomcat-health.json >&2
                            exit 1
                        fi
                        sleep 2
                    done
                    grep -Eq '"status"[[:space:]]*:[[:space:]]*"UP"' build/tomcat-health.json
                    cat build/tomcat-health.json
                '''
            }
        }
    }

    post {
        always {
            junit allowEmptyResults: true, testResults: 'build/test-results/test/*.xml'
        }
        success {
            archiveArtifacts artifacts: 'build/libs/admin-core.war,source-commit.txt,build/tomcat-health.json', fingerprint: true
        }
    }
}
