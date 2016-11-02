package ngoc.neamar.kiss.loader;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import ngoc.neamar.kiss.normalizer.PhoneNormalizer;
import ngoc.neamar.kiss.normalizer.StringNormalizer;
import ngoc.neamar.kiss.pojo.ContactsPojo;

public class LoadContactsPojos extends LoadPojos<ContactsPojo> {
    private static Pattern mobileNumberPattern;
    private static Pattern ignorePattern = Pattern.compile("[-.():/ ]");

    public LoadContactsPojos(Context context) {
        super(context, "contact://");
        PhoneNormalizer.initialize(context);
    }

    private void ensureMobileNumberPattern() {
        if (mobileNumberPattern != null) return;

        InputStream inputStream = context.getResources().openRawResource(ngoc.neamar.kiss.R.raw.phone_number_textable);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        StringBuilder mobileDetectionRegex = new StringBuilder();

        try {
            String line;
            while ((line = reader.readLine()) != null)
                mobileDetectionRegex.append(line);

            mobileNumberPattern = Pattern.compile(mobileDetectionRegex.toString());
        } catch (IOException ioex) {
            mobileNumberPattern = null;
        }
    }

    @Override
    protected ArrayList<ContactsPojo> doInBackground(Void... params) {
        ensureMobileNumberPattern();

        long start = System.nanoTime();
        String defaultCountryIso = Locale.getDefault().getCountry();

        // Run query
        Cursor cur = context.getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.Contacts.LOOKUP_KEY,
                        ContactsContract.CommonDataKinds.Phone.TIMES_CONTACTED,
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER,
                        ContactsContract.CommonDataKinds.Phone.STARRED,
                        ContactsContract.CommonDataKinds.Phone.IS_PRIMARY,
                        ContactsContract.Contacts.PHOTO_ID,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID}, null, null, ContactsContract.CommonDataKinds.Phone.TIMES_CONTACTED + " DESC");

        // Prevent duplicates by keeping in memory encountered phones.
        // The string key is "phone" + "|" + "name" (so if two contacts
        // with distinct name share same number, they both get displayed)
        Map<String, ArrayList<ContactsPojo>> mapContacts = new HashMap<>();

        if (cur != null) {
            if (cur.getCount() > 0) {
                while (cur.moveToNext()) {
                    ContactsPojo contact = new ContactsPojo();

                    contact.lookupKey = cur.getString(cur
                            .getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));

                    contact.timesContacted = Integer.parseInt(cur.getString(cur
                            .getColumnIndex(ContactsContract.CommonDataKinds.Phone.TIMES_CONTACTED)));
                    contact.setName(cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)));

                    contact.phone = PhoneNormalizer.normalizePhone(cur.getString(cur
                            .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                    if (contact.phone == null) {
                        contact.phone = "";
                    }
                    contact.phoneSimplified = ignorePattern.matcher(contact.phone).replaceAll("");
                    contact.homeNumber = mobileNumberPattern == null ||
                            !mobileNumberPattern.matcher(contact.phoneSimplified).lookingAt();
                    contact.starred = cur.getInt(cur
                            .getColumnIndex(ContactsContract.CommonDataKinds.Phone.STARRED)) != 0;
                    contact.primary = cur.getInt(cur
                            .getColumnIndex(ContactsContract.CommonDataKinds.Phone.IS_PRIMARY)) != 0;
                    String photoId = cur.getString(cur
                            .getColumnIndex(ContactsContract.Contacts.PHOTO_ID));
                    if (photoId != null) {
                        contact.icon = ContentUris.withAppendedId(ContactsContract.Data.CONTENT_URI,
                                Long.parseLong(photoId));
                    }

                    contact.id = pojoScheme + contact.lookupKey + contact.phone;

                    if (contact.name != null) {
                        contact.nameNormalized = StringNormalizer.normalize(contact.name);

                        if (mapContacts.containsKey(contact.lookupKey))
                            mapContacts.get(contact.lookupKey).add(contact);
                        else {
                            ArrayList<ContactsPojo> phones = new ArrayList<>();
                            phones.add(contact);
                            mapContacts.put(contact.lookupKey, phones);
                        }
                    }
                }
            }
            cur.close();
        }

        // Retrieve contacts' nicknames
        Cursor nickCursor = context.getContentResolver().query(
                ContactsContract.Data.CONTENT_URI,
                new String[]{
                        ContactsContract.CommonDataKinds.Nickname.NAME,
                        ContactsContract.Data.LOOKUP_KEY},
                ContactsContract.Data.MIMETYPE + "= ?",
                new String[]{ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE},
                null);

        if (nickCursor != null) {
            if (nickCursor.getCount() > 0) {
                while (nickCursor.moveToNext()) {
                    String lookupKey = nickCursor.getString(
                            nickCursor.getColumnIndex(ContactsContract.Data.LOOKUP_KEY));
                    String nick = nickCursor.getString(
                            nickCursor.getColumnIndex(ContactsContract.CommonDataKinds.Nickname.NAME));

                    if (nick != null && lookupKey != null && mapContacts.containsKey(lookupKey)) {
                        for (ContactsPojo contact : mapContacts.get(lookupKey)) {
                            contact.setNickname(nick);
                        }
                    }
                }
            }
            nickCursor.close();
        }

        ArrayList<ContactsPojo> contacts = new ArrayList<>();

        Pattern phoneFormatter = Pattern.compile("[ \\.\\(\\)]");
        for (List<ContactsPojo> phones : mapContacts.values()) {
            // Find primary phone and add this one.
            Boolean hasPrimary = false;
            for (ContactsPojo contact : phones) {
                if (contact.primary) {
                    contacts.add(contact);
                    hasPrimary = true;
                    break;
                }
            }

            // If not available, add all (excluding duplicates).
            if (!hasPrimary) {
                Map<String, Boolean> added = new HashMap<>();
                for (ContactsPojo contact : phones) {
                    String uniqueKey = phoneFormatter.matcher(contact.phone).replaceAll("");
                    uniqueKey = uniqueKey.replaceAll("^\\+33", "0");
                    uniqueKey = uniqueKey.replaceAll("^\\+1", "0");
                    if (!added.containsKey(uniqueKey)) {
                        added.put(uniqueKey, true);
                        contacts.add(contact);
                    }
                }
            }
        }
        long end = System.nanoTime();
        Log.i("time", Long.toString((end - start) / 1000000) + " milliseconds to list contacts");
        return contacts;
    }
}
