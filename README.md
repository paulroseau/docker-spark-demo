# Package your spark job in a docker container

This is a demo project that illustrate how you can package a spark job within
a docker container using [sbt-native-packager](https://github.com/sbt/sbt-native-packager) and
[sbt-assembly](https://github.com/sbt/sbt-assembly) in order to submit your job locally or to a YARN cluster.

## Requirements

- `docker` (tested with version 17.12.0)
- `sbt`

## Steps

1. Package your application within a docker container
```
sbt docker:publishLocal
```

2. Run your job

 - locally : 
```
docker run -it --rm docker-spark-demo --class com.github.paulroseau.dockersparkdemo.Job --master local\[2\] /opt/jars/docker-spark-demo-assembly.jar
```

 - on a YARN cluster : _to be tested_
