import numpy as np
import hipsternet.utils as util
from hipsternet.constant import eps as c
import copy
from sklearn.utils import shuffle as skshuffle
import time
from functools import reduce
#from hipsternet.constant import worker_num
#filename = './data.txt'
'''
filename =[]
f =[[] for i in range(worker_num)]
for i in range(worker_num):
    filename.append('./worker {}.txt'.format(i+1))
for i in range(worker_num):
    f[i]=open(filename[i],'w')
'''

def get_minibatch(X, y, minibatch_size, shuffle=True):
    minibatches = []
    if shuffle:
        X, y = skshuffle(X, y)

    for i in range(0, X.shape[0], minibatch_size):
        X_mini = X[i:i + minibatch_size]
        y_mini = y[i:i + minibatch_size]

        minibatches.append((X_mini, y_mini))

    return minibatches


def sgd(nn, X_train, y_train, val_set=None, alpha=1e-3, mb_size=256, n_iter=2000, print_after=100):
    
    minibatches = get_minibatch(X_train, y_train, mb_size)

    if val_set:
        X_val, y_val = val_set

    for iter in range(1, n_iter + 1):
        idx = np.random.randint(0, len(minibatches))
        X_mini, y_mini = minibatches[idx]

        grad, loss = nn.train_step(X_mini, y_mini)
        
        if iter % print_after == 0:
            if val_set:
                val_acc = util.accuracy(y_val, nn.predict(X_val))
                print('Iter-{} loss: {:.4f} validation: {:4f}'.format(iter, loss, val_acc))
                #f.write('Iter-{} loss: {:.4f} validation: {:4f}'.format(iter, loss, val_acc))
                np.set_printoptions(threshold=np.NaN)
            
                #f.write('grad[W1] {}:{}'.format(iter,'\n'))
                #f.write('{} {}'.format(grad['W1'],'\n'))
                #f.write('grad[b1] {}:{}'.format(iter,'\n'))
                #f.write('{} {}'.format(grad['b1'],'\n'))
                #f.write('grad[W2] {}:{}'.format(iter,'\n'))
                #f.write('{} {}'.format(grad['W2'],'\n'))
                #f.write('grad[b2] {}:{}'.format(iter,'\n'))
                #f.write('{} {}'.format(grad['b2'],'\n'))
                #f.write('grad[W3] {}:{}'.format(iter,'\n'))
                #f.write('{} {}'.format(grad['W3'],'\n'))
                #f.write('grad[b3] {}:{}'.format(iter,'\n'))
                #f.write('{} {}'.format(grad['b3'],'\n'))
                
            else:
                print('Iter-{} loss: {:.4f}'.format(iter, loss))
                print('grad:',grad)
                       
        for layer in grad:
            nn.model[layer] -= alpha * grad[layer]
           

    return nn


def sgd3(nn, X_train, y_train,worker_num ,val_set=None, alpha=1e-3, mb_size=256, n_iter=2000, print_after=100):
    
    minibatches =[[]for i in range(worker_num)]
    X_mini,y_mini=[[] for i in range(worker_num)],[[] for i in range(worker_num)]
    X_val,y_val =[[] for i in range(worker_num)],[[] for i in range(worker_num)]
    grad,loss = [[] for i in range(worker_num)],[[] for i in range(worker_num)]
    val_acc =[[] for i in range(worker_num)]
    share_time = 15
    start_time = [[[]for j in range(share_time)]for i in range(worker_num)]
    totoal_time = [[]for i in range(worker_num)]
    for k in range(worker_num):
        minibatches[k] = get_minibatch(X_train[k], y_train[k], mb_size)

        if val_set:
            #X_val[k], y_val[k] = val_set[k]
            X_val,y_val = val_set
    def f(x,y):
        return x+a[y]
    
    for iter in range(1, n_iter + 1):
        for k in range(worker_num):
            start_time[k][iter%share_time] = time.time()
            idx = np.random.randint(0, len(minibatches[k]))
            X_mini[k], y_mini[k] = minibatches[k][idx]

            grad[k], loss[k] = nn[k].train_step(X_mini[k], y_mini[k])

            
            if iter % print_after == 0:
                if val_set:
                    #val_acc[k] = util.accuracy(y_val[k], nn[k].predict(X_val[k]))
                    val_acc[k] = util.accuracy(y_val, nn[k].predict(X_val))
                    print('Iter-{} worker {}, loss: {:.4f} validation: {:4f} {}'.format(iter,k+1 ,loss[k], val_acc[k],'\n'))
                    #f[k].write('Iter-{} worker {}, loss: {:.4f} validation: {:4f} {}'.format(iter,k+1 ,loss[k], val_acc[k],'\n'))
                    
                    #np.set_printoptions(threshold=np.NaN)
                    #np.set_printoptions(precision=8)
                    #f[k].write('grad[{}][W1]{}:{}'.format(k+1,iter,'\n'))
                    #f[k].write('{}{}'.format(grad[k]['W1'],'\n'))
                    #f[k].write('grad[{}][b1]{}:{}'.format(k+1,iter,'\n'))
                    #f[k].write('{}{}'.format(grad[k]['b1'],'\n'))
                    #f[k].write('grad[{}][W2]{}:{}'.format(k+1,iter,'\n'))
                    #f[k].write('{}{}'.format(grad[k]['W2'],'\n'))
                    #f[k].write('grad[{}][b2]{}:{}'.format(k+1,iter,'\n'))
                    #f[k].write('{}{}'.format(grad[k]['b2'],'\n'))
                    #f[k].write('grad[{}][W3]{}:{}'.format(k+1,iter,'\n'))
                    #f[k].write('{}{}'.format(grad[k]['W3'],'\n'))
                    #f[k].write('grad[{}][b3]{}:{}'.format(k+1,iter,'\n'))
                    #f[k].write('{}{}'.format(grad[k]['b3'],'\n'))
                    #f[k].write('\n')
                    '''
                    #print('gamma 3 ',grad[k]['gamma3'])
                    #print('beta3  ',grad[k]['beta3'])
                else:
                    print('Iter-{} loss: {:.4f}'.format(iter, loss))
                    print('grad:',grad)
                if(k+1==worker_num):
                    print('Iter-{} average loss:{} loss: {:.4f} '.format(iter,k+1 ,sum(loss)/len(loss)))
                    #f[k].write('Iter-{} average loss:{} loss: {:.4f} '.format(iter,k+1 ,sum(loss)/len(loss)))
        
            '''
            if iter%15!=0:
                for layer in grad[0]:
                    nn[k].model[layer]-=alpha*(grad[k][layer])
                start_time[k][iter%share_time]=time.time()-start_time[k][iter%share_time]
            
            if iter%15==0 and k==worker_num-1:
                average_grad = dict()
                for layer in grad[0]:
                    average_grad[layer]=0
                    for i in range(worker_num):
                        average_grad[layer]+=grad[i][layer]
                for j in range(worker_num):
                    for layer in grad[0]:
                        nn[j].model[layer] -= alpha *average_grad[layer]/worker_num
                    start_time[j][iter%15] =time.time()-start_time[j][iter%15]
                    a = start_time[j]
                    
                    totoal_time[j] = reduce(f,range(15),0)
                    print('worker{} {}-{} totoal cost time {}ms'.format(j+1,iter-14,iter,totoal_time[j]*1000))

                
                    
        '''
        for k in range(worker_num):
            for layer in grad[k]:
                if iter%15==0:
                    nn[k].model[layer] -= alpha *average_grad[layer]/worker_num
                    a = start_time[k]
                    print(a)
                    totoal_time[k] = reduce(f,range(15),0)
                    print('worker{} {}-{} totoal cost time {}ms'.format(k+1,iter-14,iter,totoal_time[k]))
                else:
                    nn[k].model[layer]-=alpha*grad[k][layer]
                    
                    print("per time {} {} ,{}".format(k+1,iter,start_time[k][iter%share_time]))
        '''
    
    return nn


def momentum(nn, X_train, y_train, val_set=None, alpha=1e-3, mb_size=256, n_iter=2000, print_after=100):
    velocity = {k: np.zeros_like(v) for k, v in nn.model.items()}
    gamma = .9

    minibatches = get_minibatch(X_train, y_train, mb_size)

    if val_set:
        X_val, y_val = val_set

    for iter in range(1, n_iter + 1):
        idx = np.random.randint(0, len(minibatches))
        X_mini, y_mini = minibatches[idx]

        grad, loss = nn.train_step(X_mini, y_mini)

        if iter % print_after == 0:
            if val_set:
                val_acc = util.accuracy(y_val, nn.predict(X_val))
                print('Iter-{} loss: {:.4f} validation: {:4f}'.format(iter, loss, val_acc))
                print('grad:',grad)
            else:
                print('Iter-{} loss: {:.4f}'.format(iter, loss))
                print('grad:',grad)

        for layer in grad:
            velocity[layer] = gamma * velocity[layer] + alpha * grad[layer]
            nn.model[layer] -= velocity[layer]

    return nn


def nesterov(nn, X_train, y_train, val_set=None, alpha=1e-3, mb_size=256, n_iter=2000, print_after=100):
    velocity = {k: np.zeros_like(v) for k, v in nn.model.items()}
    gamma = .9

    minibatches = get_minibatch(X_train, y_train, mb_size)

    if val_set:
        X_val, y_val = val_set

    for iter in range(1, n_iter + 1):
        idx = np.random.randint(0, len(minibatches))
        X_mini, y_mini = minibatches[idx]

        nn_ahead = copy.deepcopy(nn)
        nn_ahead.model.update({k: v + gamma * velocity[k] for k, v in nn.model.items()})
        grad, loss = nn_ahead.train_step(X_mini, y_mini)

        if iter % print_after == 0:
            if val_set:
                val_acc = util.accuracy(y_val, nn.predict(X_val))
                print('Iter-{} loss: {:.4f} validation: {:4f}'.format(iter, loss, val_acc))
                print('grad:',grad)
            else:
                print('Iter-{} loss: {:.4f}'.format(iter, loss))
                print('grad:',grad)

        for layer in grad:
            velocity[layer] = gamma * velocity[layer] + alpha * grad[layer]
            nn.model[layer] -= velocity[layer]

    return nn


def adagrad(nn, X_train, y_train, val_set=None, alpha=1e-3, mb_size=256, n_iter=2000, print_after=100):
    cache = {k: np.zeros_like(v) for k, v in nn.model.items()}

    minibatches = get_minibatch(X_train, y_train, mb_size)

    if val_set:
        X_val, y_val = val_set

    for iter in range(1, n_iter + 1):
        idx = np.random.randint(0, len(minibatches))
        X_mini, y_mini = minibatches[idx]

        grad, loss = nn.train_step(X_mini, y_mini)

        if iter % print_after == 0:
            if val_set:
                val_acc = util.accuracy(y_val, nn.predict(X_val))
                print('Iter-{} loss: {:.4f} validation: {:4f}'.format(iter, loss, val_acc))
                print('grad:',grad)
            else:
                print('Iter-{} loss: {:.4f}'.format(iter, loss))
                print('grad:',grad)

        for k in grad:
            cache[k] += grad[k]**2
            nn.model[k] -= alpha * grad[k] / (np.sqrt(cache[k]) + c.eps)

    return nn


def rmsprop(nn, X_train, y_train, val_set=None, alpha=1e-3, mb_size=256, n_iter=2000, print_after=100):
    cache = {k: np.zeros_like(v) for k, v in nn.model.items()}
    gamma = .9

    minibatches = get_minibatch(X_train, y_train, mb_size)

    if val_set:
        X_val, y_val = val_set

    for iter in range(1, n_iter + 1):
        idx = np.random.randint(0, len(minibatches))
        X_mini, y_mini = minibatches[idx]

        grad, loss = nn.train_step(X_mini, y_mini)

        if iter % print_after == 0:
            if val_set:
                val_acc = util.accuracy(y_val, nn.predict(X_val))
                print('Iter-{} loss: {:.4f} validation: {:4f}'.format(iter, loss, val_acc))
                print('grad:',grad)
            else:
                print('Iter-{} loss: {:.4f}'.format(iter, loss))
                print('grad:',grad)

        for k in grad:
            cache[k] = util.exp_running_avg(cache[k], grad[k]**2, gamma)
            nn.model[k] -= alpha * grad[k] / (np.sqrt(cache[k]) + c.eps)

    return nn


def adam(nn, X_train, y_train, val_set=None, alpha=0.001, mb_size=256, n_iter=2000, print_after=100):
    M = {k: np.zeros_like(v) for k, v in nn.model.items()}
    R = {k: np.zeros_like(v) for k, v in nn.model.items()}
    beta1 = .9
    beta2 = .999

    minibatches = get_minibatch(X_train, y_train, mb_size)

    if val_set:
        X_val, y_val = val_set

    for iter in range(1, n_iter + 1):
        t = iter
        idx = np.random.randint(0, len(minibatches))
        X_mini, y_mini = minibatches[idx]

        grad, loss = nn.train_step(X_mini, y_mini)

        if iter % print_after == 0:
            if val_set:
                val_acc = util.accuracy(y_val, nn.predict(X_val))
                print('Iter-{} loss: {:.4f} validation: {:4f}'.format(iter, loss, val_acc))
                #print('grad:',grad)
                f.write('Iter-{} : {}'.format(iter,grad))
            else:
                print('Iter-{} loss: {:.4f}'.format(iter, loss))
                f.write('Iter-{} : {}'.format(iter,grad))

        for k in grad:
            M[k] = util.exp_running_avg(M[k], grad[k], beta1)
            R[k] = util.exp_running_avg(R[k], grad[k]**2, beta2)

            m_k_hat = M[k] / (1. - beta1**(t))
            r_k_hat = R[k] / (1. - beta2**(t))

            nn.model[k] -= alpha * m_k_hat / (np.sqrt(r_k_hat) + c.eps)

    return nn


def adam_rnn(nn, X_train, y_train, alpha=0.001, mb_size=256, n_iter=2000, print_after=100):
    M = {k: np.zeros_like(v) for k, v in nn.model.items()}
    R = {k: np.zeros_like(v) for k, v in nn.model.items()}
    beta1 = .9
    beta2 = .999

    minibatches = get_minibatch(X_train, y_train, mb_size, shuffle=False)

    idx = 0
    state = nn.initial_state()
    smooth_loss = -np.log(1.0 / len(set(X_train)))

    for iter in range(1, n_iter + 1):
        t = iter

        if idx >= len(minibatches):
            idx = 0
            state = nn.initial_state()

        X_mini, y_mini = minibatches[idx]
        idx += 1

        if iter % print_after == 0:
            print("=========================================================================")
            print('Iter-{} loss: {:.4f}'.format(iter, smooth_loss))
            print("=========================================================================")

            sample = nn.sample(X_mini[0], state, 100)
            print(sample)

            print("=========================================================================")
            print()
            print()

        grad, loss, state = nn.train_step(X_mini, y_mini, state)
        smooth_loss = 0.999 * smooth_loss + 0.001 * loss

        for k in grad:
            M[k] = util.exp_running_avg(M[k], grad[k], beta1)
            R[k] = util.exp_running_avg(R[k], grad[k]**2, beta2)

            m_k_hat = M[k] / (1. - beta1**(t))
            r_k_hat = R[k] / (1. - beta2**(t))

            nn.model[k] -= alpha * m_k_hat / (np.sqrt(r_k_hat) + c.eps)

    return nn
