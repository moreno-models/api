import math
from locust import HttpUser, TaskSet, task, constant
from locust import LoadTestShape


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
        user_count = f(run_time)

        return (round(user_count), max(round(user_count), 1))