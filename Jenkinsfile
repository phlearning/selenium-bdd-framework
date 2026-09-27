// Jenkins equivalent of the GitHub Actions workflows (.github/workflows):
//   - every change (SCM polling): quality checks, then @smoke
//   - every night: full regression, one build per browser, with videos
//   - on demand: "Build with Parameters"
// Browsers always run on the Selenium Grid started by docker compose: the Jenkins agent
// needs Java 21 and Docker, no browser. Test it locally with jenkins/start.sh (jenkins/README.md).

pipeline {
    agent any

    parameters {
        choice(name: 'SUITE', choices: ['smoke', 'regression'],
               description: 'smoke : @smoke ; regression : tout sauf @wip')
        string(name: 'TAGS', defaultValue: '',
               description: 'Expression de tags Cucumber (remplace celle de SUITE si renseignée)')
        choice(name: 'BROWSER', choices: ['chrome', 'firefox'], description: 'Navigateur')
        booleanParam(name: 'VIDEO', defaultValue: false,
                     description: 'Vidéos des sessions (jointes au rapport Allure des scénarios en échec)')
        string(name: 'THREADS', defaultValue: '2', description: 'Scénarios en parallèle')
        string(name: 'TEST_ENV', defaultValue: 'demo', description: 'Environnement cible')
    }

    triggers {
        pollSCM('H/5 * * * *')
        parameterizedCron('''
            H 2 * * * %SUITE=regression;BROWSER=chrome;VIDEO=true;THREADS=3
            H 3 * * * %SUITE=regression;BROWSER=firefox;VIDEO=true;THREADS=3
        ''')
    }

    options {
        timestamps()
        ansiColor('xterm')
        timeout(time: 45, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '30'))
        // One build at a time: builds share the published ports of the Grid stack.
        disableConcurrentBuilds()
    }

    environment {
        SAUCE_USERNAME = credentials('sauce-username')
        SAUCE_PASSWORD = credentials('sauce-password')
        DUMMYJSON_USERNAME = credentials('dummyjson-username')
        DUMMYJSON_PASSWORD = credentials('dummyjson-password')

        // Stack of this pipeline, isolated from a developer's stack on the same Docker host
        COMPOSE_PROJECT_NAME = 'bdd-jenkins'
        COMPOSE_PROFILES = 'grid'
        GRID_PORT = '14444'
        THE_INTERNET_PORT = '17080'
        GRID_URL = "http://host.docker.internal:${GRID_PORT}"
        // Seen from the Grid browsers, through the Docker network
        THE_INTERNET_URL = 'http://the-internet:5000'
    }

    stages {
        stage('Préparation') {
            steps {
                script {
                    // params are empty on the very first build (Jenkins only learns them by running
                    // this file) and on SCM-triggered builds: fall back to the declared defaults.
                    String suite = params.SUITE ?: 'smoke'
                    env.RUN_TAGS = params.TAGS?.trim() ?: (suite == 'smoke' ? '@smoke' : 'not @wip')
                    env.RUN_BROWSER = params.BROWSER ?: 'chrome'
                    env.RUN_VIDEO = (params.VIDEO ?: false).toString()
                    env.RUN_THREADS = params.THREADS?.trim() ?: '2'
                    env.RUN_ENV = params.TEST_ENV?.trim() ?: 'demo'
                    currentBuild.displayName = "#${env.BUILD_NUMBER} ${suite} ${env.RUN_BROWSER}"
                    currentBuild.description = "tags: ${env.RUN_TAGS}"
                }
            }
        }

        stage('Qualité') {
            steps {
                sh './mvnw -B spotless:check checkstyle:check test-compile'
            }
        }

        stage('Environnement de test') {
            steps {
                // One session per node, plus one for scenarios that drive two browsers
                sh '''
                    mkdir -p .grid/videos && chmod 777 .grid/videos
                    export CHROME_NODES=$((RUN_THREADS + 1)) FIREFOX_NODES=$((RUN_THREADS + 1))
                    docker compose up -d --wait the-internet selenium-hub "$RUN_BROWSER"
                '''
            }
        }

        stage('Tests') {
            steps {
                sh '''
                    ./mvnw -B test \
                        -Denv="$RUN_ENV" \
                        -Dbrowser="$RUN_BROWSER" \
                        -Dexecution=grid \
                        -Dgrid.url="$GRID_URL" \
                        -Dheadless=false \
                        -Dvideo="$RUN_VIDEO" \
                        -Dthreads="$RUN_THREADS" \
                        -Dcucumber.filter.tags="$RUN_TAGS"
                '''
            }
        }
    }

    post {
        always {
            // Both passes are shown; the build status comes from Maven (the rerun decides),
            // so failures of the first pass must not turn the build unstable.
            junit(testResults: 'target/surefire-reports/TEST-*.xml, target/surefire-reports-rerun/TEST-*.xml',
                  allowEmptyResults: true, skipMarkingBuildUnstable: true)
            allure(results: [[path: 'target/allure-results']])
            archiveArtifacts(artifacts: 'target/logs/**, target/cucumber-reports/**, target/rerun*.txt',
                             allowEmptyArchive: true)
        }
        failure {
            sh 'docker compose logs --no-color --tail 200 || true'
        }
        cleanup {
            sh 'docker compose down --remove-orphans || true'
        }
    }
}
