# gateway-k8s
Dockerfile
FROM <you can make the image from a basic debian image with jdk8>

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
