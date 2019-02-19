#!/bin/sh

####################### Start Common resources for all the scripts ####################
source log4bash.sh

function go_to_dir {
  cd $1
}
####################### End of Common resources for all the scripts ####################

go_to_dir service
  log_info "Building OpenWhisk Stats Exporter Service"
  mvn clean install
  log_info "Building OpenWhisk Stats Exporter Service Completed"
popd >/dev/null 2>&1

echo -n "Enter docker image name (serverless-solution/openwhisk-stats-exporter:latest): "
read docker_image

if [ -z "${docker_image}" ]; then
    docker_image=serverless-platform/openwhisk-stats-exporter
fi

echo -n "Do you want to push the OpenWhisk Stats Exporter Image to the Docker Registry? (Y/n) "
read is_docker_push_required

log_info "Building OpenWhisk Stats Exporter Docker Image"
docker build ./ --tag ${docker_image}
log_info "Building OpenWhisk Stats Exporter Docker Image Completed"

if [ -z "${is_docker_push_required}" ] || [ "${is_docker_push_required}" = "Y" ] || [ "${is_docker_push_required}" = "y" ]; then
  log_info "Pushing OpenWhisk Stats Exporter Docker Image to Registry"
  docker push ${docker_image}
  log_info "Pushing OpenWhisk Stats Exporter Docker Image to Registry Complete"
fi
