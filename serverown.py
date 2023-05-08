import os

from flask import Flask, request
import flask_restful
import pigpio
import socket

#setup pigpio
os.system("sudo pigpiod")

pi = pigpio.pi()
#pi.set_servo_pulsewidth(MOTOR_PIN, 0)
#pi.set_servo_pulsewidth(MOTOR_PIN, MOTOR_CENTER)


# create the api
app = Flask(__name__)
api = flask_restful.Api(app)


class Constants(flask_restful.Resource):

    MOTOR_PIN = 22
    MOTOR_CENTER = 1450
    MOTOR_OFFSET = 400

    SERVO_PIN = 17
    SERVO_CENTER = 1550
    SERVO_OFFSET = 500

    def get(self):
        data = {
            'speedCenter': self.MOTOR_CENTER,
            'speedOffset': self.MOTOR_OFFSET,
            'servoCenter': self.SERVO_CENTER,
            'servoOffset': self.SERVO_OFFSET
        }
        return data


class Servo(flask_restful.Resource):

    #def get(self):
    #    return pi.get_servo_pulsewidth(SERVO_PIN), 200

    def post(self):
        data = request.get_json()
        print(data)
        servo = data['servo']

        code = 200
        response_data = {}

        if Constants.SERVO_CENTER - Constants.SERVO_OFFSET <= servo <= Constants.SERVO_CENTER + Constants.SERVO_OFFSET:
            response_data = {'message': 'servo set to ' + str(servo)}
            pi.set_servo_pulsewidth(Constants.SERVO_PIN, servo)
        else:
            response_data = {'message': 'invalid servo arg'}
            code = 400

        return response_data, code


class Speed(flask_restful.Resource):

    #def get(self):
        #return pi.get_servo_pulsewidth(MOTOR_PIN), 200

    def post(self):
        data = request.get_json()
        speed = data['speed']

        code = 200
        response_data = {}

        if Constants.MOTOR_CENTER - Constants.MOTOR_OFFSET <= speed <= Constants.MOTOR_CENTER + Constants.MOTOR_OFFSET:
            response_data = {'message': 'speed set'}
            pi.set_servo_pulsewidth(Constants.MOTOR_PIN, speed)
        else:
            response_data = {'message': 'invalid motor arg'}
            code = 430

        return response_data, code


api.add_resource(Servo, '/drive/servo')
api.add_resource(Speed, '/drive/speed')
api.add_resource(Constants, '/drive/constants')

# get ip address
hostname = socket.gethostname()
ip = socket.gethostbyname(hostname)

if __name__ == '__main__':
    app.run(debug=True, host="0.0.0.0")
