# Selenium BDD Framework

Framework d'automatisation de tests UI en **Java 21 · Selenium 4 · Cucumber 7 (Gherkin en français) · JUnit 5 · Allure**.

> 🚧 Construction par jalons - état actuel : **jalon 3 (multi-onglets, multi-fenêtres, multi-navigateurs)**. Voir la [feuille de route](#feuille-de-route).

## Démarrage rapide

Prérequis : **JDK 21** et **Docker**. Maven, les drivers et même le navigateur Chrome sont téléchargés
automatiquement (Maven Wrapper + Selenium Manager).

```bash
cp .env.example .env          # puis renseigner les identifiants de démo
docker compose up -d          # démarre l'application the-internet en local (port 7080)
./mvnw test                   # tous les scénarios (hors @wip), Chrome visible
./mvnw allure:serve           # ouvre le rapport Allure
```

Exemples :

```bash
./mvnw test -Dcucumber.filter.tags="@smoke"          # filtrer par tags
./mvnw test -Dbrowser=firefox -Dheadless=true        # autre navigateur, sans interface
./mvnw test -Dsaucedemo.url=https://...              # surcharger n'importe quelle clé
./mvnw test -Dthreads=8                              # nombre de scénarios en parallèle (défaut 4)
./mvnw test -DnoRerun                                # un seul passage, pas de rejeu
```

## Configuration

Chaque clé (ex. `saucedemo.url`) est résolue dans cet ordre, la première source qui la définit gagne :

1. propriété système `-Dsaucedemo.url=…`
2. variable d'environnement `SAUCEDEMO_URL`
3. fichier local `.env` (ignoré par git - modèle : [`.env.example`](.env.example))
4. `src/test/resources/config/environments/<env>.properties`
5. `src/test/resources/config/default.properties`

Aucun secret n'est versionné : les identifiants viennent de `.env` en local et des *secrets* GitHub en CI.

| Clé | Défaut | Rôle |
|---|---|---|
| `env` | `demo` | environnement cible |
| `saucedemo.url` | `https://www.saucedemo.com` | boutique de démo |
| `the-internet.url` | `http://localhost:7080` | the-internet (Docker local) |
| `browser` | `chrome` | `chrome` \| `firefox` |
| `headless` | `false` | navigateur sans interface |
| `window.width` / `window.height` | `1920` / `1080` | taille de fenêtre |
| `timeout.explicit` | `10` | attente explicite (s) |
| `timeout.page.load` | `30` | chargement de page (s) |
| `sauce.username` / `sauce.password` | - | identifiants de la boutique de démo |

## Architecture

```
src/test/java/io/github/phlearning/bdd/
├── config/    Config - résolution des clés (sys props > env > .env > fichiers)
├── driver/    DriverFactory (options navigateur) · DriverManager (navigateurs nommés du scénario)
│              · WindowManager (onglets et fenêtres par alias)
├── pages/     Page Objects - BasePage : attentes explicites, frames, boîtes de dialogue, upload
│   ├── saucedemo/   connexion, catalogue, panier
│   └── internet/    fenêtres, cadres, alertes, téléversement
├── steps/     définitions d'étapes Gherkin (FR)
├── hooks/     ScenarioHooks (logs, preuves d'échec) · DriverHooks (fermeture) · ReportHooks (infos Allure)
├── logging/   ScenarioLogAppender - capture les logs du scénario courant (par thread)
├── reporting/ FlakyResultsMarker - marque « flaky » les scénarios réussis au rejeu
└── runner/    RunCucumberTest (1er passage) · RerunCucumberTest (rejeu des échecs)
src/test/resources/
├── features/  scénarios .feature (# language: fr), un dossier par fonctionnalité
├── testdata/  fichiers de test (téléversement…)
├── config/    propriétés par défaut et par environnement
├── allure/    catégories d'échec Allure
└── junit-platform.properties  glue, parallélisation, ressources exclusives
```

Choix principaux :

- **Page Object Model + PicoContainer** : Cucumber crée un conteneur par scénario ; `DriverManager` y est injecté
  dans les steps et les hooks, ce qui isole naturellement chaque scénario (base de la parallélisation).
- **Aucune attente implicite** : toute synchronisation passe par des attentes explicites dans `BasePage`,
  qui réessaie aussi l'action tant que le DOM bouge (élément recréé, masqué par un overlay, pas encore interactif).
- **Navigateur démarré à la demande** : un scénario qui n'utilise pas l'UI (API) n'ouvre pas de navigateur.

## Robustesse

### Exécution parallèle

Les scénarios s'exécutent en parallèle (un scénario = un thread = un navigateur), `-Dthreads=N` pour régler le
nombre de threads. Chaque scénario ayant son propre conteneur PicoContainer, rien n'est partagé entre threads.

Un scénario tagué **`@sequential`** s'exécute seul, sans aucun autre scénario en parallèle. JUnit applique ce
verrou à toute la *feature* qui le contient : regrouper ces scénarios dans un fichier `.feature` dédié.

### Rejeu des scénarios en échec

Deux niveaux :

1. **Action** : `BasePage` réessaie chaque interaction jusqu'au délai `timeout.explicit`.
2. **Scénario** : deux exécutions Surefire.
   - 1er passage (`RunCucumberTest`) : tous les scénarios ; les échecs sont écrits dans `target/rerun.txt`
     sans faire échouer le build.
   - 2e passage (`RerunCucumberTest`) : rejoue uniquement ces scénarios, **une fois**. C'est lui qui décide du
     résultat du build.

Un scénario qui échoue puis réussit apparaît dans Allure avec son historique de tentatives, marqué **flaky** et
classé dans la catégorie « Tests instables ». `-DnoRerun` désactive le rejeu (pratique en local).

### Preuves et logs

| Quand | Quoi | Où |
|---|---|---|
| toujours | logs du scénario (actions, étapes) | pièce jointe « Logs » dans Allure / Cucumber HTML |
| en échec | capture d'écran, URL, source HTML | pièces jointes du scénario |
| toujours | logs de tout le passage (tous threads, nom du scénario dans chaque ligne) | `target/logs/test-run.log`, `test-rerun.log` |

Les mots de passe sont saisis via `typeSecret` et n'apparaissent jamais dans les logs.
Niveaux réglables : `-DLOG_LEVEL=INFO` (fichiers et pièces jointes), `-DCONSOLE_LOG_LEVEL=DEBUG` (console).

## Onglets, fenêtres, cadres et navigateurs multiples

### Onglets et fenêtres

`WindowManager` (un par navigateur) désigne les fenêtres par **alias** plutôt que par identifiant technique.
La fenêtre de départ s'appelle `principale`.

| Besoin | API | Étape Gherkin |
|---|---|---|
| fenêtre ouverte par l'application (lien `target=_blank`, popup) | `openedBy(alias, action)` : attend la nouvelle fenêtre et bascule dessus | `je clique sur le lien qui ouvre la fenêtre "nouvelle"` |
| nouvel onglet / nouvelle fenêtre | `openTab(alias, url)` · `openWindow(alias, url)` | `j'ouvre un nouvel onglet "alertes" sur la page "/javascript_alerts"` |
| basculer | `switchTo(alias)` · `switchToTitle(titre)` | `je bascule sur la fenêtre "alertes"` |
| fermer et revenir à `principale` | `close(alias)` | `je ferme la fenêtre "nouvelle"` |

### Cadres (frames / iframes)

`BasePage.inFrame(action, cadre1, cadre2…)` entre dans des cadres imbriqués (du plus externe au plus interne),
exécute l'action puis **revient toujours** au document principal, même en cas d'échec.

### Boîtes de dialogue et téléversement

`BasePage.dialog()` attend une boîte `alert` / `confirm` / `prompt` ; `BasePage.upload(input, fichier)` renseigne
un `<input type="file">` avec un fichier de `src/test/resources/testdata/`.

### Plusieurs navigateurs dans un scénario

`DriverManager` gère des **sessions nommées** : `use("Alice")` démarre (si besoin) puis active le navigateur
« Alice ». Les Page Objects travaillent toujours sur la session active. En cas d'échec, capture d'écran, URL et
source HTML sont jointes **pour chaque navigateur** (`Screenshot (Alice)`, `Screenshot (Bob)`…).

```gherkin
Soit l'utilisateur standard est connecté dans le navigateur "Alice"
Et l'utilisateur standard est connecté dans le navigateur "Bob"
Quand dans le navigateur "Alice", j'ajoute le produit "Sauce Labs Backpack" au panier
Alors dans le navigateur "Bob", le panier est vide
```

## Rapports

| Rapport | Emplacement |
|---|---|
| Allure | `./mvnw allure:report` → `target/site/allure-maven-plugin/index.html` (ou `allure:serve`) |
| Cucumber HTML | `target/cucumber-reports/cucumber.html` (+ `cucumber-rerun.html` pour le rejeu) |

Le rapport Allure inclut l'onglet **Environment** (environnement, URL, navigateur, OS…) et des **catégories**
d'échec (assertion, synchronisation, infrastructure, configuration, instables) définies dans
`src/test/resources/allure/categories.json`.

## Feuille de route

- [x] **1. Socle** - Maven, configuration, gestion des navigateurs, POM, PicoContainer, scénarios `@smoke`, Allure
- [x] **2. Robustesse** - parallélisation, rejeu des scénarios en échec, captures d'écran, logs
- [x] **3. Multi** - onglets, fenêtres, iframes, plusieurs navigateurs par scénario
- [ ] **4. CI** - Selenium Grid (Docker), vidéos, GitHub Actions (push / nightly / manuel), rapports
- [ ] **5. API & qualité** - RestAssured, Spotless/Checkstyle, Dependabot, documentation complète
- [ ] **6. Jenkins** - `Jenkinsfile` équivalent

## Applications testées

| Application | Rôle | Accès |
|---|---|---|
| [saucedemo.com](https://www.saucedemo.com) | parcours métier : connexion, catalogue, panier | en ligne ; identifiants de démo affichés sur sa page de connexion |
| the-internet | cas techniques : fenêtres, cadres, alertes, téléversement | en local via `docker compose up -d` (image `gprestes/the-internet`) |
