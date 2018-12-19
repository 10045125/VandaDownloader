package vanda.wzl.vandadownloader.status;


import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


@Retention(RetentionPolicy.SOURCE)
public @interface OnStatus {
    int INVALID = 0;
    int PENGING = 1;
    int START = 2;
    int CONTECT = 3;
    int PROGRESS = 4;
    int COMPLETE = 5;
    int PAUSE = 6;
    int ERROR = 7;
    int RETRY = 8;
}
