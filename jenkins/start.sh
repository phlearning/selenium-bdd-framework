#!/usr/bin/env bash
# Starts the local Jenkins (http://localhost:8080, user admin, password JENKINS_ADMIN_PASSWORD from .env).
#   jenkins/start.sh                 job on branch main
#   JENKINS_BRANCH=my-branch jenkins/start.sh
set -euo pipefail
cd "$(dirname "$0")"

export JENKINS_DATA="$PWD/.data"
export DOCKER_GID="$(stat -c '%g' /var/run/docker.sock)"
export JENKINS_BRANCH="${JENKINS_BRANCH:-main}"
mkdir -p "$JENKINS_DATA"

docker compose --env-file ../.env up -d --build
echo "Jenkins : http://localhost:8080 (admin / JENKINS_ADMIN_PASSWORD du fichier .env), branche $JENKINS_BRANCH"
