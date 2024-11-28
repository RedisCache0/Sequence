//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.weasis.dicom.explorer.pacsrapor;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ProccessHandler {
    private final ThreadPoolExecutor executer;

    public ProccessHandler(int poolSize, int maxPoolSize) {
        this.executer = new ThreadPoolExecutor(poolSize, maxPoolSize, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue(Integer.MAX_VALUE));
    }

    public void addTask(Runnable task) {
        this.executer.execute(task);
    }
}
