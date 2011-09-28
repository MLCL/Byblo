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


# This script wraps all various tasks of the Byblo.jar software into a single
# process for building distributional thesauruses. It takes, as input, raw
# instance files composed of tab-separated entry/feature string pairs, one pair
# per line. The script produces a number of files, the last of which is the
# k-nearest-neighbours of each entry string according the defined measure of
# similarity and filtering rules.
#


# ===========================================
# Options for Sun Grid Engine submition
# ============================================

# Name of job
#$ -N Byblo

# Shell to use
#$ -S /bin/bash

# All paths relative to current working directory
#$ -cwd

# List of queues
#$ -q parallel.q

# Define parallel environment for 12 cores
#$ -pe openmp 12-

# Send mail to. (Comma separated list)
#$ -M hamish.morgan@sussex.ac.uk

# When: [b]eginning, [e]nd, [a]borted and reschedules, [s]uspended, [n]one
#$ -m beas

# Validation level (e = reject on all problems)
#$ -w e

# Merge stdout and stderr streams: yes/no
# -j no


# ====================================
# Constants
# ====================================

# Valid verbosity levels:
readonly VERBOSITY_NONE=0
readonly VERBOSITY_ERROR=1
readonly VERBOSITY_WARN=2
readonly VERBOSITY_INFO=3
readonly VERBOSITY_DEBUG=4
readonly VERBOSITY_TRACE=5

# File name modifiers:
readonly ENTRIES_SUFFIX=".entries"    
readonly FEATURES_SUFFIX=".features" 
readonly ENTRY_FEATURES_SUFFIX=".entry-features" 
readonly FILTERED_SUFFIX=".filtered" 
readonly PAIRS_SUFFIX=".pairs"       
readonly NEIGHBOURS_SUFFIX=".neighs"

readonly JAVA_ARGS="-Xmx16g"


# ====================================
# Default/initial configuration values
# ====================================

inputFile=""
outputDir=""
charset="UTF-8"
threads=""

if [[ -n "$TMPDIR" ]]; then
    tempdir="$TMPDIR"
else
    tempdir="/tmp"
fi

filters=""
allpairsChunkSize="5000"
measure="lin"
measureReversed="0"
similarityMin="0.0001"
similarityMax="Infinity"
crmiBeta=1
crmiGamma=0
leeAlpha=0.99
lpP="2"
sortChunkSize="500000"
sortK="100"
verbosity=$VERBOSITY_INFO


# ====================================
# Function Definition
# ====================================

# Print command line usage
function printUsage {
    cat <<USAGE_STRING
Usage: `basename $0` [<options>] [@<config>] <file>

  <file>                    Input file containing entry/feature pairs.

  @<config>                 Options and input files can be read from a <config>
                            file specified directly after an '@' character.
                            Options in this file should be specified exactly as
                            they would be at the command line, and may contain
                            additional '@' references to other config files. 

  General options:
    -c, --charset <name>    Set the character encoding to use for reading and
                            writing all files. Default is '$charset'.
    -h, --help              Display this usage information and exit.
    -t, --threads <num>     Number of concurrent threads to use during processes
                            that can be parallelised (i.e allpairs, sorting, and
                            knn). Default is number of CPUs.
    -T, --temp-dir <path>   Path to directory for storage of temporary files
                            used during processes (filter, sorting, and knn).
                            Default is $TMPDIR.
    -o, --output-dir <path> Directory that will contain all (non-temprary)
                            output files. Default is parent of <file>.
    -v, --verbosity <level> Set the verbosity level. 0=none, 1=error, 2=warning
                            3=info, 4=debug, 5=trace. Default is $verbosity.

  Filtering options:
    -fef, --filter-entry-freq <num>  Accept entry strings with a frequency greater
                            than or equal to <num>.
    -fep, --filter-entry-pattern <exp>  Accept entry strings that match the given
                            Perl style regular expression <exp>.
    -few, --filter-entry-whitelist <file>  Accept entry strings that are exact
                            matches a line within the given <file>.
    -fff, --filter-feature-freq <num>  Accept feature strings with a frequency
                            greater than or equal to <num>.
    -ffp, --filter-feature-pattern <exp>  Accept feature strings that match the
                            given Perl style regular expression <exp>.
    -ffw, --filter-feature-whitelist <file>  Accept feature strings that are
                            exact matches a line within the given <file>.
    -feff, --filter-entry-feature-freq <num>  Accept entry/feature feature pairs with a
                            frequency greater than or equal to <num>.

  All-pairs options:
    --allpairs-chunk-size <num> Process at most <num> records, per work
                            unit. Default is '$allpairsChunkSize'.
    -m, --measure <name>    Proximity measure to compare entries with. E.g: 
                            lin, jaccard, crmi, recallmi. Default is '$measure'.
    -r, --measure-reversed  Calculate inverse similarities. Has no effect with
                            symmertic measures.
    -Smn, --similarity-min <num> Minimum similarity value at which resultant
                            pairs will be output. Default is '$similarityMin'.
    -Smx, --similarity-max <num> Maximum similarity value at which resultant
                            pairs will be output. Default is '$similarityMin'.

  Additional options for specific measures:
    --beta <num>            Weighting between precision and recall in the
                            weighted arithmetic mean component of the CRMI
                            measure, where 0 = recall, 1 = precision, and
                            0.5 = unweighted mean. Default is '$crmiBeta'.
    --gamma <num>           Weighting between weighted arithmetic mean, and
                            harmonic mean components of the CRMI measure, where
                            0 = arithmetic mean, 1 = harmonic mean, and 0.5 is
                            the average of both. Default is '$crmiGamma'.
    --alpha <num>           For "lee" measure. Default is '$leeAlpha'.
    --p <num>               For "lp" measure. Default is '$lpP'.

  Sorting and K-nearest-neighbours options:
    -k, --sort-k <num>      For each resultant entry string, produce it's <num>
                            nearest neighbours. Default is '$sortK'.
    --sort-chunk-size <num> Process at most <num> bytes in memory, per
                            work unit. Default is '$sortChunkSize'.

  Option can be specified multiple times, and will override the previous value.
  Options are read from left to right. @<config> files will override options 
  specified before the inclusion, but will be overridden by options specified
  after inclusion.
USAGE_STRING
}

# Echo all arguments at error verbosity
function error {
    if (( $verbosity >= VERBOSITY_ERROR )); then
        echo "[Error] $@" >&2
    fi
}

# Echo all arguments at warning verbosity
function warn {
    if (( $verbosity >= VERBOSITY_WARN )); then
        echo "[Warning] $@" >&2
    fi
}

# Echo all arguments at info verbosity
function info {
    if (( $verbosity >= VERBOSITY_INFO )); then
        echo "[Info] $@"
    fi
}
# Echo all arguments at debug verbosity
function debug {
    if (( $verbosity >= VERBOSITY_DEBUG )); then
        echo "[Debug] $@"
    fi
}
# Echo all arguments at trace verbosity
function trace {
    if (( $verbosity >= VERBOSITY_TRACE )); then
        echo "[Trace] $@"
    fi
}

# Echo all arguments, and exit in error state
function die {
    trace "die(" $@ ")"

    error $@
    exit 1;
}

# Check that each argument is a file path, and that it is exceptable for data
# input (i.e it exists, contains data, and is readable.)
function checkInputFiles {
    trace "checkInputFiles(" $@ ")"

    while (( "$#" > 0 )); do
        [[ -e "$1" ]] || die "Input file '$1' does not exists."
        [[ -f "$1" ]] || die "Input file '$1' is not a regular file."
        [[ -r "$1" ]] || die "Input file '$1' is not readable."
        [[ -s "$1" ]] || die "Input file '$1' is empty."
        shift
    done
}

# Check that each argument is a directory path, and that it can be used to store
# output data (i.e that it exists, is a directory, and is writable
function checkOutputDirs {
    trace "checkOutputDirs(" $@ ")"

    while (( "$#" > 0 )); do
        [[ -e "$1" ]] || die "Directory '$1' does not exist." 
        [[ -d "$1" ]] || die "Directory '$1' is not a directory."  
        [[ -w "$1" ]] || die "Directory '$1' is not writable." 
        shift
    done
}

# Check that each argument is a file path, and that it can be used to store
# output data. If the path exists then it must be a regaular, writable file. If
# it does not exist then it must be creatable, so it's parent directory must
# be a valid output dir (see checkOutputDirs()).
function checkOutputFiles {
    trace "checkOutputFiles(" $@ ")"

    while (( "$#" > 0 )); do
        if [[ -e "$1" ]]; then
            [[ -f "$1" ]] || die "Output file '$1' is not a regular file."
            [[ -w "$1" ]] || die "Output file '$1' is not writeable."
        else
            checkOutputDirs `dirname $1`
        fi
        shift
    done
}

#
function parseArgs {
    trace "parseArgs(" $@ ")"

    # Loop through all parameters, parsing and switches. Non-switches are
    # stored and later retrieved, allowing file inputs to be specified anywhere
    # in the command line argument string
    while (( "$#" > 0 )); do
        case "$1" in
            '-h'|'--help')
                printUsage;
                exit;;
            '-c'|'--charset')
                charset="$2";
                shift 2;;
            '-t'|'--threads')
                threads="$2";
                shift 2;;
            '-T'|'--temp-dir')
                tempdir="$2";
                shift 2;;
            '-o'|'--output-dir')
                outputDir="$2";
                shift 2;;
            '-v'|'--verbosity')
                verbosity="$2";
                (( verbosity >= 0 && verbosity <= 5 )) ||
                    die "verbosity expected in range 0 to 5, found $verbosity."
                shift 2;;
            -f[fe][fpw]|-feff|\
            --filter-feature-freq|\
            --filter-feature-pattern|\
            --filter-feature-whitelist|\
            --filter-entry-freq|\
            --filter-entry-pattern|\
            --filter-entry-whitelist|\
            --filter-entry-feature-freq)
                filters="$1 $2 $filters";
                shift 2;;
            '--allpairs-chunk-size')
                allpairsChunkSize="$2";
                shift 2;;
            '-m'|'--measure')
                measure="$2";
                shift 2;;
            '-r'|'--measure-reversed')
                measureReversed="1";
                shift;;
            '-Smn'|'--similarity-min')
                similarityMin="$2";
                shift 2;;
            '-Smx'|'--similarity-max')
                similarityMax="$2";
                shift 2;;
            '--beta')
                crmiBeta="$2";
                shift 2;;
            '--gamma')
                crmiGamma="$2";
                shift 2;;
            '--alpha')
                leeAlpha="$2";
                shift 2;;
            '--p')
                lpP="$2";
                shift 2;;
            '-k'|'--sort-k')
                sortK="$2";
                shift 2;;
            '--sort-chunk-size')
                sortChunkSize="$2";
                shift 2;;
            @*) # Recurse on given config file
                configFile="${1:1}"
                debug "Reading configuration from file: '$configFile'."
                checkInputFiles $configFile
                parseArgs `cat $configFile`;
                shift;;
            '--') # Interperit all remaining arguments as input files
                shift;
                break;;
            -*)
                die "Unknown command line option: $1" >&2;;
            *)
                [[ -z "$inputFile" ]] ||
                    warn "Redefining input file from '$inputFile' to '$1'.";
                inputFile="$1"
                shift;;
           esac
    done

    if (( "$#" > 0 )); then
        [[ -z "$inputFile" ]] ||
            warn "Redefining input file from '$inputFile' to '$1'.";
        inputFile="$1"
    fi
    return 0
}


function count {
    trace "count(" $@ ")"

    info "Counting raw instances - `date`"
    debug "Input instances file: $inputFile"
    debug "Output entries files: $entriesFile"
    debug "Output features files: $featuresFile"
    debug "Output entry-features files: $entryFeaturesFile"
    debug "Charset: $charset"

    [[ -e $entriesFile ]] && warn "Overwriting entries file '$entriesFile'."
    [[ -e $featuresFile ]] && warn "Overwriting features file '$featuresFile'."
    [[ -e $entryFeaturesFile ]] && warn "Overwriting features file '$entryFeaturesFile'."

    java $JAVA_ARGS -jar Byblo.jar count \
        --charset $charset \
        --input $inputFile \
        --output-entries $entriesFile \
        --output-features $featuresFile \
        --output-entry-features $entryFeaturesFile \
        --temporary-directory $tempdir \
        || die >&2

    info "Finished counting raw instances - `date`"
}


function filter {
    trace "filter(" $@ ")"

    info "Filtering frequency data files - `date`"
    debug "Input entries files: $entriesFile"
    debug "Input features files: $featuresFile"
    debug "Input entry-features files: $entryFeaturesFile"
    debug "Output entries files: $entriesFile${FILTERED_SUFFIX}"
    debug "Output features files: $featuresFile${FILTERED_SUFFIX}"
    debug "Output entry-features files: $entryFeaturesFile${FILTERED_SUFFIX}"
    debug "Filters: $filters"
    debug "Charset: $charset"

    [[ -e $entriesFile${FILTERED_SUFFIX} ]] && \
        warn "Overwriting entries file '$entriesFile${FILTERED_SUFFIX}'."
    [[ -e $featuresFile${FILTERED_SUFFIX} ]] && \
        warn "Overwriting features file '$featuresFile${FILTERED_SUFFIX}'."
    [[ -e $entryFeaturesFile${FILTERED_SUFFIX} ]] && \
        warn "Overwriting entry-features file '$entryFeaturesFile${FILTERED_SUFFIX}'."

    java $JAVA_ARGS -jar Byblo.jar filter $filters \
        -T $tempdir \
        --input-entries $entriesFile \
        --input-features $featuresFile \
        --input-entry-features $entryFeaturesFile \
        --output-entries $entriesFile${FILTERED_SUFFIX} \
        --output-features $featuresFile${FILTERED_SUFFIX} \
        --output-entry-features $entryFeaturesFile${FILTERED_SUFFIX} \
        --charset $charset \
        || die >&2

    entriesFile=$entriesFile${FILTERED_SUFFIX}
    featuresFile=$featuresFile${FILTERED_SUFFIX}
    entryFeaturesFile=$entryFeaturesFile${FILTERED_SUFFIX}

    info "Finished filtering - `date`"

}

function allpairs {
    trace "allpairs(" $@ ")"

    info "Generate similarity file - `date`"
    debug "Input entries File: $entriesFile"
    debug "Input features File: $featuresFile"
    debug "Input entry-features File: $entryFeaturesFile"
    debug "Output: $neighsFile"
    debug "Threads: $threads"
    debug "Charset: $charset"
    debug "Similarity measure: $measure"
    debug "Similarity Range: $similarityMin to $similarityMax"
    debug "Chunk Size: $allpairsChunkSize"
    debug "Measure Reversed: $measureReversed"
    debug "Measure Arguments: beta=$crmiBeta, gamma=$crmiGamma, alpha=$leeAlpha, p=$lpP"

    apssArgs=""
    [[ -n $threads ]] && apssArgs="$apssArgs --threads $threads"
    (( $measureReversed )) && apssArgs="$apssArgs --measure-reversed"

    [[ -e $pairsFile ]] && warn "Overwriting pairs file '$pairsFile'."

    java $JAVA_ARGS -jar Byblo.jar allpairs $apssArgs \
            --input $entryFeaturesFile \
            --input-entries $entriesFile \
            --input-features $featuresFile \
            --output $pairsFile \
            --charset $charset \
            --measure $measure \
            --chunk-size $allpairsChunkSize \
            --similarity-min $similarityMin \
            --similarity-max $similarityMax \
            --crmi-beta $crmiBeta \
            --crmi-gamma $crmiGamma \
            --lee-alpha $leeAlpha \
            --mink-p $lpP \
            || die >&2

    info "Finished generate similarity file - `date`"

}

function neighbours {
    trace "neighbours(" $@ ")"

    info "Generating neighbours file - `date`"
    debug "Input pairs file: $pairsFile"
    debug "Output neighbours file: $neighsFile"
    debug "Charset: $charset"
    debug "Chunk Size: $sortChunkSize"
    debug "Threads: $threads"
    debug "Temp Dir: $tempdir"
    debug "K neighbours: $sortK"

    sortArgs=""
    [[ -n $threads ]] && sortArgs="$sortArgs --threads $threads"

    [[ -e $neighsFile ]] && warn "Overwriting neighbours file '$neighsFile'."

    java $JAVA_ARGS -jar Byblo.jar knn $sortArgs \
        --charset $charset \
        -k $sortK \
        --temporary-directory $tempdir \
        --chunk-size $sortChunkSize \
        --input $pairsFile \
        --output $neighsFile

    debug "Finished generating neighbours file - `date`"
}

# ====================================
# Process command line arguments
# ====================================

parseArgs $@


# ====================================
# Sanity check the configuration
# ====================================


# Check input file exists and is readable
checkInputFiles $inputFile

(( "${#inputFile}" > 0 && "${#outputDir}" == 0 )) &&
    outputDir=`dirname $inputFile`
checkOutputDirs $outputDir


(( "${#inputFile}" > 0 )) || die "No input file give."

[[ -n "$charset" ]] || die "Charset not set"

[[ -z "$threads" || (( "$threads" > 0 )) ]] || 
    die "Expecting no. threads >= 1, found '$threads'."

[[ "$tempdir" ]] || die "Temp dir '$tempdir' is not set."
checkOutputDirs $tempdir

#test filters ???

[[ -n "$allpairsChunkSize" ]] ||
    die "Chunk-size for all-pairs is not set."
(( "$allpairsChunkSize" >= 100 )) ||
    die "Expecting allpairs chunk-size >= 100, found '$allpairsChunkSize'."

[[ -n "$measure" ]] || die "Measure for all-pairs is not set" 

(( $measureReversed == 0 || measureReversed == 1 )) ||
    die "Expecting measure reversed to be either 0 or 1, found $measureReversed."

[[ -n "$similarityMin" ]] || die "Allpairs min similarity not set."  
[[ -n "$similarityMax" ]] || die "Allpairs max similarity not set." 
[[ -n "$crmiBeta" ]] || die "Allpairs crmi beta not set."
[[ -n "$crmiGamma" ]] || die "Allpairs crmi gamma not set." 
[[ -n "$leeAlpha" ]] || die "Allpairs lee alpha not set." 
[[ -n "$lpP" ]] || die "Allpairs lp P not set." 

[[ -n "$sortChunkSize" ]] || die "Sort chunk size not set." 
(( "$sortChunkSize" >= 10000 )) ||
    die "Expecting sort chunk size >= 10000, found '$sortChunkSize'."

[[ -n "$sortK" ]] || die "Allpairs sort K neighs not set." 
(( "$sortK" >= 1 )) || die "Expecting sort K >= 1, found '$sorkK'."





# ====================================
# Initialise all the internal variables
# ====================================


inputFileName=`basename $inputFile`

entriesFile=${outputDir}/${inputFileName}${ENTRIES_SUFFIX}
featuresFile=${outputDir}/${inputFileName}${FEATURES_SUFFIX}
entryFeaturesFile=${outputDir}/${inputFileName}${ENTRY_FEATURES_SUFFIX}
pairsFile=${outputDir}/${inputFileName}${PAIRS_SUFFIX}-$measure
neighsFile=${pairsFile}${NEIGHBOURS_SUFFIX}-${sortK}nn

checkOutputFiles $entriesFile $featuresFile \
    $entryFeaturesFile $pairsFile $neighsFile
#
# ====================================
# Run the task
# ====================================

debug "Date: `date`"
debug "System: `uname -a`"
debug "Java: `java -version 2>&1`"
debug "Working directory: `pwd`"

count

if [[ -z "$filters" ]]; then
    info "No filters specified - skipping filtering step."
else
    filter
fi

allpairs

neighbours


info "All done - `date`"

exit 0;
