# Commandes pour tester le projet

Toutes les commandes se lancent depuis la racine du dépôt, par exemple après un clone ou un nettoyage complet.

## 0. Repartir d'un dépôt propre

`git clean -fxd` supprime aussi les fichiers ignorés par Git, dont `.env` (identifiants, mot de passe Jenkins) et
`CLAUDE.local.md`. Pour les garder :

```bash
git clean -fxd -e .env -e CLAUDE.local.md
```

Le reste se recrée : `target/`, `.allure/` (historique et outil Allure, retéléchargé), `.grid/` (vidéos) et
`jenkins/.data/` (builds Jenkins).

## 1. Prérequis et configuration

Il faut **Java 21** et **Docker**. Maven est inutile : le wrapper `./mvnw` le télécharge.

Si `.env` n'existe pas, le créer à partir du modèle :

```bash
cp .env.example .env
```

Puis le remplir :

- `SAUCE_USERNAME` / `SAUCE_PASSWORD` : identifiants de démo affichés sur la page de connexion de saucedemo.com ;
- `DUMMYJSON_USERNAME` / `DUMMYJSON_PASSWORD` : utilisateurs de démo listés sur <https://dummyjson.com/users> ;
- `JENKINS_ADMIN_PASSWORD` : seulement pour le Jenkins local, par exemple généré ainsi :

```bash
openssl rand -base64 18
```

## 2. Qualité du code (sans navigateur)

```bash
./mvnw spotless:check checkstyle:check test-compile
```

Reformater automatiquement :

```bash
./mvnw spotless:apply
```

## 3. Tests en local (navigateurs sur la machine)

Démarrer the-internet, l'application de test en Docker :

```bash
docker compose up -d --wait
```

Suite complète sur Chrome, avec rejeu des échecs (les 2 scénarios visuels sont ignorés en local, c'est normal) :

```bash
./mvnw clean test
```

Seulement les `@smoke`, sans afficher le navigateur (comme la CI) :

```bash
./mvnw clean test -Dheadless=true -Dcucumber.filter.tags="@smoke"
```

Sur Firefox :

```bash
./mvnw clean test -Dbrowser=firefox
```

Une seule fonctionnalité, sans rejeu :

```bash
./mvnw clean test -DnoRerun -Dcucumber.filter.tags="@fenetres"
```

Tags disponibles : `@auth`, `@fenetres`, `@cadres`, `@dialogues`, `@fichiers`, `@multi-navigateurs`, `@api`,
`@accessibilite`, `@bidi`, `@visuel`, `@smoke`.

Nombre de scénarios en parallèle (4 par défaut) :

```bash
./mvnw clean test -Dthreads=2
```

## 4. Tests sur la Selenium Grid (comme la régression de nuit)

Démarrer the-internet, le hub et les nœuds Chrome et Firefox :

```bash
docker compose --profile grid up -d --wait
```

Les navigateurs de la Grid joignent the-internet par le réseau Docker : ajouter
`THE_INTERNET_URL=http://the-internet:5000` dans `.env`, ou l'exporter :

```bash
export THE_INTERNET_URL=http://the-internet:5000
```

Suite complète avec vidéos, visuels compris :

```bash
./mvnw clean test -Dexecution=grid -Dvideo=true -Dthreads=3
```

Sur Firefox :

```bash
./mvnw clean test -Dexecution=grid -Dbrowser=firefox -Dvideo=true -Dthreads=3
```

Console de la Grid (sessions en cours, VNC) : <http://localhost:4444>.

Régénérer les références visuelles après un changement d'apparence voulu (une fois par navigateur) :

```bash
./mvnw clean test -Dexecution=grid -Dcucumber.filter.tags="@visuel" -Dvisual.update=true
```

## 5. Rapports

Générer le rapport Allure et l'ouvrir dans le navigateur :

```bash
./mvnw allure:serve
```

Ou le générer seulement, dans `target/site/allure-maven-plugin/` :

```bash
./mvnw allure:report
```

Logs : `target/logs/`. Rapport Cucumber : `target/cucumber-reports/`. Vidéos : `.grid/videos/`.

## 6. Démonstrations

Mode démo : éléments encadrés en rouge et exécution ralentie, à regarder sur la Grid ou dans sa vidéo :

```bash
./mvnw clean test -Dexecution=grid -Dvideo=true -Ddemo=true -Dcucumber.filter.tags="@fenetres"
```

Avec les scénarios volontairement en échec (un défaut, un test instable, une régression visuelle) :

```bash
./mvnw clean test -Pdemo-scenarios -Dexecution=grid
```

Toujours avec `clean` : sinon ces scénarios restent dans `target/` et se relancent aux exécutions suivantes.

## 7. Jenkins local

```bash
jenkins/start.sh
```

Jenkins : <http://localhost:8080>, utilisateur `admin`, mot de passe `JENKINS_ADMIN_PASSWORD`. Le premier build se lance
seul en smoke ; ensuite, *Build with Parameters* lance la régression.

```bash
jenkins/stop.sh
```

## 8. Tout arrêter

```bash
docker compose --profile grid down
```
