# ------------------------------------------------------------------------
#
# Copyright 2018 WSO2, Inc. (http://wso2.com)
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License
#
# ------------------------------------------------------------------------

FROM openjdk:8-jre
MAINTAINER WSO2 Serverless Platform "dev@wso2.org"

# User Configurations
ARG USER=wso2carbon
ARG USER_ID=2000
ARG USER_GROUP=wso2
ARG USER_GROUP_ID=2000
ARG USER_HOME=/home/${USER}

# Service JAR
ARG SERVICE_JAR_DIR=service/target/
ARG SERVICE_JAR=openwhisk-stats-exporter.jar

# Create a User Group and a User
RUN groupadd --system -g ${USER_GROUP_ID} ${USER_GROUP} && \
    useradd --system --create-home --home-dir ${USER_HOME} --no-log-init -g ${USER_GROUP_ID} -u ${USER_ID} ${USER}

# Copying Dependent Files
COPY --chown=wso2carbon:wso2 ${SERVICE_JAR_DIR}/${SERVICE_JAR} ${USER_HOME}/

# Set the User and Working Directory
USER ${USER}
WORKDIR ${USER_HOME}

ENTRYPOINT ["java", "-jar", "/home/wso2carbon/openwhisk-stats-exporter.jar"]
