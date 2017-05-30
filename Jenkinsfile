pipeline { 
  agent none
  options {
    skipDefaultCheckout()
  }
  stages {
    stage('Checkout') {
      agent any
      steps {
        checkout scm
        stash(name:'ws', includes:'**')
       }
    }
    stage('Build') {
      agent {
        docker {
          image 'maven:3-alpine'
          args '-v $HOME/.m2:/root/.m2'
        }

         steps {
          echo 'Building'
          unstash 'ws'
          sh './mvnw -B -DskipTests=true clean compile package'
          stash name: 'war', includes: 'target/**/*.war'
         }
       }
    }
    stage('Test') {
      steps {
        echo 'Test'
      }
    }
  }
}
