# industrial-simulator
Simulatore di un ambiente industriale con middleware dove macchinari e Stazione di Controllo scambiano messaggi asincroni con garanzia di integrità e riservatezza.

Per un rapido avvio, usare compose/compose.yml per istanziare il simulatore con una Control Room, un Boiler, un Refrigerator e un Tank che scambiano messaggi attraverso topic su un broker MQTT.