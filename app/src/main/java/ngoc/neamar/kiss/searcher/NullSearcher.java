package ngoc.neamar.kiss.searcher;

import java.util.ArrayList;
import java.util.List;

import ngoc.neamar.kiss.MainActivity;
import ngoc.neamar.kiss.pojo.Pojo;

/**
 * Retrieve pojos from history
 */
public class NullSearcher extends Searcher {

    public NullSearcher(MainActivity activity) {
        super(activity);
    }

    @Override
    protected List<Pojo> doInBackground(Void... voids) {
        return new ArrayList<>();
    }
}
