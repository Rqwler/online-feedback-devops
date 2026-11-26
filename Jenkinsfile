pipeline {
    agent any

    options {
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                echo "Building ${env.JOB_NAME} #${env.BUILD_NUMBER}"
            }
        }
        stage('Build') {
            steps {
                mvnStep('clean compile')
            }
        }
        stage('Test') {
            steps {
                mvnStep('test')
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
                }
            }
        }
        stage('Package') {
            steps {
                mvnStep('package -DskipTests')
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }
    }

    post {
        success {
            echo 'Pipeline finished successfully.'
        }
        failure {
            echo 'Pipeline failed. Check the stage logs.'
        }
    }
}

def mvnStep(String arguments) {
    if (isUnix()) {
        sh "mvn -B ${arguments}"
    } else {
        bat "mvn -B ${arguments}"
    }
}
