/*
 * Copyright (C) 2005-2017 UCWeb Inc. All rights reserved.
 *  Description :SegmentPool.java
 *
 *  Creation    : 2017-06-03
 *  Author      : zhonglian.wzl@alibaba-inc.com
 */
package vanda.wzl.vandadownloader.quarkokio;


import org.jetbrains.annotations.Nullable;

/**
 * A collection of unused segments, necessary to avoid GC churn and zero-fill.
 * This pool is a thread-safe static singleton.
 */
final class SegmentPool {

    /** The maximum number of bytes to pool. */
    // TODO: Is 64 KiB a good maximum size? Do we ever have that many idle segments?
    static final long MAX_SIZE = 2 * 1024 * 1024; // 64 KiB.

    /** Singly-linked list of segments. */
    static @Nullable
    Segment next;

    /** Total bytes in this pool. */
    static long byteCount;

    private SegmentPool() {
    }

    static Segment take() {
        synchronized (SegmentPool.class) {
            if (next != null) {
                Segment result = next;
                next = result.next;
                result.next = null;
                byteCount -= Segment.SIZE;
                return result;
            }
        }
        return new Segment(); // Pool is empty. Don't zero-fill while holding a lock.
    }

    static void recycle(Segment segment) {
        if (segment.next != null || segment.prev != null) throw new IllegalArgumentException();
        if (segment.shared) return; // This segment cannot be recycled.
        synchronized (SegmentPool.class) {
            if (byteCount + Segment.SIZE > MAX_SIZE) return; // Pool is full.
            byteCount += Segment.SIZE;
            segment.next = next;
            segment.pos = segment.limit = 0;
            next = segment;
        }
    }

//    /**
//     * The maximum number of bytes to pool.
//     */
//    // TODO: Is 64 KiB a good maximum size? Do we ever have that many idle segments?
////    static final long MAX_SIZE = 64 * 1024 * 16; // 64 KiB.
//
//    /**
//     * Singly-linked list of segments.
//     */
//    static Segment next;
//
////    static Queue<Segment> queue = new ConcurrentLinkedQueue<>();
//
//    /**
//     * Total bytes in this pool.
//     */
//    static long byteCount;
//
//    private SegmentPool() {
//    }
//
//    static Segment take() {
//        synchronized (SegmentPool.class) {
//            if (next != null) {
//                Segment result = next;
//                next = result.next;
//                result.next = null;
//                byteCount -= Segment.SIZE;
//                return result;
//            }
//
////            if (queue.size() > 0) {
////                Segment result = queue.poll();
////                result.prev = null;
//////                next = result.next;
////                result.next = null;
////                byteCount -= Segment.SIZE;
////                return result;
////            }
//            return new Segment(); // Pool is empty. Don't zero-fill while holding a lock.
//        }
//    }
//
//    static void recycle(Segment segment) {
//        synchronized (SegmentPool.class) {
////            if (segment.next != null || segment.prev != null) throw new IllegalArgumentException();
////            if (segment.shared) {
////                return; // This segment cannot be recycled.
////            }
////      if (byteCount + Segment.SIZE > MAX_SIZE) {
////        Log.e("vanda", "Segment Pool is full.");
////        return; // Pool is full.
////      }
////            byteCount += Segment.SIZE;
////            segment.next = null;
////            segment.prev = null;
////            segment.pos = segment.limit = 0;
//////            next = segment;
////            queue.offer(segment);
//
//            if (segment.next == null && segment.prev == null) {
//                if (!segment.shared) {
//                    byteCount += Segment.SIZE;
//                    segment.next = next;
//                    segment.pos = segment.limit = 0;
//                    next = segment;
//                }
//            } else {
//                throw new IllegalArgumentException();
//            }
//        }
//    }
}
