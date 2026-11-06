pipeline{
    agent any

    triggers{
        pollSCM('H/5 * * * *')
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
            }
        }

        stage('Archive'){
            steps{
                archiveArtifacts artifacts: 'build/libs/*.jar', excludes: 'build/libs/*-plain.jar', fingerprint: true
            }
        }
    }
}