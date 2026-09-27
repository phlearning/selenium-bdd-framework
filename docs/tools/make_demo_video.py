"""Edits the demo video (MP4) and the README GIF from the Grid videos of a demo-mode run.

    ./mvnw clean test -DnoRerun -Dexecution=grid -Dvideo=true -Ddemo=true -Dthreads=2 \
        -Dcucumber.filter.tags="@fenetres or @cadres or @dialogues or @fichiers or @multi-navigateurs or @auth or @bidi"
    python3 docs/tools/make_demo_video.py

Scenario videos are found through the "Scenario '...' sessions: {...}" lines of the run log.
The report screenshots come from docs/images (capture_screenshots.py). Needs ffmpeg.
Outputs: target/media/demo.mp4 (attached to a GitHub release) and docs/images/demo.gif.
"""

import argparse
import re
import subprocess
import tempfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
FONT = "/usr/share/fonts/truetype/noto/NotoSans-Regular.ttf"
FONT_BOLD = "/usr/share/fonts/truetype/noto/NotoSans-Bold.ttf"
W, H, FPS = 1280, 720, 25
BACKGROUND = "0x111827"
ACCENT = "0x60a5fa"
# Part of the 1920x1080 Grid screen that holds the pages (the-internet is centred and narrow)
CROP = "crop=1440:810:240:0"
# The recording goes on for a moment after the browser quits, on the Grid splash screen: cut it
TAIL = 1.2
# ...and starts on it, before the browser window opens
HEAD = 0.8
ENCODE = ["-c:v", "libx264", "-preset", "medium", "-crf", "23", "-pix_fmt", "yuv420p", "-r", str(FPS), "-an"]


class Editor:
    def __init__(self, workdir, sessions, videos):
        self.workdir = Path(workdir)
        self.sessions = sessions
        self.videos = Path(videos)
        self.count = 0

    # --- helpers --------------------------------------------------------------------

    def _text_file(self, text):
        self.count += 1
        path = self.workdir / f"text{self.count}.txt"
        path.write_text(text, encoding="utf-8")
        return path

    def _drawtext(self, text, size, y, font=FONT, color="white", box=False):
        options = [
            f"fontfile={font}",
            f"textfile={self._text_file(text)}",
            f"fontsize={size}",
            f"fontcolor={color}",
            "x=(w-text_w)/2",
            f"y={y}",
            "line_spacing=10",
        ]
        if box:
            options += ["box=1", "boxcolor=black@0.7", "boxborderw=16"]
        return "drawtext=" + ":".join(options)

    def _caption(self, text):
        return self._drawtext(text, 30, "h-text_h-40", box=True) if text else "null"

    @staticmethod
    def _fades(duration):
        return f"fade=t=in:st=0:d=0.3,fade=t=out:st={duration - 0.3:.2f}:d=0.3"

    def _render(self, args, name):
        self.count += 1
        out = self.workdir / f"{self.count:03d}-{name}.mp4"
        subprocess.run(["ffmpeg", "-v", "error", "-y", *args, *ENCODE, str(out)], check=True)
        return out

    def video_of(self, scenario, browser="principal"):
        session = self.sessions[scenario][browser]
        return self.videos / f"{session}.mp4"

    @staticmethod
    def duration(path):
        out = subprocess.run(
            ["ffprobe", "-v", "error", "-show_entries", "format=duration", "-of", "csv=p=0", str(path)],
            capture_output=True, text=True, check=True,
        )
        return float(out.stdout)

    # --- segments -------------------------------------------------------------------

    def card(self, title, subtitle="", seconds=4.0):
        filters = [
            self._drawtext(title, 54, "(h/2)-90", font=FONT_BOLD),
            self._drawtext(subtitle, 30, "(h/2)+10", color="0xd1d5db") if subtitle else "null",
            f"drawbox=x=(iw-120)/2:y=(ih/2)-120:w=120:h=5:color={ACCENT}:t=fill",
            self._fades(seconds),
        ]
        return self._render(
            ["-f", "lavfi", "-i", f"color=c={BACKGROUND}:s={W}x{H}:d={seconds}:r={FPS}", "-vf", ",".join(filters)],
            "card",
        )

    def clip(self, scenario, caption, hold=1.0):
        source = self.video_of(scenario)
        length = self.duration(source) - TAIL - HEAD
        seconds = length + hold
        filters = [
            f"trim=start={HEAD}:end={HEAD + length:.2f}", "setpts=PTS-STARTPTS", CROP, f"scale={W}:{H}", "setsar=1", f"tpad=stop_mode=clone:stop_duration={hold}",
            self._caption(caption), self._fades(seconds),
        ]
        return self._render(["-i", str(source), "-vf", ",".join(filters), "-t", f"{seconds:.2f}"], "clip")

    def duo(self, scenario, left, right, caption, right_delay=3.0, hold=1.0):
        """Two browsers of one scenario, side by side; the right one started later."""
        a, b = self.video_of(scenario, left), self.video_of(scenario, right)
        length_a, length_b = self.duration(a) - TAIL - HEAD, self.duration(b) - TAIL - HEAD
        seconds = max(length_a, length_b + right_delay) + hold
        half_w, half_h = W // 2 - 10, (W // 2 - 10) * 9 // 16
        top = (H - half_h) // 2 - 20
        graph = ";".join([
            f"[0:v]trim=start={HEAD}:end={HEAD + length_a:.2f},setpts=PTS-STARTPTS,{CROP},scale={half_w}:{half_h},setsar=1,tpad=stop_mode=clone:stop_duration=30[a]",
            f"[1:v]trim=start={HEAD}:end={HEAD + length_b:.2f},setpts=PTS-STARTPTS,{CROP},scale={half_w}:{half_h},setsar=1,"
            # Nothing to show until the second browser starts: an empty panel
            f"tpad=start_mode=add:start_duration={right_delay}:color={BACKGROUND}:stop_mode=clone:stop_duration=30[b]",
            f"color=c={BACKGROUND}:s={W}x{H}:r={FPS}[bg]",
            f"[bg][a]overlay=5:{top}:shortest=0[t1]",
            f"[t1][b]overlay={W // 2 + 5}:{top}[t2]",
            f"[t2]{self._drawtext(left, 34, top - 60, font=FONT_BOLD).replace('x=(w-text_w)/2', f'x={W // 4}-text_w/2')},"
            f"{self._drawtext(right, 34, top - 60, font=FONT_BOLD).replace('x=(w-text_w)/2', f'x={3 * W // 4}-text_w/2')},"
            f"{self._caption(caption)},{self._fades(seconds)}[out]",
        ])
        return self._render(
            ["-i", str(a), "-i", str(b), "-filter_complex", graph, "-map", "[out]", "-t", f"{seconds:.2f}"], "duo"
        )

    def slide(self, image, caption, seconds=5.0):
        filters = [
            f"scale={W - 80}:{H - 130}:force_original_aspect_ratio=decrease",
            f"pad={W}:{H}:(ow-iw)/2:30:color={BACKGROUND}",
            "setsar=1",
            self._caption(caption),
            self._fades(seconds),
        ]
        return self._render(
            ["-loop", "1", "-framerate", str(FPS), "-t", str(seconds), "-i", str(image), "-vf", ",".join(filters)],
            "slide",
        )

    def concat(self, parts, out):
        listing = self.workdir / "parts.txt"
        listing.write_text("".join(f"file '{p}'\n" for p in parts), encoding="utf-8")
        out.parent.mkdir(parents=True, exist_ok=True)
        subprocess.run(
            ["ffmpeg", "-v", "error", "-y", "-f", "concat", "-safe", "0", "-i", str(listing), "-c", "copy",
             "-movflags", "+faststart", str(out)],
            check=True,
        )
        print(f"{out.name} ({self.duration(out):.0f} s)")


def read_sessions(log):
    sessions = {}
    for line in Path(log).read_text(encoding="utf-8").splitlines():
        match = re.search(r"Scenario '(.*)' sessions: \{(.*)\}", line)
        if match:
            sessions[match.group(1)] = dict(item.split("=") for item in match.group(2).split(", "))
    return sessions


def demo_mp4(ed, images, out):
    img = Path(images)
    parts = [
        ed.card("Selenium BDD Framework", "Selenium 4 · Java 21 · Cucumber en français · Allure · BiDi · axe-core", 5),
        ed.card("Des scénarios lisibles par tous",
                "Gherkin en français, exécutés en parallèle sur une Selenium Grid\n"
                "Mode démo : chaque élément utilisé est encadré en rouge", 6),
        ed.clip("Connexion réussie avec un utilisateur standard",
                "Connexion à la boutique : Page Objects et attentes explicites"),
        ed.clip("Connexion refusée : mot de passe incorrect", "Plan du scénario : un cas par ligne d'exemples"),
        ed.card("Onglets et fenêtres", "Chaque onglet ou fenêtre est désigné par un alias", 3.5),
        ed.clip("Naviguer entre des onglets et des fenêtres ouverts par le test",
                "Ouvrir des onglets et des fenêtres, puis passer de l'un à l'autre"),
        ed.clip("Un lien ouvre une nouvelle fenêtre que l'on pilote puis que l'on referme",
                "Une fenêtre ouverte par l'application est détectée et pilotée"),
        ed.card("Cadres, boîtes de dialogue, fichiers", "Les cas techniques classiques de l'automatisation", 3.5),
        ed.clip("Lire le cadre frame-top > frame-middle", "Cadres imbriqués : lecture dans un cadre de cadre"),
        ed.clip("Répondre à une invite", "Boîtes de dialogue JavaScript : alerte, confirmation, invite"),
        ed.clip("Téléverser un fichier de test", "Téléversement d'un fichier local vers un navigateur de la Grid"),
        ed.card("Plusieurs navigateurs", "Deux clients connectés en même temps, chacun son panier", 3.5),
        ed.duo("Deux clients connectés en même temps ont chacun leur panier", "Alice", "Bob",
               "Un scénario, deux navigateurs indépendants"),
        ed.card("Au-delà du fonctionnel", "Accessibilité, erreurs JavaScript et réseau, régression visuelle", 3.5),
        ed.clip("La boutique reste utilisable quand ses images ne se chargent pas",
                "WebDriver BiDi : les images échouent exprès, le catalogue reste utilisable"),
        ed.slide(img / "allure-accessibilite.png", "Accessibilité : audit axe-core WCAG 2.1 AA, écarts connus justifiés", 5),
        ed.slide(img / "allure-diff-visuel.png", "Régression visuelle : seul le bouton modifié ressort en rouge", 5),
        ed.card("Et quand un test échoue ?", "Le rapport Allure donne tout pour comprendre", 3.5),
        ed.slide(img / "allure-echec.png", "Capture, source de la page, logs et vidéo joints à l'échec", 6),
        ed.slide(img / "allure-bidi.png", "Requêtes en échec et erreurs JavaScript captées par BiDi", 5),
        ed.slide(img / "allure-instable.png", "Rejeu automatique : réussi au 2e passage, marqué instable", 5),
        ed.slide(img / "allure-api.png", "Tests d'API : requêtes et réponses jointes, secrets masqués", 5),
        ed.slide(img / "allure-apercu.png", "Vue d'ensemble, tendance d'un run à l'autre, catégories d'échecs", 5),
        ed.slide(img / "allure-combine-navigateurs.png", "Un rapport combiné pour Chrome et Firefox", 5),
        ed.slide(img / "pages-accueil.png", "Publié sur GitHub Pages après chaque régression nocturne", 5),
        ed.card("Intégration continue",
                "GitHub Actions : qualité et @smoke à chaque push, régression chaque nuit\n"
                "Jenkinsfile équivalent, testé sur un Jenkins local", 6),
        ed.card("Selenium BDD Framework", "github.com/phlearning/selenium-bdd-framework", 4),
    ]
    ed.concat(parts, out)


def readme_gif(ed, out):
    parts = [
        ed.clip("Naviguer entre des onglets et des fenêtres ouverts par le test", "Onglets et fenêtres par alias", 0.5),
        ed.duo("Deux clients connectés en même temps ont chacun leur panier", "Alice", "Bob",
               "Deux navigateurs dans un même scénario", hold=0.5),
    ]
    mp4 = ed.workdir / "gif.mp4"
    ed.concat(parts, mp4)
    palette = ed.workdir / "palette.png"
    scale = "fps=8,setpts=PTS/1.25,scale=800:-1:flags=lanczos"
    subprocess.run(["ffmpeg", "-v", "error", "-y", "-i", str(mp4), "-vf", f"{scale},palettegen=stats_mode=diff",
                    str(palette)], check=True)
    subprocess.run(["ffmpeg", "-v", "error", "-y", "-i", str(mp4), "-i", str(palette), "-lavfi",
                    f"{scale}[v];[v][1:v]paletteuse=dither=bayer:bayer_scale=5:diff_mode=rectangle",
                    str(out)], check=True)
    print(f"{out.name} ({out.stat().st_size // 1024} Ko)")


def main():
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--log", default=ROOT / "target/logs/test-run.log")
    parser.add_argument("--videos", default=ROOT / ".grid/videos")
    parser.add_argument("--images", default=ROOT / "docs/images")
    parser.add_argument("--mp4", default=ROOT / "target/media/demo.mp4", type=Path)
    parser.add_argument("--gif", default=ROOT / "docs/images/demo.gif", type=Path)
    args = parser.parse_args()
    with tempfile.TemporaryDirectory() as workdir:
        ed = Editor(workdir, read_sessions(args.log), args.videos)
        readme_gif(ed, args.gif)
        demo_mp4(ed, args.images, args.mp4)


if __name__ == "__main__":
    main()
