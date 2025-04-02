az acr login --name team04registry
docker build --platform linux/amd64 -t team04registry.azurecr.io/mail-service:latest .
docker push team04registry.azurecr.io/mail-service:latest
kubectl delete -f kubernetes/deployment.yaml
kubectl apply -f kubernetes/deployment.yaml 