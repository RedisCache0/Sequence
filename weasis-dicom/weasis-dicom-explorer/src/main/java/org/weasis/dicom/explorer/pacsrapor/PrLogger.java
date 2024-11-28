//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.weasis.dicom.explorer.pacsrapor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrLogger {
    private static boolean devel = true;
    private static final Logger Logger = LoggerFactory.getLogger("DicomExplorer");

    public PrLogger() {
    }

    public static void info(String msg) {
        if (devel) {
            Logger.info(msg);
        }

    }

    public static void info(String msg, Object obj) {
        if (devel) {
            Logger.info(msg, obj);
        }

    }

    public static void warn(String msg) {
        if (devel) {
            Logger.warn(msg);
        }

    }

    public static void warn(String msg, Object obj) {
        if (devel) {
            Logger.warn(msg, obj);
        }

    }

    public static void error(String msg) {
        if (devel) {
            Logger.error(msg);
        }

    }

    public static void error(String msg, Object obj) {
        if (devel) {
            Logger.error(msg, obj);
        }

    }
}
