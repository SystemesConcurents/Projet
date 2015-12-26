=======
Objets dupliqués
=======

Le répertoire ``src`` contient les sources du projet.

Tests
=====

Les fichier dont le nom termine par « Fuzzer » sont exécutables,
et permettent de tester certaines fonctionnalités.

Pour tester : ::

    $ rmiserver
    $ java -ea Server
    $ java -ea Fuzzer

Il est possible de lancer plusieurs fuzzer en parallèle.
Pour le fuzzer simple, on peut lancer en parallèle un ou plusieurs « Irc » pour vérifier le fonctionnement.
