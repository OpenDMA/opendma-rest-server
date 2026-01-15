# OpenDMA REST Server

Reference implementation of the [OpenDMA REST service](https://github.com/OpenDMA/opendma-spec/blob/main/opendma-rest-api-spec.yaml)
in Java using the spring framework.

## Usage
Combine this spring application with an OpenDMA Adaptor on the classpath. This will likely require
additional java and native libraries for the connectivity to the ECM system.

Configure the spring application with the connection properties required by the OpenDMA Adaptor.  
Configuration parameters:

| name | value |
|------|------|
| `odma.provider.className` | fully qualified class name of OpenDMA session provider. The OpenDMA adaptor must be on the classpath. |
| `odma.provider.props.<name>` | value of the adaptor config property `<name>` |

### Manual setup
1. Build as usual with `mvn clean package`
2. Get the jar file `opendma-rest-server-#.#.jar` and the `lib` folder from the maven build (`/target`)
3. Add an OpenDMA Adaptor and its dependencies to the `lib` folder
4. Create an `application.properties` file with configuration parameters depending on your OpenDMA Adaptor
5. Run it with `java -cp "lib/*:opendma-rest-server-#.#.#.jar" org.opendma.rest.server.OpendmaRestServer`
6. You can now access the OpenDMA rest service as http://localhost:8080/opendma/

### Setup with maven
If your OpenDMA Adaptor and all of its dependencies are available on your (local) maven repository, you can use
maven to combine everything into an executable server.

This requires to install this artifact to your local maven repository with `mvn clean install`.

See [this](https://github.com/OpenDMA/opendma-java-tutorial/tree/main/tutorial-rest-server) example combining
this service with the [OpenDMA XML Repository](https://github.com/OpenDMA/opendma-java-xmlrepo) and packaging
everything into a docker image.

## License
This code is intended to serve as reference only, not to be included into production code. Hence, it is
[licensed}(./LICENSE) under AGPL-3.0.

Please review the license terms of any artifact you combine with this code carefully and make sure to comply
with the license terms when using the final product.

## Future enhancements

#### Allow clients to control the page size
In this current implementation, the page size of object enumerations is solely controlled by the OpenDMA
adaptor. It should be possible to define min/max limits in the server configuration. Optionally, clients
should be able to influence this page size.

#### Optionally include entire class and aspect hierarchy
By including the entire inheritance hierarchy of an object's class and aspects, a client can immeditely
decide if an object is an instance of a specific class.
The current implementation requires multiple additional requests by the client to walk through the
class hierarchy.
However, it is highly recommended that clients cache class objects locally. The positive effect of this
extension might be limited.