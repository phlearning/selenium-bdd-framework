# language: fr
@visuel
Fonctionnalité: Régression visuelle
  La page affichée est comparée pixel à pixel à une image de référence, par navigateur.
  Les références sont prises sur la Selenium Grid : ces scénarios n'y tournent que là.
  En cas d'écart, le rapport montre la référence, la capture et les différences.

  Scénario: La page de connexion de la boutique n'a pas changé d'apparence
    Soit je suis sur la page de connexion
    Alors l'apparence de la page est conforme à la référence "boutique-connexion"

  Scénario: Le formulaire de connexion de the-internet n'a pas changé d'apparence
    Soit je suis sur le formulaire de connexion de the-internet
    Alors l'apparence de la page est conforme à la référence "the-internet-connexion"
