from locust import FastHttpUser, task, between
from random import choice
from uuid import uuid4
import gevent

big_photo_name = "assets/big-photo.jpg"
small_photo_name = "assets/small-photo.png"
with open(big_photo_name, "rb") as f:
    big_photo = f.read()
with open(small_photo_name, "rb") as f:
    small_photo = f.read()

photos = {
    big_photo_name: big_photo,
    small_photo_name: small_photo
}

content_types = {
    big_photo_name: "image/png",
    small_photo_name: "image/jpg"
}


class ModelAdmin(FastHttpUser):
    given_names = ['Konrad', 'Kacper', 'Wiktor', 'Kamil', 'Matthew', 'Elijah', 'Julia', 'Zosia', 'Zuzia', 'Weronika']
    family_names = ['Smith', 'Kowalski', 'Nowak', 'Sanchez', 'Bjornson', 'Perez']
    eye_colors = [None, 'blue', 'green', 'brown', 'gray']
    height = (150, 200)
    wait_time = between(30, 60)
    # 1 admins
    fixed_count = 1


    # ## Task -> Create a model
    # List all models
    # Create a model (random name, random stuff)
    # Upload 5 photos
    # List models and find that it was displayed.
    @task
    def create_model(self):
        def create_photo(photo_slug, file_name, model_slug):
            with self.rest("POST", "/photos", json={"photoSlug": photo_slug, "modelSlug": model_slug, "fileName": file_name}) as response:
                if response.js is None:
                    pass
                created_photo = response.js

                self.client.put(created_photo["uploadUri"], data = photos[file_name], headers = {"Content-Type": content_types[file_name]})


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
            print(created_model)
            # Make sure they don't collide.

            pool = gevent.pool.Pool()
            for photo_id in range(5):
                photo_slug = f"{model_slug}-{photo_id}"
                file_name = choice([big_photo_name, small_photo_name])
                pool.spawn(create_photo, photo_slug, file_name, model_slug)
            pool.join()

            # model was actually created
            self.rest("GET", f"/models/{model_slug}")
            # TODO: list models 
            # TODO: list photos
            


    # ## Task -> Browse models and edit one
    # List all models (10 at a time)
    # Browse to random page (if there are pages)
    # Select a random model, get his details
    # List first top1 photos of a model
    # Edit his height/eyecolor to random
    # Upload a photo randomly
    # @task
    # def edit_model(self):
    #     self.client.get("/models")
    #     self.client.get("/photos")

    # ## Task -> Browse models and find by name and show details
    # List all models (10 at a time)
    # Filter by given name (Konrad, Kacper, Kamil, Matthew, Elijah, Zosia, Zuzia, Karolina, Ludmiła, dxdd)
    # Select one (if there are any)
    # List the photos and the details
    # @task
    # def browse_model_with_filtering(self):
    #     self.client.get('xd')


    # ## Task -> Browse all photos
    # List all photos
    # Download all of them
    # Some pages
    # @task
    # def browse_all_photos(self):
    #     print("xddd")