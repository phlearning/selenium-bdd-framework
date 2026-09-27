# Local Jenkins to run and test the Jenkinsfile (see jenkins/README.md).
FROM jenkins/jenkins:2.568.3-lts-jdk21

USER root
# Docker CLI + compose plugin: the pipeline starts the application under test and the
# Selenium Grid on the host's Docker daemon (socket mounted by jenkins/docker-compose.yml).
RUN apt-get update \
    && apt-get install -y --no-install-recommends ca-certificates curl gnupg \
    && install -m 0755 -d /etc/apt/keyrings \
    && curl -fsSL https://download.docker.com/linux/debian/gpg -o /etc/apt/keyrings/docker.asc \
    && echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/debian $(. /etc/os-release && echo "$VERSION_CODENAME") stable" \
        > /etc/apt/sources.list.d/docker.list \
    && apt-get update \
    && apt-get install -y --no-install-recommends docker-ce-cli docker-compose-plugin \
    && rm -rf /var/lib/apt/lists/*

# Allure CLI, same version as the pom's allure.report.version; declared as a tool in casc.yaml.
ARG ALLURE_VERSION=2.46.1
RUN apt-get update && apt-get install -y --no-install-recommends unzip && rm -rf /var/lib/apt/lists/* \
    && curl -fsSL -o /tmp/allure.zip \
        "https://repo1.maven.org/maven2/io/qameta/allure/allure-commandline/${ALLURE_VERSION}/allure-commandline-${ALLURE_VERSION}.zip" \
    && unzip -q /tmp/allure.zip -d /opt && mv /opt/allure-${ALLURE_VERSION} /opt/allure && rm /tmp/allure.zip
USER jenkins

COPY --chown=jenkins:jenkins plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN jenkins-plugin-cli --plugin-file /usr/share/jenkins/ref/plugins.txt

COPY --chown=jenkins:jenkins casc.yaml /usr/share/jenkins/ref/casc.yaml
ENV CASC_JENKINS_CONFIG=/usr/share/jenkins/ref/casc.yaml
# No setup wizard (configuration as code); allow the job to clone the repository mounted at /repo.
ENV JAVA_OPTS="-Djenkins.install.runSetupWizard=false -Dhudson.plugins.git.GitSCM.ALLOW_LOCAL_CHECKOUT=true"
