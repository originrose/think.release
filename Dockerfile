FROM ubuntu:16.10
MAINTAINER ThinkTopic

# System requirements
RUN apt-get update                                && \
    apt-get upgrade -y                            && \
    DEBIAN_FRONTEND=noninteractive apt-get install -y -q \
    python-software-properties software-properties-common \
    nmap wget curl vim htop

RUN add-apt-repository ppa:webupd8team/java -y
RUN apt-get update
RUN echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | /usr/bin/debconf-set-selections
RUN apt-get install -y oracle-java8-installer

# docker
RUN echo "deb https://apt.dockerproject.org/repo ubuntu-yakkety main" >> /etc/apt/sources.list && \
    apt install -y apt-transport-https && \
    apt-key adv --keyserver hkp://p80.pool.sks-keyservers.net:80 --recv-keys 58118E89F3A912897C070ADBF76221572C52609D && \
    apt update && apt install -y docker-engine

ENV service think.release
ADD target/${service}.jar /srv/${service}.jar
WORKDIR /srv
ENTRYPOINT ["/usr/bin/java","-jar","/srv/think.release.jar"]
