# TD Test It – Bugs trouvés et corrections
Malak RAI

Mes tests ont trouvé 14 bugs dans le code. Je les ai tous corrigés.

## BookServiceTest

**Bug 1 : aucun mail à l'emprunt**
- Correction : ajout de `mailService.sendMail(...)` dans `borrowBook`.
- Pourquoi : le README dit qu'un emprunt doit envoyer un mail au membre.

## BookRepositoryIT

**Bug 2 : les clés étrangères n'ont pas de nom**
- Correction : ajout de `constraintName` dans les deux fichiers Liquibase.
- Pourquoi : Liquibase refuse de créer la base sans ce nom.

**Bug 3 : la colonne s'appelle `user`**
- Correction : renommée en `username`.
- Pourquoi : le champ Java s'appelle `username`.

**Bug 4 : `referent_id` est en `uuid`**
- Correction : changé en `bigint`.
- Pourquoi : l'id d'un membre est un `bigint`.

**Bug 5 : Java ne trouve pas la table**
- Correction : ajout de `@Table(name = "members")` dans `Member.java`.
- Pourquoi : la table s'appelle `members`, mais Java cherche `member` par défaut.

**Bug 6 : mauvaise génération de l'id**
- Correction : `SEQUENCE` remplacé par `IDENTITY` dans `Book.java`.
- Pourquoi : la table utilise l'auto-incrément, pas une séquence.

**Bug 7 : `OR` au lieu de `AND` dans la requête**
- Correction : `OR` remplacé par `AND` dans `BookRepository.java`.
- Pourquoi : on veut les livres de ce membre ET avec ce statut. Avec `OR`, la requête renvoyait aussi les livres des autres.

## BookServiceIT

**Bug 8 : `LazyInitializationException`**
- Correction : ajout de `@Transactional` sur `BookService`.
- Pourquoi : sans transaction, la connexion à la base est fermée avant de lire le membre.

## BookControllerIT

**Bug 9 : l'auteur est remplacé par le titre**
- Correction : `request.title` remplacé par `request.author` dans `createBook`.
- Pourquoi : le titre était passé deux fois.

**Bug 10 : l'emprunt répond 400**
- Correction : `returnBook` remplacé par `borrowBook` dans la méthode `/borrow`.
- Pourquoi : l'URL d'emprunt appelait la méthode qui rend le livre.

**Bug 11 : l'emprunt répond 500**
- Correction : ajout de `@JsonIgnore` sur `books` et `referent` dans `Member.java`.
- Pourquoi : Spring n'arrivait pas à mettre ces champs dans le JSON, parce qu'ils ne sont pas chargés depuis la base (`LAZY`).

## Bonus BookServiceTest

J'ai ajouté 8 tests : livre inconnu, membre inconnu, livre déjà emprunté, membre qui a déjà un livre en cours, retour par un autre membre, retour sans référent, retour avec référent, et un test avec un fake à la place du mock pour les mails.

**Bug 12 : un membre peut emprunter plusieurs livres**
- Correction : `Status.RENDU` remplacé par `Status.EMPRUNTE` dans `borrowBook`.
- Pourquoi : pour savoir si le membre a déjà un livre en cours, il faut chercher ses livres EMPRUNTE, pas ses livres RENDU.

**Bug 13 : n'importe qui peut rendre un livre**
- Correction : ajout d'une vérification dans `returnBook` : si ce n'est pas l'emprunteur, on lève une `IllegalStateException`.
- Pourquoi : le README dit que seul le membre qui a emprunté le livre peut le rendre.

**Bug 14 : le référent n'est jamais prévenu**
- Correction : `member` remplacé par `member.getReferent()` dans le 2ᵉ `sendMail` de `returnBook`.
- Pourquoi : le 2ᵉ mail partait au membre (il en recevait 2) au lieu de partir au référent.