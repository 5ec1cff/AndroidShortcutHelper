package android.os;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

import dev.rikka.tools.refine.RefineAs;

@RefineAs(PersistableBundle.class)
public class PersistableBundleHidden {
    public static PersistableBundle restoreFromXml(XmlPullParser in) throws IOException,
            XmlPullParserException {
        throw new RuntimeException("");
    }
}
