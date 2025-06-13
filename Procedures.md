# Procedure operative

## Docker
Per creare l'immagine del simulatore a partire dal Dockerfile
>docker build -t industrial-simulator:latest .

Per istanziare solo il broker mqtt sul pc locale, usando il compose/compose.yml
>docker compose up mosquitto

## Mosquitto
### Preparazione del file delle password
Creare un file chiamato passwd.clear contenente gli utenti nel formato

user1:password1   
user2:password2   
user3:password3   

eseguire

>cp passwd.clear passwd

criptare il file delle password con

>mosquitto_passwd -U passwd

### Configurazione di Mosquitto
Aprire il file di configurazione con 

>sudo nano /etc/mosquitto/mosquitto.conf

e aggiungere le seguenti righe

allow_anonymous false   
password_file /etc/mosquitto/passwd   

## Certificati
Istanziare una macchina con Ubuntu mediante Docker

>docker run --rm -v <path_locale>/industrial-simulator/cert:/root/cert -it ubuntu:22.04 bash

eseguire i seguenti comandi per aggiornare il sistema e creare le directory necessarie

>apt clean all   
>apt update   
>apt upgrade   
>apt install openssl   
>cd /root/cert   

###Certification Authority
Per generare una chiave privata
>openssl genpkey -algorithm RSA -out ca-private.pem

Per generare la chiave pubblica partendo dalla privata
>openssl rsa -in ca-private.pem -pubout -out ca-public.pem

Per creare un certificato autofirmato
>openssl req -x509 -key ca-private.pem -out ca-certificate.pem -days 365 -sha256 

Country Name (2 letter code) [AU]:IT   
State or Province Name (full name) [Some-State]:Italy   
Locality Name (eg, city) []:Rome   
Organization Name (eg, company) [Internet Widgits Pty Ltd]:Pegaso CA   
Organizational Unit Name (eg, section) []:Pegaso CA   
Common Name (e.g. server FQDN or YOUR name) []:pegasoca   
Email Address []:   

###Broker
Per generare chiave privata broker
>openssl genpkey -algorithm RSA -out broker-private.pem

Per generare CSR
>openssl req -new -key broker-private.pem -out broker-request.csr

Country Name (2 letter code) [AU]:IT   
State or Province Name (full name) [Some-State]:Italy   
Locality Name (eg, city) []:Rome   
Organization Name (eg, company) [Internet Widgits Pty Ltd]:Middleware Broker   
Organizational Unit Name (eg, section) []:Middleware Broker   
Common Name (e.g. server FQDN or YOUR name) []:mosquitto   
Email Address []:   

Please enter the following 'extra' attributes   
to be sent with your certificate request   
A challenge password []:   
An optional company name []:   

>touch v3.ext
>nano v3.ext

Inserire i seguenti dati:

subjectKeyIdentifier   = hash   
authorityKeyIdentifier = keyid:always,issuer:always   
basicConstraints       = CA:TRUE   
keyUsage               = digitalSignature, nonRepudiation, keyEncipherment, dataEncipherment, keyAgreement, keyCertSign   
subjectAltName         = DNS:mosquitto, DNS:*.mosquitto, IP:127.0.0.1   
issuerAltName          = issuer:copy   

Per creare un certificato autofirmato
>openssl x509 -req -days 365 -sha256 -in broker-request.csr -signkey broker-private.pem -out broker-certificate.pem -extfile v3.ext


### Control Room
Per creare chiave privata e certificato
>openssl genpkey -algorithm RSA -out controlroom-private.pem
>openssl req -x509 -key controlroom-private.pem -out controlroom-certificate.pem -days 365 -sha256 

Country Name (2 letter code) [AU]:IT   
State or Province Name (full name) [Some-State]:Italy   
Locality Name (eg, city) []:Rome   
Organization Name (eg, company) [Internet Widgits Pty Ltd]:Middleware ControlRoom   
Organizational Unit Name (eg, section) []:Middleware ControlRoom   
Common Name (e.g. server FQDN or YOUR name) []:controlroom   
Email Address []:   


###Boiler
Per creare chiave privata e certificato
>openssl genpkey -algorithm RSA -out boiler-private.pem
>openssl req -x509 -key boiler-private.pem -out boiler-certificate.pem -days 365 -sha256 

Country Name (2 letter code) [AU]:IT   
State or Province Name (full name) [Some-State]:Italy   
Locality Name (eg, city) []:Rome   
Organization Name (eg, company) [Internet Widgits Pty Ltd]:Boiler B01   
Organizational Unit Name (eg, section) []:Boiler B01   
Common Name (e.g. server FQDN or YOUR name) []:boilerb01   
Email Address []:   

###Refrigerator
Per creare chiave privata e certificato
>openssl genpkey -algorithm RSA -out refrigerator-private.pem
>openssl req -x509 -key refrigerator-private.pem -out refrigerator-certificate.pem -days 365 -sha256 

Country Name (2 letter code) [AU]:IT   
State or Province Name (full name) [Some-State]:Italy   
Locality Name (eg, city) []:Rome   
Organization Name (eg, company) [Internet Widgits Pty Ltd]:Refrigerator R01   
Organizational Unit Name (eg, section) []:Refrigerator R01   
Common Name (e.g. server FQDN or YOUR name) []:refrigeratorr01   
Email Address []:   


###Tank
Per creare chiave privata e certificato
>openssl genpkey -algorithm RSA -out tank-private.pem
>openssl req -x509 -key tank-private.pem -out tank-certificate.pem -days 365 -sha256 

Country Name (2 letter code) [AU]:IT   
State or Province Name (full name) [Some-State]:Italy   
Locality Name (eg, city) []:Rome   
Organization Name (eg, company) [Internet Widgits Pty Ltd]:Tank T01   
Organizational Unit Name (eg, section) []:Tank T01   
Common Name (e.g. server FQDN or YOUR name) []:tankt01   
Email Address []:   

###Importazione nel Keystore
Per importare chiavi e certificati in keystore specifici
>openssl pkcs12 -export -inkey ca-private.pem -in ca-certificate.pem -out ca.p12 -name ca   
export password: middle

>openssl pkcs12 -export -inkey broker-private.pem -in broker-certificate.pem -out broker.p12 -name broker   
export password: middle

>openssl pkcs12 -export -inkey controlroom-private.pem -in controlroom-certificate.pem -out controlroom.p12 -name controlroom   
export password: middle

>openssl pkcs12 -export -inkey boiler-private.pem -in boiler-certificate.pem -out boiler.p12 -name boiler   
export password: middle

>openssl pkcs12 -export -inkey refrigerator-private.pem -in refrigerator-certificate.pem -out refrigerator.p12 -name refrigerator   
export password: middle

>openssl pkcs12 -export -inkey tank-private.pem -in tank-certificate.pem -out tank.p12 -name tank   
export password: middle


Per importare tutti i certificati e chiavi private nel keystore finale, eseguire in un ambiente (Linux o Windows) con JDK21 installato i seguenti comandi:

>keytool -importkeystore -destkeystore middleware-keystore.p12 -srckeystore ca.p12 -srcstoretype PKCS12   
password: middle

>keytool -importkeystore -destkeystore middleware-keystore.p12 -srckeystore broker.p12 -srcstoretype PKCS12   
password: middle

>keytool -importkeystore -destkeystore middleware-keystore.p12 -srckeystore controlroom.p12 -srcstoretype PKCS12   
password: middle

>keytool -importkeystore -destkeystore middleware-keystore.p12 -srckeystore boiler.p12 -srcstoretype PKCS12   
password: middle

>keytool -importkeystore -destkeystore middleware-keystore.p12 -srckeystore refrigerator.p12 -srcstoretype PKCS12   
password: middle

>keytool -importkeystore -destkeystore middleware-keystore.p12 -srckeystore tank.p12 -srcstoretype PKCS12   
password: middle

###Utilità
Per stampare un certificato
>openssl x509 -in certificate.pem -text -noout

Per stampare una chiave privata
>openssl rsa -in private.pem -text -noout

Per stampare una chiave pubblica
>openssl rsa -in public.pem -pubin -text -noout

Per stampare contenuto di un keystore p12
>openssl pkcs12 -info -in middleware-keystore.p12   

oppure

>keytool -list -v -keystore middleware-keystore.p12 -storetype PKCS12   

