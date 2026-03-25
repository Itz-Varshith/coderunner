FROM python:3.11-slim

ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1

WORKDIR /workspace

COPY standard_checker /usr/local/bin/standard_checker


RUN chmod +x /usr/local/bin/standard_checker

RUN useradd -m runner

USER runner
