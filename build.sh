#!/bin/bash 

# ScriptPath=$(cd "$(dirname "$0")"; pwd)
Directory=$(pwd)
Platform="all"
MODULES_REGEX=".*(shapes|googlemap|slidemenu|commonjs|charts|shapes|motion|bluetooth|camera)"
IPHONE_REGEX=$MODULES_REGEX".*titanium\.xcconfig"
ANDROID_REGEX=$MODULES_REGEX".*build\.xml"
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
    for file in $(find -E "${Directory}" -type f -iregex "$IPHONE_REGEX")
    do
        dir=$(dirname "${file}")
        cd $dir
        ./build.py
        version=$(grep -oEi "(?:^version\s*:\s*)(([0-9])+(\.{0,1}([0-9]))*)+" manifest  | awk '{print $2}')
        moduleid=$(grep -oEi "(?:^moduleid\s*:\s*)(.*)" manifest  | awk '{print $2}')
        cp "$moduleid-iphone-$version.zip" $OutputDir
        cd -
    done
fi

if ([ "$Platform" = "all" -o "$Platform" = "android" ]); then
    echo "Building android modules with $ANDROID_REGEX in ${Directory}"
    for file in $(find -E "${Directory}" -type f -iregex "$ANDROID_REGEX")
    do
        dir=$(dirname "${file}")
        cd $dir
        moduleid=$(grep -oEi "(?:^moduleid\s*:\s*)(.*)" manifest  | awk '{print $2}')
        version=$(grep -oEi "(?:^version\s*:\s*)(([0-9])+(\.{0,1}([0-9]))*)+" manifest  | awk '{print $2}')
        echo "Building android module ${moduleid} version ${version} in ${dir}"
        ant clean;ant
        cp "./dist/$moduleid-android-$version.zip" "$OutputDir"
        cd -
    done
fi


