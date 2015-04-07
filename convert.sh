#!/bin/bash   
if [ "$#" != "2" ]; then   
    echo "Usage: `basename $0` dir filter"   
    exit   
fi  

odir="output"   
mkdir $odir
dir=$1   
filter=$2   
echo $1  
   
for file in `find $dir -name "$2"`; do   
    echo "$file"   
    ofile="$odir/$file"
    mkdir -p `dirname $ofile`
    iconv -f gbk -t utf8 $file > $ofile   
done 
