from pickle import load
from numpy import argmax
import numpy as np
from keras.preprocessing.sequence import pad_sequences
from keras.applications.vgg16 import VGG16
from keras.preprocessing.image import load_img
from keras.preprocessing.image import img_to_array
from keras.applications.vgg16 import preprocess_input
from keras.models import Model
from keras.models import load_model
import sys
from flask_debugtoolbar import DebugToolbarExtension
import logging
from PIL import Image
import io
import cv2
# Flask utils
from flask import Flask, redirect, url_for, request, render_template, jsonify
from werkzeug.utils import secure_filename
from gevent.pywsgi import WSGIServer

# Define a flask app
app = Flask(__name__)

# extract features from each photo in the directory
def extract_features(image):
	# re-structure the model

	model = VGG16()
	model.layers.pop()
	model = Model(inputs=model.inputs, outputs=model.layers[-1].output)
	image = np.array(image) 
	# reshape data for the model
	image = image.reshape((1, image.shape[0], image.shape[1], image.shape[2]))
	# prepare the image for the VGG model
	image = preprocess_input(image)
	# get features
	feature = model.predict(image, verbose=0)
	return feature

# map an integer to a word
def word_for_id(integer, tokenizer):
	for word, index in tokenizer.word_index.items():
		if index == integer:
			return word
	return None
@app.route("/generate", methods=['GET', 'POST'])
# generate a description for an image
def generate_desc():
		
	image = request.files["image"].read()
        image = Image.open(io.BytesIO(image))
	# seed the generation process
	tokenizer = load(open('tokenizer.pkl', 'rb'))
	# pre-define the max sequence length (from training)
	max_length = 33
	# load the model
	model = load_model('goodmodel2_19.h5')
	# load and prepare the photograph
	photo = extract_features(image)
	in_text = 'startseq'
	# iterate over the whole length of the sequence
	for i in range(max_length):
		# integer encode input sequence
		sequence = tokenizer.texts_to_sequences([in_text])[0]
		# pad input
		sequence = pad_sequences([sequence], maxlen=max_length)
		# predict next word
		yhat = model.predict([photo,sequence], verbose=0)
		# convert probability to integer
		yhat = argmax(yhat)
		# map integer to word
		word = word_for_id(yhat, tokenizer)
		# stop if we cannot map the word
		if word is None:
			break
		# append as input for generating the next word
		in_text += ' ' + word
		# stop if we predict the end of the sequence
		if word == 'endseq':
			break

	# remove start_seq from the sentence
	in_text = in_text.split(' ', 1)[1]
	# remove end_seq from the sentence
	in_text = in_text.rsplit(' ', 1)[0]
	# write the output sentence for the image to terminal
	print("\n" + in_text)
	# write the output sentence to a file
	f = open("image_output_caption.txt", "w")
	f.write(in_text)
	# return the output sentence for gui
	print(in_text)
	return jsonify(in_text)
@app.route("/hello",methods=['GET', 'POST'])
def Hello():
	with app.app_context():
		return jsonify("Hello")
if __name__ == '__main__':
	app.run(port=5004,debug=True)
	http_server = WSGIServer(('', 5000), app)
	http_server.serve_forever()
