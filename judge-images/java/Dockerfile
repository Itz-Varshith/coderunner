FROM eclipse-temurin:21-jdk-jammy

RUN apt-get update && apt-get install -y \
    python3 \
    python3-pip \
    coreutils \
    procps \
    time \
    && rm -rf /var/lib/apt/lists/*


RUN useradd -m runner


WORKDIR /workspace


RUN chown -R runner:runner /workspace


USER runner


SHELL ["/bin/bash", "-c"]
