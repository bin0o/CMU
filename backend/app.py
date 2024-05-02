from flask import Flask
from flask_sqlalchemy import SQLAlchemy
from flask import request, jsonify


app = Flask(__name__)


@app.route('/', methods=['GET'])
def hello():
    return "Hello World!"

