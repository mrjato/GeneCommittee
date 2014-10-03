GeneCommittee
=============

What is GeneCommittee?
----------------------
GeneCommittee is a web-based interactive tool for giving specific support to the study of the discriminative classification power of custom hypothesis in the form of biological relevant gene sets. Provided with a straightforward and intuitive interface, GeneCommittee is able to render valuable information for diagnostic analyses and clinical management decisions based on systematically evaluating custom hypothesis over different data sets using complementary classifiers, a key aspect in clinical research.

You can test or use GeneCommittee in http://sing.ei.uvigo.es/GC

Requirements
------------
To run GeneCommittee you need:
  - Java 1.6+
  - Tomcat 7 (or any other Java Servlet 3.0 container)
  - A hibernate-compatible database (MySQL 5.1+ recommended)
  - An email account compatible with Java Mail (to allow notifications and feedback sending)
  - Maven 2+
  - Git
  - ZK EE 6.5.0. (**Note**: Although ZK EE is a commercial license, you can request a free ZK Open Source License if your project is Open Source. More info: http://www.zkoss.org/license)

Installation
------------
### 1. Download
You can download GeneCommittee directly from Github using the following command:
`git clone https://github.com/michada/GeneCommittee.git`

### 2. Configuration
Once you have downloaded GeneCommittee, there are three elements that you should configure before deploying GeneCommittee in your Tomcat: database, email account and environment parameters. Optionally, you can also configure your Google Analytics account.

#### 2.1 Database configuration
Database configuration can be changed modifying the `src/main/resources/hibernate.cfg.xml` file. This is a standard Hibernate configuration file. You should usually only have to change the database URL, user and password parameters.
The configured database must exists and be empty, and the user must have SELECT, UPDATE, DELETE, INSERT and CREATE TABLE permissions.

If you want to use a DBMS other than MySQL, then you also have to modify the `pom.xml` file to replace the MySQL driver with a driver suitable for your DBMS.

#### 2.2 Email configuration
Since version 1.0.1, email can be configured in the `src/main/webapp/META-INF/web.xml` file. The entries prefixed with `genecommittee/mail/` will be provided to `javax.mail.Session` as properties. Login and password values should be configured in the `genecommittee/mail/login` and `genecommittee/mail/password` entries. Please, refer to the [Java Mail](https://java.net/projects/javamail/pages/Home) documentation for further information. 

###### Version 1.0
Email address configuration can be changed modifying the `src/main/webapp/META-INF/context.xml` file. In this file you will find an example "Resource" declaration for a Gmail account. You can modify it to use your preferred email server.

#### 2.3 Environment parameters
The `src/main/webapp/WEB-INF/web.xml` contains serveral parameters that allow you to tune up GeneCommittee. The only parameter that you must change is the `genecommittee.storage.files.dataDirectory`, where you must specify a valid directory where the dataset files will be stored. It is important that Tomcat have read and writer permissions for this directory.

#### 2.4 Google Analytics
You can configure GeneCommittee to send access and usage statistics to Google Analytics. To do so, you have to configure the `src/main/webapp/js/ga.js` file to use your account id.

### 3. Build
Once GeneCommittee is configured, you can build it using Maven. Go to the GeneCommittee base directory and run the following command:
`mvn install`

The files resulting from the build are placed in the `target` directory.

### 4. Deploy
After GeneCommittee building you can find a `GC.war` file in the `target` directory, that you can deploy in your Tomcat and start using your own GeneCommittee.

Citing
------
If you use GeneCommittee, please, cite us:

>M. Reboiro-Jato; J. Arrais; J.L. Oliveira; F. Fdez-Riverola. [geneCommittee: a web-based tool for extensively testing the discriminatory power of biologically relevant gene sets in microarray data classification](http://www.biomedcentral.com/1471-2105/15/31). BMC Bioinformatics (2014). 15:31. ISSN: 1471-2105
