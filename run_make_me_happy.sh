# block 1 and 3
curl "http://localhost:8081/?block=172.18.0.23"
curl "http://localhost:8083/?block=172.18.0.21"
# block 1 and 4
curl "http://localhost:8081/?block=172.18.0.24"
curl "http://localhost:8084/?block=172.18.0.21"
# block 1 and 5
curl "http://localhost:8081/?block=172.18.0.25"
curl "http://localhost:8085/?block=172.18.0.21"
# block 1 and 6
curl "http://localhost:8081/?block=172.18.0.26"
curl "http://localhost:8086/?block=172.18.0.21"
# block 1 and 7
curl "http://localhost:8081/?block=172.18.0.27"
curl "http://localhost:8087/?block=172.18.0.21"


# block 2 and 4
curl "http://localhost:8082/?block=172.18.0.24"
curl "http://localhost:8084/?block=172.18.0.22"
# block 2 and 5
curl "http://localhost:8082/?block=172.18.0.25"
curl "http://localhost:8085/?block=172.18.0.22"
# block 2 and 6
curl "http://localhost:8082/?block=172.18.0.26"
curl "http://localhost:8086/?block=172.18.0.22"
# block 2 and 7
curl "http://localhost:8082/?block=172.18.0.27"
curl "http://localhost:8087/?block=172.18.0.22"


# block 3 and 6
curl "http://localhost:8083/?block=172.18.0.26"
curl "http://localhost:8086/?block=172.18.0.23"
# block 3 and 7
curl "http://localhost:8083/?block=172.18.0.27"
curl "http://localhost:8087/?block=172.18.0.23"


# block 4 and 5
curl "http://localhost:8084/?block=172.18.0.25"
curl "http://localhost:8085/?block=172.18.0.24"
# block 4 and 7
curl "http://localhost:8084/?block=172.18.0.27"
curl "http://localhost:8087/?block=172.18.0.24"


# block 5 and 7
curl "http://localhost:8085/?block=172.18.0.27"
curl "http://localhost:8087/?block=172.18.0.25"
