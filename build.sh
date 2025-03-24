az acr login --name team04registry

if [ -z "$1" ]; then
    echo "Image Tag Version need in arg1"
else
    docker build -t team04registry.azurecr.io/mail-service:$1 .
    docker push team04registry.azurecr.io/mail-service:$1
fi