"""Takes the screenshots of the README (docs/images/) with a headless Chrome.

    python3 -m venv .venv && .venv/bin/pip install selenium
    .venv/bin/python docs/tools/capture_screenshots.py --report http://localhost:8765 \
        --site https://phlearning.github.io/selenium-bdd-framework \
        [--jenkins http://localhost:8080]

--report: a local Allure report that contains the demo scenarios (one real failure, one flaky
          test), served over HTTP. docs/tools/README.md explains how to produce it.
--site:   the GitHub Pages site (home page and combined report).
--jenkins: the local Jenkins (jenkins/start.sh) after at least one build; the password is read
          from the JENKINS_ADMIN_PASSWORD environment variable.
"""

import argparse
import os
import time
from pathlib import Path

from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support import expected_conditions as ec
from selenium.webdriver.support.ui import WebDriverWait

OUT = Path(__file__).resolve().parents[1] / "images"


class Camera:
    def __init__(self, width=1440, height=900):
        options = webdriver.ChromeOptions()
        options.add_argument("--headless=new")
        options.add_argument(f"--window-size={width},{height}")
        options.add_argument("--force-device-scale-factor=1")
        options.add_argument("--lang=fr-FR")
        self.driver = webdriver.Chrome(options=options)
        self.wait = WebDriverWait(self.driver, 20)

    def open(self, url, settle=1.5):
        self.driver.get(url)
        # Only the #fragment may have changed: reload, so that the SPA starts from a fresh state
        # (collapsed tree), not from the one left by the previous screenshot.
        self.driver.refresh()
        time.sleep(settle)  # animations of the Allure SPA

    def allure_settings(self, origin):
        """French UI, light theme: set once per origin, before opening the report."""
        self.open(origin, settle=0.5)
        self.driver.execute_script(
            "localStorage.setItem('allure-theme', 'light');"
            "localStorage.setItem('ALLURE_REPORT_SETTINGS', JSON.stringify("
            "{sidebarCollapsed: false, sideBySidePosition: [42, 58], language: 'fr'}));"
        )
        self.driver.refresh()

    def click_text(self, css, text, last=False):
        """Clicks the element of class css containing text: the first one, or the last one."""
        xpath = f"//*[contains(concat(' ', @class, ' '), ' {css} ')][contains(., \"{text}\")]"
        locator = (By.XPATH, f"({xpath})[last()]" if last else xpath)
        element = self.wait.until(ec.element_to_be_clickable(locator))
        self.driver.execute_script("arguments[0].scrollIntoView({block: 'center'});", element)
        element.click()
        time.sleep(0.8)

    def scroll_pane_to(self, css, text):
        locator = (By.XPATH, f"//*[contains(concat(' ', @class, ' '), ' {css} ')][contains(., \"{text}\")]")
        element = self.wait.until(ec.presence_of_element_located(locator))
        self.driver.execute_script("arguments[0].scrollIntoView({block: 'start'});", element)
        time.sleep(0.5)

    def shot(self, name):
        OUT.mkdir(parents=True, exist_ok=True)
        path = OUT / f"{name}.png"
        self.driver.save_screenshot(str(path))
        print(f"{path.relative_to(OUT.parents[1])}")

    def resize(self, width, height):
        """Exact viewport size, whatever the size of the browser window decorations."""
        self.driver.execute_cdp_cmd(
            "Emulation.setDeviceMetricsOverride",
            {"width": width, "height": height, "deviceScaleFactor": 1, "mobile": False},
        )

    def quit(self):
        self.driver.quit()


def site_pages(cam, site):
    cam.resize(1100, 1000)
    cam.open(f"{site}/")
    cam.shot("pages-accueil")

    cam.resize(1440, 900)
    cam.allure_settings(f"{site}/tous/")
    cam.open(f"{site}/tous/#suites")
    for browser in ("chrome", "firefox"):
        cam.click_text("node__title", browser)
    # The same scenario, run on Firefox: "Navigateur" parameter
    cam.click_text("node__title", "Plusieurs navigateurs", last=True)
    cam.click_text("node__title", "Deux clients connectés", last=True)
    cam.shot("allure-combine-navigateurs")


def report_pages(cam, report):
    cam.resize(1440, 900)
    cam.allure_settings(f"{report}/")
    cam.open(f"{report}/#")
    cam.shot("allure-apercu")

    # A UI scenario: its steps
    cam.open(f"{report}/#suites")
    cam.click_text("node__title", "Onglets et fenêtres multiples")
    cam.click_text("node__title", "Naviguer entre des onglets")
    cam.shot("allure-scenario")

    # An API scenario: request and response attached to the step, secrets masked
    cam.open(f"{report}/#suites")
    cam.click_text("node__title", "API d'authentification")
    cam.click_text("node__title", "Un utilisateur valide obtient un jeton")
    cam.click_text("step__title", "je m'authentifie sur l'API")
    cam.click_text("attachment-row", "Requête POST")
    cam.scroll_pane_to("step__title", "je m'authentifie sur l'API")
    cam.shot("allure-api")

    # A real failure: error, screenshot and video of the Grid session
    cam.open(f"{report}/#suites")
    cam.click_text("node__title", "Démonstration des échecs")
    cam.click_text("node__title", "Le catalogue n'a pas")
    cam.click_text("step__title", "Post exécution")
    cam.click_text("step__title", "captureFailureEvidence")
    cam.click_text("attachment-row", "Screenshot")
    cam.scroll_pane_to("step__title", "Alors le catalogue contient")
    cam.shot("allure-echec")

    # A flaky scenario: failed on the first pass, passed on the rerun
    cam.open(f"{report}/#suites")
    cam.click_text("node__title", "Démonstration des échecs")
    cam.click_text("node__title", "Un test instable")
    cam.click_text("tab", "Tentatives")
    cam.shot("allure-instable")


def jenkins_pages(cam, jenkins):
    password = os.environ["JENKINS_ADMIN_PASSWORD"]
    cam.resize(1440, 900)
    cam.open(f"{jenkins}/login")
    cam.driver.find_element(By.ID, "j_username").send_keys("admin")
    cam.driver.find_element(By.ID, "j_password").send_keys(password)
    cam.driver.find_element(By.NAME, "Submit").click()
    time.sleep(2)
    job = f"{jenkins}/job/selenium-bdd-framework"
    cam.resize(1440, 1180)
    cam.open(f"{job}/", settle=3)
    cam.shot("jenkins-job")
    cam.resize(1440, 900)
    cam.open(f"{job}/lastSuccessfulBuild/", settle=2)
    cam.click_text("task-link", "Pipeline Overview")
    time.sleep(3)
    cam.shot("jenkins-pipeline")
    cam.allure_settings(f"{job}/lastSuccessfulBuild/allure/")
    cam.open(f"{job}/lastSuccessfulBuild/allure/", settle=3)
    cam.shot("jenkins-allure")


def main():
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--report")
    parser.add_argument("--site")
    parser.add_argument("--jenkins")
    args = parser.parse_args()
    cam = Camera()
    try:
        if args.site:
            site_pages(cam, args.site.rstrip("/"))
        if args.report:
            report_pages(cam, args.report.rstrip("/"))
        if args.jenkins:
            jenkins_pages(cam, args.jenkins.rstrip("/"))
    finally:
        cam.quit()


if __name__ == "__main__":
    main()
