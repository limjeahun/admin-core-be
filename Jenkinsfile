pipeline {
    agent any

    options {
        disableConcurrentBuilds()
        skipDefaultCheckout(true)
        timestamps()
        timeout(time: 20, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    environment {
        TOMCAT_DEPLOY_ROOT = '/var/jenkins_home/deploy/admin-core-tomcat'
        TOMCAT_HEALTH_URL = 'http://ubuntu-lab:8080/admin-core/actuator/health'
    }

    stages {
        stage('Checkout GitHub') {
            steps {
                deleteDir()
                script {
                    def source = checkout scm
                    if (!(source.GIT_COMMIT ==~ /[0-9a-f]{40}|[0-9a-f]{64}/)) {
                        error('Checkout did not return a complete Git commit ID.')
                    }
                    env.SOURCE_COMMIT = source.GIT_COMMIT
                    currentBuild.description = "GitHub: ${env.SOURCE_COMMIT.take(12)} -> Docker Tomcat"
                    writeFile file: 'source-commit.txt', text: env.SOURCE_COMMIT + '\n'
                }
                sh 'java -version'
                sh 'git log -1 --format=%H'
            }
        }

        stage('Test and build WAR') {
            steps {
                sh 'sh ./gradlew --no-daemon --max-workers=4 --no-build-cache clean test bootWar'
                sh '''
                    set -eu
                    war="$(pwd -P)/build/libs/admin-core.war"
                    test -f "$war" && test ! -L "$war"
                    test "$(realpath -e "$war")" = "$war"
                    jar tf "$war" > build/war-entries.txt
                    grep -Fxq 'WEB-INF/classes/com/espay/admincore/AdminCoreServletInitializer.class' build/war-entries.txt
                    grep -Fxq 'META-INF/MANIFEST.MF' build/war-entries.txt
                    grep -Eq '^WEB-INF/lib/[^/]+[.]jar$' build/war-entries.txt
                    sha256sum build/libs/admin-core.war > build/admin-core.war.sha256
                '''
            }
        }

        stage('Deploy to Docker Tomcat') {
            steps {
                sh '''
                    set -eu
                    webapps="$TOMCAT_DEPLOY_ROOT/webapps"
                    staging="$TOMCAT_DEPLOY_ROOT/staging"
                    backups="$TOMCAT_DEPLOY_ROOT/backups"
                    target="$webapps/admin-core.war"
                    previous="$backups/previous-admin-core.war"
                    upload=''
                    backup_upload=''
                    cleanup() {
                        test -z "$upload" || rm -f -- "$upload"
                        test -z "$backup_upload" || rm -f -- "$backup_upload"
                    }
                    trap cleanup EXIT

                    # Only webapps is mounted into ubuntu-lab. Staging and backups
                    # remain outside the application's mount, on the same volume.
                    test -d "$TOMCAT_DEPLOY_ROOT" && test ! -L "$TOMCAT_DEPLOY_ROOT"
                    test "$(realpath -e "$TOMCAT_DEPLOY_ROOT")" = "$TOMCAT_DEPLOY_ROOT"
                    test -d "$webapps" && test ! -L "$webapps"
                    test "$(realpath -e "$webapps")" = "$webapps"
                    for directory in "$staging" "$backups"; do
                        test ! -L "$directory"
                        if test ! -d "$directory"; then
                            mkdir -m 0700 -- "$directory"
                        fi
                        test "$(realpath -e "$directory")" = "$directory"
                        test "$(stat -c '%u' "$directory")" = "$(id -u)"
                        test "$(stat -c '%d' "$directory")" = "$(stat -c '%d' "$webapps")"
                    done
                    test -w "$webapps"

                    upload=$(mktemp "$staging/admin-core.XXXXXXXX.war.upload")
                    cp -P -- build/libs/admin-core.war "$upload"
                    test -f "$upload" && test ! -L "$upload"
                    chmod 0640 -- "$upload"
                    expected_sha=$(cut -d ' ' -f 1 build/admin-core.war.sha256)
                    test "$(sha256sum "$upload" | cut -d ' ' -f 1)" = "$expected_sha"

                    if test -e "$target" || test -L "$target"; then
                        test -f "$target" && test ! -L "$target"
                        test "$(realpath -e "$target")" = "$target"
                        backup_upload=$(mktemp "$backups/previous.XXXXXXXX.war.upload")
                        # -P copies a raced source symlink instead of following it.
                        cp -P -- "$target" "$backup_upload"
                        test -f "$backup_upload" && test ! -L "$backup_upload"
                        chmod 0600 -- "$backup_upload"
                        mv -fT -- "$backup_upload" "$previous"
                        backup_upload=''
                    fi

                    # autoDeploy=true must observe removal before the new WAR
                    # appears, so an old healthy context cannot pass this build.
                    rm -f -- "$target"
                    attempt=0
                    while :; do
                        code=$(curl --silent --connect-timeout 2 --max-time 5 \
                            -o /dev/null -w '%{http_code}' "$TOMCAT_HEALTH_URL" || true)
                        test "$code" != 404 || break
                        attempt=$((attempt + 1))
                        if test "$attempt" -ge 30; then
                            printf 'Previous application did not undeploy to HTTP 404. Check Tomcat autoDeploy.\\n' >&2
                            exit 1
                        fi
                        sleep 2
                    done
                    mv -fT -- "$upload" "$target"
                    upload=''
                '''
            }
        }

        stage('Verify application') {
            steps {
                timeout(time: 180, unit: 'SECONDS') {
                    sh '''
                        set -eu
                        until curl --fail --silent --connect-timeout 2 --max-time 5 \
                            "$TOMCAT_HEALTH_URL" > build/tomcat-health.json && \
                            grep -Eq '^[[:space:]]*[{][[:space:]]*"status"[[:space:]]*:[[:space:]]*"UP"[[:space:]]*[,}]' build/tomcat-health.json; do
                            sleep 2
                        done
                        cat build/tomcat-health.json
                        cat build/admin-core.war.sha256
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
            archiveArtifacts artifacts: 'build/libs/admin-core.war,source-commit.txt,build/admin-core.war.sha256,build/tomcat-health.json', fingerprint: true
        }
    }
}
