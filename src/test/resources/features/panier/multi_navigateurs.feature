# language: fr
@panier @multi-navigateurs
Fonctionnalité: Plusieurs navigateurs dans un même scénario
  Un scénario peut piloter plusieurs navigateurs indépendants à la fois,
  par exemple deux clients connectés en même temps.

  Scénario: Deux clients connectés en même temps ont chacun leur panier
    Soit l'utilisateur standard est connecté dans le navigateur "Alice"
    Et l'utilisateur standard est connecté dans le navigateur "Bob"
    Quand dans le navigateur "Alice", j'ajoute le produit "Sauce Labs Backpack" au panier
    Et dans le navigateur "Alice", j'ajoute le produit "Sauce Labs Bike Light" au panier
    Alors dans le navigateur "Alice", le panier contient 2 articles
    Et dans le navigateur "Bob", le panier est vide
    Quand dans le navigateur "Bob", j'ajoute le produit "Sauce Labs Onesie" au panier
    Alors dans le navigateur "Bob", le panier contient 1 article
    Et dans le navigateur "Alice", le panier contient 2 articles
