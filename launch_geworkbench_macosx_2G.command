logfile=geworkbench.log

cd "`dirname "$0"`"
if [ -d jre ] 
then
	./jre/bin/java -cp lib/ant.jar:lib/ant-launcher.jar org.apache.tools.ant.launch.Launcher run2G >> ${HOME}/$logfile.stdout.log 2>> ${HOME}/$logfile.stderr.log
else
	java -cp lib/ant.jar:lib/ant-launcher.jar org.apache.tools.ant.launch.Launcher run2G >> ${HOME}/$logfile.stdout.log 2>> ${HOME}/$logfile.stderr.log
fi