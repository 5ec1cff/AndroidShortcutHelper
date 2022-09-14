package android.util;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.io.InputStream;

import dev.rikka.tools.refine.RefineAs;

@RefineAs(Xml.class)
public class XmlHidden {
    @RequiresApi(Build.VERSION_CODES.S)
    public static @NonNull TypedXmlPullParser resolvePullParser(@NonNull InputStream in)
            throws IOException {
        throw new RuntimeException("");
    }
}
