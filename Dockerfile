FROM java:8-jdk
MAINTAINER estarter
ENV JAVA_HOME /usr/lib/jvm/java-8-openjdk-amd64

ENV KARAF_VERSION=3.0.9

RUN wget http://www-us.apache.org/dist/karaf/${KARAF_VERSION}/apache-karaf-${KARAF_VERSION}.tar.gz; \
    mkdir /opt/karaf; \
    tar --strip-components=1 -C /opt/karaf -xzf apache-karaf-${KARAF_VERSION}.tar.gz; \
    rm apache-karaf-${KARAF_VERSION}.tar.gz

COPY src/main/resources/file-7.xlsx /tmp/file-orig.xlsx
COPY target/*.jar /opt/karaf/deploy/.
COPY target/dependency/docx4j-3.3.6.jar /opt/karaf/deploy/.
COPY target/dependency/commons-io-2.4.jar /opt/karaf/deploy/.
COPY target/dependency/org.apache.servicemix.bundles.xalan-2.7.2_3.jar /opt/karaf/deploy/.

WORKDIR /opt/karaf
EXPOSE 1099 8101 44444
ENTRYPOINT ["/opt/karaf/bin/karaf"]
