pipeline {
    agent any

    environment {
        IMAGE_NAME = 'spring-app'
        COMPOSE_FILE = '/home/ubuntu/app/docker-compose.yml'
        CONFIG_DIR = '/home/ubuntu/app/config'
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Docker Build') {
            steps {
                sh 'docker build -t ${IMAGE_NAME}:latest .'
            }
        }

        stage('Prepare Config') {
            steps {
                withCredentials([
                    file(credentialsId: 'application-yaml', variable: 'APP_YML')
                ]) {
                    sh '''
                        mkdir -p ${CONFIG_DIR}

                        cp "$APP_YML" ${CONFIG_DIR}/application.yaml

                        chmod 644 ${CONFIG_DIR}/application.yaml
                    '''
                }
            }
        }

        stage('Deploy') {
            steps {
                sh '''
                    docker compose -f ${COMPOSE_FILE} up -d --force-recreate app
                '''
            }
        }

        stage('Health Check') {
            steps {
                script {
                    def maxRetry = 40
                    def success = false

                    for (int i = 0; i < maxRetry; i++) {
                        def status = sh(
                            script: "curl -s http://localhost:8080/actuator/health | grep UP || true",
                            returnStdout: true
                        ).trim()

                        if (status.contains("UP")) {
                            success = true
                            echo "Health Check 성공"
                            break
                        }

                        echo "Health Check 재시도 중... (${i + 1}/${maxRetry})"
                        sleep 5
                    }

                    if (!success) {
                        sh '''
                            echo "========== Spring App Logs =========="
                            docker logs spring-app --tail=200
                        '''
                        error("Health Check 실패")
                    }
                }
            }
        }
    }
}