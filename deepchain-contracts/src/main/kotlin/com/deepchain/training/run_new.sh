#!/bin/sh
basename=$(cd `dirname $0`;pwd)
fileprefix=mnist_
filename=run_mnist_
bash=14
for i in $(seq 4 10)
do
  r=$((bash-i))
  echo ${r}
  cd $basename/$fileprefix${i}
  python ${filename}*
done
