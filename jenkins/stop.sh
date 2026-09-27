#!/usr/bin/env bash
# Stops the local Jenkins started by jenkins/start.sh. Data stays in jenkins/.data/.
#   jenkins/stop.sh           stop and remove the container
#   jenkins/stop.sh --purge   also delete jenkins/.data/ (next start begins from scratch)
set -euo pipefail
cd "$(dirname "$0")"

# docker-compose.yml requires these variables even to stop: same values as start.sh
export JENKINS_DATA="$PWD/.data"
export DOCKER_GID="$(stat -c '%g' /var/run/docker.sock)"

docker compose --env-file ../.env down
# Test stack of an interrupted build, if any
docker compose -p bdd-jenkins down --remove-orphans 2>/dev/null || true

if [[ "${1:-}" == "--purge" ]]; then
    # Files written by Grid containers (videos) belong to another user: remove them from a container.
    docker run --rm -v "$PWD:/jenkins" alpine rm -rf /jenkins/.data
    echo "Données Jenkins supprimées."
fi
