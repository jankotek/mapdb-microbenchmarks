Building the benchmarks. After the project is generated, you can build it with the following Maven command:
    
    mvn clean install

Running the benchmarks. After the build is done, you will get the self-contained executable JAR, which holds your benchmark, and all essential JMH infrastructure code:

    java -jar target/benchmarks.jar

Run with -h to see the command line options available.