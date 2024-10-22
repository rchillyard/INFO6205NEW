import matplotlib.pyplot as plt
import numpy as np

data = {
  "binary": {
    "insert": [],
    "delete": [],
  },
  "binaryfloyd": {
    "insert": [],
    "delete": [],
  },
  "4ary": {
    "insert": [],
    "delete": [],
  },
  "4aryfloyd": {
    "insert": [],
    "delete": [],
  },
  # "fib": {
  #   "insert": [],
  #   "delete": [],
  # },
}
n = []
with open("data.csv") as file:
  line = file.readline()
  skip = True
  while(line):
    if skip:
      skip = False
    else:
      tokens = line.strip().split(",")
      n.append(int(tokens[0]))
      data["binary"]["insert"].append(float(tokens[1]))
      data["binary"]["delete"].append(float(tokens[2]))
      data["binaryfloyd"]["insert"].append(float(tokens[3]))
      data["binaryfloyd"]["delete"].append(float(tokens[4]))
      data["4ary"]["insert"].append(float(tokens[5]))
      data["4ary"]["delete"].append(float(tokens[6]))
      data["4aryfloyd"]["insert"].append(float(tokens[7]))
      data["4aryfloyd"]["delete"].append(float(tokens[8]))
      # data["fib"]["insert"].append(float(tokens[9]))
      # data["fib"]["delete"].append(float(tokens[10]))
    line = file.readline()

print(data)

log = False
for algorithm in data:
  for operation in data[algorithm]:
    if operation == "delete":
      continue
    if log:
      plt.plot([np.log(v) for v in n], [np.log(v) for v in data[algorithm][operation]], label=f'{algorithm}-{operation}')
    else:
      plt.plot(n, data[algorithm][operation], label=f'{algorithm}-{operation}')


# Add labels and title
plt.xlabel('N-Input size')
plt.ylabel('Implementations')
plt.title('Deletions of Priority Queue' + (" with log" if log else " without log"))

# Show the legend
plt.legend()

# Display the plot
plt.show()