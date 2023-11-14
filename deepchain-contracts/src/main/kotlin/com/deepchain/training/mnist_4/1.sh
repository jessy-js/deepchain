#!/bin/bash
copy_file=run_mnist_4.py
bashpath1=$(cd `dirname $0`; pwd)
bashpath2=$(cd "$(dirname "$0")/..";pwd)
want_file_path=mnist_
change_file=run_mnist_
suffix=.py
echo $bashpath2
echo $want_file_path
echo $change_file
echo $suffix
for i in $(seq 5 10)
do 
   cp ${bashpath1}/${copy_file} ${bashpath2}/${want_file_path}${i}/
   new_path=${bashpath2}/${want_file_path}${i}
   echo $new_path
   cd ${new_path}
   mv ${copy_file} ${change_file}${i}${suffix}
done
