# - - - - - - - - - - - - - - backend-demo - - - - - - - - - - - - - - 

#### <font color="blue">api</font>
GET : /api/demo/test/get

#### Dockerfile

FROM you can make the image from a basic debian image with jdk8

MAINTAINER yann

RUN mkdir -p /opt/microservice/client

RUN mkdir -p /opt/microservice/log/client

COPY backend.service-0.0.1-SNAPSHOT.jar /opt/microservice/client

RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime

EXPOSE 9955

EXPOSE 19955

CMD ["sh","-c","java -jar -Xms512m -Xmx512m /opt/microservice/client/backend.service-0.0.1-SNAPSHOT.jar --server.port=9955 --management.server.port=19955 --spring.application.name=demo-Y-test123-hello-service"]

# - - - - - - - - - - - - - - gateway-k8s - - - - - - - - - - - - - - 

#### Dockerfile

FROM you can make the image from a basic debian image with jdk8

MAINTAINER yann

RUN apt-get update

RUN apt-get install -y procps

RUN apt-get install -y vim

RUN apt-get install -y curl

RUN apt-get install -y xinetd

RUN apt-get install -y telnet

RUN apt-get install -y less

RUN mkdir -p /opt/microservice/product-gateway

RUN mkdir -p /opt/microservice/log/product-gateway

COPY microservice.gateway.jar /opt/microservice/product-gateway

RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime

EXPOSE 9891

EXPOSE 19891

CMD ["sh","-c","java -jar -Xms512m -Xmx512m /opt/microservice/product-gateway/microservice.gateway.jar --spring.profiles.active=gatewaypeer1 --deployed.in.cluster=default --deployed.pod.name=default"]
