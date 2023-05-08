#Bibliotheken
import RPi.GPIO as GPIO
import time

#GPIO definieren (Modus, Pins, Output)
#GPIO.setmode(GPIO.BCM)
#GPIO_TRIGGER_1 = 26
#GPIO_ECHO_1 = 19
#GPIO_TRIGGER_2 = 20
#GPIO_ECHO_2 = 16
#GPIO.setup(GPIO_TRIGGER_1, GPIO.OUT)
#GPIO.setup(GPIO_ECHO_1, GPIO.IN)
#GPIO.setup(GPIO_TRIGGER_2, GPIO.OUT)
#GPIO.setup(GPIO_ECHO_2, GPIO.IN)


def entfernung(slot):
    # Trig High setzen
    GPIO.output(triggers[1*slot], True)

    # Trig Low setzen (nach 0.01ms)
    time.sleep(0.00001)
    GPIO.output(triggers[1*slot], False)

    Startzeit = time.time()
    Endzeit = time.time()

    # Start/Stop Zeit ermitteln
    while GPIO.input(echos[1*slot]) == 0:
        Startzeit = time.time()

    while GPIO.input(echos[1*slot]) == 1:
        Endzeit = time.time()

    # Vergangene Zeit
    Zeitdifferenz = Endzeit - Startzeit
    # Schallgeschwindigkeit (34300 cm/s) einbeziehen
    abstand = (Zeitdifferenz * 34300) / 2

    return abstand


if __name__ == '__main__':
    GPIO.setmode(GPIO.BCM)
    triggers = (0, 26, 20)
    echos = (0, 19, 16)
    GPIO.setup(triggers[1], GPIO.OUT)
    GPIO.setup(echos[1], GPIO.IN)
    GPIO.setup(triggers[2], GPIO.OUT)
    GPIO.setup(echos[2], GPIO.IN)
    try:
        while True:
            distanz = entfernung(1)
            print ("Distanz Vorne = %.1f cm" % distanz)
            distanz = entfernung(2)
            print ("Distanz hinten = %.1f cm" % distanz)
            time.sleep(1)

        # Programm beenden
    except KeyboardInterrupt:
        print("Programm abgebrochen")
        GPIO.cleanup()
        