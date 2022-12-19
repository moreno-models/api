import math
from locust import HttpUser, TaskSet, task, constant
from locust import LoadTestShape


class Steady(LoadTestShape):
    """
        3h od startu -> 20 użytkowników
        przez 30 minut, ramp-ump do 60 użytkowników
        potem przez 2h nic
        potem 10 użytkowników przez 1h
        po 6h od startu
        przez 60 minut, ramp-up do 40 użytkowników
        potem ramp down do 20 użytkowników
        potem już nic
    """

    peak_one_users = 60
    peak_two_users = 40
    time_limit = 600

    minute = 60
    minutes_in_hour = 60
    flat_first_users = 20
    peak_one_end = 4 * minute
    flat_two_start = 4 * minute
    peak_two_start = 5 * minute
    peak_two_end = 7 * minute
    flat_two_users = 10

    flat_three_users = 20


    def tick(self):
        run_time = round(self.get_run_time())

        if run_time < self.time_limit:
            if run_time < self.peak_one_end:
                user_count = self.flat_first_users + self.peak_one_users * math.e ** -((run_time - 3 * self.minute) / 40) ** 2
            elif run_time >= self.flat_two_start and run_time <= self.peak_two_start:
                return (0, 5.0)
            elif run_time <= self.peak_two_end:
                user_count = self.flat_two_users + self.peak_two_users * math.e ** -((run_time - 6 * self.minute) / 30) ** 2
            else:
                user_count = self.flat_three_users
            
            return (round(user_count), round(user_count))
        else:
            return None