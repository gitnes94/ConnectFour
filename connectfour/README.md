# Connect Four – Distribuerat microservices-system

Laboration 2: microservices, BFF, gRPC, event-driven arkitektur, JWT, Docker & Kubernetes.
Temat är Connect Four (fyra i rad) mot en bot, "meddelandet" är speldragen!

## Tjänster

| Tjänst | Port (REST) | Port (gRPC) | Roll |
|--------|-------------|-------------|------|
| authservice | 9000 | – | Login → utfärdar RS256-signerad JWT, publicerar publik nyckel på `/jwks` |
| userservice | 8082 | 9090 | Spelarprofiler (REST CRUD + egen DB), gRPC-endpoint `GetPlayer` |
| gameservice | 8083 | – | Connect Four-bräde, tar emot drag (REST), gRPC-klient mot user, Transactional Outbox → RabbitMQ |
| botservice | – | – | Konsumerar `move-played` (RabbitMQ), räknar ut motdrag, postar tillbaka via REST |
| bff | 8080 | – | Enda ingången för klienten. Validerar JWT, routar till tjänsterna |
| client | – | – | En HTML-sida med spelbräde som pratar med BFF |

## Köra lokalt med Docker Compose (enklast)

```bash
docker compose up --build
# Öppna sedan client/index.html i en webbläsare.
# Logga in som alice / password.
```

RabbitMQ-adminpanel: http://localhost:15672 (guest / guest).

## Köra i Kubernetes (Minikube)

```bash
# 1. Starta Minikube eller docker desktop
minikube start

# 2. Bygg images IN i Minikubes Docker-daemon (annars hittas de inte)
eval $(minikube docker-env)
./build-images.sh

# 3. Applicera alla manifest
kubectl apply -f k8s/

# 4. Kontrollera att allt kör
kubectl get pods -n connectfour

# 5. Nå BFF utifrån (NodePort 30080)
minikube service bff -n connectfour --url
# Sätt BFF-adressen överst i client/index.html (konstanten BFF) till den url:en.
```

### Visa intern DNS-kommunikation (för redovisningen)
```bash
# Lista tjänsterna och deras ClusterIP
kubectl get svc -n connectfour

# Gå in i en pod och anropa en annan tjänst via dess DNS-namn
kubectl exec -it deploy/bff -n connectfour -- sh
#   wget -qO- http://authservice:9000/jwks
#   wget -qO- http://userservice:8082/actuator/health
```
Att anropet fungerar med namnet `authservice` (inte en IP) bevisar att intern
DNS via Kubernetes Services används.

## Bygg-anteckning om gRPC
userservice och gameservice genererar Java-klasser från `src/main/proto/user.proto`
vid bygget (protobuf-maven-plugin). Första gången måste du köra `mvn compile` (eller
låta Docker-bygget göra det) innan IntelliJ slutar visa fel på `...grpc.proto`-paketet.


