pipeline {
  agent {
    node {
      label 'maven'
    }
  }

  parameters {
    string(name: 'PROJECT_VERSION', defaultValue: 'v0.0Beta', description: '')
    string(name: 'PROJECT_NAME', defaultValue: '', description: '')
  }

  environment {
    DOCKER_CREDENTIAL_ID = 'dockerhub-id'
    GITHUB_CREDENTIAL_ID = 'github-id'
    KUBECONFIG_CREDENTIAL_ID = 'demo-kubeconfig'
    REGISTRY = 'docker.io'
    DOCKERHUB_NAMESPACE = 'st9360606'
    GITHUB_ACCOUNT = 'st9360606'
    SONAR_CREDENTIAL_ID = 'sonar-qube'
    BRANCH_NAME = 'master'
  }

   stages {
      stage('拉取代碼') {
        steps {
          git(url: 'https://github.com/st9360606/gulimall.git', credentialsId: 'github-id', branch: 'master', changelog: true, poll: false)
          sh 'echo 正在構建 $PROJECT_NAME  版本號: $PROJECT_VERSION 將會提交給 $REGISTRY 鏡像倉庫'
          container ('maven') {
            sh "mvn clean install -Dmaven.test.skip=true -gs `pwd`/mvn-settings.xml"
          }
        }
      }

      stage('sonarqube 代碼質量分析') {
        steps {
          container ('maven') {
            withCredentials([string(credentialsId: "$SONAR_CREDENTIAL_ID", variable: 'SONAR_TOKEN')]) {
              withSonarQubeEnv('sonar') {
               sh "echo 當前目錄 `pwd`"
               sh "mvn sonar:sonar -gs `pwd`/mvn-settings.xml -Dsonar.branch=$BRANCH_NAME -Dsonar.login=$SONAR_TOKEN"
              }
            }
            timeout(time: 1, unit: 'HOURS') {
              waitForQualityGate abortPipeline: true
            }
          }
        }
      }
   }
}