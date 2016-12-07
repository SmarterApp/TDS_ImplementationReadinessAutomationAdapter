export MAVEN_OPTS='-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8000'
./run-app.sh -Dlogging.level.org.cresst.sb.irp.automation.adapter=DEBUG \
 -Dlogging.level.org.apache.http=DEBUG \
 -Dlogging.level.org.apache.http.wire=ERROR
