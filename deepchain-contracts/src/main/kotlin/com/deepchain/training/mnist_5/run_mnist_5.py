import numpy as np
import sys
sys.path.append('..')
import hipsternet.input_data as input_data
import hipsternet.neuralnet as nn
from hipsternet.solver import *
import os
os.environ['TF_CPP_MIN_LOG_LEVEL']='2'
from tensorflow.examples.tutorials.mnist import input_data
from hipsternet.split_mnist import Split_data
from hipsternet.constant import worker_num5
n_iter = 1500                                                                                                                                                                                                                                                                                                                                                                                             
#alpha = 1e-2 adam
alpha =5e-1
mb_size = 64
n_experiment = 1
reg = 1e-5
print_after = 50
p_dropout = .8
loss = 'cross_ent'
nonlin = 'relu'
solver = 'sgd'
solver3 = 'sgd3'
#worker_num5 =10
'''
filename1 = './1.txt'
filename2 = './2.txt'
f1 = open(filename1,'w')
f2 = open(filename2,'w')
'''
#mnist = input_data.read_data_sets('MNIST_Data/',one_hot = True)

def prepro(X_train, X_val, X_test):
    mean = np.mean(X_train)
    return X_train - mean, X_val - mean, X_test - mean


if __name__ == '__main__':
    if len(sys.argv) > 1:
        net_type = sys.argv[1]
        valid_nets = ('ff', 'cnn')

        if net_type not in valid_nets:
            raise Exception('Valid network type are {}'.format(valid_nets))
    else:
        net_type = 'cnn'

    mnist = input_data.read_data_sets('./MNIST_data', one_hot=False)
    
    
    val_set = [[] for i in range(worker_num5)]
    data = Split_data(mnist,worker_num5)
    X_train,y_train,\
    sample_train_x,sample_train_y,\
    train_x,train_y,\
    test_x,test_y,\
    vali_x,vali_y= data.split_data()
    for i in range(worker_num5):
        M, D, C = X_train[i].shape[0], X_train[i].shape[1], y_train[i].max() + 1
        X_train[i] = X_train[i] -np.mean(X_train[i])
        #val_set[i]=(X_val[i], y_val[i])
        print("M {} D {} C {} ".format(M,D,C))
        print("sample_train_x shape",sample_train_x.shape)
    
    if net_type == 'cnn':
        img_shape = (1, 28, 28)
        for i in range(worker_num5):
            X_train[i] = X_train[i].reshape(-1, *img_shape)
            #X_val[i] = X_val[i].reshape(-1, *img_shape)
            #X_test[i] = X_test[i].reshape(-1, *img_shape)
            #val_set[i]=(X_val[i], y_val[i])
    X1_train,y1_train,X_test,y_test,X1_val,y1_val= mnist.train.images,mnist.train.labels,\
                                                   mnist.test.images,mnist.test.labels,\
                                                   mnist.validation.images,mnist.validation.labels
    
    
    X1_train,X_test ,X1_val= prepro(X1_train,X_test,X1_val)
    #X1_train = X1_train.reshape(-1,*img_shape)
    X_test = X_test.reshape(-1,*img_shape)
    X1_val = X1_val.reshape(-1,*img_shape)
    sample_train_x = sample_train_x.reshape(-1,*img_shape)

    val1_set = (X1_val,y1_val)
    solvers = dict(
        sgd=sgd,
        sgd3 = sgd3,
        momentum=momentum,
        nesterov=nesterov,
        adagrad=adagrad,
        rmsprop=rmsprop,
        adam=adam
    )

    solver_fun = solvers[solver3]
    solver2_fun = solvers[solver]
    accs = []
    '''
    for i in range(4):
        accs.append(np.zeros(n_experiment))
        '''
    accs1 = np.zeros(n_experiment)
    accs2 = np.zeros(n_experiment)


    print()
    print('Experimenting on {}'.format(solver3))
    print()

    for k in range(n_experiment):
        print('Experiment-{}'.format(k + 1))

        # Reset model
       
        #multi worker
        net = []
        net1 = []
        for i in range(worker_num5):
            if net_type == 'ff':
                net.append(nn.FeedForwardNet(D, C, H=128, lam=reg, p_dropout=p_dropout, loss=loss, nonlin=nonlin))
                net1 = nn.FeedForwardNet(D, C, H=128,lam=reg,p_dropout=p_dropout,loss = loss,nonlin = nonlin)
                net2 = nn.FeedForwardNet(D, C, H=128,lam=reg,p_dropout=p_dropout,loss = loss,nonlin = nonlin)
            elif net_type == 'cnn':
                net.append(nn.ConvNet(10, C, H=128))
                net1.append(nn.ConvNet(10,C,H=128))
                net2 = nn.ConvNet(10,C,H=128)
        
        net = solver_fun(
            net, X_train, y_train, worker_num=worker_num5,val_set=val1_set, mb_size=mb_size, alpha=alpha,
            n_iter=n_iter, print_after=print_after
        )
        y_pred = []
        accs=[]
        for i in range(worker_num5):
            y_pred.append(net[i].predict(X_test))
            accs.append(np.mean(y_pred[i] == y_test))

        for i in range(worker_num5):
            print('Mean accuracy {}: {:.4f}, std: {:.4f}'.format(i+1,accs[i].mean(), accs[i].std()))
        
        accs1 =[]
        y1_pred =[]
        for i in range(worker_num5):
            net1[i] = solver2_fun(
                net1[i],X_train[i],y_train[i],val_set = val1_set,mb_size=mb_size,alpha=alpha,
                n_iter = n_iter,print_after = print_after
                )
            y1_pred.append(net1[i].predict(X_test))
            accs1.append(np.mean(y1_pred[i]==y_test))
        for i in range(worker_num5):
            print('Single Mean accuracy{} :{:.4f},std :{:.4f}'.format(i+1,accs1[i].mean(),accs1[i].std()))

        net2 = solver2_fun(
            net2,sample_train_x,sample_train_y,val_set = val1_set,mb_size = mb_size,alpha = alpha,
            n_iter = n_iter,print_after = print_after
            )
        y2_pred = net2.predict(X_test)
        accs2 = np.mean(y2_pred==y_test)
    print()
    for i in range(worker_num5):
        print('Mean accuracy {}: {:.4f}, std: {:.4f}'.format(i+1,accs[i].mean(), accs[i].std()))
    for i in range(worker_num5):
        print('Single Mean accuracy :{:.4f},std :{:.4f}'.format(accs1[i].mean(),accs1[i].std()))
    print('Mean accuracy :{:.4f},std :{:.4f}'.format(accs2.mean(),accs2.std()))
