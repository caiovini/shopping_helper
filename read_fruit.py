from flask import request, url_for
from flask_api import FlaskAPI, status, exceptions
from base64 import b64decode
from utils import constants
import tensorflow as tf



# build an array of labels from the labels file so we can translate the result of the network to a human readable label
with open(constants.root_dir + '\\utils\\labels') as f:
    labels = f.readlines()
labels = [x.strip() for x in labels]
labels = ["nothing"] + labels
result = ""

notes = {
    0: 'Test data'
}

app = FlaskAPI(__name__)

# load image
# similar to the method used for train/test, only here we read a jpeg image, not a tfrecords file
def read_image(image_file, image_reader):

    #################################################################################
    ####  No need for this, the image will be converted when received by the api ####
    ####                                                                         ####
    #### filename_queue = tf.train.string_input_producer([image_path])           ####
    #### _, image_file = image_reader.read(filename_queue)                       ####
    ################################################################################
    
    local_image = tf.image.decode_jpeg(image_file)
    local_image = tf.image.convert_image_dtype(local_image, tf.float32)
    gray_image = tf.image.rgb_to_grayscale(local_image)
    local_image = tf.image.rgb_to_hsv(local_image)
    shape = tf.shape(local_image)
    local_height = shape[0]
    local_width = shape[1]
    local_depth = shape[2]
    local_image = tf.reshape(local_image, [local_height, local_width, local_depth])
    final_image = tf.concat([local_image, gray_image], 2)
    return final_image, local_height, local_width, local_depth + 1



def predict(sess, X, softmax, keep_prob, images):
    images = sess.run(images)
    # the result of running this method is an array of probabilities, where each index in the array corresponds to a label
    probability = sess.run(softmax, feed_dict={X: images, keep_prob: 1.0})
    # get the highest probability from the array and that should be the result
    prediction = sess.run(tf.argmax(probability, 1))
    return prediction

def process_image(sess, X, softmax, keep_prob, image, image_height, image_width, image_depth):
    image_depth = sess.run(image_depth)
    image_height = sess.run(image_height)
    image_width = sess.run(image_width)
    # resize the image to 100 x 100 pixels and shape it to be like an array of one image, since that is the required input for the network
    # for smaller parts of an image and feed those to the network, tensorflow has a method called "extract_image_patches"
    img = tf.image.resize_images(tf.reshape(image, [1, image_height, image_width, image_depth]), [100, 100])
    img = tf.reshape(img, [-1, 100 * 100 * 4])
    rez = predict(sess, X, softmax, keep_prob, img)
    #return ('Label index: %d - Label: %s' % (rez, labels[rez[0]]))
    return labels[rez[0]]

def define_session(image_file):

    sess = tf.Session()
    image_reader = tf.WholeFileReader()

    # restore the trained model from the saved checkpoint; provide the path to the meta file
    saver = tf.train.import_meta_graph(constants.fruit_models_dir + 'model.ckpt.meta')
    # provide the path to the folder containing the checkpoints
    saver.restore(sess, tf.train.latest_checkpoint(constants.fruit_models_dir))
    graph = tf.get_default_graph()

    # to obtain a tensor from the saved model, we must get it by name, which is why we name the tensors when we create them
    # even if there is only one tensor with a name, in the meta and checkpoint files it is saved as an array, so we have to provide the index of the
    # tensor that we want to get -> thus we call "get_tensor_by_name(tensor_name:0)"

    # obtain the input tensor by name
    X = graph.get_tensor_by_name('X:0')
    # obtain the keep_prob tensor
    keep_prob = graph.get_tensor_by_name('keep_prob:0')
    # obtain the output layer by name and apply softmax on in in order to obtain an output of probabilities
    softmax = tf.nn.softmax(graph.get_tensor_by_name('softmax:0'))

    image, height, width, depth = read_image(image_file, image_reader)
    coord = tf.train.Coordinator()
    threads = tf.train.start_queue_runners(sess=sess, coord=coord)
    process_result = process_image(sess, X, softmax, keep_prob, image, height, width, depth)

    coord.request_stop()
    coord.join(threads)
    sess.close()
    return process_result

def note_repr(key):
    return {
        'url': request.host_url.rstrip('/') + url_for('notes_detail', key=key),
        'text': notes[key]
    }

@app.route("/upload", methods=['GET', 'POST'])
def notes_list():
    """
    List images.
    """
    if request.method == 'POST':
        #Implementation to receive 64 encoded images
        #image_encoded = str(request.data.get('image_encoded', ''))
        #Decode image, analyze it and close the image
        #data = b64decode(image_encoded)
        #result_json = (define_session(data))

        #Implementation to receive multipart image
        img_file = request.files['image'].read()
        result_json = (define_session(img_file))
        return {'label': result_json }, status.HTTP_201_CREATED

    # request.method == 'GET'
    return [note_repr(idx) for idx in sorted(notes.keys())]

@app.route("/<int:key>/", methods=['GET', 'PUT', 'DELETE'])
def notes_detail(key):
    """
    Retrieve, update or delete note instances.
    """
    if request.method == 'PUT':
        note = str(request.data.get('text', ''))
        notes[key] = note
        return note_repr(key)

    elif request.method == 'DELETE':
        notes.pop(key, None)
        return '', status.HTTP_204_NO_CONTENT

    # request.method == 'GET'
    if key not in notes:
        raise exceptions.NotFound()
    return note_repr(key)


if __name__ == "__main__":
    app.run(debug=True)
