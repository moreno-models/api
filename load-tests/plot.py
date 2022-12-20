import matplotlib.pyplot as plt
from steady import f

seconds_in_minute = 60
minutes_in_hour = 60
time_limit = 8 * minutes_in_hour * seconds_in_minute

x = [x / minutes_in_hour / seconds_in_minute for x in range(time_limit)]
y = [f(x) for x in range(time_limit)]
plt.plot(x, y)
plt.ylabel('Liczba użytkowników')
plt.xlabel('Czas od startu [h]')
plt.show()