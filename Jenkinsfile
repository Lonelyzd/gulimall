pipeline {
  agent {
    node {
      label 'maven'
    }
  }

  parameters {
      string(name:'PROJECT_VERSIN',defaultValue: 'v0.0Beta',description:'项目版本号')
      string(name:'PROJECT_NAME',defaultValue: '',description:'需要构建的项目')
  }

   environment {
      DOCKER_CREDENTIAL_ID = 'dockerhub-id'
      GITHUB_CREDENTIAL_ID = 'github-id'
      KUBECONFIG_CREDENTIAL_ID = 'demo-kubeconfig'
      REGISTRY = 'docker.io'
      DOCKERHUB_NAMESPACE = 'docker_username'
      GITHUB_ACCOUNT = 'kubesphere'
      APP_NAME = 'devops-java-sample'
      SONAR_CREDENTIAL_ID = 'sonar-token'
  }

  stages {
      stage('拉取代码') {
        steps {
          git(url: 'https://github.com/Lonelyzd/gulimall.git', credentialsId: 'github-id', branch: 'master', changelog: true, poll: false)
          sh 'echo 正在构建 $PROJECT_NAME 版本号 $PROJECT_VERSIN '
        }

      }

      stage('sonarqube代码质量分析') {
        steps {
          container ('maven') {
            withCredentials([string(credentialsId: "$SONAR_CREDENTIAL_ID", variable: 'SONAR_TOKEN')]) {
              withSonarQubeEnv('sonar') {
               sh "echo 当前目录 `pwd`"
               sh "mvn sonar:sonar -o -gs `pwd`/settings.xml -Dsonar.login=$SONAR_TOKEN"
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