source env.sh
cd irp-automation-adapter-sboss-application
IRP_PACKAGE_LOCATION=file:///Users/seng/cresst-dev/irp-resources/irp-package mvn -Dserver.port=8181 $@ spring-boot:run
cd - 
