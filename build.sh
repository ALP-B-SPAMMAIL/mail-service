docker build -t team04registry.azurecr.io/mail-service:latest .
docker push team04registry.azurecr.io/mail-service:latest
kubectl rollout restart deployment mail-service