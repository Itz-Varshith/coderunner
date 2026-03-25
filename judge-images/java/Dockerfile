FROM eclipse-temurin:21-jdk-jammy

RUN apt-get update && apt-get install -y \
    python3 \
    python3-pip \
    coreutils \
    procps \
    time \
    && rm -rf /var/lib/apt/lists/*


COPY standard_checker /usr/local/bin/standard_checker


RUN chmod +x /usr/local/bin/standard_checker

RUN useradd -m runner


WORKDIR /workspace


RUN chown -R runner:runner /workspace


USER runner


SHELL ["/bin/bash", "-c"]
