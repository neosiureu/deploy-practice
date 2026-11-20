pipeline {
    agent none

    options {
        disableConcurrentBuilds()
    }

    triggers {
        pollSCM('H/5 * * * *')
    }

    environment {
        JAR = 'build/libs/deploy-practice-0.0.1-SNAPSHOT.jar'
        DEPLOY_HOST = '172.31.35.98'
        DEPLOY_USER = 'deploy'
        DEPLOY_ENV = '/etc/deploy-practice/deploy.env'
    }

    stages {
        stage('Build') {
            agent {
                kubernetes {
                    cloud 'k3s'
                    defaultContainer 'build'
                    yaml '''
                        apiVersion: v1
                        kind: Pod
                        spec:
                          securityContext:
                            runAsUser: 1000
                          containers:
                          - name: jnlp
                            resources:
                              requests:
                                cpu: 100m
                                memory: 256Mi
                              limits:
                                cpu: 500m
                                memory: 512Mi
                          - name: build
                            image: eclipse-temurin:21-jdk-noble
                            imagePullPolicy: IfNotPresent
                            command: ["sleep"]
                            args: ["infinity"]
                            env:
                            - name: GRADLE_USER_HOME
                              value: /cache/gradle
                            resources:
                              requests:
                                cpu: 500m
                                memory: 1Gi
                              limits:
                                cpu: "2"
                                memory: 1536Mi
                            volumeMounts:
                            - name: gradle-cache
                              mountPath: /cache
                          volumes:
                          - name: gradle-cache
                            persistentVolumeClaim:
                              claimName: gradle-cache
                    '''
                }
            }
            stages {
                stage('Test') {
                    steps {
                        sh './gradlew test --no-daemon'
                    }
                }
                stage('Build jar') {
                    steps {
                        sh './gradlew bootJar --no-daemon'
                        script {
                            env.JAR_SHA = sh(script: 'sha256sum "$JAR" | cut -d" " -f1', returnStdout: true).trim()
                            env.COMMIT = env.GIT_COMMIT
                        }
                        echo "sha256=${env.JAR_SHA}"
                    }
                }
                stage('Archive') {
                    steps {
                        archiveArtifacts artifacts: 'build/libs/*.jar', excludes: 'build/libs/*-plain.jar', fingerprint: true
                        stash name: 'jar', includes: env.JAR
                    }
                }
            }
        }

        stage('Deploy') {
            agent { label 'built-in' }
            options {
                skipDefaultCheckout()
            }
            input {
                message '이 판을 서버에 배포할까요?'
                ok '배포'
                submitterParameter 'APPROVER'
            }
            steps {
                sh 'rm -f deploy-record.txt deploy-out.txt'
                unstash 'jar'
                script {
                    env.VER = "b${env.BUILD_NUMBER}-${env.COMMIT.substring(0,7)}"
                    env.REMOTE_JAR = "/var/tmp/deploy-practice-${env.VER}.jar"
                }
                sshagent(credentials: ['deploy-ssh']) {
                    sh '''
                        started=$(date -Is)
                        rc=0
                        scp "$JAR" "$DEPLOY_USER@$DEPLOY_HOST:$REMOTE_JAR" > deploy-out.txt 2>&1 || rc=$?
                        if [ "$rc" -eq 0 ]; then
                            ssh "$DEPLOY_USER@$DEPLOY_HOST" /usr/local/bin/deploy.sh "$DEPLOY_ENV" "file://$REMOTE_JAR" "$VER" "$JAR_SHA" "jenkins-$BUILD_NUMBER" >> deploy-out.txt 2>&1 || rc=$?
                        fi
                        ended=$(date -Is)
                        cat deploy-out.txt
                        result=$(grep '^result ' deploy-out.txt | tail -n 1 | cut -d' ' -f2-)
                        {
                            echo "what:   version=$VER commit=$COMMIT sha256=$JAR_SHA"
                            echo "where:  host=$DEPLOY_HOST env=$DEPLOY_ENV"
                            echo "who:    approver=$APPROVER build=$BUILD_URL"
                            echo "result: rc=$rc ${result:-none}"
                            echo "when:   started=$started ended=$ended"
                        } > deploy-record.txt
                        cat deploy-record.txt
                        exit $rc
                    '''
                }
            }
            post {
                always {
                    archiveArtifacts artifacts: 'deploy-record.txt', allowEmptyArchive: true
                }
            }
        }
    }
}