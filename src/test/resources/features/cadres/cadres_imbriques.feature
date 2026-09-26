# language: fr
@cadres
Fonctionnalité: Cadres imbriqués
  Le framework lit le contenu de cadres imbriqués et revient toujours
  au document principal ensuite.

  Contexte:
    Soit je suis sur la page des cadres imbriqués

  Plan du scénario: Lire le cadre <chemin>
    Alors le cadre "<chemin>" contient le texte "<texte>"
    Et le document principal contient 2 cadres

    Exemples:
      | chemin                   | texte  |
      | frame-top > frame-left   | LEFT   |
      | frame-top > frame-middle | MIDDLE |
      | frame-top > frame-right  | RIGHT  |
      | frame-bottom             | BOTTOM |
