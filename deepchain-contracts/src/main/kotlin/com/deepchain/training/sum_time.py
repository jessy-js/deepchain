import os
import sys
import numpy as np 
import math 
from functools import reduce
import re

f = open('./1.txt','r')
data = f.read()
print(data)
print(type(data))
data1 = data.split(' ')
print(data1)
newdata = []
for element in data1:
	if 'ms' in element:
		newdata.append(element)

new_elemnent = []
for element in newdata:
	index = element.find('m')
	print(index)
	element = element[:index]
	new_elemnent.append(element)
print(new_elemnent)
sum = 0
for i in new_elemnent:
	sum+=float(i)
print(sum)

