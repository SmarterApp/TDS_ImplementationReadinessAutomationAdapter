source env.sh
cd irp-automation-adapter-sboss-application
mvn -Dserver.port=8181 $@ spring-boot:run
cd - 
