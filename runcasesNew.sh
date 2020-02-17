

# docker build -t javaapptest .
# docker container ls -a
# docker rm $(docker ps -aq)
# docker stop $(docker ps -aq)

date -jn >> node1.txt
docker run -it -p 8081:8080 --cap-add=NET_ADMIN --net nodenet --ip 172.18.0.21 javaapptest 1 >> node1.txt 2>&1 &

date -jn >> node2.txt
docker run -it -p 8082:8080 --cap-add=NET_ADMIN --net nodenet --ip 172.18.0.22 javaapptest 2 >> node2.txt 2>&1 &

date -jn >> node3.txt
docker run -it -p 8083:8080 --cap-add=NET_ADMIN --net nodenet --ip 172.18.0.23 javaapptest 3 >> node3.txt 2>&1 &

date -jn >> node4.txt
docker run -it -p 8084:8080 --cap-add=NET_ADMIN --net nodenet --ip 172.18.0.24 javaapptest 4 >> node4.txt 2>&1 &

date -jn >> node5.txt
docker run -it -p 8085:8080 --cap-add=NET_ADMIN --net nodenet --ip 172.18.0.25 javaapptest 5 >> node5.txt 2>&1 &

date -jn >> node6.txt
docker run -it -p 8086:8080 --cap-add=NET_ADMIN --net nodenet --ip 172.18.0.26 javaapptest 6 >> node6.txt 2>&1 &

date -jn >> node7.txt
docker run -it -p 8087:8080 --cap-add=NET_ADMIN --net nodenet --ip 172.18.0.27 javaapptest 7 >> node7.txt 2>&1 &


sleep 20

# block 1 and 2 
curl "http://localhost:8081/?block=172.18.0.22"
curl "http://localhost:8082/?block=172.18.0.21"

# unblock 1 and 2
curl "http://localhost:8081/?unblock=172.18.0.22"
curl "http://localhost:8082/?unblock=172.18.0.21"


# block 1 and 3
curl "http://localhost:8081/?block=172.18.0.23"
curl "http://localhost:8083/?block=172.18.0.21"

# unblock 1 and 3
curl "http://localhost:8081/?unblock=172.18.0.23"
curl "http://localhost:8083/?unblock=172.18.0.21"


# block 2 and 3
curl "http://localhost:8082/?block=172.18.0.23"
curl "http://localhost:8083/?block=172.18.0.22"

# unblock 2 and 3
curl "http://localhost:8082/?unblock=172.18.0.23"
curl "http://localhost:8083/?unblock=172.18.0.22"



# later
# block 1 and 4
curl "http://localhost:8081/?block=172.18.0.24"
curl "http://localhost:8084/?block=172.18.0.21"

curl "http://localhost:8081/?unblock=172.18.0.24"
curl "http://localhost:8084/?unblock=172.18.0.21"

# block 1 and 5
curl "http://localhost:8081/?block=172.18.0.25"
curl "http://localhost:8085/?block=172.18.0.21"

# block 2 and 4
curl "http://localhost:8082/?block=172.18.0.24"
curl "http://localhost:8084/?block=172.18.0.22"

curl "http://localhost:8082/?unblock=172.18.0.24"
curl "http://localhost:8084/?unblock=172.18.0.22"

# block 2 and 5
curl "http://localhost:8082/?block=172.18.0.25"
curl "http://localhost:8085/?block=172.18.0.22"

# block 3 and 4
curl "http://localhost:8083/?block=172.18.0.24"
curl "http://localhost:8084/?block=172.18.0.23"

curl "http://localhost:8083/?unblock=172.18.0.24"
curl "http://localhost:8084/?unblock=172.18.0.23"

# block 3 and 5
curl "http://localhost:8083/?block=172.18.0.25"
curl "http://localhost:8085/?block=172.18.0.23"

# block 4 and 5
curl "http://localhost:8084/?block=172.18.0.25"
curl "http://localhost:8085/?block=172.18.0.24"


sleep 20

# stop EVERYONE and remove instances.
docker stop $(docker ps -a -q)
docker rm $(docker ps -a -q)
