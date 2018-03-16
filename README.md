# Karaf Simple

This is a simplest java/maven project to be run in Karaf OSGI server.

# Docker runtime

```bash
mvn package
docker build -t test_karaf .
docker run --rm test_karaf
```
