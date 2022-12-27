import requests
from random import choice
from uuid import uuid4
import gevent
import sys

MAX_MODELS = 100
API_URL=sys.argv[1]
if API_URL == "":
    print("Provide the target URL as first argument.")
    sys.exit(1)

test = requests.get(f"{API_URL}/models")
if test.status_code != 200:
    print("Failed to connect to the API", test.json())
    sys.exit(2)

given_names = ['Konrad', 'Kacper', 'Wiktor', 'Kamil', 'Matthew', 'Elijah', 'Julia', 'Zosia', 'Zuzia', 'Weronika']
family_names = ['Smith', 'Kowalski', 'Nowak', 'Sanchez', 'Bjornson', 'Perez']
eye_colors = [None, 'blue', 'green', 'brown', 'gray']
height_range = (150, 200)


def create_photo(photo_slug, photo_id, model_slug):
    file_name = f"assets/big-photo-{photo_id}.jpg"
    with open(file_name, "rb") as photo_data:
        created_photo = requests.post(f"{API_URL}/photos", json={"photoSlug": photo_slug, "modelSlug": model_slug, "fileName": file_name}).json()
        requests.put(created_photo["uploadUri"], data = photo_data, headers = {"Content-Type": "image/jpg"})

for model_id in range(MAX_MODELS):
    session_id = str(uuid4())[:5]
    given_name = choice(given_names)
    family_name = choice(family_names)
    eye_color = choice(eye_colors)
    height = choice(range(height_range[0], height_range[1]))
    model_slug = f"{given_name.lower()}-{family_name.lower()}-{session_id}"

    body = {
        "modelSlug": model_slug,
        "givenName": given_name,
        "familyName": family_name,
        "eyeColor": eye_color,
        "height": height
    }

    created_model = requests.post(f"{API_URL}/models", json=body).json()
    jobs = []
    for photo_id in range(1, 5 + 1):
        photo_slug = f"{model_slug}-{photo_id}"
        jobs.append(gevent.spawn(create_photo, photo_slug, photo_id, model_slug))
        print(f"Spawned photo creation for model {model_id} - {photo_slug}")
        
    gevent.joinall(jobs)