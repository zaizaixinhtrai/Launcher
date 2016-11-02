package ngoc.neamar.kiss.searcher;

import java.util.List;

import ngoc.neamar.kiss.KissApplication;
import ngoc.neamar.kiss.MainActivity;
import ngoc.neamar.kiss.pojo.Pojo;

/**
 * Returns the list of all applications on the system
 */
public class ApplicationsSearcher extends Searcher {
    public ApplicationsSearcher(MainActivity activity) {
        super(activity);
    }

    @Override
    protected List<Pojo> doInBackground(Void... voids) {
        // Ask for records
        return KissApplication.getDataHandler(activity).getApplications();
    }
}
