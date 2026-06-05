pipeline {
    agent any

    environment {
        IMAGE_NAME = 'spring-app'
        COMPOSE_FILE = '/home/ubuntu/app/docker-compose.yml'
        CONFIG_DIR = '/home/ubuntu/app/config'
        NGINX_UPSTREAM_FILE = '/home/ubuntu/nginx/conf.d/upstream.conf'
        CURRENT_COLOR_FILE = '/home/ubuntu/app/current_color'
        NGINX_CONTAINER = 'nginx'
        SERVER_IP = '3.34.56.146'
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

        stage('Decide Target') {
            steps {
                sh '''
                    if [ ! -f ${CURRENT_COLOR_FILE} ]; then
                        echo blue > ${CURRENT_COLOR_FILE}
                    fi

                    CURRENT_COLOR=$(cat ${CURRENT_COLOR_FILE})

                    if [ "$CURRENT_COLOR" = "blue" ]; then
                        echo green > /tmp/target_color
                        echo 8082 > /tmp/target_port
                        echo app-green > /tmp/target_service
                        echo app-blue > /tmp/old_service
                    else
                        echo blue > /tmp/target_color
                        echo 8080 > /tmp/target_port
                        echo app-blue > /tmp/target_service
                        echo app-green > /tmp/old_service
                    fi

                    echo "Current color: $CURRENT_COLOR"
                    echo "Target color: $(cat /tmp/target_color)"
                    echo "Target port: $(cat /tmp/target_port)"
                    echo "Target service: $(cat /tmp/target_service)"
                '''
            }
        }

        stage('Deploy Target') {
            steps {
                sh '''
                    TARGET_SERVICE=$(cat /tmp/target_service)

                    mkdir -p /home/ubuntu/app/logs/blue /home/ubuntu/app/logs/green
                    chown -R 1000:1000 /home/ubuntu/app/logs || true
                    chmod -R 755 /home/ubuntu/app/logs || true

                    docker compose -f ${COMPOSE_FILE} up -d --force-recreate $TARGET_SERVICE
                '''
            }
        }

        stage('Health Check Target') {
            steps {
                script {
                    def maxRetry = 10
                    def success = false
                    def targetPort = sh(script: "cat /tmp/target_port", returnStdout: true).trim()
                    def targetService = sh(script: "cat /tmp/target_service", returnStdout: true).trim()
                    def targetContainer = targetService == "app-blue" ? "spring-app-blue" : "spring-app-green"

                    for (int i = 0; i < maxRetry; i++) {
                        def status = sh(
                            script: "curl -s http://${SERVER_IP}:${targetPort}/actuator/health | grep UP || true",
                            returnStdout: true
                        ).trim()

                        if (status.contains("UP")) {
                            success = true
                            echo "Health Check 성공: ${targetService}"
                            break
                        }

                        echo "Health Check 재시도 중... (${i + 1}/${maxRetry})"
                        sleep 5
                    }

                    if (!success) {
                        sh """
                            echo "========== ${targetContainer} Logs =========="
                            docker logs ${targetContainer} --tail=200 || true
                        """
                        error("Health Check 실패")
                    }
                }
            }
        }

        stage('Switch Nginx') {
            steps {
                sh '''
                    TARGET_PORT=$(cat /tmp/target_port)

                    cat > ${NGINX_UPSTREAM_FILE} <<EOF
upstream spring_backend {
    server host.docker.internal:${TARGET_PORT};
}
EOF

                    docker exec ${NGINX_CONTAINER} nginx -t
                    docker exec ${NGINX_CONTAINER} nginx -s reload
                '''
            }
        }

        stage('Save Current Color') {
            steps {
                sh '''
                    TARGET_COLOR=$(cat /tmp/target_color)
                    echo $TARGET_COLOR > ${CURRENT_COLOR_FILE}
                    echo "Current color updated to $TARGET_COLOR"
                '''
            }
        }

        stage('Stop Old Container') {
            steps {
                sh '''
                    OLD_SERVICE=$(cat /tmp/old_service)

                    docker compose -f ${COMPOSE_FILE} stop $OLD_SERVICE || true

                    # 최초 전환 때 기존 spring-app이 남아있으면 정리
                    docker stop spring-app || true
                    docker rm spring-app || true
                '''
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
                           "content": "✅ **배포 성공**\\n작성자: ${author}\\n커밋: ${message}\\n실행 시간: ${duration}\\n빌드 번호: #${BUILD_NUMBER}\\nURL: ${BUILD_URL}"
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
                           "content": "❌ **배포 실패**\\n작성자: ${author}\\n커밋: ${message}\\n실행 시간: ${duration}\\n빌드 번호: #${BUILD_NUMBER}\\nURL: ${BUILD_URL}"
                         }' \
                         "$DISCORD_WEBHOOK"
                    """
                }
            }
        }
    }
}