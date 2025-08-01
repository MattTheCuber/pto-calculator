package utilities;

import static com.calendarfx.util.LoggingDomain.SEARCH;
import static java.util.logging.Level.FINE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.view.SearchResultView;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class CustomSearchResultView extends SearchResultView {
    private final SearchService searchService;

    public CustomSearchResultView() {
        super();
        searchService = new SearchService();
        searchService.setOnSucceeded(evt -> updateSearchResults());

        searchTextProperty().addListener(it -> {
            if (SEARCH.isLoggable(FINE)) {
                SEARCH.fine("restarting search service");
            }

            searchService.restart();
        });
    }

    private void updateSearchResults() {
        List<Entry<?>> searchResult = searchService.getValue();
        getSearchResults().setAll(searchResult);
    }

    private class SearchService extends Service<List<Entry<?>>> {

        public SearchService() {
        }

        @Override
        protected Task<List<Entry<?>>> createTask() {
            return new SearchTask();
        }

        class SearchTask extends Task<List<Entry<?>>> {

            @Override
            protected List<Entry<?>> call() throws Exception {
                if (!isCancelled()) {

                    String searchText = getSearchText();

                    if (SEARCH.isLoggable(FINE)) {
                        SEARCH.fine("search text: " + searchText);
                    }

                    System.out.println("test");
                    // if (searchText == null || searchText.trim().isEmpty()) {
                    // return Collections.emptyList();
                    // }

                    /*
                     * Let's sleep a little bit, so we don't run a query after
                     * every key press event.
                     */
                    Thread.sleep(200);

                    if (SEARCH.isLoggable(FINE)) {
                        SEARCH.fine("performing search after delay");
                    }

                    if (!isCancelled()) {

                        List<Entry<?>> totalResult = new ArrayList<>();

                        for (CalendarSource source : getCalendarSources()) {

                            if (SEARCH.isLoggable(FINE)) {
                                SEARCH.fine("searching in source "
                                        + source.getName());
                            }

                            for (Calendar calendar : source.getCalendars()) {

                                if (SEARCH.isLoggable(FINE)) {
                                    SEARCH.fine("searching in calendar "
                                            + calendar.getName());
                                }

                                if (!isCancelled()) {
                                    try {
                                        List<? extends Entry<?>> result = calendar
                                                .findEntries(searchText);
                                        if (result != null) {
                                            for (Entry<?> entry : result) {
                                                totalResult.add(entry);
                                            }
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }

                        if (SEARCH.isLoggable(FINE)) {
                            if (isCancelled()) {
                                SEARCH.fine("search was canceled");
                            }
                        }

                        if (SEARCH.isLoggable(FINE)) {
                            SEARCH.fine(
                                    "found " + totalResult.size() + " entries");
                        }

                        return totalResult;
                    }
                }

                if (SEARCH.isLoggable(FINE)) {
                    SEARCH.fine("returning empty search result");
                }

                return Collections.emptyList();
            }
        }
    }
}
