FROM jetbrains/teamcity-minimal-agent:2017.1.4
ARG DOCKER_VERSION=17.06.0

RUN curl https://download.docker.com/linux/static/stable/x86_64/docker-${DOCKER_VERSION}-ce.tgz -o /tmp/docker.tgz && \
    tar xzf /tmp/docker.tgz -C /tmp/ --strip-components=1 && \
    mv /tmp/docker /usr/local/bin/ && \
    rm /tmp/*
