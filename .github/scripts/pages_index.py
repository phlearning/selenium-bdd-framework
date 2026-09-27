"""Builds the home page of the GitHub Pages site from the generated Allure reports.

    python3 pages_index.py SITE tous="Tous les navigateurs" chrome=Chrome firefox=Firefox

Each argument is a report directory of SITE and its title; the first one is shown first and
larger. Figures come from the reports themselves (widgets/summary.json, history-trend.json),
the run context from the GitHub Actions environment variables.
"""

import html
import json
import os
import sys
from datetime import datetime, timezone
from pathlib import Path

STATUSES = [
    ("passed", "réussis"),
    ("failed", "en échec"),
    ("broken", "cassés"),
    ("skipped", "ignorés"),
    ("unknown", "inconnus"),
]
TREND_RUNS = 20


def read_json(path, default):
    try:
        return json.loads(path.read_text(encoding="utf-8"))
    except (OSError, ValueError):
        return default


def duration(ms):
    seconds = round((ms or 0) / 1000)
    return f"{seconds // 60} min {seconds % 60:02d} s" if seconds >= 60 else f"{seconds} s"


def load_report(site, directory, title):
    widgets = site / directory / "widgets"
    summary = read_json(widgets / "summary.json", None)
    if summary is None:
        return None
    stats = {key: summary.get("statistic", {}).get(key, 0) for key, _ in STATUSES}
    stats["total"] = summary.get("statistic", {}).get("total", sum(stats.values()))
    trend = read_json(widgets / "history-trend.json", [])[:TREND_RUNS]
    return {
        "dir": directory,
        "title": title,
        "stats": stats,
        "duration": summary.get("time", {}).get("duration", 0),
        "start": summary.get("time", {}).get("start"),
        "trend": list(reversed(trend)),
    }


def trend_svg(trend):
    """Stacked bars of the last runs, oldest on the left."""
    if len(trend) < 2:
        return '<p class="muted small">La tendance apparaît à partir du 2e run.</p>'
    width, height, gap = 100 / len(trend), 40, 0.6
    bars = []
    top = max(point.get("data", {}).get("total", 0) for point in trend) or 1
    for index, point in enumerate(trend):
        data = point.get("data", {})
        y = height
        for key, _ in STATUSES:
            value = data.get(key, 0)
            if not value:
                continue
            h = value / top * height
            y -= h
            bars.append(
                f'<rect class="{key}" x="{index * width + gap / 2:.2f}" y="{y:.2f}" '
                f'width="{width - gap:.2f}" height="{h:.2f}"><title>'
                f'Run {html.escape(str(point.get("buildOrder", index + 1)))} : {value} {key}</title></rect>'
            )
    return (
        f'<svg class="trend" viewBox="0 0 100 {height}" preserveAspectRatio="none" role="img" '
        f'aria-label="Résultats des {len(trend)} derniers runs">{"".join(bars)}</svg>'
        f'<p class="muted small">{len(trend)} derniers runs, du plus ancien au plus récent</p>'
    )


def card(report, main):
    stats = report["stats"]
    total = stats["total"] or 1
    problems = stats["failed"] + stats["broken"]
    rate = stats["passed"] / total * 100
    state = "ok" if problems == 0 else "ko"
    segments = "".join(
        f'<span class="{key}" style="width:{stats[key] / total * 100:.2f}%"></span>'
        for key, _ in STATUSES
        if stats[key]
    )
    counts = "".join(
        f'<li><span class="dot {key}"></span><b>{stats[key]}</b> {label}</li>'
        for key, label in STATUSES
        if stats[key] or key in ("passed", "failed")
    )
    return f"""
    <a class="card {state}{' main' if main else ''}" href="{html.escape(report['dir'])}/">
      <div class="card-head">
        <h2>{html.escape(report['title'])}</h2>
        <span class="badge {state}">{'Tout est vert' if problems == 0 else f'{problems} en échec'}</span>
      </div>
      <p class="rate"><b>{rate:.0f} %</b> de {stats['total']} scénarios réussis, en {duration(report['duration'])}</p>
      <div class="bar" aria-hidden="true">{segments}</div>
      <ul class="counts">{counts}</ul>
      {trend_svg(report['trend']) if main else ''}
      <span class="open">Ouvrir le rapport &rarr;</span>
    </a>"""


def context():
    env = os.environ
    repo_url = f"{env.get('GITHUB_SERVER_URL', 'https://github.com')}/{env.get('GITHUB_REPOSITORY', '')}"
    sha = env.get("GITHUB_SHA", "")
    items = []
    if env.get("GITHUB_RUN_ID"):
        items.append(
            f'<a href="{repo_url}/actions/runs/{env["GITHUB_RUN_ID"]}">Run #{env.get("GITHUB_RUN_NUMBER", "")}</a>'
        )
    if sha:
        items.append(f'Commit <a href="{repo_url}/commit/{sha}"><code>{sha[:7]}</code></a>')
    trigger = {"schedule": "régression nocturne", "workflow_dispatch": "lancement manuel"}
    if env.get("GITHUB_EVENT_NAME"):
        items.append(trigger.get(env["GITHUB_EVENT_NAME"], env["GITHUB_EVENT_NAME"]))
    if env.get("TEST_TAGS"):
        items.append(f"tags <code>{html.escape(env['TEST_TAGS'])}</code>")
    return items


def page(reports):
    first = reports[0]
    problems = sum(r["stats"]["failed"] + r["stats"]["broken"] for r in reports[1:] or reports)
    started = first["start"]
    when = (
        datetime.fromtimestamp(started / 1000, timezone.utc).strftime("%d/%m/%Y à %H:%M UTC")
        if started
        else ""
    )
    meta = " · ".join(([f"Exécuté le {when}"] if when else []) + context())
    cards = card(first, True) + '<div class="grid">' + "".join(card(r, False) for r in reports[1:]) + "</div>"
    return f"""<!doctype html>
<html lang="fr">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>Rapports de tests</title>
<style>
  :root {{
    color-scheme: light dark;
    --bg: #f6f7f9; --surface: #ffffff; --text: #1b1f24; --muted: #5f6b7a; --border: #dde1e6;
    --passed: #2e9e5b; --failed: #d64545; --broken: #e0a526; --skipped: #9aa3ad; --unknown: #8b62c9;
    --accent: #2f6fdb;
  }}
  @media (prefers-color-scheme: dark) {{
    :root {{
      --bg: #0f1216; --surface: #181c22; --text: #e6e9ee; --muted: #9aa4b1; --border: #2a3039;
      --passed: #3fb870; --failed: #ef6464; --broken: #f0b93d; --skipped: #6f7985; --unknown: #a987e0;
      --accent: #6aa0ff;
    }}
  }}
  * {{ box-sizing: border-box; }}
  body {{ margin: 0; background: var(--bg); color: var(--text);
    font: 16px/1.5 system-ui, -apple-system, "Segoe UI", Roboto, sans-serif; }}
  main {{ max-width: 960px; margin: 0 auto; padding: 40px 16px 56px; }}
  h1 {{ font-size: 1.75rem; margin: 0 0 4px; letter-spacing: -0.01em; }}
  h2 {{ font-size: 1.1rem; margin: 0; }}
  a {{ color: var(--accent); }}
  code {{ font: 0.9em ui-monospace, SFMono-Regular, Menlo, monospace; }}
  .muted {{ color: var(--muted); }}
  .small {{ font-size: 0.8rem; margin: 4px 0 0; }}
  header {{ margin-bottom: 24px; }}
  .banner {{ display: flex; align-items: center; gap: 10px; padding: 12px 16px; border-radius: 10px;
    margin-bottom: 20px; font-weight: 600; border: 1px solid var(--border); background: var(--surface); }}
  .banner::before {{ content: ""; width: 10px; height: 10px; border-radius: 50%; flex: none; }}
  .banner.ok::before {{ background: var(--passed); }}
  .banner.ko::before {{ background: var(--failed); }}
  .card {{ display: block; padding: 20px; border-radius: 12px; background: var(--surface);
    border: 1px solid var(--border); color: inherit; text-decoration: none;
    transition: border-color .15s, transform .15s; }}
  .card:hover, .card:focus-visible {{ border-color: var(--accent); transform: translateY(-1px); }}
  .card.main {{ margin-bottom: 16px; }}
  .card-head {{ display: flex; justify-content: space-between; align-items: center; gap: 12px; flex-wrap: wrap; }}
  .badge {{ font-size: 0.8rem; font-weight: 600; padding: 2px 10px; border-radius: 999px; }}
  .badge.ok {{ color: var(--passed); background: color-mix(in srgb, var(--passed) 14%, transparent); }}
  .badge.ko {{ color: var(--failed); background: color-mix(in srgb, var(--failed) 14%, transparent); }}
  .rate {{ margin: 10px 0 8px; color: var(--muted); }}
  .rate b {{ color: var(--text); font-size: 1.5rem; margin-right: 4px; }}
  .main .rate b {{ font-size: 2rem; }}
  .bar {{ display: flex; height: 10px; border-radius: 5px; overflow: hidden; background: var(--border); }}
  .bar .passed, .dot.passed {{ background: var(--passed); }}
  .trend .passed {{ fill: var(--passed); }}
  .bar .failed, .dot.failed {{ background: var(--failed); }}
  .trend .failed {{ fill: var(--failed); }}
  .bar .broken, .dot.broken {{ background: var(--broken); }}
  .trend .broken {{ fill: var(--broken); }}
  .bar .skipped, .dot.skipped {{ background: var(--skipped); }}
  .trend .skipped {{ fill: var(--skipped); }}
  .bar .unknown, .dot.unknown {{ background: var(--unknown); }}
  .trend .unknown {{ fill: var(--unknown); }}
  .counts {{ list-style: none; padding: 0; margin: 12px 0 0; display: flex; flex-wrap: wrap; gap: 4px 16px;
    font-size: 0.9rem; color: var(--muted); }}
  .counts b {{ color: var(--text); }}
  .dot {{ display: inline-block; width: 8px; height: 8px; border-radius: 50%; margin-right: 6px; }}
  .trend {{ display: block; width: 100%; height: 56px; margin-top: 16px; }}
  .open {{ display: inline-block; margin-top: 14px; font-size: 0.9rem; font-weight: 600; color: var(--accent); }}
  .grid {{ display: grid; grid-template-columns: repeat(auto-fit, minmax(260px, 1fr)); gap: 16px; }}
  footer {{ margin-top: 32px; font-size: 0.85rem; }}
</style>
</head>
<body>
<main>
  <header>
    <h1>Rapports de tests</h1>
    <p class="muted" style="margin:0">Régression de la branche <code>main</code> - {meta}</p>
  </header>
  <div class="banner {'ok' if problems == 0 else 'ko'}">
    {'Tous les scénarios passent sur tous les navigateurs.' if problems == 0
     else f'{problems} scénario(s) en échec : voir le détail dans les rapports.'}
  </div>
  {cards}
  <footer class="muted">
    Rapports Allure générés par GitHub Actions après chaque régression de <code>main</code>.
    Un scénario rejoué avec succès apparaît comme réussi, marqué instable (<i>flaky</i>) dans le rapport.
  </footer>
</main>
</body>
</html>
"""


def main(site, specs):
    site = Path(site)
    reports = []
    for spec in specs:
        directory, title = spec.split("=", 1)
        report = load_report(site, directory, title)
        if report is None:
            print(f"{directory}: no report, skipped")
        else:
            reports.append(report)
    if not reports:
        sys.exit("no report to index")
    (site / "index.html").write_text(page(reports), encoding="utf-8")
    print(f"index.html: {len(reports)} reports")


if __name__ == "__main__":
    if len(sys.argv) < 3:
        sys.exit(__doc__)
    main(sys.argv[1], sys.argv[2:])
