#!/bin/bash
#
# Copyright (c) 2010-2011, MLCL, University of Sussex
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

# MLCLLib Libarary Downloader
#
# Author: Hamish Morgan <hamish.morgan@sussex.ac.uk>
# Date: 20th September 2011
#
# This script attempts to download (and compile where neccessary) the 
# dependancies for the MLCLLib project. It is assumed that it will be run from
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
# Download binary distribution of junit
# Requires: curl
#

ju=junit
ju_version=junit-4.10

echo "[${ju}] Starting"
which -s curl || die "Can't find curl"

ju_url=https://github.com/downloads/KentBeck/junit/${ju_version}.jar
ju_dl_file=${ju_version}.jar
echo "[${ju}] Downloading from ${ju_url} to $ju_dl_file"
curl -L "${ju_url}" > "${ju_dl_file}" || die
echo "[${ju}] Done"




