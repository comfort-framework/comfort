public class Class1 {
    public Class1(int number) {
        i = i++;
        i = ++i;
        i = i--;
        i = --i;
        i = i==1;
        i = i!=1;
        i = i<=1;
        i = i>=1;
        i = i<1;
        i = i>1;
        i = !i;
        i = i&&j;
        i = i||j;
        i = i+1;
        i = i-1;
        i = i*1;
        i = i/1;
        i = i%1;
        i = i&1;
        i = i|1;
        i = i^1;
        i = i<<1;
        i = i>>1;
        i = i>>>1;
        i = i&&1;
        i = -j;
        i++;
        l--;
        this.suspendResumeLock = config.isAllowPoolSuspension() ? new SuspendResumeLock(true) : SuspendResumeLock.FAUX_LOCK;
    }
}