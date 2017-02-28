# Overview #
This is the source repository for the IRP Automation Adapter for the
Smarter Balanced Open Source Assessment Delivery System. It was developed by
UCLA CRESST as part of the Smarter Balanced initiative. The IRP Automation
Adapter automates the generation of TDS Report XML documents. It is used
in conjunction with the Implementation Readiness Package web application
(http://smarterapp.cresst.net).

For more information visit http://www.smarterapp.org.

### What is this repository for? ###

* IRP Automation Adapter for the Smarter Balanced Open Source Assessment Delivery System
* Version: 1.0.0

### Technology Used ###
* Maven
* Spring Framework
* Polymer
* Thymeleaf

### How do I get set up? ###

* Install node/npm (http://nodejs.org/download/)
* Install bower (http://bower.io/)
* Install bower-installer (https://github.com/blittle/bower-installer)
* Install Java 7
* Install Maven >= 3.2
* Clone the irp-package repository (https://bitbucket.org/sbcresst/irp-package)

### Environment Variables ###
To build locally, create a file called `env.sh` with the following
environment variables and appropriate values for your environment:
```
export ADAPTER_AUTOMATION_TENANTNAME=<Tenant name of system being automated. For example, SBAC_PT>
export ADAPTER_AUTOMATION_STATEABBREVIATION=<The State abbreviation of the system being automated. For example, CA>
export ADAPTER_AUTOMATION_DISTRICT=<The district identifier of the system being automated>
export ADAPTER_AUTOMATION_INSTITUTION=<The institution identifier of the system being automated>
export ADAPTER_AUTOMATION_OAUTHURL=<The OpenAM OAuth url that includes /auth/oauth2/access_token?realm=/sbac as the path>
export ADAPTER_AUTOMATION_PROGRAMMANAGEMENTURL=<The URL to Program Manager>
export ADAPTER_AUTOMATION_PROGRAMMANAGEMENTCLIENTID=<Program Manager's OpenAM client ID>
export ADAPTER_AUTOMATION_PROGRAMMANAGEMENTCLIENTSECRET=<Program Manager's OpenAM client ID secret key>
export ADAPTER_AUTOMATION_PROGRAMMANAGEMENTUSERID=<Administrator user to log into Program Manager as>
export ADAPTER_AUTOMATION_PROGRAMMANAGEMENTUSERPASSWORD=<The administrator's password for Program Manager>
export ADAPTER_AUTOMATION_TESTSPECBANKURL=<The URL to Test Spec Bank>
export ADAPTER_AUTOMATION_TESTSPECBANKUSERID=<A user ID to use to log into Test Spec Bank>
export ADAPTER_AUTOMATION_TESTSPECBANKPASSWORD=<The user's password who will log into Test Spec Bank>
export ADAPTER_AUTOMATION_ARTURL=<ART URL>
export ADAPTER_AUTOMATION_ARTUSERID=<An administrator user ID to log into ART as> 
export ADAPTER_AUTOMATION_ARTPASSWORD=<The administrator's password>
export ADAPTER_AUTOMATION_PROCTORURL=<Proctor application's URL>
export ADAPTER_AUTOMATION_PROCTORUSERID=<A proctor's user ID to log into the Proctor application>
export ADAPTER_AUTOMATION_PROCTORPASSWORD=<The Proctor's password>
export ADAPTER_AUTOMATION_STUDENTURL=<The URL to the Student application>
export ADAPTER_AUTOMATION_TISDBURL=<The JDBC connection URL to TIS's database>
export ADAPTER_AUTOMATION_TISDBUSERNAME=<The TIS database user name>
export ADAPTER_AUTOMATION_TISDBPASSWORD=<The TIS datatbase user's password>
export ADAPTER_AUTOMATION_TDSDBURL=<The JDBC connection URL to TDS's database>
export ADAPTER_AUTOMATION_TDSDBUSERNAME=<The TDS database user name>
export ADAPTER_AUTOMATION_TDSDBPASSWORD=<The TDS database user's password>
export IRP_PACKAGE_LOCATION=<The location of the irp-package directory>
```

### How do I build? ###
* To build: `mvn clean install`
* To run in debug mode: `./debug-app.sh`
* To run: `./run-app.sh`

### Deployment ###
The IRP Automation Adapter is built by Bitbucket's Pipelines and hosted on AWS Elastic Beanstalk.
Only one Elastic Beanstalk instance exists.  The `bitbucket_beanstalk_deploy.sh` script is executed
by Pipelines after a successful build of `master`. That script deploys the successfully built WAR file.
The AWS Beanstalk instance is configured with environment variables necessary for it to run.

### Contribution guidelines ###

* Write tests - Acceptance Tests using Cucumber and drive your design with TDD
* Code review

### Who do I talk to? ###

* Repo owner or admin
* Other community or team contact
