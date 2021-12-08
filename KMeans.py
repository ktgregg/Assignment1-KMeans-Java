from scipy.spatial import distance 
import random

data = array
data_and_cluster_index = np.zeros((len(data),3))
print(data_and_cluster_index)

index = 0
while index < len(data):
    data_and_cluster_index[index,0] = data[index,0]
    data_and_cluster_index[index,1] = data[index,1]
    data_and_cluster_index[index,2] = -1
    index += 1
    
print(data_and_cluster_index)

num_elements = len(data)
print(num_elements)
rand_centroid_index_one = random.randint(0,num_elements-1)
rand_centroid_index_two = random.randint(0,num_elements-1)
rand_centroid_index_three = random.randint(0,num_elements-1)
print("1st centroid randomly located at index", rand_centroid_index_one, ", data is ", data[rand_centroid_index_one])
print("2nd centroid randomly located at index", rand_centroid_index_two, ", data is ", data[rand_centroid_index_two])
print("3rd centroid randomly located at index", rand_centroid_index_three, ", data is ", data[rand_centroid_index_three])

centroid_one = np.array([data[rand_centroid_index_one][0], data[rand_centroid_index_one][1]])
centroid_two = np.array([data[rand_centroid_index_two][0], data[rand_centroid_index_two][1]])
centroid_three = np.array([data[rand_centroid_index_three][0], data[rand_centroid_index_three][1]])

print("\nstarting centroid locations:")
cluster_centers = np.array([centroid_one, centroid_two, centroid_three])

max_iter = 1

print(cluster_centers, "\n")

print("data: ", data)
print("data/cluster index: ", data_and_cluster_index)
for iter in range(max_iter):
    for datapoint in data_and_cluster_index:
        x_coordinate = datapoint[0]
        y_coordinate = datapoint[1]
        #print(x_coordinate, ",", y_coordinate)
        cluster_index = 0
        while cluster_index < len(cluster_centers):
            data_and_cluster_index[cluster_index,0] = data[cluster_index,0]
            data_and_cluster_index[cluster_index,1] = data[cluster_index,1]
            data_and_cluster_index[cluster_index,2] = -1
            index += 1
        for cluster in cluster_centers:
            d = distance.euclidean([x_coordinate, y_coordinate], cluster)
            #print("\t", d)