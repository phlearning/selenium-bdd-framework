# language: fr
@bidi
Fonctionnalité: Erreurs JavaScript et trafic réseau
  WebDriver BiDi remonte en direct ce qui se passe dans le navigateur : erreurs JavaScript
  et requêtes en échec, joints au rapport de tout scénario en échec. Il permet aussi de
  faire échouer des requêtes pour vérifier comment l'application s'en accommode.

  Scénario: Une page saine ne produit aucune erreur JavaScript
    Soit je suis sur le formulaire de connexion de the-internet
    Alors la console du navigateur ne contient aucune erreur

  Scénario: Une erreur JavaScript levée au chargement de la page est détectée
    Quand j'ouvre une page qui déclenche une erreur JavaScript au chargement
    Alors la console du navigateur signale 1 erreur

  Scénario: Les images introuvables sur le serveur sont repérées
    Quand j'ouvre la page des images cassées
    Alors 2 images de la page renvoient une erreur 404

  Scénario: La boutique reste utilisable quand ses images ne se chargent pas
    Soit je suis sur la page de connexion
    Et les requêtes vers ".jpg" échouent
    Quand je me connecte avec l'utilisateur standard
    Alors le catalogue contient 6 produits
    Et aucune image de produit n'est affichée
