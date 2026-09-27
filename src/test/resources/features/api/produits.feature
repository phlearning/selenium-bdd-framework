# language: fr
@api
Fonctionnalité: API du catalogue de produits

  @smoke
  Scénario: Consulter un produit
    Quand je consulte le produit 1
    Alors la réponse a le statut 200
    Et la réponse respecte le schéma "produit"
    Et le champ "id" de la réponse vaut "1"
    Et la réponse arrive en moins de 3000 ms

  Scénario: Un produit inexistant renvoie une erreur 404
    Quand je consulte le produit 99999
    Alors la réponse a le statut 404
    Et le champ "message" de la réponse vaut "Product with id '99999' not found"

  Scénario: Rechercher des produits
    Quand je recherche les produits "phone"
    Alors la réponse a le statut 200
    Et la recherche renvoie au moins 1 produit

  Scénario: Une recherche sans correspondance ne renvoie rien
    Quand je recherche les produits "zzzqqq"
    Alors la réponse a le statut 200
    Et la recherche ne renvoie aucun produit

  Scénario: Créer un produit
    Quand je crée le produit suivant :
      | titre     | Clavier BDD |
      | prix      | 49.9        |
      | catégorie | laptops     |
    Alors la réponse a le statut 201
    Et le produit créé reprend ces informations avec un nouvel identifiant
