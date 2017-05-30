pipeline {
  agent any
  stages {
    stage('Checkout') {
      agent any
      steps {
        checkout scm
        stash(name:'ws', includes:'**')
       }
    }
    stage('Build') {
      steps {
        echo 'Building'
      }
    }
    stage('Test') {
      steps {
        echo 'Test'
      }
    }
  }
}
