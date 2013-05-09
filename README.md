<!--
  The SALUS Project licenses the semanticMDR project with all of its source files 
  to You under the Apache License, Version 2.0 (the "License"); you may not use 
  this file except in compliance with the License.  
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->

triplestore
===========

Applications using semantic technologies, requires using triple stores. In scope of SALUS project, many different components need triple store as their back-end. Two mainly used triple stores are Virtuoso Quad Store and Jena TDB. Virtuoso Quad Store is scalable, high-performance and open-source triple store; however requires seperate installation of Virtuoso Universal Server and additional libraries to work with Jena RDF API. Jena TDB is native file based triple store of Jena, which is highly scalable and requires no extra tool other than Jena Framework. 

Purpose of this project is create a unified interface, which is <code>JenaStore</code>, which allows to use either Virtuoso Quad Store or Jena TDB as backend and provides Jena compliant Java API to manipulate underlying triple store.

## Installation
===========

Apache Maven is required to build the triplestore. Please visit
http://maven.apache.org/ in order to install Maven on your system.

Under the root directory of the triplestore project run the following:

	$ triplestore> mvn install

In order to make a clean install run the following:

	$ triplestore> mvn clean install

## Usage
===========

After building the project, *triplestore-1.0.0.jar* is found under *target* directory. To use the triplestore,
archive file can be directly added as dependency, or can be specified as maven dependecny by adding following to *pom.xml*.

	<dependency>
		<groupId>tr.com.srdc</groupId>
		<artifactId>triplestore</artifactId>
		<version>1.0.0</version>
    </dependency>
	
since above mentioned installation process deploys **triplestore** to local maven repository.
Interface for managing triple store is provided by the <code>TripleStoreProvider</code>. TripleStoreProvider is designed as a singleton
and instance of it can be retrieved by static method <code>TripleStoreProvider.getInstance()</code>

TripleStoreProvider has number of utilty methods which are responsible of creation, retrieval and removal of triple stores with different settings.
Detailed explanation for these methods can be found as Javadocs.

Basic methods of TripleStoreProvider are:

	-createStore(type, name) : creates a JenaStore with given type and name
	-getStore(name) : returns existing store with given name
	-removeStore(name) : removes the store with given name and its all related files