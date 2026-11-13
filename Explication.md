# TD Test It - Bugs trouvés et corrections

Author: Malak RAI
---

## Exercice 1 - BookServiceTest

**Bug 1 - pas de mail quand on emprunte**
Mon test vérifiait qu'un mail était envoyé au membre, mais Mockito disait que `sendMail` n'était jamais appelé.
J'ai ajouté `mailService.sendMail(...)` à la fin de `borrowBook`, parce que le README dit qu'un emprunt envoie un mail.

---

## Exercice 2 - BookRepositoryIT

Ici le test ne démarrait même pas : la base créée par Liquibase ne correspondait pas aux classes Java. J'ai dû corriger plusieurs choses avant que le test se lance.

**Bug 2 - clés étrangères sans nom**
Liquibase refusait de créer la base.
J'ai ajouté `constraintName` dans les deux fichiers XML.

**Bug 3 - colonne `user` au lieu de `username`**
Dans la classe Member le champ s'appelle `username`, donc la colonne doit avoir le même nom.
J'ai renommé la colonne.

**Bug 4 - `referent_id` en `uuid`**
L'id d'un membre est un `bigint`, donc la colonne qui pointe vers un membre doit aussi être en `bigint`.
J'ai changé le type.

**Bug 5 - table introuvable**
La table s'appelle `members` mais Java cherchait `member`.
J'ai ajouté `@Table(name = "members")` dans `Member.java`.

**Bug 6 - mauvaise génération de l'id**
`Book` utilisait `SEQUENCE` alors que la table est en auto-incrément.
J'ai mis `IDENTITY`.

**Bug 7 - OR au lieu de AND dans la requête**
Une fois la base réparée, le test a enfin tourné et il trouvait 3 livres au lieu de 1. Avec `OR`, la requête renvoyait aussi les livres de Nour et les livres déjà rendus.
J'ai remplacé `OR` par `AND`, parce qu'on veut les livres de ce membre ET avec ce statut.

---

## Exercice 3 - BookServiceIT

**Bug 8 - LazyInitializationException**
Le membre n'est chargé depuis la base qu'au moment où on s'en sert (`LAZY`), et comme il n'y avait pas de transaction, la connexion était déjà fermée.
J'ai ajouté `@Transactional` sur `BookService`.

---

## Exercice 4 - BookControllerIT

**Bug 9 - l'auteur était remplacé par le titre**
Le JSON renvoyait « Les Misérables » comme auteur. Dans `createBook`, le titre était passé deux fois.
J'ai mis `request.author` en deuxième paramètre.

**Bug 10 - l'emprunt répondait 400**
La méthode `/borrow` appelait `returnBook` au lieu de `borrowBook`. Je pense que c'est un copier-coller de la méthode `/return`.
J'ai remplacé par `borrowBook`.

**Bug 11 - l'emprunt répondait 500**
Spring essayait de mettre dans le JSON la liste des livres du membre et son référent, alors qu'ils n'étaient pas chargés depuis la base.
J'ai ajouté `@JsonIgnore` sur `books` et `referent` dans `Member.java`.

---

## Bonus

### Tests en plus dans BookServiceTest

J'ai testé : livre inconnu, membre inconnu, livre déjà emprunté, membre qui a déjà un livre, retour par un autre membre, retour avec et sans référent. J'ai aussi fait un test avec un fake à la place du mock pour les mails (une petite classe qui garde les destinataires dans une liste).

**Bug 12 - on pouvait emprunter plusieurs livres**
Pour vérifier si le membre avait déjà un livre, le code cherchait ses livres RENDU au lieu de ses livres EMPRUNTE.
J'ai remplacé `Status.RENDU` par `Status.EMPRUNTE`.

**Bug 13 - n'importe qui pouvait rendre un livre**
Le code ne vérifiait pas qui rendait le livre.
J'ai ajouté une vérification : si ce n'est pas l'emprunteur, ça lève une `IllegalStateException`.

**Bug 14 - le référent n'était jamais prévenu**
Le membre recevait 2 mails et le référent aucun, parce que le 2e mail partait au membre.
J'ai remplacé `member` par `member.getReferent()`.

### Tests en plus dans BookControllerIT

J'ai testé de modifier un livre emprunté, supprimer un livre qui n'existe pas, emprunter sans être connecté, et créer un livre avec un titre ou un auteur vide. Les tests avec un titre ou un auteur vide passaient déjà grâce à `@NotBlank`.

**Bug 15 - le PUT remettait le livre à DISPONIBLE**
Le PUT remplaçait tout le livre, donc on perdait le statut et l'emprunteur.
Maintenant `updateBook` récupère le livre et change seulement le titre et l'auteur.

**Bug 16 - erreur 500 sur le PUT**
Même problème que le bug 11, mais avec l'emprunteur du livre.
J'ai mis `FetchType.EAGER` sur `borrower` dans `Book.java`.

**Bug 17 - supprimer un livre qui n'existe pas répondait 204**
204 veut dire « supprimé », ce qui est faux si le livre n'existe pas.
Le contrôleur répond maintenant 404.

**Bug 18 - emprunter sans être connecté faisait planter le serveur**
L'exception n'était pas attrapée.
`borrowBook` et `returnBook` répondent maintenant 401.

---

