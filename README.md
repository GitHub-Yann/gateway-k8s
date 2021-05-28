# - - - - - - - - - - - - - - backend-demo - - - - - - - - - - - - - - 

#### - - - api
GET : /api/demo/test/get

#### - - - Dockerfile

```yaml
FROM codenvy/debian_jdk8

MAINTAINER yann

RUN sudo mkdir -p /opt/microservice/client

RUN sudo mkdir -p /opt/microservice/log/client

RUN sudo chown -R user:user /opt/microservice

COPY backend.service-0.0.1-SNAPSHOT.jar /opt/microservice/client

RUN sudo ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime

EXPOSE 9955

EXPOSE 19955

CMD ["sh","-c","java -jar -Xms512m -Xmx512m /opt/microservice/client/backend.service-0.0.1-SNAPSHOT.jar --server.port=9955 --management.server.port=19955 --spring.application.name=demo-Y-test123-hello-service"]
```

# - - - - - - - - - - - - - - gateway-k8s - - - - - - - - - - - - - - 

#### - - - Dockerfile

```yaml
FROM codenvy/debian_jdk8

MAINTAINER yann

RUN sudo  mkdir -p /opt/microservice/product-gateway

RUN sudo mkdir -p /opt/microservice/log/product-gateway

RUN sudo chown -R user:user /opt/microservice

COPY microservice.gateway.jar /opt/microservice/product-gateway

RUN sudo ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime

EXPOSE 9891

EXPOSE 19891

CMD ["sh","-c","java -jar -Xms512m -Xmx512m /opt/microservice/product-gateway/microservice.gateway.jar --spring.profiles.active=gatewaypeer1 --deployed.in.cluster=default --deployed.pod.name=default"]
```
