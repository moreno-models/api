#!/bin/bash

BASTION_KEY="../../embassy.pem"
BASTION_USER="ec2-user"
BASTION_HOST="embassymodels.net"

WORKER_KEY="../../load-test-london.pem"

MAIN_ADDR="13.40.149.9"
MAIN_PORT=2210

WORKER_ADDRS=("35.177.73.42" "18.130.66.155" "13.40.4.25" "13.40.19.190")
WORKERS=${#WORKER_ADDRS[@]}

function start_tunnels() {
    echo "Killing all screens..."
    killall -9 screen
    echo "Waiting to kill..."
    sleep 2

    echo "Starting the main tunnel..."
    screen -AmdS load-main ssh -i $BASTION_KEY $BASTION_USER@$BASTION_HOST -L $MAIN_PORT:$MAIN_ADDR:22 -N -o StrictHostKeyChecking=no

    echo "Going through the workers..."
    for ((worker_idx = 0; worker_idx < WORKERS; worker_idx++))
    do
        ADDR=${WORKER_ADDRS[worker_idx]}
        LOCAL_PORT=$((MAIN_PORT + 1 + worker_idx))
        echo "Starting tunnel: $worker_idx, $ADDR:$LOCAL_PORT"
        screen -AmdS load-worker-$worker_idx ssh -i $BASTION_KEY $BASTION_USER@$BASTION_HOST -L "$LOCAL_PORT:$ADDR:22" -N -o StrictHostKeyChecking=no
    done
}
