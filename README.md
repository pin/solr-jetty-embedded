Embedded Jetty 8 and Solr 4 build with Maven
======================================

Solr packaged with embedded Jetty using Maven

How to build
------------

		mvn package

How to run
----------

		java -Dsolr.solr.home=<core_conf_dir> -jar target/solr-jetty-embedded-1.0-SNAPSHOT.jar
