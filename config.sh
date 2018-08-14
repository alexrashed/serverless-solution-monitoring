#!/bin/sh

####################### Start Common resources for all the scripts ####################

CURRENT_DIR="$(cd $(dirname "$0")/ && pwd)"
# In each script change below to reflect the correct WSP_HOME
WSP_HOME=${WSP_HOME:-$CURRENT_DIR/..}

SCRIPTS_DIR="${WSP_HOME}/scripts"
. ${SCRIPTS_DIR}/utils.sh

####################### End of Common resources for all the scripts ####################

# K8s namespace
NAMESPACE="openwhisk"

# Actions
DOWNLOAD="download"
APPLY="apply"

# Config types
PROMETHEUS="prometheus"
ALERT_MANAGER="alertmanager"

SCRIPT=$0
ACTION=$1
CONFIG_TYPE=$2
FILE=$3

print_help () {
    echo
    echo "Usage:"
    echo "\t$0 (${DOWNLOAD} | ${APPLY}) (${PROMETHEUS} | ${ALERT_MANAGER}) FILE"
    echo
    echo "Arguments:"
    echo "\t(${DOWNLOAD} | ${APPLY})\t\t- Specify whether to download or upload the configuration"
    echo "\t(${PROMETHEUS} | ${ALERT_MANAGER})\t- Specify which configuration to upload or download"
    echo "\tFILE\t\t\t\t- The file to/from which the configuration should be saved/loaded"
}

get_config_map_name () {
    if [ "${CONFIG_TYPE}" = "${PROMETHEUS}" ]; then
        echo "prometheus-server"
    elif [ "${CONFIG_TYPE}" = "${ALERT_MANAGER}" ]; then
        echo "prometheus-alertmanager"
    fi
}

download_config () {
    log_info "Downloading config ${CONFIG_TYPE} into ${FILE}"
    kubectl get configmaps $(get_config_map_name) -n openwhisk -o yaml > ${FILE}
    log_info "Downloading configuration complete"
}

upload_config () {
    log_info "Uploading config ${CONFIG_TYPE} from ${FILE}"
    kubectl replace --filename=${FILE}
    log_info "Uploading configuration complete"
}

# Checking if a help flag is provided
for var in "$@"
do
    if [ "$var" = "-h" ] || [ "$var" = "--help" ] || [ "$var" = "help" ]; then
        print_help
        exit 1
    fi
done

# Checking the arguments
input_valid=false
if [ "${ACTION}" != "${DOWNLOAD}" ] && [ "${ACTION}" != "${APPLY}" ]; then
    log_error "\"${SCRIPT}\" requires one of [ \"${DOWNLOAD}\", \"${APPLY}\" ] as an argument"
    print_help
else
    if [ "${CONFIG_TYPE}" != "${PROMETHEUS}" ] && [ "${CONFIG_TYPE}" != "${ALERT_MANAGER}" ]; then
        log_error "\"${SCRIPT} ${ACTION}\" requires one of [ \"${PROMETHEUS}\", \"${ALERT_MANAGER}\" ] as an argument"
        print_help
    else
        if [ -z "${FILE}" ]; then
            log_error "\"${SCRIPT} ${ACTION} ${CONFIG_TYPE}\" requires a file as an argument"
            print_help
        else
            input_valid=true
        fi
    fi
fi

# Uploading/downloading configuration
if [ input_valid = true ]; then
    if [ "${ACTION}" = "${DOWNLOAD}" ]; then
        download_config
    elif [ "${ACTION}" = "${APPLY}" ]; then
        upload_config
    fi
fi
