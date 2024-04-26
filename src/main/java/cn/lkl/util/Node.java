package cn.lkl.util;

import java.util.Hashtable;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Lock;

public class Node {
    int level = 0;
    int index;
    Node pre;
    Node nexts;
    Node sameLevel;
    AbstractQueuedSynchronizer[] curSources;
    Thread curThread;
    AbstractQueuedSynchronizer waitSource;
    Hashtable<Integer, Node> indexTable;
    int tableSize;
}
