FROM debian:bookworm-slim

ENV DEBIAN_FRONTEND=noninteractive

WORKDIR /workspace

RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        g++ \
        python3 \
        python3-pip \
        ca-certificates \
    && rm -rf /var/lib/apt/lists/*

COPY standard_checker /usr/local/bin/standard_checker


RUN chmod +x /usr/local/bin/standard_checker


RUN useradd -m runner


USER runner
