Command utils

```
sam build
```
```
sam local start-api --env-vars env.json
```



Docker


```
docker run -p 8000:8000 amazon/dynamodb-local
```

```
docker inspect  -f '{{range.NetworkSettings.Networks}}{{.IPAddress}}{{end}}' container_id
```

```
{
    "DbAppFunction": {
    "DYNAMODB_ENDPOINT": "http://172.18.0.2:8000"
    }
}
```



```
aws dynamodb create-table --table-name books  \
--endpoint-url http://127.0.0.1:8000 \
--attribute-definitions AttributeName=bookid,AttributeType=S  \
--key-schema AttributeName=bookid,KeyType=HASH  \
--billing-mode PAY_PER_REQUEST  
```



