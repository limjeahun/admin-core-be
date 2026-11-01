pipeline {
    agent any

    options {
        disableConcurrentBuilds()
        skipDefaultCheckout(true)
        timestamps()
        timeout(time: 20, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    parameters {
        string(name: 'TOMCAT_SSH_HOST', defaultValue: 'ubuntu-server-lab.mshome.net', trim: true,
            description: 'VM SSH host reachable from the Jenkins container')
        string(name: 'TOMCAT_SSH_PORT', defaultValue: '22', trim: true,
            description: 'Ubuntu VM SSH port (direct: 22; optional Windows forward: 2222)')
    }

    environment {
        TOMCAT_KNOWN_HOSTS = '/var/jenkins_home/.ssh/ubuntu-vm-known-hosts'
    }

    stages {
        stage('Validate VM target') {
            steps {
                script {
                    def host = params.TOMCAT_SSH_HOST.trim()
                    def port = params.TOMCAT_SSH_PORT.trim()
                    if (!(host ==~ /[A-Za-z0-9][A-Za-z0-9.-]{0,252}/)) {
                        error('TOMCAT_SSH_HOST must be a DNS name or IPv4 address.')
                    }
                    if (!(port ==~ /[0-9]{1,5}/) || port.toInteger() < 1 || port.toInteger() > 65535) {
                        error('TOMCAT_SSH_PORT must be between 1 and 65535.')
                    }
                    env.TOMCAT_SSH_HOST = host
                    env.TOMCAT_SSH_PORT = port.toInteger().toString()
                }
                sh 'test -s "$TOMCAT_KNOWN_HOSTS"'
            }
        }

        stage('Checkout GitHub') {
            steps {
                deleteDir()
                script {
                    def source = checkout scm
                    if (!(source.GIT_COMMIT ==~ /[0-9a-f]{40}|[0-9a-f]{64}/)) {
                        error('Checkout did not return a complete Git commit ID.')
                    }
                    env.SOURCE_COMMIT = source.GIT_COMMIT
                    currentBuild.description = "GitHub: ${env.SOURCE_COMMIT.take(12)} -> Ubuntu VM"
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

        stage('Deploy to VM Tomcat') {
            steps {
                withCredentials([sshUserPrivateKey(
                    credentialsId: 'ubuntu-vm-tomcat',
                    keyFileVariable: 'VM_SSH_KEY',
                    usernameVariable: 'VM_SSH_USER'
                )]) {
                    sh '''
                        set +x
                        set -eu
                        test "$VM_SSH_USER" = deploy
                        case "$BUILD_NUMBER" in ''|*[!0-9]*) exit 1 ;; esac
                        upload_name="admin-core-${SOURCE_COMMIT}-${BUILD_NUMBER}.war.upload"
                        war_sha=$(sha256sum build/libs/admin-core.war | cut -d ' ' -f 1)
                        scp -P "$TOMCAT_SSH_PORT" -o BatchMode=yes -o ConnectTimeout=10 \
                            -o StrictHostKeyChecking=yes -o UserKnownHostsFile="$TOMCAT_KNOWN_HOSTS" \
                            -i "$VM_SSH_KEY" build/libs/admin-core.war \
                            "$VM_SSH_USER@$TOMCAT_SSH_HOST:/var/lib/admin-core-deploy/incoming/$upload_name"
                        ssh -p "$TOMCAT_SSH_PORT" -o BatchMode=yes -o ConnectTimeout=10 \
                            -o StrictHostKeyChecking=yes -o UserKnownHostsFile="$TOMCAT_KNOWN_HOSTS" \
                            -i "$VM_SSH_KEY" "$VM_SSH_USER@$TOMCAT_SSH_HOST" \
                            "sudo -n /usr/local/sbin/deploy-admin-core '$upload_name' '$war_sha'" \
                            > build/tomcat-health.json
                        grep -Eq '"status"[[:space:]]*:[[:space:]]*"UP"' build/tomcat-health.json
                        cat build/tomcat-health.json
                    '''
                }
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
