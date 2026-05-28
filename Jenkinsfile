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
                sh '''
                    for i in {1..20}; do
                      STATUS=$(curl -s http://3.34.56.146:8080/actuator/health | grep UP || true)

                      if [ -n "$STATUS" ]; then
                        echo "Health Check 성공"
                        exit 0
                      fi

                      echo "Health Check 재시도 중... ($i/20)"
                      sleep 5
                    done

                    docker logs spring-app --tail=100

                    exit 1
                '''
            }
        }
    }
}