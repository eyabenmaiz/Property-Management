# PropertyManagement

Plateforme de **gestion locative** en architecture **microservices** : gestion des biens immobiliers, des locataires, des contrats de location et des paiements de loyers.

Les services communiquent de manière **synchrone** (OpenFeign via Eureka) et **asynchrone** (événements Kafka).

---

## Sommaire

1. [Stack technique](#stack-technique)
2. [Architecture](#architecture)
3. [Services](#services)
4. [Démarrage rapide (Docker Compose)](#démarrage-rapide-docker-compose)
5. [Déploiement Kubernetes](#déploiement-kubernetes-minikube)
6. [Accès aux interfaces](#accès-aux-interfaces)
7. [API REST](#api-rest)
8. [Événements Kafka](#événements-kafka)
9. [Tâches planifiées](#tâches-planifiées)
10. [Monitoring](#monitoring)
11. [CI/CD](#cicd-gitlab)
12. [Structure du dépôt](#structure-du-dépôt)
13. [Configuration et sécurité](#configuration-et-sécurité)

---

## Stack technique

| Domaine | Technologie |
|---|---|
| Langage / Build | Java 17, Maven (wrapper `mvnw` inclus) |
| Framework | Spring Boot, Spring Cloud 2025.1 |
| Découverte de services | Netflix Eureka |
| API Gateway | Spring Cloud Gateway (WebFlux) |
| Communication synchrone | OpenFeign |
| Messagerie | Apache Kafka + Zookeeper (Confluent 7.5.0) |
| Bases de données | PostgreSQL 15 (contrats), MySQL 8.0 (biens, paiements) |
| Documentation API | springdoc-openapi (Swagger UI) |
| Monitoring | Actuator, Micrometer, Prometheus, Grafana |
| Conteneurs / Orchestration | Docker, Docker Compose, Kubernetes (Kustomize) |
| CI | GitLab CI |

---

## Architecture

```
                           ┌──────────────────┐
         Client ─────────► │  Proxy-Service   │  :8080  (API Gateway)
                           └────────┬─────────┘
                                    │ routage dynamique via Eureka
              ┌─────────────────────┼─────────────────────┐
              ▼                     ▼                     ▼
     ┌────────────────┐    ┌────────────────┐    ┌────────────────┐
     │ Property-Service│◄───│ ContractService│◄───│ payment-service│
     │     :8081       │Feign│     :8082      │Feign│     :8083      │
     └───────┬────────┘    └───────┬────────┘    └───────┬────────┘
             │ MySQL               │ PostgreSQL          │ MySQL
             ▼                     ▼                     ▼
        property_db            contract_db           payment_db

     ┌─────────────── Kafka ───────────────┐
     │ property-events   contract-events   payment-events │
     └─────────────────────────────────────┘

     discovery-service (Eureka) :8761  ◄── tous les services s'enregistrent
```

**Flux principal :** création d'un contrat → le ContractService vérifie la disponibilité du bien (Feign), le marque indisponible, enregistre le contrat et publie `CONTRACT_CREATED`. À la fin du contrat (résiliation ou expiration), un événement fait repasser le bien en « disponible ».

---

## Services

| Service | Dossier | Port interne | Rôle | Base de données |
|---|---|---|---|---|
| **discovery-service** | `discovery-service/` | 8761 | Registre Eureka | — |
| **Proxy-Service** | `Proxy-Service/` | 8080 | API Gateway, routage automatique via Eureka | — |
| **Property-Service** | `Property-Service/` | 8081 | Gestion des biens, recherche, statistiques de disponibilité | MySQL `property_db` |
| **ContractService** | `ContractService/` | 8082 | Gestion des locataires et des contrats | PostgreSQL `contract_db` |
| **payment-service** | `payment-service/` | 8083 | Gestion des paiements de loyers, relances | MySQL `payment_db` |

### Modèle de données (résumé)

- **Property** : `type`, `address`, `rentPrice`, `description` (`rooms`, `surface`, `criteria`), `available`
- **Tenant** : `name`, `cin`, `phone`
- **Contract** : `tenantId`, `propertyId`, `startDate`, `endDate`, `monthlyRent`, `active`
- **Payment** : `contractId`, `propertyId`, `amount`, `status` (`PENDING`, `COMPLETED`, `FAILED`, `REFUNDED`), `method` (`CASH`, `BANK_TRANSFER`, `CARD`), `paymentDate`, `description`

---

## Démarrage rapide (Docker Compose)

### Prérequis

- Docker et Docker Compose v2
- (Optionnel, développement local) JDK 17 et Maven

### 1. Configurer les variables d'environnement

```bash
cp .env.example .env
```

Puis éditer `.env` :

| Variable | Description |
|---|---|
| `DB_USER` | Utilisateur des bases de données |
| `DB_PASSWORD` | Mot de passe des bases de données |
| `DB_ROOT_PASSWORD` | Mot de passe root MySQL |
| `GRAFANA_USER` | Identifiant administrateur Grafana |
| `GRAFANA_PASSWORD` | Mot de passe administrateur Grafana |

> Le fichier `.env` ne doit **jamais** être versionné (déjà présent dans `.gitignore`).

### 2. Lancer la stack

```bash
docker compose up -d --build
```

Les `depends_on` + `healthcheck` assurent l'ordre de démarrage : Eureka → bases/Kafka → Property → Contract → Payment → Prometheus. Compter quelques minutes au premier démarrage.

### 3. Vérifier

```bash
docker compose ps
```

Puis ouvrir Eureka (http://localhost:8761) : les 3 services métiers doivent apparaître.

### Arrêter

```bash
docker compose down        # conserve les volumes (données)
docker compose down -v     # supprime aussi les données
```

---

## Déploiement Kubernetes (Minikube)

Les manifests sont dans `k8s/` et assemblés avec Kustomize (namespace `propertymanagement`).

```
k8s/
├── namespace.yaml
├── kustomization.yaml
├── infra/   zookeeper, kafka, db-contract, db-property, db-payment
└── apps/    eureka, proxy, property-service, contract-service, payment-service
```

### Étapes

1. **Construire les images** (elles sont chargées localement dans Minikube, `imagePullPolicy: IfNotPresent`) :

   ```bash
   docker compose build
   minikube image load propertymanagement-eureka:latest
   minikube image load propertymanagement-proxy:latest
   minikube image load propertymanagement-property-service:latest
   minikube image load propertymanagement-contract-service:latest
   minikube image load propertymanagement-payment-service:latest
   ```

2. **Créer le namespace et le secret des identifiants BDD** (les manifests lisent le secret `db-credentials`, qui n'est pas fourni dans le dépôt) :

   ```bash
   kubectl apply -f k8s/namespace.yaml
   kubectl -n propertymanagement create secret generic db-credentials --help
   ```

   Consulter les clés attendues (`secretKeyRef`) dans `k8s/apps/*.yaml` et `k8s/infra/db-*.yaml` avant de créer le secret.

3. **Déployer** :

   ```bash
   kubectl apply -k k8s/
   kubectl -n propertymanagement get pods -w
   ```

4. **Accéder à la passerelle** :

   ```bash
   kubectl -n propertymanagement port-forward svc/proxy 8080:8080
   ```

Pour utiliser les images du registre GitLab après la CI :

```bash
kustomize edit set image propertymanagement-eureka=registry.gitlab.com/<chemin>/discovery-service:<tag>
```

---

## Accès aux interfaces

Ports exposés sur l'hôte avec Docker Compose :

| Interface | URL |
|---|---|
| API Gateway | http://localhost:8080 |
| Eureka | http://localhost:8761 |
| Property-Service (direct) | http://localhost:8091 |
| ContractService (direct) | http://localhost:8082 |
| payment-service (direct) | http://localhost:8083 |
| Swagger UI (par service) | `http://localhost:<port>/swagger-ui.html` |
| Kafka UI | http://localhost:8090 |
| Prometheus | http://localhost:9093 |
| Grafana | http://localhost:3001 |
| PostgreSQL (contrats) | `localhost:5433` |
| MySQL (biens) | `localhost:3307` |
| MySQL (paiements) | `localhost:3308` |
| Kafka (hôte) | `localhost:9092` |

Via la passerelle, le routage est automatique à partir du nom du service (en minuscules) :

```
http://localhost:8080/property-service/properties
http://localhost:8080/contract-service/contracts
http://localhost:8080/payment-service/payments
```

---

## API REST

La documentation interactive complète est disponible dans Swagger UI. Aperçu des endpoints :

### Property-Service — `/properties`

| Méthode | Route | Description |
|---|---|---|
| POST | `/properties` | Créer un bien |
| GET | `/properties` | Lister les biens |
| GET | `/properties/{id}` | Détail d'un bien |
| PUT | `/properties/{id}` | Modifier un bien |
| DELETE | `/properties/{id}` | Supprimer un bien |
| PUT | `/properties/{id}/availability` | Changer la disponibilité |
| GET | `/properties/search` | Recherche multicritère (mot-clé, nombre de pièces, surface minimale, …) |
| GET | `/properties/stats/available` | Statistiques des biens disponibles |
| GET | `/properties/stats/unavailable` | Statistiques des biens indisponibles |

### ContractService — `/tenants` et `/contracts`

| Méthode | Route | Description |
|---|---|---|
| POST / GET | `/tenants` | Créer / lister les locataires |
| GET / PUT / DELETE | `/tenants/{id}` | Détail / modifier / supprimer un locataire |
| POST | `/contracts` | Créer un contrat |
| GET | `/contracts` | Lister les contrats |
| GET | `/contracts/{id}` | Détail d'un contrat |
| PUT | `/contracts/{id}/terminate` | Résilier un contrat |
| GET | `/contracts/active` | Contrats actifs |
| GET | `/contracts/inactive` | Contrats inactifs |
| GET | `/contracts/available-soon` | Biens bientôt disponibles |

### payment-service — `/payments`

| Méthode | Route | Description |
|---|---|---|
| POST | `/payments` | Enregistrer un paiement |
| GET | `/payments` | Lister les paiements |
| GET | `/payments/{id}` | Détail d'un paiement |
| GET | `/payments/contract/{contractId}` | Paiements d'un contrat |
| GET | `/payments/contract/{contractId}/total` | Total payé pour un contrat |
| GET | `/payments/property/{propertyId}` | Paiements d'un bien |
| GET | `/payments/status/{status}` | Paiements par statut |
| PATCH | `/payments/{id}/status` | Modifier le statut |
| DELETE | `/payments/{id}` | Supprimer un paiement |
| GET | `/payments/properties/active/{status}` | Biens sous contrat actif selon l'état de paiement |

### Exemple

```bash
# Créer un bien
curl -X POST http://localhost:8080/property-service/properties \
  -H "Content-Type: application/json" \
  -d '{
        "type": "Appartement",
        "address": "12 rue Habib Bourguiba, Sfax",
        "rentPrice": 800,
        "description": { "rooms": 3, "surface": 85, "criteria": "Meublé, balcon" }
      }'
```

---

## Événements Kafka

| Topic | Producteur | Consommateur(s) | Événements |
|---|---|---|---|
| `property-events` | Property-Service | ContractService (`contract-group`) | Événements sur les biens |
| `contract-events` | ContractService | Property-Service (`property-group`), payment-service (`payment-group`) | `CONTRACT_CREATED`, `CONTRACT_TERMINATED`, `CONTRACT_EXPIRED`, `CONTRACT_EXPIRING_SOON` |
| `payment-events` | payment-service | — | Événements de paiement (dont relances) |

Effets principaux :

- `CONTRACT_TERMINATED` / `CONTRACT_EXPIRED` → Property-Service remet le bien **disponible**.
- `CONTRACT_CREATED` / `CONTRACT_EXPIRING_SOON` → payment-service journalise (loyer attendu, dernier paiement à prévoir).

---

## Tâches planifiées

| Service | Tâche | Fréquence par défaut | Propriété de configuration |
|---|---|---|---|
| ContractService | Marque les contrats échus comme inactifs et publie `CONTRACT_EXPIRED` | Tous les jours à 00:00 | `scheduler.contract.expired.cron` |
| ContractService | Publie `CONTRACT_EXPIRING_SOON` pour les contrats expirant sous 30 jours | Tous les jours à 09:00 | `scheduler.contract.expiring.cron` |
| payment-service | Détection des paiements en retard | Toutes les 30 s (configuration actuelle de dev) | `scheduler.payment.overdue.cron` |
| payment-service | Rappel des paiements à venir | Toutes les 30 s (configuration actuelle de dev) | `scheduler.payment.upcoming.cron` |

---

## Monitoring

- Chaque service métier expose `/actuator/health` et `/actuator/prometheus`.
- **Prometheus** (`prometheus.yml`) scrape Property-Service (8081), ContractService (8082) et payment-service (8083) toutes les 15 s.
- **Grafana** (http://localhost:3001) : ajouter Prometheus comme source de données avec l'URL `http://prometheus:9090`, puis importer ou créer vos dashboards.
- **Kafka UI** permet d'inspecter les topics et messages.

---

## CI/CD (GitLab)

Le pipeline `.gitlab-ci.yml` s'exécute à chaque push, avec un job par service en parallèle :

| Étape | Action |
|---|---|
| `validate` | `docker compose config --quiet` : validité du `docker-compose.yml` |
| `build` | `mvn clean package -DskipTests` (Maven 3.9, JDK 17) pour chaque service |
| `package` | Build de l'image Docker et push sur le GitLab Container Registry (`<sha>` et `latest` sur la branche par défaut) |

> Les tests actuels (`contextLoads`) nécessitent une base de données et Kafka, ils sont donc ignorés en CI.

---

## Structure du dépôt

```
PropertyManagement/
├── discovery-service/      # Serveur Eureka
├── Proxy-Service/          # API Gateway
├── Property-Service/       # Biens immobiliers
├── ContractService/        # Locataires et contrats
├── payment-service/        # Paiements
├── k8s/                    # Manifests Kubernetes (Kustomize)
├── docker-compose.yml      # Stack complète (services, BDD, Kafka, monitoring)
├── prometheus.yml          # Configuration Prometheus
├── .gitlab-ci.yml          # Pipeline CI
├── .env.example            # Modèle de variables d'environnement
└── .gitignore
```

### Développement local d'un service

```bash
cd Property-Service
./mvnw spring-boot:run
```

Les `application.properties` ciblent les hôtes Docker (`db-property`, `eureka`, `kafka:29092`, …). Pour un lancement hors Docker, surcharger ces valeurs par variables d'environnement (ex. `SPRING_DATASOURCE_URL`, `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE`, `SPRING_KAFKA_BOOTSTRAP_SERVERS`) ou via un profil Spring dédié.

---

## Configuration et sécurité

- Les identifiants de base de données sont injectés par **variables d'environnement** (`SPRING_DATASOURCE_USERNAME` / `SPRING_DATASOURCE_PASSWORD`), alimentées par `.env` (Compose) ou par le secret `db-credentials` (Kubernetes).
- Ne jamais committer `.env`. Utiliser `.env.example` comme modèle.
- L'archive transmise contient un fichier `.env` : vérifier qu'il n'est pas poussé sur le dépôt Git et changer les mots de passe s'il l'a été.
