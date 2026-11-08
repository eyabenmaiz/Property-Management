# Deploiement Kubernetes (minikube)

Manifests du projet PropertyManagement : 5 services Java, Kafka + Zookeeper,
3 bases de donnees (PostgreSQL + 2 MySQL). Assembles avec Kustomize.

## Deploiement local

    minikube start --memory=6144 --cpus=4
    kubectl apply -f k8s/namespace.yaml
    # creer le secret depuis .env (jamais commite)
    kubectl apply -k k8s
    kubectl get pods -n propertymanagement -w

## Acces

    kubectl port-forward svc/proxy 8088:8080 -n propertymanagement
    kubectl port-forward svc/eureka 8761:8761 -n propertymanagement

## Choix techniques

- `enableServiceLinks: false` : evite que le Service "kafka" injecte une variable KAFKA_PORT qui casse l'image.
- `initContainers` : attendent les dependances (equivalent des `depends_on` de docker-compose).
- Kafka/Zookeeper en `Recreate` : un seul broker a la fois (evite l'erreur ZooKeeper NodeExists).
- Bases de donnees en StatefulSet avec volume persistant.
- Identifiants dans un Secret `db-credentials`, jamais dans Git.
