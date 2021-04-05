# Stream Aggregator

## Prerequisites
1. Java 8
2. Maven 3.X (Any version should work, I am using 3.6.3)

## Clone the repository
```
git clone https://github.com/omkardeshpande8/streamaggregator.git
```

## Build the jar and download dependencies
```
mvn clean install
```

## Start the streaming application
```
java -cp "target/*" com.mycompany.sse.Driver
```
Output will be printed on the terminal and also will be written to `logs/output.txt` file.

## Alternatives
If maven is not installed on the system, following options are available
### Run the artifact from the releases
Download the uber jar from the latest [release](https://github.com/omkardeshpande8/streamaggregator/releases) and run
```
java -jar <jar-name>
```
### Github actions workflow
If you don't feel comfortable downloading jars from untrusted sources, there is a github action set up [here](https://github.com/omkardeshpande8/streamaggregator/actions/workflows/run.yml) that executes the project. It takes the timeout in seconds as input.

### Explanation
The assumptions and design are described [here](docs/explanation.md)

### Further work
[This](docs/further.md) section details the further work
