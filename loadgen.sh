# single post using curl
curl -H 'Content-Type: application/json' -X POST -d ' { "orderID": 1, "dateOfCreation": "2024-12-16", "content": "I love coffee" } ' http://localhost:8080/orders

# -p means to POST it
# -H adds an Auth header (could be Basic or Token)
# -T sets the Content-Type
# -c is concurrent clients
# -n is the number of requests to run in the test
# loadgen_data.json contains the json you want to post

# multiple posts using Apache Bench
ab -p loadgen_data.json -T application/json -c 10 -n 50 http://localhost:8080/orders