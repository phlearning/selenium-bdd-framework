"""Merges the Allure results of several browsers into one results directory.

    python3 combine_allure_results.py OUT chrome=results/chrome firefox=results/firefox

The same scenario run on two browsers has the same historyId: Allure would show the second
one as a retry of the first. Each result gets a browser-specific historyId, a "Navigateur"
parameter and the browser as parent suite (Suites tab: browser > feature > scenario).
Attempts of one scenario on one browser (first pass and rerun) keep sharing their historyId,
so they stay grouped as retries.
"""

import hashlib
import json
import shutil
import sys
from pathlib import Path

# Written by each run, rebuilt here for the combined report
PER_RUN_FILES = {"environment.properties", "executor.json"}


def tag_result(result, browser):
    history_id = result.get("historyId") or result.get("fullName", "")
    result["historyId"] = hashlib.md5(f"{history_id}:{browser}".encode()).hexdigest()
    result["parameters"] = [p for p in result.get("parameters", []) if p.get("name") != "Navigateur"]
    result["parameters"].append({"name": "Navigateur", "value": browser})
    labels = [label for label in result.get("labels", []) if label.get("name") != "parentSuite"]
    labels.append({"name": "parentSuite", "value": browser})
    labels.append({"name": "tag", "value": browser})
    result["labels"] = labels
    return result


def read_properties(path):
    """Raw key -> raw value, enough to merge the files written by java.util.Properties."""
    entries = {}
    for line in path.read_text(encoding="utf-8").splitlines():
        if line and not line.startswith(("#", "!")) and "=" in line:
            key, value = line.split("=", 1)
            entries[key] = value
    return entries


def main(out, sources):
    out = Path(out)
    out.mkdir(parents=True, exist_ok=True)
    environment = {}
    for source in sources:
        browser, directory = source.split("=", 1)
        directory = Path(directory)
        if not directory.is_dir():
            print(f"{browser}: no results in {directory}, skipped")
            continue
        count = 0
        for file in directory.iterdir():
            if not file.is_file() or file.name in PER_RUN_FILES:
                continue
            if file.name.endswith("-result.json"):
                result = tag_result(json.loads(file.read_text(encoding="utf-8")), browser)
                (out / file.name).write_text(json.dumps(result, ensure_ascii=False), encoding="utf-8")
                count += 1
            else:
                # Attachments and containers have unique (uuid) names; categories.json is shared.
                shutil.copy2(file, out / file.name)
        env_file = directory / "environment.properties"
        if env_file.is_file():
            for key, value in read_properties(env_file).items():
                values = environment.setdefault(key, [])
                if value not in values:
                    values.append(value)
        print(f"{browser}: {count} results")
    lines = [f"{key}={', '.join(values)}" for key, values in sorted(environment.items())]
    (out / "environment.properties").write_text("\n".join(lines) + "\n", encoding="utf-8")


if __name__ == "__main__":
    if len(sys.argv) < 3:
        sys.exit(__doc__)
    main(sys.argv[1], sys.argv[2:])
