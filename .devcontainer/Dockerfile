# Base image with OpenJDK (Java 8+ required for Akka)
FROM openjdk:11-jdk-slim

# Set up environment
ENV SCALA_VERSION=2.12.7
ENV SBT_VERSION=1.8.2

# Install basic utilities
RUN apt-get update && apt-get install -y \
    curl git unzip zip bash build-essential && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Install Scala
RUN curl -fsL https://downloads.lightbend.com/scala/${SCALA_VERSION}/scala-${SCALA_VERSION}.deb -o scala.deb && \
    dpkg -i scala.deb && \
    rm scala.deb

# Install SBT
RUN curl -L -o sbt.deb https://github.com/sbt/sbt/releases/download/v${SBT_VERSION}/sbt-${SBT_VERSION}.deb && \
    dpkg -i sbt.deb && \
    apt-get update && \
    apt-get install sbt && \
    rm sbt.deb

# Set working directory
WORKDIR /workspace