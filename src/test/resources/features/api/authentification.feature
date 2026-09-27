# language: fr
@api
Fonctionnalité: API d'authentification
  L'API délivre un jeton d'accès aux utilisateurs authentifiés ;
  ce jeton est exigé pour consulter son profil.

  @smoke
  Scénario: Un utilisateur valide obtient un jeton et consulte son profil
    Quand je m'authentifie sur l'API avec l'utilisateur de démonstration
    Alors la réponse a le statut 200
    Et la réponse respecte le schéma "jeton"
    Et la réponse contient un jeton d'accès
    Quand je consulte mon profil avec ce jeton
    Alors la réponse a le statut 200
    Et la réponse respecte le schéma "profil"
    Et le profil est celui de l'utilisateur de démonstration

  Scénario: Un mot de passe incorrect est refusé
    Quand je m'authentifie sur l'API avec l'identifiant "emilys" et le mot de passe "incorrect"
    Alors la réponse a le statut 400
    Et le champ "message" de la réponse vaut "Invalid credentials"

  Plan du scénario: Le profil est protégé : <cas>
    Quand je consulte mon profil <jeton>
    Alors la réponse a le statut 401
    Et le champ "message" de la réponse vaut "<message>"

    Exemples:
      | cas             | jeton                   | message                  |
      | sans jeton      | sans jeton              | Access Token is required |
      | jeton invalide  | avec le jeton "abc.def" | Invalid/Expired Token!   |
