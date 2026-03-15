FROM debian:bookworm-slim

WORKDIR /workspace

RUN apt-get update && \
    apt-get install -y g++ && \
    rm -rf /var/lib/apt/lists/*

RUN useradd -m runner
USER runner
