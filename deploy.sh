#!/bin/sh

####################### Start Common resources for all the scripts ####################

CURRENT_DIR="$(cd $(dirname "$0")/ && pwd)"
# In each script change below to reflect the correct WSP_HOME
WSP_HOME=${WSP_HOME:-$CURRENT_DIR/..}

SCRIPTS_DIR="${WSP_HOME}/scripts"
. ${SCRIPTS_DIR}/utils.sh

####################### End of Common resources for all the scripts ####################

NAMESPACE="openwhisk"

kubeSystemDeploymentHealthCheck () {
  if [ -z "$1" ]; then
    log_error "Component health check called without a component parameter"
    exit 1
  fi

  PASSED=false
  TIMEOUT=0
  until ${PASSED} || [ ${TIMEOUT} -eq 60 ]; do
    KUBE_DEPLOY_STATUS=$(kubectl -n ${NAMESPACE} get pods -o wide | grep "$1" | awk '{print $3}')
    if [ "$KUBE_DEPLOY_STATUS" == "Running" ]; then
      PASSED=true
      break
    fi

    let TIMEOUT=TIMEOUT+1 >/dev/null 2>&1
    sleep 10 >/dev/null 2>&1
  done

  if [ "$PASSED" = false ]; then
    log_error "Failed to finish deploying $1"

    kubectl -n ${NAMESPACE} logs $(kubectl -n ${NAMESPACE} get pods -o wide | grep "$1" | awk '{print $1}')
    exit 1
  fi

  log_info "$1 is up and running"
}

log_info "Initializing Helm"

# Adding a service account with admin privileges for tiller installations
helm repo update
kubectl create serviceaccount --namespace=${NAMESPACE} tiller
kubectl create clusterrolebinding tiller-cluster-rule --clusterrole=cluster-admin --serviceaccount=${NAMESPACE}:tiller

log_info "Creating Service Account for Tiller completed"

# Setting up helm
helm init --service-account tiller --tiller-namespace ${NAMESPACE}
kubeSystemDeploymentHealthCheck "tiller" >/dev/null 2>&1

log_info "Installing Tiller Completed"

log_info "Installing Prometheus"

# Installing Prometheus StatsD exporter for transferring OpenWhisk metrics to Prometheus
helm install ./prometheus-statsd-exporter --name prometheus-statsd-exporter --tiller-namespace ${NAMESPACE} --namespace ${NAMESPACE} >/dev/null 2>&1
kubeSystemDeploymentHealthCheck "prometheus-statsd-exporter" >/dev/null 2>&1

# Installing Prometheus
helm install stable/prometheus --version 6.8.0 --name prometheus --tiller-namespace ${NAMESPACE} --namespace ${NAMESPACE} --values prometheus-helm-values.yml >/dev/null 2>&1
kubeSystemDeploymentHealthCheck "prometheus-server" >/dev/null 2>&1
kubeSystemDeploymentHealthCheck "prometheus-alertmanager" >/dev/null 2>&1
kubeSystemDeploymentHealthCheck "prometheus-pushgateway" >/dev/null 2>&1

# Installing OpenWhisk User Stats exporter for transferring OpenWhisk user stats published to the user_events Kafka topic
helm install ./openwhisk-stats-exporter/chart --name openwhisk-stats-exporter --tiller-namespace ${NAMESPACE} --namespace ${NAMESPACE} >/dev/null 2>&1
kubeSystemDeploymentHealthCheck "openwhisk-stats-exporter" >/dev/null 2>&1

log_info "Installing Prometheus Completed"

log_info "Installing Grafana"

# Installing Grafana
helm install stable/grafana --version 1.12.0 --name grafana --tiller-namespace ${NAMESPACE} --namespace ${NAMESPACE} --values grafana-helm-values.yml >/dev/null 2>&1
kubeSystemDeploymentHealthCheck "grafana" >/dev/null 2>&1

log_info "Installing Grafana Completed"

log_info "Get your Grafana 'admin' user password by running: kubectl get secret --namespace ${NAMESPACE} grafana -o jsonpath=\"{.data.admin-password}\" | base64 --decode ; echo"
