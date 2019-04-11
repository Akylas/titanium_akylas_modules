#!/bin/bash 

# ScriptPath=$(cd "$(dirname "$0")"; pwd)
Directory=$(pwd)
Platform="all"
MODULES_REGEX=".*(ti\.paint|shapes|connectiq|googlemap|slidemenu|commonjs|charts2|shapes|motion|bluetooth|camera|zoomableimage|udp)"
IPHONE_REGEX=$MODULES_REGEX".*titanium\.xcconfig"
ANDROID_REGEX=$MODULES_REGEX".*build\.properties"
OutputDir="/Volumes/data/dev/titanium/dist_modules"
Usage()
{
cat <<-ENDOFMESSAGE

$0 [OPTION] REQ1 REQ2

options:

    -p -platform  ios or android are supported
    -m -module  module name
    -h --help   display this message

ENDOFMESSAGE
    exit 1
}

Die()
{
    echo "$*"
    exit 1
}

GetOpts() {
    argv=
    while [ $# -gt 0 ]
    do
        opt=$1
        shift
        case ${opt} in
            -p|--platform)
                if [ $# -eq 0 -o "${1:0:1}" = "-" ]; then
                    Die "The ${opt} option requires an argument."
                fi
                Platform="$1"
                shift
                ;;
            -d|--directory)
                if [ $# -eq 0 -o "${1:0:1}" = "-" ]; then
                    Die "The ${opt} option requires an argument."
                fi
                Directory="$1"
                shift
                ;;
            -o|--output)
                if [ $# -eq 0 -o "${1:0:1}" = "-" ]; then
                    Die "The ${opt} option requires an argument."
                fi
                OutputDir="$1"
                shift
                ;;
            -m|--module)
                if [ $# -eq 0 -o "${1:0:1}" = "-" ]; then
                    Die "The ${opt} option requires an argument."
                fi
                MODULES_REGEX=".*($1)"
                shift
                ;;
            -h|--help)
                Usage;;
            *)
                if [ "${opt:0:1}" = "-" ]; then
                    Die "${opt}: unknown option."
                fi
                argv+=${opt};;
        esac
    done
}

GetOpts $*

echo "Directory ${Directory}"
echo "Platform ${Platform}"




if ([ "$Platform" = "all" -o "$Platform" = "ios" ]); then
    echo "Building ios modules with $IPHONE_REGEX in ${Directory}"
    for file in $(find -E "${Directory}" . ! \( -type d \) -iregex "$IPHONE_REGEX")
    do
        dir=$(dirname "${file}")
        cd $dir
        rm -fr build/
        version=$(grep -oEi "(?:^version\s*:\s*)(([0-9])+(\.{0,1}([0-9]))*)+" manifest  | awk '{print $2}')
        moduleid=$(grep -oEi "(?:^moduleid\s*:\s*)(.*)" manifest  | awk '{print $2}')
        echo "Building ios module ${moduleid} version ${version} in ${dir}"
        ti build -p ios --output-dir="$OutputDir"
        # if [ $? -e 0 ] ; then
        #     cp "$moduleid-iphone-$version.zip" "$OutputDir"
        # fi
        cd -
    done
fi

if ([ "$Platform" = "all" -o "$Platform" = "android" ]); then
    echo "Building android modules with $ANDROID_REGEX in ${Directory}"
    for file in $(find -E "${Directory}" . ! \( -type d \) -iregex "$ANDROID_REGEX")
    do
        echo "test ${file}"
        dir=$(dirname "${file}")
        cd $dir
        rm -fr build/
        moduleid=$(grep -oEi "(?:^moduleid\s*:\s*)(.*)" manifest  | awk '{print $2}')
        version=$(grep -oEi "(?:^version\s*:\s*)(([0-9])+(\.{0,1}([0-9]))*)+" manifest  | awk '{print $2}')
        echo "Building android module ${moduleid} version ${version} in ${dir}"
        ti build -p android --output-dir="$OutputDir"
        # if [ $? -e 0 ] ; then
        #     cp "./dist/$moduleid-android-$version.zip" "$OutputDir"
        # fi
        cd -
    done
fi


