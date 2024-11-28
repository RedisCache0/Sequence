//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.weasis.dicom.explorer.wado;

import java.util.concurrent.atomic.AtomicInteger;
import org.weasis.core.api.media.data.MediaSeriesGroup;

public class DownloadPriority {
    public static final AtomicInteger COUNTER = new AtomicInteger(2147483646);
    private final MediaSeriesGroup patient;
    private final MediaSeriesGroup study;
    private final MediaSeriesGroup series;
    private final boolean concurrentDownload;
    private Integer priority;

    public DownloadPriority(MediaSeriesGroup patient, MediaSeriesGroup study, MediaSeriesGroup series, boolean concurrentDownload) {
        this.patient = patient;
        this.study = study;
        this.series = series;
        this.concurrentDownload = concurrentDownload;
        this.priority = Integer.MAX_VALUE;
    }

    public MediaSeriesGroup getPatient() {
        return this.patient;
    }

    public MediaSeriesGroup getStudy() {
        return this.study;
    }

    public MediaSeriesGroup getSeries() {
        return this.series;
    }

    public boolean hasConcurrentDownload() {
        return this.concurrentDownload;
    }

    public Integer getPriority() {
        return this.priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority == null ? Integer.MAX_VALUE : priority;
    }
}
