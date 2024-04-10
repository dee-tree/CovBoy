FROM gradle:8.5-jdk8 AS BUILD_STAGE

ENV PROJECT /project

COPY . $PROJECT
COPY build.gradle.kts settings.gradle.kts $PROJECT
COPY CovBoy-core/src CovBoy-core/build.gradle.kts $PROJECT/CovBoy-core/
COPY CovBoy-runner/src CovBoy-runner/build.gradle.kts $PROJECT/CovBoy-runner/
COPY CovBoy-runner/src CovBoy-runner/build.gradle.kts $PROJECT/CovBoy-runner/

WORKDIR $PROJECT

USER root

RUN gradle clean
RUN gradle CovBoy-runner:shadowJar


#FROM adoptopenjdk/openjdk8:alpine-jre # can't use this due to lack of some native libs as dependencies for solvers
FROM ubuntu:latest

# Install OpenJDK-8
RUN apt-get update && \
    apt-get install -y openjdk-8-jre && \
    apt-get install -y ant && \
    apt-get clean;

# Fix certificate issues
RUN apt-get update && \
    apt-get install ca-certificates-java && \
    apt-get clean && \
    update-ca-certificates -f;

# Setup JAVA_HOME
ENV JAVA_HOME /usr/lib/jvm/java-8-openjdk-amd64/
RUN export JAVA_HOME

ENV ARTIFACT=CovBoy-runner-1.0-SNAPSHOT-all.jar
ENV PROJECT=/project

ENV BENCH benchmark.smt2
ENV BENCH_DIR $PROJECT/benchmarks

ENV COVERAGE .Z3.cov
ENV COVERAGE_DIR $PROJECT/coverage

ENV SOLVER Z3
ENV SAMPLER PredicatesPropagatingSampler
ENV COMPLETEMODELS true
ENV SOLVERTIMEOUT 1000
ENV STATISTICS_FILE ""
ENV STATISTICS_DIR $PROJECT/statisticss

WORKDIR $PROJECT
COPY --from=BUILD_STAGE $PROJECT/CovBoy-runner/build/libs/$ARTIFACT .

ENTRYPOINT exec java -jar ${ARTIFACT} --$SOLVER --in=$BENCH_DIR/$BENCH --out=$COVERAGE_DIR/$COVERAGE --$SAMPLER --stm=$SOLVERTIMEOUT --cm=$COMPLETEMODELS --sf=$STATISTICS_DIR/$STATISTICS_FILE

# docker build . -t sampler_main