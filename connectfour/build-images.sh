#!/usr/bin/env bash
# Builds all five service images. Run AFTER pointing Docker at Minikube:
#   eval $(minikube docker-env)      # Linux/macOS
# Then:  ./build-images.sh
set -e
for svc in authservice userservice gameservice botservice bff; do
  echo "==> building connectfour/$svc:1.0"
  docker build -t connectfour/$svc:1.0 ./$svc
done
echo "All images built."
