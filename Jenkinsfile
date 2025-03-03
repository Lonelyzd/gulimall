pipeline {
  agent {
    node {
      label 'maven'
    }
  }

  parameters {
      string(name:'PROJECT_VERSIN',defaultValue: 'defaultVersin',description:'项目版本号')
      string(name:'PROJECT_NAME',defaultValue: 'gulimall-gateway',description:'需要构建的项目')
  }

   environment {
      DOCKER_CREDENTIAL_ID = 'aliyun-hub-id'
      GITHUB_CREDENTIAL_ID = 'github-id'
      KUBECONFIG_CREDENTIAL_ID = 'demo-kubeconfig'
      REGISTRY = 'registry.cn-hangzhou.aliyuncs.com'
      DOCKERHUB_NAMESPACE = 'ice-gulimall'
      GITHUB_ACCOUNT = 'kubesphere'
      APP_NAME = 'devops-java-sample'
      SONAR_CREDENTIAL_ID = 'sonar-token'
  }

  stages {
      stage('拉取代码') {
        steps {
          git(url: 'https://github.com/Lonelyzd/gulimall.git', credentialsId: 'github-id', branch: 'master', changelog: true, poll: false)
          sh 'echo 正在构建 $PROJECT_NAME 版本号 $PROJECT_VERSIN '

          container ('maven') {
             sh 'echo 正在完整编译项目.... '
             sh 'mvn clean install -gs settings.xml -Dmaven.test.skip=true'
          }
        }
      }

      stage ('构建镜像-推送镜像') {
          steps {
              container ('maven') {
                  sh 'mvn  -Dmaven.test.skip=true -gs `pwd`/settings.xml clean package'
                  sh 'cd $PROJECT_NAME &&  docker build -f Dockerfile -t $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:SNAPSHOT-$BRANCH_NAME-$BUILD_NUMBER .'
                  withCredentials([usernamePassword(passwordVariable : 'DOCKER_PASSWORD' ,usernameVariable : 'DOCKER_USERNAME' ,credentialsId : "$DOCKER_CREDENTIAL_ID" ,)]) {
                      sh 'echo "$DOCKER_PASSWORD" | docker login $REGISTRY -u "$DOCKER_USERNAME" --password-stdin'
                      sh 'docker tag  $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:SNAPSHOT-$BRANCH_NAME-$BUILD_NUMBER $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:latest '
                      sh 'docker push  $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:latest '
                  }
              }
          }
      }

      stage('部署到k8s') {
        when{
          branch 'master'
        }
        steps {
          input(id: "deploy-to-dev-$PROJECT_NAME", message: "是否将$PROJECT_NAME 部署到集群中?")
          kubernetesDeploy(configs: "$PROJECT_NAME/deploy/**", enableConfigSubstitution: true, kubeconfigId: "$KUBECONFIG_CREDENTIAL_ID")
        }
      }

      stage('发布版本'){
        when{
          expression{
            return params.PROJECT_VERSIN =~ /v.*/
          }
        }
        steps {
            container ('maven') {
              input(id: 'release-image-with-tag', message: '发布当前版本镜像吗?')
                withCredentials([usernamePassword(credentialsId: "$GITHUB_CREDENTIAL_ID", passwordVariable: 'GIT_PASSWORD', usernameVariable: 'GIT_USERNAME')]) {
                  sh 'git config --global user.email "iceblue.top@foxmail.com" '
                  sh 'git config --global user.name "Lonelyzd" '
                  sh 'git tag -a $PROJECT_VERSIN -m "$PROJECT_VERSIN" '
                  sh 'git push http://$GIT_USERNAME:$GIT_PASSWORD@github.com/$GITHUB_ACCOUNT/gulimall.git --tags --ipv4'
                }
              sh 'docker tag  $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:SNAPSHOT-$BRANCH_NAME-$BUILD_NUMBER $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:$PROJECT_VERSIN '
              sh 'docker push  $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:$PROJECT_VERSIN '
        }
        }
      }

  }
}