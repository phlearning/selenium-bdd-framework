# language: fr
@dialogues
Fonctionnalité: Boîtes de dialogue JavaScript
  Le framework lit, accepte, refuse et renseigne les boîtes de dialogue natives
  du navigateur (alert, confirm, prompt).

  Contexte:
    Soit je suis sur la page des alertes JavaScript

  @smoke
  Scénario: Accepter une alerte
    Quand je clique sur le bouton "Click for JS Alert"
    Alors une boîte de dialogue affiche "I am a JS Alert"
    Quand j'accepte la boîte de dialogue
    # Le libellé de l'application contient une faute de frappe ("successfuly").
    Alors le résultat affiché est "You successfuly clicked an alert"

  Scénario: Refuser une confirmation
    Quand je clique sur le bouton "Click for JS Confirm"
    Alors une boîte de dialogue affiche "I am a JS Confirm"
    Quand je refuse la boîte de dialogue
    Alors le résultat affiché est "You clicked: Cancel"

  Scénario: Répondre à une invite
    Quand je clique sur le bouton "Click for JS Prompt"
    Et je réponds "Bonjour" à la boîte de dialogue
    Alors le résultat affiché est "You entered: Bonjour"
