# Getting Started


## Build and Execute

```
mvn clean install

mvn spring-boot:run 
```

## Test

Test source includes integration tests for the end points. Please execute AssignmentChallengeApplicationTests. The test execute will take time after some transaction sleep command is used, so that expiry of the ticks can be verified.

```
mvn -Dtest=AssignmentChallengeApplicationTests test
```

## Assumption
To achieve statistics end-point execute in constant time and memory O(1), concurrent hash map is created. Every tick will update the statistics.
The periodic job has the responsibility to expire the tick / transaction and re-calculate the statistics. 

## Improvements
1- Memory utilization of period job.

## APIs

### /ticks
```
curl -H "Content-Type: application/json" -X POST http://localhost:8080/ticks -d '
{
	"instrument": "IBM.N",
	"price":12.0,
	"timeStamp":1571906273207
}'
```
### /statistics
This is the end point with aggregated statistics, this has to execute in constant time and memory (O(1)). It returns the statistic based on the ticks which happened in the last 60 seconds.

```
curl -H "Content-Type: application/json" -X GET http://localhost:8080/statistics
```

### /statistics/IBM
This is the  end point with aggregated statistics by instrument, this has to execute in constant time and memory (O(1)). It returns the statistic based on the ticks which happened in the last 60 seconds.

```
curl -H "Content-Type: application/json" -X GET http://localhost:8080/statistics
```