FROM ubuntu:16.04 

SHELL ["/bin/bash", "-c"]

RUN apt update && apt upgrade -y && apt install -y wget unzip aptitude vim-gnome \
   ant openjdk-8* epiphany-browser curl grep sed git mercurial \
   subversion 

RUN echo `dbus-uuidgen` > /etc/machine-id

RUN cd / ; git clone https://github.com/hmlatapie/opennars opennars

RUN cd /opennars ; ./build.sh 

WORKDIR /opennars

CMD [ "/bin/bash" ]

