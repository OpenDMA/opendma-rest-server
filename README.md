# OpenDMA REST Server
Provides a rest-ful server implementation in Java to extend OpenDMA enabled repositories
across media boundaries.

The API is documented in the Open API spec file [opendma-api-spec-070.yaml](./opendma-api-spec-070.yaml).

## Current state

## Usage
Run the executable jar file `opendma-rest-server-#.#.#.jar`.  
The server is configured using springs default configuration mechanism. You can either put the config parameters
in an `application.properties` file in the current directory or pass them directly to the java process
via `-Dproperty=value` switches. Configuration parameters:

| name | value |
|------|------|
| odma.provider.className | fully qualified class name of OpenDMA session provider. The OpenDMA adaptor must be on the classpath. |
| odma.provider.props.<name> | sets the config property `<name>` at the session provider |

There is a sample file located in this directory configuring this server with a XML repository. |

## Building
As usual  with `mvn clean package`.

## Further enhancements

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