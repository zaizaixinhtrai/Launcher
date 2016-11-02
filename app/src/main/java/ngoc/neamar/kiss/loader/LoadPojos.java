package ngoc.neamar.kiss.loader;

import android.content.Context;
import android.os.AsyncTask;

import java.util.ArrayList;

import ngoc.neamar.kiss.dataprovider.Provider;
import ngoc.neamar.kiss.pojo.Pojo;

public abstract class LoadPojos<T extends Pojo> extends AsyncTask<Void, Void, ArrayList<T>> {

    final Context context;
    String pojoScheme = "(none)://";
    private Provider<T> provider;

    LoadPojos(Context context, String pojoScheme) {
        super();
        this.context = context;
        this.pojoScheme = pojoScheme;
    }

    public void setProvider(Provider<T> provider) {
        this.provider = provider;
    }

    public String getPojoScheme() {
        return pojoScheme;
    }

    @Override
    protected void onPostExecute(ArrayList<T> result) {
        super.onPostExecute(result);
        provider.loadOver(result);
    }

}
