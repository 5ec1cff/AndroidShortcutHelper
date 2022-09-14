package fivecc.tools.shortcut_helper.utils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

// from android 11 source
// frameworks/base/core/java/com/android/internal/util/XmlUtils.java
public class XmlUtils {
    public static final String[] readThisStringArrayXml(XmlPullParser parser, String endTag,
                                                        String[] name) throws XmlPullParserException, java.io.IOException {

        int num;
        try {
            num = Integer.parseInt(parser.getAttributeValue(null, "num"));
        } catch (NullPointerException e) {
            throw new XmlPullParserException("Need num attribute in string-array");
        } catch (NumberFormatException e) {
            throw new XmlPullParserException("Not a number in num attribute in string-array");
        }
        parser.next();

        String[] array = new String[num];
        int i = 0;

        int eventType = parser.getEventType();
        do {
            if (eventType == parser.START_TAG) {
                if (parser.getName().equals("item")) {
                    try {
                        array[i] = parser.getAttributeValue(null, "value");
                    } catch (NullPointerException e) {
                        throw new XmlPullParserException("Need value attribute in item");
                    } catch (NumberFormatException e) {
                        throw new XmlPullParserException("Not a number in value attribute in item");
                    }
                } else {
                    throw new XmlPullParserException("Expected item tag at: " + parser.getName());
                }
            } else if (eventType == parser.END_TAG) {
                if (parser.getName().equals(endTag)) {
                    return array;
                } else if (parser.getName().equals("item")) {
                    i++;
                } else {
                    throw new XmlPullParserException("Expected " + endTag + " end tag at: " +
                            parser.getName());
                }
            }
            eventType = parser.next();
        } while (eventType != parser.END_DOCUMENT);

        throw new XmlPullParserException("Document ended before " + endTag + " end tag");
    }

}
