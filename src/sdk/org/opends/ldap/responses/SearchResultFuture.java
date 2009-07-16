package org.opends.ldap.responses;



import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;



/**
 * Created by IntelliJ IDEA. User: boli Date: Jul 7, 2009 Time: 3:36:55
 * PM To change this template use File | Settings | File Templates.
 */
public interface SearchResultFuture extends ResultFuture
{
  /**
   * Retrieves the current number of result entries received from the
   * server.
   *
   * @return
   */
  int getNumSearchResultEntries();



  /**
   * Retrieves the current number of result references received from the
   * server.
   *
   * @return
   */
  int getNumSearchResultReferences();



  SearchResult get() throws InterruptedException, ErrorResultException;



  SearchResult get(long timeout, TimeUnit unit)
      throws InterruptedException, TimeoutException,
      ErrorResultException;

}
