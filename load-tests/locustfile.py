from locust import FastHttpUser, task, between
from random import choice
from uuid import uuid4
import time
import gevent

photos = {}
content_types = {}
photo_names = []

for photo_id in range(1, 5 + 1):
    file_name = f"assets/big-photo-{photo_id}.jpg"
    photo_names.append(file_name)
    with open(file_name, "rb") as f:
        photos[file_name] = f.read()
        content_types[file_name] = "image/jpg"

class ModelVisitor(FastHttpUser):
    wait_time = between(5, 10)

    def download_model_photos(self, model_slug):
        with self.rest("GET", f"/photos?modelSlug={model_slug}&pageSize=10", name="/photos?modelSlug=") as response:
            pool = gevent.pool.Pool()
            for item in response.js["items"]:
                pool.spawn(self.download_photo, item["uri"])
            pool.join()

    def download_photo(self, uri):
        if uri is None:
            return
        # print(f"Download photo of uri: {uri}")
        # Downloading the photo.
        # with self.client.get(uri, name="/download-photo") as response:
        #     content = response.content

    def front_page(self, max_pages=1):
        seenPages = 0
        while True:
            nextToken = ""
            with self.rest("GET", f"/photos?pageSize=20&nextToken={nextToken}", name="/photos/{all}") as response:
                if response.js is None:
                    pass
                nextToken = response.js["metadata"]["nextToken"]
                pool = gevent.pool.Pool()
                for item in response.js["items"]:
                    pool.spawn(self.download_photo, item["uri"])
                pool.join()
                seenPages += 1
                # Wait 5 second to see the page.
                time.sleep(5)
            if nextToken is None or seenPages >= max_pages:
                break


    # ## Task -> Go to the front page, browse recent photos. max 5 pages.
    @task(10)
    def browse_all_photos(self):
        self.front_page(choice(range(1, 5)))

    # ## Task -> Go to the front page, go to the models, browse models.
    # Browse random num of models on each page.
    @task
    def browse_some_models(self):
        self.front_page(1)

        max_pages = choice(range(1, 5))
        seenPages = 0
        while True:
            nextToken = ""
            with self.rest("GET", f"/models?pageSize=10&nextToken={nextToken}", name="/models") as response:
                # Give 5 seconds to decide which model
                time.sleep(5)
                if len(response.js['items']) == 0:
                    return

                for _ in range(choice(range(1, 5))):
                    selected_model = choice(response.js["items"])
                    with self.rest("GET", f"/models/{selected_model['modelSlug']}", name="/models/{modelSlug}"):
                        self.download_model_photos(selected_model['modelSlug'])
                        # Look at photos...
                    time.sleep(5)

                nextToken = response.js['metadata']['nextToken']

            seenPages += 1

            if seenPages >= max_pages or nextToken is None:
                break



class ModelAdmin(FastHttpUser):
    max_retries = 3

    given_names = ['Konrad', 'Kacper', 'Wiktor', 'Kamil', 'Matthew', 'Elijah', 'Julia', 'Zosia', 'Zuzia', 'Weronika']
    family_names = ['Smith', 'Kowalski', 'Nowak', 'Sanchez', 'Bjornson', 'Perez']
    eye_colors = [None, 'blue', 'green', 'brown', 'gray']
    height = (150, 200)
    wait_time = between(10 * 60, 11 * 60)
    # 1 admins
    fixed_count = 1

    def download_photo(self, uri):
        if uri is None:
            return
        # print(f"Download photo of uri: {uri}")
        # Downloading the photo.
        # with self.client.get(uri, name="/download-photo") as response:
        #     content = response.content

    def create_photo(self, photo_slug, file_name, model_slug):
        with self.rest("POST", "/photos", json={"photoSlug": photo_slug, "modelSlug": model_slug, "fileName": file_name}) as response:
            if response.js is None:
                pass
            created_photo = response.js

            self.client.put(created_photo["uploadUri"], data = photos[file_name], headers = {"Content-Type": content_types[file_name]}, name="/upload-photo")

    def download_model_photos(self, model_slug):
        with self.rest("GET", f"/photos?modelSlug={model_slug}&pageSize=10", name="/photos?modelSlug=") as response:
            pool = gevent.pool.Pool()
            for item in response.js["items"]:
                pool.spawn(self.download_photo, item["uri"])
            pool.join()


    # ## Task -> Create a model
    # List all models
    # Create a model (random name, random stuff)
    # Upload 5 photos
    # List models and find that it was displayed.
    @task
    def create_model(self):
        # Before getting to model create, we need to see a list of models.
        self.client.get('/models')

        session_id = str(uuid4())[:5]


        given_name = choice(self.given_names)
        family_name = choice(self.family_names)
        eye_color = choice(self.eye_colors)
        height = choice(range(self.height[0], self.height[1]))
        model_slug = f"{given_name.lower()}-{family_name.lower()}-{session_id}"

        body = {
            "modelSlug": model_slug,
            "givenName": given_name,
            "familyName": family_name,
            "eyeColor": eye_color,
            "height": height
        }
        with self.rest("POST", "/models", json=body) as response:
            if response.js is None:
                pass
            created_model = response.js
            # Make sure they don't collide.

            # 10 seconds to select the photos.
            time.sleep(10)

            pool = gevent.pool.Pool()
            for photo_id in range(5):
                photo_slug = f"{model_slug}-{photo_id}"
                file_name = choice(photo_names)
                pool.spawn(self.create_photo, photo_slug, file_name, model_slug)
            pool.join()

            # model was actually created
            self.client.get(f"/models/{model_slug}", name="/models/{modelSlug}")
            self.download_model_photos(model_slug)

    # ## Task -> Browse all photos
    # List at max 3 pages, of photos and download them.
    @task(5)
    def browse_all_photos(self):
        seenPages = 0
        while True:
            nextToken = ""
            with self.rest("GET", f"/photos?pageSize=10&nextToken={nextToken}", name="/photos/{all}") as response:
                if response.js is None:
                    pass
                nextToken = response.js["metadata"]["nextToken"]
                pool = gevent.pool.Pool()
                for item in response.js["items"]:
                    pool.spawn(self.download_photo, item["uri"])
                pool.join()
                seenPages += 1
                # Wait 5 second to see the page.
                time.sleep(5)
            if nextToken is None or seenPages > 3:
                break

    # ## Task -> Browse models and edit one
    # List all models (10 at a time)
    # Browse to random page (if there are pages)
    # Select a random model, get his details
    # List first top1 photos of a model
    # Edit his height to random
    # Upload a photo randomly
    @task(2)
    def edit_model(self):        
        with self.rest("GET", "/models?pageSize=10", name="/models") as response:
            # Give 5 seconds to decide which model
            time.sleep(5)
            if len(response.js['items']) == 0:
                return

            selected_model = choice(response.js["items"])

            with self.rest("GET", f"/models/{selected_model['modelSlug']}", name="/models/{modelSlug}") as response:
                self.download_model_photos(selected_model['modelSlug'])
                # Look at photos...
                time.sleep(10) 
                actions = ['height', 'photo']
                action = choice(actions)
                if action == 'height':
                    with self.rest("PUT", f"/models/{selected_model['modelSlug']}", name="/models/{modelSlug}", json={"version": selected_model["version"], "height": choice(range(self.height[0], self.height[1]))}) as r:
                        pass
                elif action == 'photo':
                    self.create_photo(f"{selected_model['modelSlug']}-{str(uuid4())[:5]}", choice(photo_names), selected_model['modelSlug'])
                # Give 10 seconds to read the details and photos


