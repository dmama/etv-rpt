#!/usr/bin/env bash

SCRIPT_PATH=$(dirname `which $0`)
source $SCRIPT_PATH"/envs.sh"
source $SCRIPT_PATH"/metier.sh"

# les parametres de lancement du batch
batch_params="-DperiodeFiscale=${periodeFiscale} -Dpopulation=${population} -Dmode=${mode} -Dversion_ws=${version_ws}"

# les parametres en options pour le démarage de la VM
vm_options="-Dch.vd.appDir=${appDir} -Dunireg-rpt-web.properties.path=${web_properties_path} -Dunireg-rpt-nexus.properties.path=${nexus_properties_path} "
vm_options=$vm_options"-Dunireg-rpt.properties.path=${rpt_properties_path} -Dunireg-rpt.credentials.path=${rpt_credentials_path} "
vm_options=$vm_options"-Dlog4j.configurationFile=${log4j_configurationFile_path} -Dfile.encoding=UTF-8 "

