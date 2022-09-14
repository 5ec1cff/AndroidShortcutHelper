package fivecc.tools.shortcut_helper.utils;

import android.app.Person;
import android.content.ComponentName;
import android.content.Intent;
import android.content.LocusId;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutInfoHidden;
import android.os.Build;
import android.os.PersistableBundle;
import android.os.PersistableBundleHidden;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.AtomicFile;
import android.util.Log;
import android.util.Xml;
import android.util.XmlHidden;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

// frameworks/base/services/core/java/com/android/server/pm/ShortcutService.java
// frameworks/base/services/core/java/com/android/server/pm/ShortcutUser.java
// frameworks/base/services/core/java/com/android/server/pm/ShortcutPackage.java
public class ShortcutParser {
    private static final String TAG = "ShortcutParser";

    static final String DIRECTORY_PACKAGES = "packages";
    static final String FILENAME_USER_PACKAGES = "shortcuts.xml";
    static final String DIRECTORY_PER_USER = "shortcut_service";

    static final String TAG_PACKAGE_ROOT = "package";
    static final String TAG_USER_ROOT = "user";
    private static final String TAG_INTENT_EXTRAS_LEGACY = "intent-extras";
    private static final String TAG_INTENT = "intent";
    private static final String TAG_EXTRAS = "extras";
    private static final String TAG_SHORTCUT = "shortcut";
    private static final String TAG_CATEGORIES = "categories";
    private static final String TAG_PERSON = "person";

    private static final String ATTR_NAME = "name";
    private static final String ATTR_ID = "id";
    private static final String ATTR_ACTIVITY = "activity";
    private static final String ATTR_TITLE = "title";
    private static final String ATTR_TITLE_RES_ID = "titleid";
    private static final String ATTR_TITLE_RES_NAME = "titlename";
    private static final String ATTR_TEXT = "text";
    private static final String ATTR_TEXT_RES_ID = "textid";
    private static final String ATTR_TEXT_RES_NAME = "textname";
    private static final String ATTR_DISABLED_MESSAGE = "dmessage";
    private static final String ATTR_DISABLED_MESSAGE_RES_ID = "dmessageid";
    private static final String ATTR_DISABLED_MESSAGE_RES_NAME = "dmessagename";
    private static final String ATTR_DISABLED_REASON = "disabled-reason";
    private static final String ATTR_INTENT_LEGACY = "intent";
    private static final String ATTR_INTENT_NO_EXTRA = "intent-base";
    private static final String ATTR_RANK = "rank";
    private static final String ATTR_TIMESTAMP = "timestamp";
    private static final String ATTR_FLAGS = "flags";
    private static final String ATTR_ICON_RES_ID = "icon-res";
    private static final String ATTR_ICON_RES_NAME = "icon-resname";
    private static final String ATTR_BITMAP_PATH = "bitmap-path";
    private static final String ATTR_ICON_URI = "icon-uri";
    private static final String ATTR_LOCUS_ID = "locus-id";

    private static final String ATTR_PERSON_NAME = "name";
    private static final String ATTR_PERSON_URI = "uri";
    private static final String ATTR_PERSON_KEY = "key";
    private static final String ATTR_PERSON_IS_BOT = "is-bot";
    private static final String ATTR_PERSON_IS_IMPORTANT = "is-important";

    private static final String NAME_CATEGORIES = "categories";

    private static final String TAG_STRING_ARRAY_XMLUTILS = "string-array";
    private static final String ATTR_NAME_XMLUTILS = "name";

    private static File getUserFile(int userId) {
        return new File(injectUserDataPath(userId), FILENAME_USER_PACKAGES);
    }

    private static File injectUserDataPath(int userId) {
        // Environment.getDataSystemCeDirectory(userId)
        return new File("/data/system_ce/" + userId, DIRECTORY_PER_USER);
    }

    public static void closeQuietly(@Nullable AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (RuntimeException rethrown) {
                throw rethrown;
            } catch (Exception ignored) {
            }
        }
    }

    @Nullable
    public static List<ShortcutInfo> loadUserLocked(int userId) {
        final File path = getUserFile(userId);
        final AtomicFile file = new AtomicFile(path);

        final FileInputStream in;
        try {
            in = file.openRead();
        } catch (FileNotFoundException e) {
            Log.d(TAG, "Not found " + path);
            return null;
        }
        try {
            return loadUserInternal(userId, in, /* forBackup= */ false);
        } catch (IOException | XmlPullParserException e) {
            Log.e(TAG, "Failed to read file " + file.getBaseFile(), e);
            return null;
        } finally {
            closeQuietly(in);
        }
    }

    private static XmlPullParser newPullParserCompat(InputStream is) throws XmlPullParserException, IOException  {
        XmlPullParser parser;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            parser = XmlHidden.resolvePullParser(is);
        } else {
            parser = Xml.newPullParser();
            parser.setInput(is, StandardCharsets.UTF_8.name());
        }
        return parser;
    }

    private static List<ShortcutInfo> loadUserInternal(int userId, InputStream is,
                                          boolean fromBackup) throws XmlPullParserException, IOException {
        XmlPullParser parser = newPullParserCompat(is);
        List<ShortcutInfo> ret = null;

        int type;
        while ((type = parser.next()) != XmlPullParser.END_DOCUMENT) {
            if (type != XmlPullParser.START_TAG) {
                continue;
            }
            final int depth = parser.getDepth();

            final String tag = parser.getName();
            if ((depth == 1) && TAG_USER_ROOT.equals(tag)) {
                ret = loadUserFromXml(parser, userId, fromBackup);
            }
            // throwForInvalidTag(depth, tag);
        }
        return ret;
    }

    private static List<ShortcutInfo> loadUserFromXml(XmlPullParser parser, int userId,
                                           boolean fromBackup) throws IOException, XmlPullParserException {
        List<ShortcutInfo> ret = new ArrayList<>();
        boolean readShortcutItems = false;
        final int outerDepth = parser.getDepth();
        int type;
        while ((type = parser.next()) != XmlPullParser.END_DOCUMENT
                && (type != XmlPullParser.END_TAG || parser.getDepth() > outerDepth)) {
            if (type != XmlPullParser.START_TAG) {
                continue;
            }
            final int depth = parser.getDepth();
            final String tag = parser.getName();

            if (depth == outerDepth + 1) {
                if (Objects.equals(tag, ShortcutParser.TAG_PACKAGE_ROOT)) {
                    ret.addAll(loadPackageFromXml(parser, fromBackup, userId));
                    readShortcutItems = true;
                    continue;
                }
            }
            warnForInvalidTag(depth, tag);
        }

        if (!readShortcutItems) {
            // If the shortcuts info was read from the main Xml, skip reading from individual files.
            // Data will get stored in the new format during the next call to saveToXml().
            final File root = injectUserDataPath(userId);

            forAllFilesIn(new File(root, DIRECTORY_PACKAGES), (File f) -> {
                final List<ShortcutInfo> sp = loadPackageFromFile(f, fromBackup, userId);
                if (sp != null) {
                    ret.addAll(sp);
                }
            });
        }

        return ret;
    }

    private static void forAllFilesIn(File path, Consumer<File> callback) {
        if (!path.exists()) {
            return;
        }
        File[] list = path.listFiles();
        assert list != null;
        for (File f : list) {
            callback.accept(f);
        }
    }

    private static List<ShortcutInfo> loadPackageFromFile(File path, boolean fromBackup, int userId) {

        final AtomicFile file = new AtomicFile(path);
        final FileInputStream in;
        try {
            in = file.openRead();
        } catch (FileNotFoundException e) {
            return null;
        }

        try {
            final BufferedInputStream bis = new BufferedInputStream(in);

            List<ShortcutInfo> ret = null;
            XmlPullParser parser = newPullParserCompat(bis);

            int type;
            while ((type = parser.next()) != XmlPullParser.END_DOCUMENT) {
                if (type != XmlPullParser.START_TAG) {
                    continue;
                }
                final int depth = parser.getDepth();

                final String tag = parser.getName();
                if ((depth == 1) && TAG_PACKAGE_ROOT.equals(tag)) {
                    ret = loadPackageFromXml(parser, fromBackup, userId);
                }
            }
            return ret;
        } catch (XmlPullParserException | IOException e) {
            Log.e(TAG, "loadPackageFromFile", e);
            return null;
        } finally {
            closeQuietly(in);
        }
    }

    private static List<ShortcutInfo> loadPackageFromXml(XmlPullParser parser, boolean fromBackup, int userId)
            throws IOException, XmlPullParserException {

        final String packageName = parseStringAttribute(parser,
                ATTR_NAME);

        List<ShortcutInfo> ret = new ArrayList<>();

        final int outerDepth = parser.getDepth();
        int type;
        while ((type = parser.next()) != XmlPullParser.END_DOCUMENT
                && (type != XmlPullParser.END_TAG || parser.getDepth() > outerDepth)) {
            if (type != XmlPullParser.START_TAG) {
                continue;
            }
            final int depth = parser.getDepth();
            final String tag = parser.getName();
            if (depth == outerDepth + 1) {
                if (TAG_SHORTCUT.equals(tag)) {
                    final ShortcutInfo si = parseShortcut(parser, packageName,
                            userId, fromBackup);
                    ret.add(si);
                    continue;
                }
            }
            warnForInvalidTag(depth, tag);
        }
        return ret;
    }

    private static ShortcutInfo parseShortcut(XmlPullParser parser, String packageName,
                                              int userId, boolean fromBackup)
            throws IOException, XmlPullParserException {
        String id;
        ComponentName activityComponent;
        // Icon icon;
        String title;
        int titleResId;
        String titleResName;
        String text;
        int textResId;
        String textResName;
        String disabledMessage;
        int disabledMessageResId;
        String disabledMessageResName;
        int disabledReason;
        Intent intentLegacy;
        PersistableBundle intentPersistableExtrasLegacy = null;
        ArrayList<Intent> intents = new ArrayList<>();
        int rank;
        PersistableBundle extras = null;
        long lastChangedTimestamp;
        int flags;
        int iconResId;
        String iconResName;
        String bitmapPath;
        String iconUri;
        final String locusIdString;
        ArraySet<String> categories = null;
        ArrayList<Person> persons = new ArrayList<>();

        id = parseStringAttribute(parser, ATTR_ID);
        activityComponent = parseComponentNameAttribute(parser,
                ATTR_ACTIVITY);
        title = parseStringAttribute(parser, ATTR_TITLE);
        titleResId = parseIntAttribute(parser, ATTR_TITLE_RES_ID);
        titleResName = parseStringAttribute(parser, ATTR_TITLE_RES_NAME);
        text = parseStringAttribute(parser, ATTR_TEXT);
        textResId = parseIntAttribute(parser, ATTR_TEXT_RES_ID);
        textResName = parseStringAttribute(parser, ATTR_TEXT_RES_NAME);
        disabledMessage = parseStringAttribute(parser, ATTR_DISABLED_MESSAGE);
        disabledMessageResId = parseIntAttribute(parser,
                ATTR_DISABLED_MESSAGE_RES_ID);
        disabledMessageResName = parseStringAttribute(parser,
                ATTR_DISABLED_MESSAGE_RES_NAME);
        disabledReason = parseIntAttribute(parser, ATTR_DISABLED_REASON);
        intentLegacy = parseIntentAttributeNoDefault(parser, ATTR_INTENT_LEGACY);
        rank = (int) parseLongAttribute(parser, ATTR_RANK);
        lastChangedTimestamp = parseLongAttribute(parser, ATTR_TIMESTAMP);
        flags = (int) parseLongAttribute(parser, ATTR_FLAGS);
        iconResId = (int) parseLongAttribute(parser, ATTR_ICON_RES_ID);
        iconResName = parseStringAttribute(parser, ATTR_ICON_RES_NAME);
        bitmapPath = parseStringAttribute(parser, ATTR_BITMAP_PATH);
        iconUri = parseStringAttribute(parser, ATTR_ICON_URI);
        locusIdString = parseStringAttribute(parser, ATTR_LOCUS_ID);

        final int outerDepth = parser.getDepth();
        int type;
        while ((type = parser.next()) != XmlPullParser.END_DOCUMENT
                && (type != XmlPullParser.END_TAG || parser.getDepth() > outerDepth)) {
            if (type != XmlPullParser.START_TAG) {
                continue;
            }
            final int depth = parser.getDepth();
            final String tag = parser.getName();
            switch (tag) {
                case TAG_INTENT_EXTRAS_LEGACY:
                    intentPersistableExtrasLegacy = PersistableBundleHidden.restoreFromXml(parser);
                    continue;
                case TAG_INTENT:
                    intents.add(parseIntent(parser));
                    continue;
                case TAG_EXTRAS:
                    extras = PersistableBundleHidden.restoreFromXml(parser);
                    continue;
                case TAG_CATEGORIES:
                    // This just contains string-array.
                    continue;
                case TAG_PERSON:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        persons.add(parsePerson(parser));
                    }
                    continue;
                case TAG_STRING_ARRAY_XMLUTILS:
                    if (NAME_CATEGORIES.equals(parseStringAttribute(parser,
                            ATTR_NAME_XMLUTILS))) {
                        final String[] ar = XmlUtils.readThisStringArrayXml(
                                parser, TAG_STRING_ARRAY_XMLUTILS, null);
                        categories = new ArraySet<>(ar.length);
                        categories.addAll(Arrays.asList(ar));
                    }
                    continue;
            }
            throw throwForInvalidTag(depth, tag);
        }

        if (intentLegacy != null) {
            // For the legacy file format which supported only one intent per shortcut.
            ShortcutInfoHidden.setIntentExtras(intentLegacy, intentPersistableExtrasLegacy);
            intents.clear();
            intents.add(intentLegacy);
        }

        LocusId locusId = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            locusId = locusIdString == null ? null : new LocusId(locusIdString);
        }

        return ShortcutInfoHelperKt.newShortcutInfoCompat(
                userId, id, packageName, activityComponent, /* icon= */ null,
                title, titleResId, titleResName, text, textResId, textResName,
                disabledMessage, disabledMessageResId, disabledMessageResName,
                categories,
                intents.toArray(new Intent[intents.size()]),
                rank, extras, lastChangedTimestamp, flags,
                iconResId, iconResName, bitmapPath, iconUri,
                disabledReason, persons.toArray(new Person[persons.size()]), locusId);
    }

    private static Intent parseIntent(XmlPullParser parser)
            throws IOException, XmlPullParserException {

        Intent intent = parseIntentAttribute(parser,
                ATTR_INTENT_NO_EXTRA);

        final int outerDepth = parser.getDepth();
        int type;
        while ((type = parser.next()) != XmlPullParser.END_DOCUMENT
                && (type != XmlPullParser.END_TAG || parser.getDepth() > outerDepth)) {
            if (type != XmlPullParser.START_TAG) {
                continue;
            }
            final int depth = parser.getDepth();
            final String tag = parser.getName();
            if (TAG_EXTRAS.equals(tag)) {
                ShortcutInfoHidden.setIntentExtras(intent,
                        PersistableBundleHidden.restoreFromXml(parser));
                continue;
            }
            throw throwForInvalidTag(depth, tag);
        }
        return intent;
    }

    static IOException throwForInvalidTag(int depth, String tag) throws IOException {
        throw new IOException(String.format("Invalid tag '%s' found at depth %d", tag, depth));
    }

    static void warnForInvalidTag(int depth, String tag) throws IOException {
        Log.w(TAG, String.format("Invalid tag '%s' found at depth %d", tag, depth));
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    private static Person parsePerson(XmlPullParser parser)
            throws IOException, XmlPullParserException {
        CharSequence name = parseStringAttribute(parser, ATTR_PERSON_NAME);
        String uri = parseStringAttribute(parser, ATTR_PERSON_URI);
        String key = parseStringAttribute(parser, ATTR_PERSON_KEY);
        boolean isBot = parseBooleanAttribute(parser, ATTR_PERSON_IS_BOT);
        boolean isImportant = parseBooleanAttribute(parser,
                ATTR_PERSON_IS_IMPORTANT);

        Person.Builder builder = new Person.Builder();
        builder.setName(name).setUri(uri).setKey(key).setBot(isBot).setImportant(isImportant);
        return builder.build();
    }

    static String parseStringAttribute(XmlPullParser parser, String attribute) {
        return parser.getAttributeValue(null, attribute);
    }

    static boolean parseBooleanAttribute(XmlPullParser parser, String attribute) {
        return parseLongAttribute(parser, attribute) == 1;
    }

    static boolean parseBooleanAttribute(XmlPullParser parser, String attribute, boolean def) {
        return parseLongAttribute(parser, attribute, (def ? 1 : 0)) == 1;
    }

    static int parseIntAttribute(XmlPullParser parser, String attribute) {
        return (int) parseLongAttribute(parser, attribute);
    }

    static int parseIntAttribute(XmlPullParser parser, String attribute, int def) {
        return (int) parseLongAttribute(parser, attribute, def);
    }

    static long parseLongAttribute(XmlPullParser parser, String attribute) {
        return parseLongAttribute(parser, attribute, 0);
    }

    static long parseLongAttribute(XmlPullParser parser, String attribute, long def) {
        final String value = parseStringAttribute(parser, attribute);
        if (TextUtils.isEmpty(value)) {
            return def;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    @Nullable
    static ComponentName parseComponentNameAttribute(XmlPullParser parser, String attribute) {
        final String value = parseStringAttribute(parser, attribute);
        if (TextUtils.isEmpty(value)) {
            return null;
        }
        return ComponentName.unflattenFromString(value);
    }

    @Nullable
    static Intent parseIntentAttributeNoDefault(XmlPullParser parser, String attribute) {
        final String value = parseStringAttribute(parser, attribute);
        Intent parsed = null;
        if (!TextUtils.isEmpty(value)) {
            try {
                parsed = Intent.parseUri(value, /* flags =*/ 0);
            } catch (URISyntaxException e) {
                Log.e(TAG, "Error parsing intent", e);
            }
        }
        return parsed;
    }

    @Nullable
    static Intent parseIntentAttribute(XmlPullParser parser, String attribute) {
        Intent parsed = parseIntentAttributeNoDefault(parser, attribute);
        if (parsed == null) {
            // Default intent.
            parsed = new Intent(Intent.ACTION_VIEW);
        }
        return parsed;
    }
}
