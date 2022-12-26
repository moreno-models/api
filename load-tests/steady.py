import math
from locust import HttpUser, TaskSet, task, constant
from locust import LoadTestShape


"""
    170 użytkowników
    500 odsłon

    170 uż / 8 h = 21 użytkowników na 1h
    500 odsłon / 8 h = 62 odsłon na 1h
    # odsłona -> zadanie
    # 1 administrator, zadanie co 10 minut
    # 40 gości, zadanie co 5-10 minut
    # =>
    # 40 gości, 6-12 zadań na godzinę
    # 40 użytkowników, robi 240-480 zadań na godzinę
    # 8h -> 1920-3840 odsłon.



    # Steady
    # Administrator sobie klika co 2h przez 15min.
    # Cały dzień ktoś wchodzi, średnio raz na 15minut.

    # Burst
    # Administrator sobie klika co 2h przez 15min.
    # ALE, w pewnym momencie na stronę wchodzi X użytkowników na 15 minut.

    # Wavy
    # Administrator sobie klika co 2h przez 15min.
    # Po 1h przychodzi fala A gości.
    # Po 3h przychodzi fala B gości.
    # Po 4h przychodzi fala C gości.
    # Po 7h przychodzi fala D gości.

    # To zajmie 4dni, żeby uruchomić te testy.
"""


def f(run_time):
    peak_one_users = 60
    peak_two_users = 40

    hour = 60 * 60
    flat_one_users = 30
    peak_one = 3 * hour
    peak_two = 6 * hour
    flat_two_users = 15

    ramp_up = 1 * hour

    if run_time < ramp_up:
        user_count = min(0.01 * run_time, 25)
    elif run_time < peak_one + hour:
        user_count = flat_one_users + peak_one_users * math.e ** -((run_time - peak_one) / hour * 2) ** 2
    elif run_time < peak_two + hour:
        user_count = flat_two_users + peak_two_users * math.e ** -((run_time - peak_two) / hour * 3) ** 2
    else:
        user_count = 5

    return round(user_count)




class Steady(LoadTestShape):
    time_limit = 60 * 60 * 8
    # TODO:
    # Launch locust 

    # Notes:
    # Upload photo does not work properly, because -> lambda.

    def tick(self):
        run_time = round(self.get_run_time())

        if run_time < self.time_limit:
            user_count = f(run_time)
            return (round(user_count), max(round(user_count), 1))
        else:
            return None