# Selenium BDD Framework

Framework d'automatisation de tests UI en **Java 21 · Selenium 4 · Cucumber 7 (Gherkin en français) · JUnit 5 · Allure**.

> 🚧 Construction par jalons — état actuel : **jalon 1 (socle)**. Voir la [feuille de route](#feuille-de-route).

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
```

## Configuration

Chaque clé (ex. `base.url`) est résolue dans cet ordre, la première source qui la définit gagne :

1. propriété système `-Dbase.url=…`
2. variable d'environnement `BASE_URL`
3. fichier local `.env` (ignoré par git — modèle : [`.env.example`](.env.example))
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
| `sauce.username` / `sauce.password` | — | identifiants de la boutique de démo |

## Architecture

```
src/test/java/io/github/phlearning/bdd/
├── config/    Config — résolution des clés (sys props > env > .env > fichiers)
├── driver/    DriverFactory (options navigateur) · DriverManager (1 navigateur par scénario, démarrage paresseux)
├── pages/     Page Objects — BasePage centralise les attentes explicites
├── steps/     définitions d'étapes Gherkin (FR)
├── hooks/     hooks Cucumber (fermeture du navigateur…)
└── runner/    RunCucumberTest — point d'entrée JUnit Platform
src/test/resources/
├── features/  scénarios .feature (# language: fr)
├── config/    propriétés par défaut et par environnement
└── junit-platform.properties  glue, plugins Cucumber (pretty, html, Allure)
```

Choix principaux :

- **Page Object Model + PicoContainer** : Cucumber crée un conteneur par scénario ; `DriverManager` y est injecté
  dans les steps et les hooks, ce qui isole naturellement chaque scénario (base de la parallélisation).
- **Aucune attente implicite** : toute synchronisation passe par des attentes explicites dans `BasePage`.
- **Navigateur démarré à la demande** : un scénario qui n'utilise pas l'UI (API) n'ouvre pas de navigateur.

## Rapports

| Rapport | Emplacement |
|---|---|
| Allure | `./mvnw allure:report` → `target/site/allure-maven-plugin/index.html` (ou `allure:serve`) |
| Cucumber HTML | `target/cucumber-reports/cucumber.html` |

## Feuille de route

- [x] **1. Socle** — Maven, configuration, gestion des navigateurs, POM, PicoContainer, scénarios `@smoke`, Allure
- [ ] **2. Robustesse** — parallélisation, rejeu des scénarios en échec, captures d'écran, logs
- [ ] **3. Multi** — onglets, fenêtres, iframes, plusieurs navigateurs par scénario
- [ ] **4. CI** — Selenium Grid (Docker), vidéos, GitHub Actions (push / nightly / manuel), rapports
- [ ] **5. API & qualité** — RestAssured, Spotless/Checkstyle, Dependabot, documentation complète
- [ ] **6. Jenkins** — `Jenkinsfile` équivalent

## Application testée

Boutique de démonstration publique [saucedemo.com](https://www.saucedemo.com) (les identifiants de démo sont
affichés sur sa page de connexion) et [the-internet](https://the-internet.herokuapp.com) pour les cas techniques.
