# Getting Started


## Build and Execute

```
mvn clean install

mvn spring-boot:run 
```

## Test

Test source includes integration tests for the endpoints. Please execute AssignmentChallengeApplicationTests.

```
mvn -Dtest=AssignmentChallengeApplicationTests test
```

## APIs

### /ticks
```
curl -H "Content-Type: application/json" -X POST http://localhost:8080/ticks -d '
{
	"instrument": "MIC",
	"price":0.0,
	"timeStamp":1571906273207
}'
```
### /statistics
This is the endpoint with aggregated statistics, this has to execute in constant time and memory (O(1)). It returns the statistic based on the ticks which happened in the last 60 seconds.

```
curl -H "Content-Type: application/json" -X GET http://localhost:8080/statistics
```

### /statistics/IBM
This is the  endpoint with aggregated statistics by instrument, this has to execute in constant time and memory (O(1)). It returns the statistic based on the ticks which happened in the last 60 seconds.

```
curl -H "Content-Type: application/json" -X GET http://localhost:8080/statistics
```