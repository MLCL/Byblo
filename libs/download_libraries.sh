#!/bin/bash
# Build Distribution Thresaurus, version 1
#
# Copyright (c) 2010-2011, University of Sussex
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

#
# Download and build JCommander
#
 

jc=jcommander
jc_version=jcommander-1.17

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

#
# Download binary distribution of fastutil
#

fu=fastutil
fu_version=fastutil-6.4

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

#
# Download binary distribution of commons logging
#

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

#
# Download binary distribution of google guava
# Requires: unzip, curl
#

gg=google-guava
gg_version=guava-r09

echo "[${gg}] Starting"
which -s unzip || die "Can't find unzip"
which -s curl || die "Can't find curl"


gg_url=http://guava-libraries.googlecode.com/files/${gg_version}.zip
gg_dl_file=`mktemp -t ${gg_version}-download`
gg_ext_dir=`mktemp -dt ${gg_version}-extracted`
echo "[${gg}] Downloading from ${gg_url} to $gg_dl_file"
curl -L "${gg_url}" > "${gg_dl_file}" || die
echo "[${gg}] Extracting to ${gg_ext_dir}"
unzip -q "${gg_dl_file}" -d "${gg_ext_dir}"  || die
cp "${gg_ext_dir}/${gg_version}/${gg_version}.jar" "$libs_dir" || die

echo "[${gg}] Cleaning temporary files"
rm -rf "$gg_ext_dir"
rm -f "$gg_dl_file"

cd "$libs_dir"

echo "[${gg}] Done"


