#!/bin/sh

#
# chkconfig: 345 20 80
# description: WebModeler Server
# processname: wm-server
# pidfile: /opt/processeditor/wm.pid
# logfile: /opt/processeditor/wm.log
#

. /etc/rc.d/init.d/functions

# SET YOUR USER HERE
USER="YOURUSER"

DAEMON="/usr/bin/java"
# SET THE DIRECTORY HERE
ROOT_DIR="/opt/processeditor"

SERVER="com.inubit.research.server.ProcessEditorServer"
OPTIONS="-Xmx1024m -cp processeditor.jar:lib/*"
LOG_FILE="log.txt"

LOCK_FILE="/var/lock/subsys/wm-server"

do_start()
{
        if [ ! -f "$LOCK_FILE" ] ; then
                echo -n $"Starting $SERVER: "
                runuser -l "$USER" -c "cd $ROOT_DIR && $DAEMON $OPTIONS $SERVER >> $LOG_FILE &" && echo_success || echo_failure
                RETVAL=$?
                echo
                [ $RETVAL -eq 0 ] && touch $LOCK_FILE
        else
                echo "$SERVER is locked via $LOCK_FILE."
                RETVAL=1
        fi
}
do_stop()
{
        echo -n $"Stopping $DAEMON $OPTIONS: "
        pid=`ps -aefw | grep "$DAEMON $OPTIONS" | grep -v " grep " | awk '{print $2}'`
        kill $pid > /dev/null 2>&1 && echo_success || echo_failure
        RETVAL=$?
        echo
        [ $RETVAL -eq 0 ] && rm -f $LOCK_FILE
}

case "$1" in
        start)
                do_start
                ;;
        stop)
                do_stop
                ;;
        restart)
                do_stop
                do_start
                ;;
        *)
                echo "Usage: $0 {start|stop|restart}"
                RETVAL=1
esac

exit $RETVAL

# OLD MANUAL STARTUP
#/bin/bash
#nohup java -Xmx1024m -cp 'processeditor.jar:lib/*' com.inubit.research.server.ProcessEdito