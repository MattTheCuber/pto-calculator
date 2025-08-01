package utilities;

import com.calendarfx.view.CalendarView;

public class CustomCalendarView extends CalendarView {
    private final CustomSearchResultView searchResultView;

    public CustomCalendarView() {
        this(Page.values());
    }

    public CustomCalendarView(Page... availablePages) {
        super(availablePages);
        this.searchResultView = new CustomSearchResultView();
    }

    // public final SearchResultView getSearchResultView() {
    // return searchResultView;
    // }
}
