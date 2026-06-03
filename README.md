# Connect Four – distribuerat microservice-system

Ett distribuerat system byggt med Spring Boot, uppdelat i fem oberoende microservices som kommunicerar via REST, gRPC och en meddelandekö (RabbitMQ). Klienten spelar Connect Four mot en bot; varje speldrag flödar genom hela kedjan av tjänster.

Projektet demonstrerar tjänsteisolering, BFF, JWT-autentisering, intern gRPC-kommunikation, event-driven arkitektur, containerisering och driftsättning i Kubernetes.

## Arkitektur

```
Klient ──REST──> BFF ──REST──> Auth Service        (utfärdar JWT)
                  │   ──REST──> User Service ──┐    (CRUD + egen DB)
                  │   ──REST──> Game Service   │gRPC (hämta profil)
                  │                  │ <───────┘
                  │                  └─publish─> RabbitMQ ──> Bot Service
                  │                                              │
                  └<──────────── REST (botens motdrag) ──────────┘
```

| Tjänst | REST | gRPC | Databas | Roll |
|--------|------|------|---------|------|
| **bff** | 8080 | – | – | Enda ingången utåt, validerar JWT, routar vidare |
| **authservice** | 9000 | – | – | Inloggning, utfärdar JWT (RS256), publicerar publik nyckel på `/jwks` |
| **userservice** | 8082 | 9090 | UserDB (H2) | Spelarprofiler (CRUD) + gRPC-uppslag |
| **gameservice** | 8083 | – | GameDB (H2) | Spellogik, tar emot drag, publicerar `move-played`-event |
| **botservice** | 8084 | – | – | Konsumerar event från kön, beräknar motdrag, svarar via REST |

## Teknik

- Java 25, Spring Boot
- Spring Security (OAuth2 Resource Server, JWT/RS256)
- gRPC + Protocol Buffers (intern tjänst-till-tjänst-kommunikation)
- RabbitMQ (event-driven arkitektur)
- H2 (inbäddad databas per tjänst)
- Docker / Docker Compose
- Kubernetes (Docker Desktop eller Minikube)

## Förutsättningar

- Docker Desktop (med Kubernetes aktiverat för k8s-delen)
- Maven och JDK 25 om du vill köra utan Docker

---

## Kör lokalt med Docker Compose

Från projektmappen (där `docker-compose.yml` ligger):

```bash
docker compose up --build
```

Detta bygger alla fem images och startar tjänsterna + RabbitMQ. Verifiera att allt kör:

```bash
docker compose ps
```

Alla sex containrar ska visa `Up`. Stäng ner med:

```bash
docker compose down
```

BFF:en nås på `http://localhost:8080`. RabbitMQ-adminpanelen finns på `http://localhost:15672` (guest / guest).

---

## Kör i Kubernetes

Förutsätter att Kubernetes är aktiverat i Docker Desktop (eller att Minikube körs).

```bash
# 1. Bygg images med namnen som manifesten förväntar sig
docker build -t connectfour/authservice:1.0 ./authservice
docker build -t connectfour/userservice:1.0 ./userservice
docker build -t connectfour/gameservice:1.0 ./gameservice
docker build -t connectfour/botservice:1.0 ./botservice
docker build -t connectfour/bff:1.0 ./bff

# 2. Driftsätt allt
kubectl apply -f k8s/

# 3. Vänta tills alla poddar kör
kubectl get pods -n connectfour -w
```

> Använder du Minikube i stället för Docker Desktop: kör `eval $(minikube docker-env)` innan bygget så att images hamnar i Minikubes Docker-daemon.

BFF:en exponeras via NodePort `30080`, alltså `http://localhost:30080`.

Verifiera den interna DNS-kommunikationen (tjänsterna hittar varandra via namn, inte IP):

```bash
kubectl get svc -n connectfour
kubectl exec -it deploy/bff -n connectfour -- sh
#   wget -qO- http://authservice:9000/jwks
```

---

## Använda systemet

### Via klienten
Öppna `client/index.html` i en webbläsare. Sätt rätt BFF-adress överst i scriptet:
`http://localhost:8080` för Docker Compose, `http://localhost:30080` för Kubernetes.

Logga in och spela. Botens motdrag kommer asynkront via meddelandekön.

### Via curl

```bash
# Logga in och hämta JWT
TOKEN=$(curl -s -X POST http://localhost:8080/api/login \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"password"}' | jq -r .token)

# Starta nytt spel (notera "gameId" i svaret)
curl -s -X POST http://localhost:8080/api/games \
  -H "Authorization: Bearer $TOKEN"

# Gör ett drag (kolumn 0–6)
curl -s -X POST http://localhost:8080/api/games/<GAME_ID>/moves \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"column":3}'

# Hämta spelet igen – botens motdrag ska ha lagts till
curl -s http://localhost:8080/api/games/<GAME_ID> \
  -H "Authorization: Bearer $TOKEN"
```

### Demo-användare

| Användarnamn | Lösenord |
|--------------|----------|
| alice | password |
| bob | password |

---

## Projektstruktur

```
.
├── authservice/      # Inloggning, JWT, JWKS
├── userservice/      # Spelarprofiler (REST CRUD + gRPC-server)
├── gameservice/      # Spellogik, event-publicering, gRPC-klient
├── botservice/       # Event-konsument, bot-strategi
├── bff/              # Backend-for-Frontend (API-gateway)
├── client/           # Webbklient (index.html)
├── k8s/              # Kubernetes-manifest (Deployment + Service per tjänst)
├── docker-compose.yml
└── build-images.sh   # Bygger alla images (för Minikube)
```

## Nyckelkoncept

- **BFF** – en enda ingång för klienten som validerar JWT och döljer den interna topologin.
- **JWT med JWKS** – authservice signerar tokens med RS256 (privat nyckel) och publicerar den publika nyckeln på `/jwks`, så varje tjänst kan validera tokens själv utan att fråga authservice.
- **gRPC** – gameservice anropar userservice över ett typat, binärt kontrakt (`user.proto`) för intern kommunikation.
- **Event-driven** – ett speldrag (`move-played`) publiceras till RabbitMQ; botservice reagerar asynkront utan att gameservice känner till den.
- **Transactional Outbox** – draget och eventet skrivs i samma databastransaktion; en relay publicerar eventet separat, så inget event tappas vid krasch.
- **Idempotent consumer** – botservice deduplicerar på event-id, eftersom kön ger at-least-once-leverans.
