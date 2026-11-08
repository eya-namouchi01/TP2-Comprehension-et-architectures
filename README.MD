# HAI913I — TP 2 : Compréhension et architectures

Outil en ligne de commande qui part des faits extraits d'un projet Java (classes, méthodes, graphe d'appel), calcule le couplage entre classes, regroupe les classes par clustering hiérarchique ascendant, construit le dendrogramme et le coupe en modules selon un seuil CP.
---

## 1. Projet

### Chemin d'entrée

Le projet utilise le **chemin 2** du sujet (SECTION 4) : le jeu de données canonique fourni (`resources/`, format CSV). Les faits sont lus par la classe `Facts` du starter.

Le reste du programme ne lit jamais les fichiers CSV : il passe par l'interface `FactsInterface`, implémentée par `Facts`. Un adaptateur du modèle de faits du TP 1 pourrait implémenter la même interface sans rien changer d'autre.

### Structure

```
TP2-Compréhension-et-architectures/
|-- resources/                  jeu de données canonique (fourni)
|-- examples/                   micro jeu de données facts-mini et démonstrations (fournis)
|-- starter/                    le projet Maven
    |-- pom.xml
    |-- sorties/                fichiers produits par l'outil (voir plus bas)
    |-- src/main/java/hai913i/tp2/
    |   |-- Main.java               lecture des arguments, enchaînement des étapes
    |   |-- FactsInterface.java     interface d'entrée : classes, méthodes, arcs
    |   |-- Facts.java              lecture des CSV (fourni, implémente FactsInterface)
    |   |-- CouplingFunction.java   interface : un nombre pour deux classes
    |   |-- Coupling.java           A1 : couplage C(A,B)
    |   |-- CouplingGraph.java      A2 : graphe pondéré, export CSV et DOT, rechargement
    |   |-- Cluster.java            un groupe (un noeud du dendrogramme)
    |   |-- Clustering.java         B1 : regroupement ascendant, lien moyen
    |   |-- Dendrogram.java         B2 : vérification et exports du dendrogramme
    |   |-- Cut.java                B3 : coupe en modules, contrainte M/2
    |   |-- CpScan.java             B4 : effet de CP sur les modules (option --scan)
    |   |-- Report.java             textes affichés à l'écran
    |-- src/test/java/hai913i/tp2/  tests JUnit (un fichier par étape)
    |-- src/test/resources/         micro jeu de données et copie du jeu de validation
```

### Architecture

Chaque responsabilité de l'annexe est dans une classe distincte :

```
Facts (FactsInterface) -> Coupling -> CouplingGraph
                              |
                               -> Clustering -> Dendrogram
                                       |
                                        -> Cut -> modules
                                            `-> CpScan (option --scan)
```

- `Coupling` est la seule classe qui lit les arcs d'appel.
- `Clustering` et `Cut` ne connaissent que l'interface `CouplingFunction` : ils reçoivent des noms de classes et une fonction qui donne C(A,B).
- L'affichage est regroupé dans `Report`, et l'écriture des fichiers dans `Main`.

### Conventions appliquées 

| Point              | Convention                                                                                                                                                        |
|--------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Relation comptée   | l'appel de méthode uniquement                                                                                                                                     |
| Couplage           | C(A,B) = r(A,B) / \|E_inter\|, en comptant des arcs (et non des sites d'appel) ; arcs internes à une classe écartés ; sens de l'appel ignoré                      |
| Critère de liaison | lien moyen, sur tous les couples entre les deux groupes, zéros compris                                                                                            |
| Égalités           | groupes numérotés de 0 à M − 1 par ordre alphabétique du nom qualifié, puis M, M + 1… ; à égalité (tolérance 1e-9), on retient le plus petit couple (min, max)    |
| Coupe              | depuis la racine : une feuille est un module ; un noeud est un module si son couplage interne moyen C(G) > CP (strictement) ; sinon on descend dans ses deux fils |
| Contrainte         | au plus M/2 modules (division entière) ; une violation est signalée, jamais corrigée                                                                              |

### Entrées

Un dossier contenant trois fichiers CSV de même préfixe : `<préfixe>-classes.csv`, `<préfixe>-methods.csv` et `<préfixe>-calls.csv` (format décrit dans `resources/README.txt`). Le préfixe est déduit automatiquement.

### Sorties

Affichage à l'écran : les compteurs A0, puis les résultats de A1, A2, B1, B2 et B3, et l'effet de CP si l'option `--scan` est donnée.

Fichiers écrits dans le dossier `sorties/` du répertoire de travail :

| Fichier                   | Contenu                                                                         | Étape |
|---------------------------|---------------------------------------------------------------------------------|-------|
| `graphe.csv`              | graphe pondéré (noeuds et arêtes), rechargeable par le programme                | A2    |
| `graphe.dot`              | graphe pondéré pour Graphviz (épaisseur des arêtes proportionnelle au couplage) | A2    |
| `dendrogramme.txt`        | arbre indenté                                                                   | B2    |
| `fusions.csv`             | liste des fusions : étape, similarité, groupe A, groupe B                       | B2    |
| `dendrogramme.dot`        | dendrogramme pour Graphviz                                                      | B2    |

Les images `graphe.png` et `dendrogramme.png` sont produites avec Graphviz (section 4).

### Dépendances

Aucune dépendance d'exécution. JUnit Jupiter 5.14.4 est utilisé pour les tests uniquement (déclaré dans `pom.xml`). Graphviz sert seulement à transformer les fichiers DOT en images.

### Limites connues

- Seul l'appel de méthode est compté : l'héritage, l'implémentation d'interface, les types d'attributs, l'instanciation et les exceptions ne créent pas de lien. Sur le projet de validation, Dvd et LoanException sont donc isolées.
- Le seuil CP vaut 0,025 par défaut (valeur reprise du starter) ; ce n'est pas une valeur universelle, d'où l'option `--scan`.
- Le format JSON du jeu de données n'est pas utilisé.
- Pas d'interface graphique.

---

## 2. Environnement

| Élément  | Version                                                                  |
|----------|--------------------------------------------------------------------------|
| Système  | Ubuntu 24.04.3 LTS                                                       |
| IDE      | IntelliJ IDEA 2026.2.3 (build IU-262.10968.63)                           |
| JDK      | Eclipse Temurin 17.0.20.1                                                |
| Maven    | 3.9.16                                                                   |
| JUnit    | Jupiter 5.14.4                                                           |
| Graphviz | 2.43.0                                                                   |


---

## 3. Construction

Depuis une copie fraîche du projet, dans le dossier `starter` :

```bash
cd starter
mvn clean package
```

Cette commande compile le code, lance les tests et produit `target/hai913i-tp2.jar`. Pour lancer uniquement les tests :

```bash
mvn clean test
```
---

## 4. Exécution

Toujours depuis le dossier `starter` :

```bash
java -jar target/hai913i-tp2.jar <dossier-des-faits> [--cp <valeur>] [--prefix <préfixe>] [--scan]
```

| Argument              | Rôle                                                                                                                                                            |
|-----------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `<dossier-des-faits>` | dossier qui contient les trois fichiers CSV (obligatoire)                                                                                                       |
| `--cp <valeur>`       | seuil de couplage interne moyen pour la coupe (défaut : 0.025)                                                                                                  |
| `--prefix <préfixe>`  | force le préfixe des fichiers ; par défaut il est déduit du dossier                                                                                             |
| `--scan`              | affiche le couplage interne moyen de chaque noeud de l'arbre, puis les modules obtenus quand CP augmente par pas de 0,001, jusqu'à la première violation de M/2 |

### Exemples

```bash
# projet de validation, CP par défaut (0.025)
java -jar target/hai913i-tp2.jar ../resources

# pour trois valeurs de CP 
java -jar target/hai913i-tp2.jar ../resources --cp 0.015
java -jar target/hai913i-tp2.jar ../resources --cp 0.025
java -jar target/hai913i-tp2.jar ../resources --cp 0.045

# effet de CP, pour choisir le seuil
java -jar target/hai913i-tp2.jar ../resources --scan

# micro jeu de données
java -jar target/hai913i-tp2.jar ../examples --cp 0.20
java -jar target/hai913i-tp2.jar ../examples --cp 0.50
```

### Images Graphviz

Après une exécution, depuis `starter` :

```bash
dot -Tpng sorties/graphe.dot -o sorties/graphe.png
dot -Tpng sorties/dendrogramme.dot -o sorties/dendrogramme.png
```

### Dans IntelliJ IDEA

1. Ouvrir le dossier `starter` comme projet Maven.
2. Créer une configuration Run de type Application : classe principale `hai913i.tp2.Main`, arguments `../resources` (ajouter par exemple `--cp 0.045` ou `--scan`), répertoire de travail `starter`.
3. Pour les tests : clic droit sur `src/test/java`, puis Run 'All Tests'.

### Codes de sortie

| Code | Signification                                                                                         |
|------|-------------------------------------------------------------------------------------------------------|
| 0    | exécution terminée (y compris quand la contrainte M/2 est violée : c'est un résultat, pas une erreur) |
| 1    | arguments invalides (dossier manquant, option inconnue, CP non numérique, négatif)                    |
| 2    | fichiers de faits illisibles, ou écriture des sorties impossible                                      |

---

## 5. Validation

### Jeux de données

- **Projet de validation** : `resources/` (15 classes, 59 méthodes, 44 arcs).
- **Micro jeu de données** : `examples/facts-mini` (3 classes), valeurs calculées à la main dans `examples/README.txt`.

### Résultats attendus sur le projet de validation (points de contrôle du sujet)

| Étape          | Attendu                                                                                                                    |
|----------------|----------------------------------------------------------------------------------------------------------------------------|
| A0             | 15 classes, 59 méthodes, 44 arcs                                                                                           |
| A1             | 7 arcs internes écartés, \|E_inter\| = 37, 19 couples non nuls sur 105, somme = 1.0000, Item – LoanService = 4/37 = 0.1081 |
| A2             | 15 noeuds, 19 arêtes, Dvd et LoanException isolées, rechargement avec les mêmes valeurs                                    | 
| B1             | 14 fusions ; 0.1081, 0.0811, 0.0608 ; similarités jamais croissantes ; deux dernières à 0                                  | 
| B2             | 15 feuilles, 14 noeuds internes, arbre valide                                                                              | 
| B3, CP = 0.015 | 4 modules, contrainte respectée (un module de 11 classes)                                                                  | 
| B3, CP = 0.025 | 7 modules, contrainte respectée (7 = M/2)                                                                                  | 
| B3, CP = 0.045 | 10 modules, contrainte violée                                                                                              | 

Avec `--scan`, la contrainte est respectée jusqu'à 7 modules, pour CP strictement inférieur à 0.0270 ; la même partition en 7 modules est obtenue pour CP dans [0.0233 ; 0.0270).

### Résultats attendus sur le micro jeu de données

- A0 : 3 classes, 5 méthodes, 4 arcs.
- A1 : \|E_inter\| = 3, C(A, B) = 0.6667, C(B, C) = 0.3333.
- B1 : {A} + {B} à 0.6667, puis {A, B} + {C} à 0.1667.
- B3 : CP = 0.20 donne 1 module (respectée) ; CP = 0.50 donne 2 modules (violée).

### Tests automatiques

`mvn clean test` lance un fichier de tests par étape :

| Fichier             | Vérifie                                                                                                                                 |
|---------------------|-----------------------------------------------------------------------------------------------------------------------------------------|
| `FactsTest`         | A0 : chargement du micro jeu de données (fourni avec le starter)                                                                        |
| `CouplingTest`      | A1 : valeurs du micro jeu, symétrie, diagonale nulle, somme = 1, point de contrôle                                                      |
| `CouplingGraphTest` | A2 : noeuds, arêtes, classes isolées, sauvegarde puis rechargement                                                                      |
| `ClusteringTest`    | B1 : fusions attendues, similarités jamais croissantes, deux exécutions identiques, règle de départage sur la matrice de `TieBreakDemo` |
| `DendrogramTest`    | B2 : nombre de feuilles et de noeuds, union disjointe des membres, racine complète                                                      |
| `CutTest`           | B3 : nombre de modules pour les trois CP, partition des classes, C(G) > CP                                                              |

Les tests utilisent `src/test/resources/` (micro jeu de données) et `src/test/resources/validation/` (copie du jeu de validation).

### Reproductibilité

Deux exécutions successives donnent une sortie identique :

```bash
java -jar target/hai913i-tp2.jar ../resources > run1.txt
java -jar target/hai913i-tp2.jar ../resources > run2.txt
cmp run1.txt run2.txt     # aucun affichage : les fichiers sont identiques
```

---

