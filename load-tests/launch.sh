#!/bin/bash

source ./common.sh
start_tunnels

sleep 3

CLEAN_LOCUST_STATS_COMMAND="rm -rf ~/api/load-tests/current_*"
CLEAN_LOCUST_LOGS_COMMAND="rm -rf ~/api/load-tests/locust.log"
KILL_SCREENS_COMMAND="killall -9 screen"
START_MAIN_COMMAND="cd ~/api/load-tests && screen -AmdS main ~/.local/bin/locust --master"
START_WORKER_COMMAND="cd ~/api/load-tests && screen -AmdS worker ~/.local/bin/locust --worker --master-host=172.31.23.168"

echo "$WORKER_KEY"

ssh -i "$WORKER_KEY" ec2-user@localhost -p "$MAIN_PORT" "$KILL_SCREENS_COMMAND"
echo "Cleanup..."
ssh -i "$WORKER_KEY" ec2-user@localhost -p "$MAIN_PORT" "screen -wipe"
ssh -i "$WORKER_KEY" ec2-user@localhost -p "$MAIN_PORT" "$CLEAN_LOCUST_STATS_COMMAND"
ssh -i "$WORKER_KEY" ec2-user@localhost -p "$MAIN_PORT" "$CLEAN_LOCUST_LOGS_COMMAND"
echo "Starting main..."
ssh -i "$WORKER_KEY" ec2-user@localhost -p "$MAIN_PORT" "$START_MAIN_COMMAND"

sleep 3

echo "Starting workers..."
for ((worker_idx = 0; worker_idx < WORKERS; worker_idx++))
do
    LOCAL_PORT=$((MAIN_PORT + 1 + worker_idx))

    echo "Cleaning worker: $LOCAL_PORT"
    ssh -i "$WORKER_KEY" ec2-user@localhost -p $LOCAL_PORT "$KILL_SCREENS_COMMAND"
    ssh -i "$WORKER_KEY" ec2-user@localhost -p $LOCAL_PORT "screen -wipe"
    ssh -i "$WORKER_KEY" ec2-user@localhost -p $LOCAL_PORT "$CLEAN_LOCUST_STATS_COMMAND"
    ssh -i "$WORKER_KEY" ec2-user@localhost -p $LOCAL_PORT "$CLEAN_LOCUST_LOGS_COMMAND"
    echo "Starting locust on worker: $LOCAL_PORT"
    ssh -i "$WORKER_KEY" ec2-user@localhost -p $LOCAL_PORT "$START_WORKER_COMMAND"
    sleep 2
done