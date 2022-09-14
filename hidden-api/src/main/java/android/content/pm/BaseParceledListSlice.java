package android.content.pm;

import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

abstract class BaseParceledListSlice<T> implements Parcelable {

    /*
     * TODO get this number from somewhere else. For now set it to a quarter of
     * the 1MB limit.
     */

    public BaseParceledListSlice(List<T> list) {
        throw new RuntimeException("STUB");
    }

    @SuppressWarnings("unchecked")
    BaseParceledListSlice(Parcel p, ClassLoader loader) {
        throw new RuntimeException("STUB");
    }

    public List<T> getList() {
        throw new RuntimeException("STUB");
    }

    /**
     * Set a limit on the maximum number of entries in the array that will be included
     * inline in the initial parcelling of this object.
     */
    public void setInlineCountLimit(int maxCount) {
        throw new RuntimeException("STUB");
    }

    /**
     * Write this to another Parcel. Note that this discards the internal Parcel
     * and should not be used anymore. This is so we can pass this to a Binder
     * where we won't have a chance to call recycle on this.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        throw new RuntimeException("STUB");
    }

    protected abstract void writeElement(T parcelable, Parcel reply, int callFlags);

    protected abstract void writeParcelableCreator(T parcelable, Parcel dest);

    protected abstract Parcelable.Creator<?> readParcelableCreator(Parcel from, ClassLoader loader);
}