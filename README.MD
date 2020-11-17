# Amazon Keyspaces (for Apache Cassandra) Snappy compression demo.

This Repository contains the source-code for the blog post Compress and Conquer with Amazon Keyspaces.

Prerequisites

    JDK 1.7.0u71 or better
    Maven 3.3.9 or better
    Snappy for java 1.1.7.3 or better
    DataStax java driver 4.9.0 or better
    Authentication Plugin for the DataStax Java Driver 4.0.3 or better
    json-simple 1.1.1 or better
	
If you want to build everything at once, from the top directory run

	````mvn install````

### Let's first create one keyspace and two tables

````
    CREATE KEYSPACE compression WITH replication = {'class':
    'com.amazonaws.cassandra.DefaultReplication'} AND durable_writes = true;
````

````
    CREATE TABLE table_with_compressed_json (
    id timeuuid PRIMARY KEY,
    data blob)
````
````
    CREATE TABLE table_with_uncompressed_json (
    id timeuuid PRIMARY KEY,
    data blob
````
### Configure the performance test in resources/config.properties

````
    contactPoint=cassandra.us-east-1.amazonaws.com
    port=9124
    input_jsons=/path/to/json
    output_partitions_compressed=/path/to/compressed_data/partition_keys
    output_partitions_uncompressed=/path/to/uncompressed_data/partition_keys
````

### Run write performance test

````
    java -cp SnappyKeyspaces-1.0-SNAPSHOT-jar-with-dependencies.jar PerformanceTestWriteRunner
````

### Run read performance test

````
    java -cp SnappyKeyspaces-1.0-SNAPSHOT-jar-with-dependencies.jar PerformanceTestReadRunner
````

### Reading data stored in BLOB columns

To help you access and read data stored in a BLOB column by using the console (https://docs.aws.amazon.com/keyspaces/latest/devguide/console_keyspaces.html)
or cqlsh (https://docs.aws.amazon.com/keyspaces/latest/devguide/programmatic.cqlsh.html),
I’ve created a helper wrapper cqlsh-experimental.sh

1. Clone Amazon Keyspaces (for Apache Cassandra) developer toolkit  

````
    git clone https://github.com/aws-samples/amazon-keyspaces-toolkit.git
````

2. Modify the Dockerfile 
````
    #Install jq
    RUN yum install -y jq && \
        yum install -y python-pip && \
        yum install -y gcc-c++ && \
        yum install -y snappy-devel && \
        yum install -y python-devel && \
        pip install --no-cache-dir --user python-snappy && \
        yum clean all
        
        .....
        
    #toolkit helpers
    COPY bin/ $AWS_KEYSPACES_WORKING_DIR/bin/
    RUN chmod +x $AWS_KEYSPACES_WORKING_DIR/bin/cqlsh-experimental.sh    
````

3. Copy cqlsh-experimental.sh under /bin folder

4. Build your Docker Image
````
    docker build --tag amazon/keyspaces-toolkit --build-arg CLI_VERSION=latest .

````
5. Run cqlsh from the amazon/keyspaces-toolkit image  
````
   docker run --rm -ti --entrypoint cqlsh-experimental.sh \
   amazon/keyspaces-toolkit cassandra.us-east-1.amazonaws.com 9142 \
   -u "USERNAME" -p "PASSWORD" -d snappy --ssl
````

Enjoy! Feedback and PR's welcome!

## License

This library is licensed under the MIT-0 License. See the LICENSE file.