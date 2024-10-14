FROM eclipse-temurin:21 AS base

RUN <<EOF
set -eu
apt-get update
DEBIAN_FRONTEND=noninteractive apt-get install -y --quiet curl git
apt-get clean
rm -rf /var/lib/apt/lists/* /var/cache/apt/archives/*

mkdir /gradle-global /gradle-build
EOF

ENV GRADLE_USER_HOME=/gradle-global

COPY init.gradle /root/.gradle/init.gradle

WORKDIR /root

FROM base AS builder

ARG branch=1.21

RUN git clone https://github.com/Kotori316/QuarryPlus.git -b ${branch}
WORKDIR /root/QuarryPlus
RUN ./gradlew testClasses
RUN <<EOF
(./gradlew downloadAssets) || \
    (sleep 5 && ./gradlew downloadAssets) || \
    (sleep 5 && ./gradlew downloadAssets) || true
EOF
RUN ./gradlew runData
RUN ./gradlew runCommonData
RUN ./gradlew runGameTestServer
RUN ./gradlew --stop
RUN <<EOF
set -x
ls -al /gradle-global
ls -al /gradle-build
EOF

FROM base AS result

COPY --from=builder /gradle-global /gradle-global
COPY --from=builder /gradle-build /gradle-build

LABEL org.opencontainers.image.source="https://github.com/Kotori316/QuarryPlus"
LABEL org.opencontainers.image.description="A cache image for QuarryPlus build"
