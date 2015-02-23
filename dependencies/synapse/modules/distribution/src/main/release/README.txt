Apache Synapse 2.0 build  (September 2010) - http://synapse.apache.org/
------------------------------------------------------------------------------------------

-------------------
First Steps
===================

Once you extract the downloaded binary distribution, it will create the following directory
structure.

	synapse
		/bin
			synapse.sh
			synapse-daemon.sh
			synapse.bat
			install-synapse-service.bat
			uninstall-synapse-service.bat
		/docs
			<documentation>
		/lib
			<libraries>
			/patches
			    <any patches to be applied>
			/endorsed
			    <any endorsed JARs>
			trust.jks
			identity.jks
			log4j.properties
			providers.xml
		/repository
			/conf
				synapse.xml
				axis2.xml
				wrapper.conf
			    /sample
				    <sample configuration files>
				    /resources
					    <sample resources>
			/modules
		/samples
			/axis2Client
				<ant script to run sample clients>
			/axis2Server
				axis2Server.sh
				axis2Server.bat
				/src/
					<sample services source>


You could start Synapse using the bin/synapse.sh or bin/synapse.bat script, which will load 
the configuration found in repository/conf/synapse.xml. To configure the underlying Axis2
SOAP engine (e.g. to enable JMS) you need to configure the repository/conf/axis2.xml. To 
configure logging levels and to turn on/off debug level logging, please configure the 
lib/log4j.properties file, and set the line "log4j.category.org.apache.synapse=INFO" as
"log4j.category.org.apache.synapse=DEBUG" to turn on debug logging.

-------------------
Documentation
===================
 
Documentation can be found in the 'docs' directory included with the binary distribution 
and in the 'src/site/resources' directory in the source distribution. 

For Synapse mediation samples please see the Synapse_Quickstart.html, Synapse_Samples.html 
and Synapse_Samples_Setup.html

For more information on the Synapse Configuration language syntax and usage refer to
Synapse_Configuration_Language.html

-------------------
Getting Started
===================

Refer to the Synapse_Quickstart.html document to get started with Synapse in just a couple of minutes.

More indepth samples could be found in Synapse_Samples_Setup.html and Synapse_Samples.html found in
the docs directory.

The actual sample Synapse configurations could be found at <SYNAPSE>/respository/conf/sample.
The resources sub-directory contains the sample XSLT transformations, XSD schemas, WS policies
and all other resources required to demonstrate various aspects of Synapse.

-------------------
Support
===================

Please refer to the release_notes.txt file for information on common issues and the solutions.

Any issues with this release can be reported to Apache Synapse mailing list or in the JIRA issue tracker.

Mailing list subscription:
    dev-subscribe@synapse.apache.org
    user-subscribe@synapse.apache.org

Jira:
    http://issues.apache.org/jira/browse/Synapse

-------------------
Cryptography
===================

This distribution includes cryptographic software.  The country in 
which you currently reside may have restrictions on the import, 
possession, use, and/or re-export to another country, of 
encryption software.  BEFORE using any encryption software, please 
check your country's laws, regulations and policies concerning the
import, possession, or use, and re-export of encryption software, to 
see if this is permitted.  See <http://www.wassenaar.org/> for more
information.

The U.S. Government Department of Commerce, Bureau of Industry and
Security (BIS), has classified this software as Export Commodity 
Control Number (ECCN) 5D002.C.1, which includes information security
software using or performing cryptographic functions with asymmetric
algorithms.  The form and manner of this Apache Software Foundation
distribution makes it eligible for export under the License Exception
ENC Technology Software Unrestricted (TSU) exception (see the BIS 
Export Administration Regulations, Section 740.13) for both object 
code and source code.

This release includes support for WS-Security and WS-SecureConversation 
based on the Rampart project. For more details please visit
http://ws.apache.org/rampart/

Thank you for using Synapse!
The Synapse Team. 


