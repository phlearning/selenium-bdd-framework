# Selenium BDD Framework

Framework d'automatisation de tests UI en **Java 21 · Selenium 4 · Cucumber 7 (Gherkin en français) · JUnit 5 · Allure**.

> 🚧 Construction par jalons - état actuel : **jalon 2 (robustesse)**. Voir la [feuille de route](#feuille-de-route).

## Démarrage rapide

Prérequis : **JDK 21**. Maven, les drivers et même le navigateur Chrome sont téléchargés automatiquement
(Maven Wrapper + Selenium Manager).

```bash
cp .env.example .env          # puis renseigner les identifiants de démo
./mvnw test                   # tous les scénarios (hors @wip), Chrome visible
./mvnw allure:serve           # ouvre le rapport Allure
```

Exemples :

```bash
./mvnw test -Dcucumber.filter.tags="@smoke"          # filtrer par tags
./mvnw test -Dbrowser=firefox -Dheadless=true        # autre navigateur, sans interface
./mvnw test -Denv=demo -Dbase.url=https://...        # surcharger n'importe quelle clé
./mvnw test -Dthreads=8                              # nombre de scénarios en parallèle (défaut 4)
./mvnw test -DnoRerun                                # un seul passage, pas de rejeu
```

## Configuration

Chaque clé (ex. `base.url`) est résolue dans cet ordre, la première source qui la définit gagne :

1. propriété système `-Dbase.url=…`
2. variable d'environnement `BASE_URL`
3. fichier local `.env` (ignoré par git - modèle : [`.env.example`](.env.example))
4. `src/test/resources/config/environments/<env>.properties`
5. `src/test/resources/config/default.properties`

Aucun secret n'est versionné : les identifiants viennent de `.env` en local et des *secrets* GitHub en CI.

| Clé | Défaut | Rôle |
|---|---|---|
| `env` | `demo` | environnement cible |
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
├── driver/    DriverFactory (options navigateur) · DriverManager (1 navigateur par scénario, démarrage paresseux)
├── pages/     Page Objects - BasePage centralise les attentes explicites
├── steps/     définitions d'étapes Gherkin (FR)
├── hooks/     ScenarioHooks (logs, preuves d'échec) · DriverHooks (fermeture) · ReportHooks (infos Allure)
├── logging/   ScenarioLogAppender - capture les logs du scénario courant (par thread)
├── reporting/ FlakyResultsMarker - marque « flaky » les scénarios réussis au rejeu
└── runner/    RunCucumberTest (1er passage) · RerunCucumberTest (rejeu des échecs)
src/test/resources/
├── features/  scénarios .feature (# language: fr)
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
- [ ] **3. Multi** - onglets, fenêtres, iframes, plusieurs navigateurs par scénario
- [ ] **4. CI** - Selenium Grid (Docker), vidéos, GitHub Actions (push / nightly / manuel), rapports
- [ ] **5. API & qualité** - RestAssured, Spotless/Checkstyle, Dependabot, documentation complète
- [ ] **6. Jenkins** - `Jenkinsfile` équivalent

## Application testée

Boutique de démonstration publique [saucedemo.com](https://www.saucedemo.com) (les identifiants de démo sont
affichés sur sa page de connexion) et [the-internet](https://the-internet.herokuapp.com) pour les cas techniques.
