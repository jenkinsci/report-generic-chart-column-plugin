#!/bin/bash

###Warning! hardcoded values!
###Serves moreover as inspiration

set -x
set -e
set -o pipefail

## resolve folder of this script, following all symlinks,
## http://stackoverflow.com/questions/59895/can-a-bash-script-tell-what-directory-its-stored-in
SCRIPT_SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SCRIPT_SOURCE" ]; do # resolve $SOURCE until the file is no longer a symlink
  SCRIPT_DIR="$( cd -P "$( dirname "$SCRIPT_SOURCE" )" && pwd )"
  SCRIPT_SOURCE="$(readlink "$SCRIPT_SOURCE")"
  # if $SOURCE was a relative symlink, we need to resolve it relative to the path where the symlink file was located
  [[ $SCRIPT_SOURCE != /* ]] && SCRIPT_SOURCE="$SCRIPT_DIR/$SCRIPT_SOURCE"
done
readonly SCRIPT_DIR="$( cd -P "$( dirname "$SCRIPT_SOURCE" )" && pwd )"
readonly PROJECT_DIR=`readlink -f "$SCRIPT_DIR/.."`

medium="${1}"
#medium="/run/media/$USER/8313ec6e-7099-48fd-b765-315ef90ba0e6"
target_jobs_dir="$medium/mnt-mod/raid/jobs"

export CLASSPATH="\
$SCRIPT_DIR/target/report-generic-chart-column-4.2-SNAPSHOT-with-deps.jar\
"
export ADD_FILES="../../config.xml,build.xml;"
export JENKINS_URL="http://hydra.brq.redhat.com:8080/;"
export NVR_DIR="$medium/db/nvr-db"
export JOB_DIR="$medium/db/job-db" 

function runit() {
  regex="${1}"
  v="${2}"
  for x in $(ls "${target_jobs_dir}" | grep $v "${regex}" ) ; do
    let counter=$counter+1
    echo "$counter/$total started $x"
    pushd "$target_jobs_dir/$x";
      java io.jenkins.plugins.genericchart.regenerate.Main 
    popd;
    echo "$counter/$total finished $x"
  done
}

echo "in $medium, the"
echo "There may be hardcoded subdirs. Have you read teh script?"
echo "-url? -nvr-db? -job-db? "
total=$(ls "$target_jobs_dir" | wc -l)
echo "   $target_jobs_dir contains: $total direct items/jobs"
echo ok?
read

counter=0
runit ".*" 




