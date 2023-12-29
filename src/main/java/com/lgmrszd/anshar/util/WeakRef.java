package com.lgmrszd.anshar.util;

import java.lang.ref.WeakReference;
import java.util.function.Consumer;

public class WeakRef<T> extends WeakReference<T> {

    public WeakRef(T referent) { super(referent); }
    
    public void ifPresent(Consumer<T> consumer){
        if (!refersTo(null)) consumer.accept(get());
    }

    public void ifPresentOrElse(Consumer<T> consumer, Runnable els){
        if (!refersTo(null)) consumer.accept(get());
        else els.run();
    }

    

}
