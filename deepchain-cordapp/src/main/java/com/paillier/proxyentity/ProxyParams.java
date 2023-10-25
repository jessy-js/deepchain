package com.paillier.proxyentity;

/**
 * Created by jessy on 2019/6/3.
 */
public class ProxyParams {
    private ProxyPublicParams pp;
    private ProxySecretParams sp;

    public ProxyPublicParams getPp() {
        return pp;
    }

    public void setPp(ProxyPublicParams pp) {
        this.pp = pp;
    }

    public ProxySecretParams getSp() {
        return sp;
    }

    public void setSp(ProxySecretParams sp) {
        this.sp = sp;
    }
}
