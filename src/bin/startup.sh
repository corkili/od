#!/usr/bin/env bash
bin=`dirname "$0"`
OD_HOME=`cd "$bin/.."; pwd`

echo $OD_HOME

nohup java -jar $OD_HOME/lib/od-1.0.0.jar -Dspring.config.location=$OD_HOME/config/application.properties > ../od.log 2>&1 &

PID=`ps aux | grep $OD_HOME | grep -v grep | awk '{print $2}'`

echo "start successfully, pid: $PID"
