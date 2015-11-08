Had to copy spring-integration-core-*.jar because CoreApplicationContext.xml
was including the namespace:

http://www.springframework.org/schema/integration
http://www.springframework.org/schema/integration/spring-integration.xsd

Jetty apparently overrides files with the same name, e.g. 
	spring.handlers
	spring.schemas
which define namespace handlers for Spring.

https://code.google.com/p/google-web-toolkit/issues/detail?id=5728
https://code.google.com/p/google-web-toolkit/issues/detail?id=5693

Error:

Unable to locate Spring NamespaceHandler for XML schema
namespace [http://www.springframework.org/schema/integration]
Offending resource: class path resource
[com/ait/tooling/server/core/config/CoreApplicationContext.xml]

I think this also causes the Jetty startup warnings like:

[WARN] Server class 'org.springframework.messaging.MessageChannel' could not be found in the web app, but was found on the system classpath
   [WARN] Adding classpath entry 'file:/C:/Users/Enno/.gradle/caches/modules-2/files-2.1/org.springframework/spring-messaging/4.2.1.RELEASE/ee93f4eabc91fd8b496ccdae9df1b515e83f91a8/spring-messaging-4.2.1.RELEASE.jar' to the web app classpath for this session
   For additional info see: file:/C:/tools/gwt-2.7.0/doc/helpInfo/webAppClassPath.html
