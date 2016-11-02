package ngoc.neamar.kiss.loader;

import android.content.Context;

import java.util.ArrayList;

import ngoc.neamar.kiss.dataprovider.PhoneProvider;
import ngoc.neamar.kiss.pojo.PhonePojo;

public class LoadPhonePojos extends LoadPojos<PhonePojo> {

    public LoadPhonePojos(Context context) {
        super(context, PhoneProvider.PHONE_SCHEME);
    }

    @Override
    protected ArrayList<PhonePojo> doInBackground(Void... params) {
        return null;
    }
}
