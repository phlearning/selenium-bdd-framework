# language: fr
@televersement
Fonctionnalité: Téléversement de fichiers

  Scénario: Téléverser un fichier de test
    Soit je suis sur la page de téléversement
    Quand je téléverse le fichier "exemple.txt"
    Alors la page affiche le titre "File Uploaded!"
    Et le fichier "exemple.txt" figure parmi les fichiers reçus
