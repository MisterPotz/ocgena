FROM ubuntu:22.04

ENV DEBIAN_FRONTEND noninteractive
ENV LANG 'en_US.UTF-8'

RUN apt-get update -yqq && apt-get update -yqq && apt-get install -y \
  curl \
  expect \
  git \
  make \
  git-core \
#   libc6:i386 \
#   libgcc1:i386 \
#   libncurses5:i386 \
#   libstdc++6:i386 \
#   zlib1g:i386 \
#   openjdk-11-jdk \
  wget \
  zip \
  unzip \
  vim \
  openssh-client \
  locales \
  && apt-get clean \
  && rm -rf /var/lib/apt/lists/* \
  && localedef -i en_US -c -f UTF-8 -A /usr/share/locale/locale.alias en_US.UTF-8

RUN echo "$HOME"

RUN groupadd -g 1000 -r jenkins && \
  groupadd -g 999 -r docker && \
  groupadd -g 994 -r dockerroot && \
  useradd --create-home -d "/home/jenkins" -u 1000 -r -g jenkins -G docker,dockerroot jenkins

USER jenkins

RUN curl -s "https://get.sdkman.io" | bash && \
    source "$HOME/.sdkman/bin/sdkman-init.sh" &&\
    sdk install java 17.0.8-ms && \
    sdk install gradle

# RUN mkdir "/sdk"
# RUN chown -R jenkins "/sdk"