# language: fr
@fenetres
Fonctionnalité: Onglets et fenêtres multiples
  Le framework pilote plusieurs onglets et fenêtres d'un même navigateur,
  désignés par des alias plutôt que par des identifiants techniques.

  Contexte:
    Soit je suis sur la page des fenêtres multiples

  @smoke
  Scénario: Un lien ouvre une nouvelle fenêtre que l'on pilote puis que l'on referme
    Quand je clique sur le lien qui ouvre la fenêtre "nouvelle"
    Alors 2 fenêtres sont ouvertes
    Et la fenêtre "nouvelle" est active
    Et la page affiche le titre "New Window"
    Quand je ferme la fenêtre "nouvelle"
    Alors 1 fenêtres sont ouvertes
    Et la fenêtre "principale" est active
    Et la page affiche le titre "Opening a new window"

  Scénario: Naviguer entre des onglets et des fenêtres ouverts par le test
    Quand j'ouvre un nouvel onglet "alertes" sur la page "/javascript_alerts"
    Et j'ouvre une nouvelle fenêtre "téléversement" sur la page "/upload"
    Alors 3 fenêtres sont ouvertes
    Quand je bascule sur la fenêtre "alertes"
    Alors la page affiche le titre "JavaScript Alerts"
    Quand je bascule sur la fenêtre "principale"
    Alors la page affiche le titre "Opening a new window"
    Quand je bascule sur la fenêtre "téléversement"
    Alors la page affiche le titre "File Uploader"

  Scénario: Retrouver une fenêtre par le titre de son onglet
    Quand je clique sur le lien qui ouvre la fenêtre "nouvelle"
    Et je bascule sur la fenêtre "principale"
    Et je bascule sur la fenêtre intitulée "New Window"
    Alors le titre de l'onglet est "New Window"
    Et la fenêtre "nouvelle" est active
