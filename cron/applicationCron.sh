#!/bin/sh

export TOMCAT_HOME="/usr/share/tomcat5"
ant -buildfile ~/build.xml applicationCron | grep "java" >> ~/logs/halfhourly.log
rm -f ~/logs/velocity.log.txt

