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
                    def maxRetry = 10
                    def success = false

                    for (int i = 0; i < maxRetry; i++) {
                        def status = sh(
                            script: "curl -s http://3.34.56.146:8080/actuator/health | grep UP || true",
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

    post {
        success {
            script {
                def author = sh(script: "git log -1 --pretty=format:'%an'", returnStdout: true).trim()
                def message = sh(script: "git log -1 --pretty=format:'%s'", returnStdout: true).trim()
                def duration = currentBuild.durationString.replace(' and counting', '')

                withCredentials([string(credentialsId: 'discord-webhook', variable: 'DISCORD_WEBHOOK')]) {
                    sh """
                    curl -H "Content-Type: application/json" \
                         -X POST \
                         -d '{
                           "content": "✅ **배포 성공**\\\\n작성자: ${author}\\\\n커밋: ${message}\\\\n실행 시간: ${duration}\\\\n빌드 번호: #${BUILD_NUMBER}\\\\nURL: ${BUILD_URL}"
                         }' \
                         "$DISCORD_WEBHOOK"
                    """
                }
            }
        }

        failure {
            script {
                def author = sh(script: "git log -1 --pretty=format:'%an' || echo unknown", returnStdout: true).trim()
                def message = sh(script: "git log -1 --pretty=format:'%s' || echo unknown", returnStdout: true).trim()
                def duration = currentBuild.durationString.replace(' and counting', '')

                withCredentials([string(credentialsId: 'discord-webhook', variable: 'DISCORD_WEBHOOK')]) {
                    sh """
                    curl -H "Content-Type: application/json" \
                         -X POST \
                         -d '{
                           "content": "❌ **배포 실패**\\\\n작성자: ${author}\\\\n커밋: ${message}\\\\n실행 시간: ${duration}\\\\n빌드 번호: #${BUILD_NUMBER}\\\\nURL: ${BUILD_URL}"
                         }' \
                         "$DISCORD_WEBHOOK"
                    """
                }
            }
        }
    }
}