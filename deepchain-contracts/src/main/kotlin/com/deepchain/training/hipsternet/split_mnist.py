import numpy as np
from sklearn.model_selection import train_test_split
from tensorflow.examples.tutorials.mnist import input_data
import tensorflow as tf
from sklearn.utils import shuffle as skshuffle
import os
os.environ["TF_CPP_MIN_LOG_LEVEL"]="2"
mnist = input_data.read_data_sets("MNIST_Data/",one_hot=True)
#train_x,train_y,test_x,test_y = mnist.train.images,mnist.train.labels,mnist.test.images,mnist.test.labels
def get_sample(X,y,size,shuffle=True):
    return X[:size],y[:size]

class Split_data(object):
    def __init__(self,data,worker_num):
        self.data = data
        self.worker_num = worker_num
        
    def split_data(self):
        #data = input_data.read_data_sets('MNIST_Data/',one_hot=True )
        train_x,train_y,test_x,test_y,vali_x,vali_y = self.data.train.images,self.data.train.labels,self.data.test.images,self.data.test.labels,self.data.validation.images,self.data.validation.labels
    
        sample_train_x,sample_train_y = get_sample(train_x,train_y,int(self.worker_num*55000/10))
        split_slice_train = int(len(sample_train_x)/(self.worker_num))
        #split_slice_test = int(len(test_x)/(self.worker_num))
        #split_slice_vali = int(len(vali_x)/(self.worker_num))
        split_data_train_x = []
        split_data_train_y = []
        #split_data_test_x  = []
        #split_data_test_y  = []
        #split_data_vali_x  = []
        #split_data_vali_y  = []
        start_train = 0
        #start_test=0
        #start_vali = 0
        for i in range(self.worker_num):
            split_data_train_x.append(train_x[start_train:start_train+split_slice_train])
            split_data_train_y.append(train_y[start_train:start_train+split_slice_train])
            #split_data_test_x.append(test_x [start_test :start_test +split_slice_test])
            #split_data_test_y.append(test_y [start_test :start_test +split_slice_test])
            #split_data_vali_x.append(vali_x[start_vali:start_vali+split_slice_vali])
            #split_data_vali_y.append(vali_y[start_vali:start_vali+split_slice_vali])
            start_train+=split_slice_train
            #start_test +=split_slice_test
            #start_vali +=split_slice_vali
        
        return split_data_train_x,split_data_train_y,\
               sample_train_x,sample_train_y,\
               train_x,train_y,\
               test_x,test_y,\
               vali_x,vali_y
'''
data = Split_data(mnist,4)
split_data_train_x,split_data_test_x,split_data_train_y,split_data_test_y = data.split_data()
print('split_data_train_x:',split_data_train_x[1].shape)
print('split_data_test_x:',split_data_test_x[1].shape)
print('split_data_train_y:',split_data_train_y[1].shape)
print('split_data_test_y:',split_data_test_y[1].shape)

'''
    
