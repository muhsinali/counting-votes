import play.filters.gzip.GzipFilter;
import play.http.HttpFilters;
import play.mvc.EssentialFilter;

import javax.inject.Inject;

/**
 * Use Gzip filter to ensure that all server responses are gzipped
 */
public class Filters implements HttpFilters {

  private EssentialFilter[] filters;

  @Inject
  public Filters(GzipFilter gzipFilter) {
    filters = new EssentialFilter[] { gzipFilter.asJava() };
  }

  public EssentialFilter[] filters() {
    return filters;
  }
}