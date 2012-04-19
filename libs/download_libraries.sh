#!/bin/bash
# Build Distribution Thresaurus, version 1
#
# Copyright (c) 2010-2012, University of Sussex
# All rights reserved.
# 
# Redistribution and use in source and binary forms, with or without 
# modification, are permitted provided that the following conditions are met:
# 
#  * Redistributions of source code must retain the above copyright notice, 
#    this list of conditions and the following disclaimer.
# 
#  * Redistributions in binary form must reproduce the above copyright notice,
#    this list of conditions and the following disclaimer in the documentation
#    and/or other materials provided with the distribution.
# 
#  * Neither the name of the University of Sussex nor the names of its 
#    contributors may be used to endorse or promote products derived from this 
#    software without specific prior written permission.
# 
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
# AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
# IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
# ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE 
# LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
# CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
# SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
# INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
# CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
# ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
# POSSIBILITY OF SUCH DAMAGE.

# Byblo Libarary Downloader
#
# Author: Hamish Morgan <hamish.morgan@sussex.ac.uk>
# Date: 20th September 2011
#
# This script attempts to download (and compile where neccessary) the 
# dependancies for the Byblo project. It is assumed that it will be run from
# within the libraries directory (libs) --- if not change the libs_dir 
# variable bellow.
#
# curl and maven must be install on the system for this script to work.
#

libs_dir=`pwd`


function die {
    echo "Error: $@" >&2
    exit 1;
}

# Download and build JCommander
function download_jcommander {
    jc=jcommander
    jc_version=jcommander-1.23

    echo "[${jc}] Starting"
    which -s mvn || die "Can't find maven"
    which -s javac || die "Can't find javac"
    which -s unzip || die "Can't find unzip"
    which -s curl || die "Can't find curl"


    jc_url="http://github.com/cbeust/jcommander/zipball/${jc_version}"
    jc_dl_file=`mktemp -t ${jc_version}-download`
    echo "[${jc}] Downloading from ${jc_url} to $jc_dl_file"
    curl -L "${jc_url}" > "${jc_dl_file}" || die

    jc_ext_dir=`mktemp -dt ${jc_version}-extracted`
    echo "[${jc}] Extracting to ${jc_ext_dir}"
    unzip -q "${jc_dl_file}" -d "${jc_ext_dir}" || die

    echo "[${jc}] Buildings"
    cd ${jc_ext_dir}/`ls ${jc_ext_dir}`
    mvn clean package || die
    cp "target/${jc_version}.jar" "$libs_dir" || die

    echo "[${jc}] Cleaning temporary files"
    rm -rf "$jc_ext_dir"
    rm -f "$jc_dl_file"

    cd "$libs_dir"

    echo "[${jc}] Done"
}

# Download binary distribution of fastutil
function download_fastutil {
    fu=fastutil
    fu_version=fastutil-6.4.1

    echo "[${fu}] Starting"
    which -s tar || die "Can't find tar"
    which -s gzip || die "Can't find gzip"
    which -s curl || die "Can't find curl"

    fu_url=http://fastutil.dsi.unimi.it/${fu_version}-bin.tar.gz
    fu_dl_file=`mktemp -t ${fu_version}-download`
    echo "[${fu}] Downloading from ${fu_url} to $fu_dl_file"
    curl -L "${fu_url}" > "${fu_dl_file}" || die

    fu_ext_dir=`mktemp -dt ${fu_version}-extracted`
    echo "[${fu}] Extracting to ${fu_ext_dir}"
    tar xfz "${fu_dl_file}" -C "${fu_ext_dir}"  || die
    cp "${fu_ext_dir}/${fu_version}/${fu_version}.jar" "$libs_dir" || die

    echo "[${fu}] Cleaning temporary files"
    rm -rf "$fu_ext_dir"
    rm -f "$fu_dl_file"

    echo "[${fu}] Done"
}

# Download binary distribution of commons logging
function download_commons_logging {
    cl=commons-logging
    cl_version=commons-logging-1.1.1

    echo "[${cl}] Starting"
    which -s tar || die "Can't find tar"
    which -s gzip || die "Can't find gzip"
    which -s curl || die "Can't find curl"
    which -s grep || die "Can't find grep"
    which -s head || die "Can't find head"

    echo "[${cl}] Selecting apache mirror"
    cl_url_file="commons/logging/binaries/${cl_version}-bin.tar.gz"
    cl_url_mirror=`curl -s http://www.apache.org/dyn/closer.cgi/${cl_url_file} | grep mirror | grep -oE "http://[^\"<]*${cl_url_file}" | head -n 1`

    cl_dl_file=`mktemp -t ${cl_version}-download`
    echo "[${cl}] Downloading from ${cl_url_mirror} to $cl_dl_file"
    curl -L "${cl_url_mirror}" > "${cl_dl_file}" || die

    echo "[${cl}] Extracting to ${cl_ext_dir}"
    cl_ext_dir=`mktemp -dt ${cl_version}-extracted`
    tar xfz "${cl_dl_file}" -C "${cl_ext_dir}"  || die
    cp "${cl_ext_dir}/${cl_version}/${cl_version}.jar" "$libs_dir" || die

    echo "[${cl}] Cleaning temporary files"
    rm -rf "$fu_ext_dir"
    rm -f "$fu_dl_file"

    cd "$libs_dir"

    echo "[${cl}] Done"
}

# Download binary distribution of google guava
function download_google_guava {
    gg=google-guava
    gg_version=guava-10.0.1

    echo "[${gg}] Starting"
    which -s unzip || die "Can't find unzip"
    which -s curl || die "Can't find curl"

    gg_url=http://search.maven.org/remotecontent?filepath=com/google/guava/guava/10.0.1/${gg_version}.jar
    gg_dl_file=${gg_version}.jar
    echo "[${gg}] Downloading from ${gg_url} to $gg_dl_file"
    curl -L "${gg_url}" > "${gg_dl_file}" || die
    echo "[${gg}] Done"
}

#
# Download binary distribution of junit
# Requires: curl
#


function download_junit {
    ju=junit
    ju_version=junit-4.10

    echo "[${ju}] Starting"
    which -s curl || die "Can't find curl"

    ju_url=https://github.com/downloads/KentBeck/junit/${ju_version}.jar
    ju_dl_file=${ju_version}.jar

    echo "[${ju}] Downloading from ${ju_url} to $ju_dl_file"
    curl -L "${ju_url}" > "${ju_dl_file}" || die
    echo "[${ju}] Done"
}


function download_mlcllib {
    ml=mlcllib
    #ml_version=mlcllib-0.1.0
    ml_version=87aa4ed0e899a3bd467bd589f05c1bc61bf2cf29

    echo "[${ml}] Starting"
    which -s curl || die "Can't find curl"
    which -s unzip || die "Can't find unzip"

    ml_url=https://github.com/MLCL/MLCLLib/zipball/${ml_version}
    ml_dl_file=${ju_version}.zip
    echo "[${ml}] Downloading from ${ml_url} to $ml_dl_file"
    curl -L "${ml_url}" > "${ml_dl_file}" || die

    ml_ext_dir=`mktemp -dt ${ml_version}-extracted`
    echo "[${ml}] Extracting to ${ml_ext_dir}"
    unzip -q "${ml_dl_file}" -d "${ml_ext_dir}" || die

    echo "[${ml}] Buildings"
    cd ${ml_ext_dir}/`ls ${ml_ext_dir}`
    ant clean jar || die

    cp "dist/MLCLLib.jar" "$libs_dir" || die


    echo "[${ml}] Cleaning temporary files"
    rm -rf "$ml_ext_dir"
    rm -f "$ml_dl_file"

    cd "$libs_dir"

    echo "[${ml}] Done"
}



# Download and build JDBM
function download_jdbm {
    jdbm=jdbm
    jdbm_version=b90079b89d582e7c728ff6590bb02bf8b6ed3bfb

    echo "[${jdbm}] Starting"
    which -s mvn || die "Can't find maven"
    which -s javac || die "Can't find javac"
    which -s unzip || die "Can't find unzip"
    which -s curl || die "Can't find curl"


    jdbm_url="https://github.com/jankotek/JDBM3/zipball/${jdbm_version}"
    jdbm_dl_file=`mktemp -t ${jdbm_version}-download`
    echo "[${jdbm}] Downloading from ${jdbm_url} to $jdbm_dl_file"
    curl -L "${jdbm_url}" > "${jdbm_dl_file}" || die

    jdbm_ext_dir=`mktemp -dt ${jdbm_version}-extracted`
    echo "[${jdbm}] Extracting to ${jdbm_ext_dir}"
    unzip -q "${jdbm_dl_file}" -d "${jdbm_ext_dir}" || die

    echo "[${jdbm}] Buildings"
    cd ${jdbm_ext_dir}/`ls ${jdbm_ext_dir}`
    mvn clean package || die
    cp target/*.jar "$libs_dir" || die

    echo "[${jdbm}] Cleaning temporary files"
    rm -rf "$jdbm_ext_dir"
    rm -f "$jdbm_dl_file"

    cd "$libs_dir"

    echo "[${jdbm}] Done"
}


function download_all {
    download_jcommander
      
    download_fastutil
      
    download_commons_logging
      
    download_google_guava 
      
    download_junit 
      
    download_mlcllib
 
	download_jdbm
}



download_all
