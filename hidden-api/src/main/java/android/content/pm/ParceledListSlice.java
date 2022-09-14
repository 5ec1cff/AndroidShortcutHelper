package android.content.pm;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class ParceledListSlice<T extends Parcelable> extends BaseParceledListSlice<T> {
    public ParceledListSlice(List<T> list) {
        super(list);
    }

    private ParceledListSlice(Parcel in, ClassLoader loader) {
        super(in, loader);
    }

    public static <T extends Parcelable> ParceledListSlice<T> emptyList() {
        throw new RuntimeException("STUB");
    }

    @Override
    public int describeContents() {
        throw new RuntimeException("STUB");
    }

    @Override
    protected void writeElement(T parcelable, Parcel dest, int callFlags) {
        throw new RuntimeException("STUB");
    }

    @Override
    protected void writeParcelableCreator(T parcelable, Parcel dest) {
        throw new RuntimeException("STUB");
    }

    @Override
    protected Parcelable.Creator<?> readParcelableCreator(Parcel from, ClassLoader loader) {
        throw new RuntimeException("STUB");
    }

    public static final Parcelable.ClassLoaderCreator<ParceledListSlice> CREATOR =
            new Parcelable.ClassLoaderCreator<ParceledListSlice>() {
                public ParceledListSlice createFromParcel(Parcel in) {
                    throw new RuntimeException("STUB");
                }

                @Override
                public ParceledListSlice createFromParcel(Parcel in, ClassLoader loader) {
                    throw new RuntimeException("STUB");
                }

                @Override
                public ParceledListSlice[] newArray(int size) {
                    throw new RuntimeException("STUB");
                }
            };
}