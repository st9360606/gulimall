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
    GITEE_CREDENTIAL_ID = 'gitee-id'
    KUBECONFIG_CREDENTIAL_ID = 'demo-kubeconfig'
    REGISTRY = 'docker.io'
    DOCKERHUB_NAMESPACE = 'st9360606'
    GITHUB_ACCOUNT = 'st9360606'
    GITEE_ACCOUNT = 'st9360606%40gmail.com'
    SONAR_CREDENTIAL_ID = 'sonar-qube'
  }

  stages {
    stage('拉取代碼') {
      steps {
        git(url: 'https://github.com/st9360606/gulimall.git', credentialsId: 'github-id', branch: 'master', changelog: true, poll: false)
        sh 'echo 正在構建 $PROJECT_NAME  版本號: $PROJECT_VERSION'
      }
    }
  }

}