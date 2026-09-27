# language: fr
@accessibilite
Fonctionnalité: Accessibilité
  axe-core audite la page affichée selon les règles WCAG 2.1 de niveaux A et AA.
  Le détail de l'audit (règles, éléments en cause, documentation) est joint au rapport.

  Scénario: La page de connexion de la boutique est accessible
    Soit je suis sur la page de connexion
    Alors la page respecte les règles d'accessibilité WCAG 2.1 AA

  Scénario: Le formulaire de connexion de the-internet n'a pas d'autre défaut que ses écarts connus
    Soit je suis sur le formulaire de connexion de the-internet
    Alors la page respecte les règles d'accessibilité WCAG 2.1 AA, hormis les écarts connus :
      | règle          | raison                                                                    |
      | color-contrast | texte d'aide et lien de pied de page trop peu contrastés, défaut du site |
