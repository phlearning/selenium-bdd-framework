# Captures d'écran et vidéo de démonstration

Les images de [`docs/images/`](../images) et la vidéo de présentation se régénèrent par script, par exemple après
une évolution des rapports. Prérequis : Docker, Python 3, ffmpeg, et les polices Noto Sans.

```bash
python3 -m venv .venv && .venv/bin/pip install selenium
docker compose --profile grid up -d --wait
export THE_INTERNET_URL=http://the-internet:5000     # navigateurs de la Grid
```

## Captures d'écran

1. Un rapport Allure qui contient aussi les scénarios de démonstration (un défaut réel, un test instable) :

   ```bash
   ./mvnw clean test -Pdemo-scenarios -Dexecution=grid -Dvideo=true -Dthreads=3
   ./mvnw allure:report
   (cd target/site/allure-maven-plugin && python3 -m http.server 8765) &
   ```

2. Les captures du rapport local, du site GitHub Pages et, si le Jenkins local tourne et a au moins un build
   réussi, de Jenkins :

   ```bash
   set -a && . ./.env && set +a                        # JENKINS_ADMIN_PASSWORD
   .venv/bin/python docs/tools/capture_screenshots.py \
       --report http://localhost:8765 \
       --site https://phlearning.github.io/selenium-bdd-framework \
       --jenkins http://localhost:8080
   ```

## GIF et vidéo

1. Des vidéos de la Grid en [mode démo](../../README.md#mode-démo), sans rejeu :

   ```bash
   rm -f .grid/videos/*
   ./mvnw clean test -DnoRerun -Dexecution=grid -Dvideo=true -Ddemo=true -Dthreads=2 \
       -Dcucumber.filter.tags="@fenetres or @cadres or @dialogues or @fichiers or @multi-navigateurs or @auth"
   ```

2. Le montage : le script retrouve la vidéo de chaque scénario grâce aux lignes `Scenario '...' sessions: {...}`
   du log, ajoute cartons et sous-titres, puis les captures du rapport.

   ```bash
   python3 docs/tools/make_demo_video.py
   ```

   Résultat : `docs/images/demo.gif` (README) et `target/media/demo.mp4`, à joindre à une release GitHub :

   ```bash
   gh release upload v1.0.0 target/media/demo.mp4 --clobber
   ```
