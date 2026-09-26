# language: fr
@fichiers
Fonctionnalité: Téléversement et téléchargement de fichiers
  Fonctionne aussi bien avec un navigateur local qu'avec un navigateur de la Grid :
  le fichier à envoyer est transmis au nœud, le fichier reçu est rapatrié depuis le nœud.

  Scénario: Téléverser un fichier de test
    Soit je suis sur la page de téléversement
    Quand je téléverse le fichier "exemple.txt"
    Alors la page affiche le titre "File Uploaded!"
    Et le fichier "exemple.txt" figure parmi les fichiers reçus

  Scénario: Télécharger un fichier
    Soit je suis sur la page de téléchargement
    Quand je télécharge le fichier "some-file.txt"
    Alors le fichier "some-file.txt" est téléchargé et n'est pas vide
