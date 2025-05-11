FROM openjdk:21-jdk-slim

# OS update
RUN apt clean && apt update && apt upgrade -y && apt autoremove

# Setting working dir
WORKDIR /app

# Copying app file and keystore
COPY build/libs/industrial-simulator-1.0-all.jar /app/industrial-simulator-1.0-all.jar
COPY cert/middleware-keystore.p12 /app/middleware-keystore.p12

# Starting app
ENTRYPOINT ["sh", "-c", "java -jar /app/industrial-simulator-1.0-all.jar $MACHINE_TYPE $MACHINE_ID $MQTT_BROKER $MQTT_USERNAME $MQTT_PASSWORD middleware-keystore.p12 middle $KEYSTORE_KEYALIAS $KEYSTORE_KEYPASSWORD"]
