# language: fr
@demo
Fonctionnalité: Démonstration des échecs dans le rapport
  Scénarios volontairement en échec, qui montrent dans le rapport un défaut réel, un test instable
  et une régression visuelle.
  Ils ne sont sur le classpath qu'avec le profil Maven demo-scenarios : ./mvnw clean test -Pdemo-scenarios

  Scénario: Le catalogue n'a pas le nombre de produits attendu
    Soit je suis sur la page de connexion
    Quand je me connecte avec l'utilisateur standard
    Alors le catalogue contient 7 produits

  Scénario: Un test instable réussit au rejeu
    Soit je suis sur la page de connexion
    Quand je me connecte avec l'utilisateur standard
    Et le réseau est instable au premier passage
    Alors la page "Products" est affichée

  @visuel
  Scénario: Une régression visuelle est détectée
    Soit je suis sur la page de connexion
    Quand le bouton de connexion change de couleur
    Alors l'apparence de la page est conforme à la référence "boutique-connexion"
