FROM gradle:9.0.0-jdk21

WORKDIR ./

COPY ./ .

RUN gradle installDist

CMD ./build/install/app/bin/app

