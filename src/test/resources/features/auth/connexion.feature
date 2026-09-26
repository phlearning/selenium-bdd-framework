# language: fr
@auth
Fonctionnalité: Connexion à la boutique
  En tant que client de la boutique
  Je veux me connecter avec mes identifiants
  Afin d'accéder au catalogue des produits

  Contexte:
    Soit je suis sur la page de connexion

  @smoke
  Scénario: Connexion réussie avec un utilisateur standard
    Quand je me connecte avec l'utilisateur standard
    Alors la page "Products" est affichée
    Et le catalogue contient 6 produits

  @regression
  Plan du scénario: Connexion refusée : <cas>
    Quand je me connecte avec l'identifiant "<identifiant>" et le mot de passe "<mot de passe>"
    Alors le message d'erreur "<message>" est affiché

    Exemples:
      | cas                    | identifiant   | mot de passe | message                                                                   |
      | mot de passe incorrect | standard_user | incorrect    | Epic sadface: Username and password do not match any user in this service |
      | identifiant manquant   |               | incorrect    | Epic sadface: Username is required                                        |
      | mot de passe manquant  | standard_user |              | Epic sadface: Password is required                                        |
