#!/bin/sh

export TOMCAT_HOME="/usr/share/tomcat5"
ant -buildfile ~/build.xml  testDB test invariants dailyCron


echo " "
echo "-- TO-MERGE LOG --"
echo " "

cat ~/logs/warnings.log.txt | grep fail

echo " "
echo "-- WARNING LOG --"
echo " "

cat ~/logs/warnings.log.txt
touch ~/temp.txt
cat ~/temp.txt > ~/logs/warnings.log.txt

echo " "
echo "-- HALF-HOURLY LOG --"
echo " "

cat ~/logs/halfhourly.log | grep -v "transaction committed"
cat ~/temp.txt > ~/logs/halfhourly.log

rm ~/temp.txt
rm -f ~/logs/velocity.log.txt
