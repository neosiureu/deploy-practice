pipeline{
    agent any
    options{
        disableConcurrentBuilds()
    }
    

    triggers{
        pollSCM('H/5 * * * *')
    }

     environment{
        JAR = 'build/libs/deploy-practice-0.0.1-SNAPSHOT.jar'
        DEPLOY_HOST = '172.31.35.98'
        DEPLOY_USER = 'deploy'
        DEPLOY_ENV = '/etc/deploy-practice/deploy.env'
    }

    stages{
        stage('Test'){
            steps{
                sh './gradlew test --no-daemon'
            }
        }
        stage('Build jar'){
            steps{
                sh './gradlew bootJar --no-daemon'
                script{
                    env.JAR_SHA = sh(script: 'sha256sum "$JAR" | cut -d" " -f1', returnStdout: true).trim()
                }
                echo "sha256=${env.JAR_SHA}"
            }
        }

        stage('Archive'){
            steps{
                archiveArtifacts artifacts: 'build/libs/*.jar', excludes: 'build/libs/*-plain.jar', fingerprint: true
            }
        }

                stage('Deploy'){
            input{
                message '이 판을 서버에 배포할까요?'
                ok '배포'
                submitterParameter 'APPROVER'
            }
            steps{
                script{
                    env.VER = "b${env.BUILD_NUMBER}-${env.GIT_COMMIT.substring(0,7)}"
                    env.REMOTE_JAR = "/var/tmp/deploy-practice-${env.VER}.jar"
                }
                echo "deploy version=${env.VER} sha256=${env.JAR_SHA} commit=${env.GIT_COMMIT} approved_by=${env.APPROVER} host=${env.DEPLOY_HOST}"
                sshagent(credentials: ['deploy-ssh']){
                    sh 'scp "$JAR" "$DEPLOY_USER@$DEPLOY_HOST:$REMOTE_JAR"'
                    sh 'ssh "$DEPLOY_USER@$DEPLOY_HOST" /usr/local/bin/deploy.sh "$DEPLOY_ENV" "file://$REMOTE_JAR" "$VER" "$JAR_SHA" "jenkins-$BUILD_NUMBER"'
                }
            }
        }
    }
}