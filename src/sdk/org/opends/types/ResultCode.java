package org.opends.types;



import static org.opends.messages.CoreMessages.INFO_RESULT_ADMIN_LIMIT_EXCEEDED;
import static org.opends.messages.CoreMessages.INFO_RESULT_AFFECTS_MULTIPLE_DSAS;
import static org.opends.messages.CoreMessages.INFO_RESULT_ALIAS_DEREFERENCING_PROBLEM;
import static org.opends.messages.CoreMessages.INFO_RESULT_ALIAS_PROBLEM;
import static org.opends.messages.CoreMessages.INFO_RESULT_ASSERTION_FAILED;
import static org.opends.messages.CoreMessages.INFO_RESULT_ATTRIBUTE_OR_VALUE_EXISTS;
import static org.opends.messages.CoreMessages.INFO_RESULT_AUTHORIZATION_DENIED;
import static org.opends.messages.CoreMessages.INFO_RESULT_AUTH_METHOD_NOT_SUPPORTED;
import static org.opends.messages.CoreMessages.INFO_RESULT_BUSY;
import static org.opends.messages.CoreMessages.INFO_RESULT_CANCELED;
import static org.opends.messages.CoreMessages.INFO_RESULT_CANNOT_CANCEL;
import static org.opends.messages.CoreMessages.INFO_RESULT_CLIENT_SIDE_AUTH_UNKNOWN;
import static org.opends.messages.CoreMessages.INFO_RESULT_CLIENT_SIDE_CLIENT_LOOP;
import static org.opends.messages.CoreMessages.INFO_RESULT_CLIENT_SIDE_CONNECT_ERROR;
import static org.opends.messages.CoreMessages.INFO_RESULT_CLIENT_SIDE_CONTROL_NOT_FOUND;
import static org.opends.messages.CoreMessages.INFO_RESULT_CLIENT_SIDE_DECODING_ERROR;
import static org.opends.messages.CoreMessages.INFO_RESULT_CLIENT_SIDE_ENCODING_ERROR;
import static org.opends.messages.CoreMessages.INFO_RESULT_CLIENT_SIDE_FILTER_ERROR;
import static org.opends.messages.CoreMessages.INFO_RESULT_CLIENT_SIDE_LOCAL_ERROR;
import static org.opends.messages.CoreMessages.INFO_RESULT_CLIENT_SIDE_MORE_RESULTS_TO_RETURN;
import static org.opends.messages.CoreMessages.INFO_RESULT_CLIENT_SIDE_NOT_SUPPORTED;
import static org.opends.messages.CoreMessages.INFO_RESULT_CLIENT_SIDE_NO_MEMORY;
import static org.opends.messages.CoreMessages.INFO_RESULT_CLIENT_SIDE_NO_RESULTS_RETURNED;
import static org.opends.messages.CoreMessages.INFO_RESULT_CLIENT_SIDE_PARAM_ERROR;
import static org.opends.messages.CoreMessages.INFO_RESULT_CLIENT_SIDE_REFERRAL_LIMIT_EXCEEDED;
import static org.opends.messages.CoreMessages.INFO_RESULT_CLIENT_SIDE_SERVER_DOWN;
import static org.opends.messages.CoreMessages.INFO_RESULT_CLIENT_SIDE_TIMEOUT;
import static org.opends.messages.CoreMessages.INFO_RESULT_CLIENT_SIDE_USER_CANCELLED;
import static org.opends.messages.CoreMessages.INFO_RESULT_COMPARE_FALSE;
import static org.opends.messages.CoreMessages.INFO_RESULT_COMPARE_TRUE;
import static org.opends.messages.CoreMessages.INFO_RESULT_CONFIDENTIALITY_REQUIRED;
import static org.opends.messages.CoreMessages.INFO_RESULT_CONSTRAINT_VIOLATION;
import static org.opends.messages.CoreMessages.INFO_RESULT_ENTRY_ALREADY_EXISTS;
import static org.opends.messages.CoreMessages.INFO_RESULT_INAPPROPRIATE_AUTHENTICATION;
import static org.opends.messages.CoreMessages.INFO_RESULT_INAPPROPRIATE_MATCHING;
import static org.opends.messages.CoreMessages.INFO_RESULT_INSUFFICIENT_ACCESS_RIGHTS;
import static org.opends.messages.CoreMessages.INFO_RESULT_INVALID_ATTRIBUTE_SYNTAX;
import static org.opends.messages.CoreMessages.INFO_RESULT_INVALID_CREDENTIALS;
import static org.opends.messages.CoreMessages.INFO_RESULT_INVALID_DN_SYNTAX;
import static org.opends.messages.CoreMessages.INFO_RESULT_LOOP_DETECT;
import static org.opends.messages.CoreMessages.INFO_RESULT_NAMING_VIOLATION;
import static org.opends.messages.CoreMessages.INFO_RESULT_NOT_ALLOWED_ON_NONLEAF;
import static org.opends.messages.CoreMessages.INFO_RESULT_NOT_ALLOWED_ON_RDN;
import static org.opends.messages.CoreMessages.INFO_RESULT_NO_OPERATION;
import static org.opends.messages.CoreMessages.INFO_RESULT_NO_SUCH_ATTRIBUTE;
import static org.opends.messages.CoreMessages.INFO_RESULT_NO_SUCH_OBJECT;
import static org.opends.messages.CoreMessages.INFO_RESULT_NO_SUCH_OPERATION;
import static org.opends.messages.CoreMessages.INFO_RESULT_OBJECTCLASS_MODS_PROHIBITED;
import static org.opends.messages.CoreMessages.INFO_RESULT_OBJECTCLASS_VIOLATION;
import static org.opends.messages.CoreMessages.INFO_RESULT_OFFSET_RANGE_ERROR;
import static org.opends.messages.CoreMessages.INFO_RESULT_OPERATIONS_ERROR;
import static org.opends.messages.CoreMessages.INFO_RESULT_OTHER;
import static org.opends.messages.CoreMessages.INFO_RESULT_PROTOCOL_ERROR;
import static org.opends.messages.CoreMessages.INFO_RESULT_REFERRAL;
import static org.opends.messages.CoreMessages.INFO_RESULT_SASL_BIND_IN_PROGRESS;
import static org.opends.messages.CoreMessages.INFO_RESULT_SIZE_LIMIT_EXCEEDED;
import static org.opends.messages.CoreMessages.INFO_RESULT_SORT_CONTROL_MISSING;
import static org.opends.messages.CoreMessages.INFO_RESULT_STRONG_AUTH_REQUIRED;
import static org.opends.messages.CoreMessages.INFO_RESULT_SUCCESS;
import static org.opends.messages.CoreMessages.INFO_RESULT_TIME_LIMIT_EXCEEDED;
import static org.opends.messages.CoreMessages.INFO_RESULT_TOO_LATE;
import static org.opends.messages.CoreMessages.INFO_RESULT_UNAVAILABLE;
import static org.opends.messages.CoreMessages.INFO_RESULT_UNAVAILABLE_CRITICAL_EXTENSION;
import static org.opends.messages.CoreMessages.INFO_RESULT_UNDEFINED_ATTRIBUTE_TYPE;
import static org.opends.messages.CoreMessages.INFO_RESULT_UNWILLING_TO_PERFORM;
import static org.opends.messages.CoreMessages.INFO_RESULT_VIRTUAL_LIST_VIEW_ERROR;
import static org.opends.messages.CoreMessages.INFO_UNDEFINED_TYPE;

import java.util.Arrays;
import java.util.List;

import org.opends.messages.Message;
import org.opends.server.protocols.ldap.LDAPResultCode;



/**
 * Defines the set of possible result codes that may be used for
 * providing clients with information about result of processing an
 * operation.
 */
public class ResultCode
{
  private static final ResultCode[] ELEMENTS = new ResultCode[16655];

  /**
   * The result code that indicates that the operation completed
   * successfully.
   */
  public static final ResultCode SUCCESS =
      register(LDAPResultCode.SUCCESS, INFO_RESULT_SUCCESS.get(), false);

  /**
   * The result code that indicates that an internal error prevented the
   * operation from being processed properly.
   */
  public static final ResultCode OPERATIONS_ERROR =
      register(LDAPResultCode.OPERATIONS_ERROR,
          INFO_RESULT_OPERATIONS_ERROR.get());

  /**
   * The result code that indicates that the client sent a malformed or
   * illegal request to the server.
   */
  public static final ResultCode PROTOCOL_ERROR =
      register(LDAPResultCode.PROTOCOL_ERROR,
          INFO_RESULT_PROTOCOL_ERROR.get());

  /**
   * The result code that indicates that a time limit was exceeded while
   * attempting to process the request.
   */
  public static final ResultCode TIME_LIMIT_EXCEEDED =
      register(LDAPResultCode.TIME_LIMIT_EXCEEDED,
          INFO_RESULT_TIME_LIMIT_EXCEEDED.get());

  /**
   * The result code that indicates that a size limit was exceeded while
   * attempting to process the request.
   */
  public static final ResultCode SIZE_LIMIT_EXCEEDED =
      register(LDAPResultCode.SIZE_LIMIT_EXCEEDED,
          INFO_RESULT_SIZE_LIMIT_EXCEEDED.get());

  /**
   * The result code that indicates that the attribute value assertion
   * included in a compare request did not match the targeted entry.
   */
  public static final ResultCode COMPARE_FALSE =
      register(LDAPResultCode.COMPARE_FALSE, INFO_RESULT_COMPARE_FALSE
          .get(), false);

  /**
   * The result code that indicates that the attribute value assertion
   * included in a compare request did match the targeted entry.
   */
  public static final ResultCode COMPARE_TRUE =
      register(LDAPResultCode.COMPARE_TRUE, INFO_RESULT_COMPARE_TRUE
          .get(), false);

  /**
   * The result code that indicates that the requested authentication
   * attempt failed because it referenced an invalid SASL mechanism.
   */
  public static final ResultCode AUTH_METHOD_NOT_SUPPORTED =
      register(LDAPResultCode.AUTH_METHOD_NOT_SUPPORTED,
          INFO_RESULT_AUTH_METHOD_NOT_SUPPORTED.get());

  /**
   * The result code that indicates that the requested operation could
   * not be processed because it requires that the client has completed
   * a strong form of authentication.
   */
  public static final ResultCode STRONG_AUTH_REQUIRED =
      register(LDAPResultCode.STRONG_AUTH_REQUIRED,
          INFO_RESULT_STRONG_AUTH_REQUIRED.get());

  /**
   * The result code that indicates that a referral was encountered.
   */
  public static final ResultCode REFERRAL =
      register(LDAPResultCode.REFERRAL, INFO_RESULT_REFERRAL.get(),
          false);

  /**
   * The result code that indicates that processing on the requested
   * operation could not continue because an administrative limit was
   * exceeded.
   */
  public static final ResultCode ADMIN_LIMIT_EXCEEDED =
      register(LDAPResultCode.ADMIN_LIMIT_EXCEEDED,
          INFO_RESULT_ADMIN_LIMIT_EXCEEDED.get());

  /**
   * The result code that indicates that the requested operation failed
   * because it included a critical extension that is unsupported or
   * inappropriate for that request.
   */
  public static final ResultCode UNAVAILABLE_CRITICAL_EXTENSION =
      register(LDAPResultCode.UNAVAILABLE_CRITICAL_EXTENSION,
          INFO_RESULT_UNAVAILABLE_CRITICAL_EXTENSION.get());

  /**
   * The result code that indicates that the requested operation could
   * not be processed because it requires confidentiality for the
   * communication between the client and the server.
   */
  public static final ResultCode CONFIDENTIALITY_REQUIRED =
      register(LDAPResultCode.CONFIDENTIALITY_REQUIRED,
          INFO_RESULT_CONFIDENTIALITY_REQUIRED.get());

  /**
   * The result code that should be used for intermediate responses in
   * multi-stage SASL bind operations.
   */
  public static final ResultCode SASL_BIND_IN_PROGRESS =
      register(LDAPResultCode.SASL_BIND_IN_PROGRESS,
          INFO_RESULT_SASL_BIND_IN_PROGRESS.get(), false);

  /**
   * The result code that indicates that the requested operation failed
   * because it targeted an attribute or attribute value that did not
   * exist in the specified entry.
   */
  public static final ResultCode NO_SUCH_ATTRIBUTE =
      register(LDAPResultCode.NO_SUCH_ATTRIBUTE,
          INFO_RESULT_NO_SUCH_ATTRIBUTE.get());

  /**
   * The result code that indicates that the requested operation failed
   * because it referenced an attribute that is not defined in the
   * server schema.
   */
  public static final ResultCode UNDEFINED_ATTRIBUTE_TYPE =
      register(LDAPResultCode.UNDEFINED_ATTRIBUTE_TYPE,
          INFO_RESULT_UNDEFINED_ATTRIBUTE_TYPE.get());

  /**
   * The result code that indicates that the requested operation failed
   * because it attempted to perform an inappropriate type of matching
   * against an attribute.
   */
  public static final ResultCode INAPPROPRIATE_MATCHING =
      register(LDAPResultCode.INAPPROPRIATE_MATCHING,
          INFO_RESULT_INAPPROPRIATE_MATCHING.get());

  /**
   * The result code that indicates that the requested operation failed
   * because it would have violated some constraint defined in the
   * server.
   */
  public static final ResultCode CONSTRAINT_VIOLATION =
      register(LDAPResultCode.CONSTRAINT_VIOLATION,
          INFO_RESULT_CONSTRAINT_VIOLATION.get());

  /**
   * The result code that indicates that the requested operation failed
   * because it would have resulted in a conflict with an existing
   * attribute or attribute value in the target entry.
   */
  public static final ResultCode ATTRIBUTE_OR_VALUE_EXISTS =
      register(LDAPResultCode.ATTRIBUTE_OR_VALUE_EXISTS,
          INFO_RESULT_ATTRIBUTE_OR_VALUE_EXISTS.get());

  /**
   * The result code that indicates that the requested operation failed
   * because it violated the syntax for a specified attribute.
   */
  public static final ResultCode INVALID_ATTRIBUTE_SYNTAX =
      register(LDAPResultCode.INVALID_ATTRIBUTE_SYNTAX,
          INFO_RESULT_INVALID_ATTRIBUTE_SYNTAX.get());

  /**
   * The result code that indicates that the requested operation failed
   * because it referenced an entry that does not exist.
   */
  public static final ResultCode NO_SUCH_OBJECT =
      register(LDAPResultCode.NO_SUCH_OBJECT,
          INFO_RESULT_NO_SUCH_OBJECT.get());

  /**
   * The result code that indicates that the requested operation failed
   * because it attempted to perform an illegal operation on an alias.
   */
  public static final ResultCode ALIAS_PROBLEM =
      register(LDAPResultCode.ALIAS_PROBLEM, INFO_RESULT_ALIAS_PROBLEM
          .get());

  /**
   * The result code that indicates that the requested operation failed
   * because it would have resulted in an entry with an invalid or
   * malformed DN.
   */
  public static final ResultCode INVALID_DN_SYNTAX =
      register(LDAPResultCode.INVALID_DN_SYNTAX,
          INFO_RESULT_INVALID_DN_SYNTAX.get());

  /**
   * The result code that indicates that a problem was encountered while
   * attempting to dereference an alias for a search operation.
   */
  public static final ResultCode ALIAS_DEREFERENCING_PROBLEM =
      register(LDAPResultCode.ALIAS_DEREFERENCING_PROBLEM,
          INFO_RESULT_ALIAS_DEREFERENCING_PROBLEM.get());

  /**
   * The result code that indicates that an authentication attempt
   * failed because the requested type of authentication was not
   * appropriate for the targeted entry.
   */
  public static final ResultCode INAPPROPRIATE_AUTHENTICATION =
      register(LDAPResultCode.INAPPROPRIATE_AUTHENTICATION,
          INFO_RESULT_INAPPROPRIATE_AUTHENTICATION.get());

  /**
   * The result code that indicates that an authentication attempt
   * failed because the user did not provide a valid set of credentials.
   */
  public static final ResultCode INVALID_CREDENTIALS =
      register(LDAPResultCode.INVALID_CREDENTIALS,
          INFO_RESULT_INVALID_CREDENTIALS.get());

  /**
   * The result code that indicates that the client does not have
   * sufficient permission to perform the requested operation.
   */
  public static final ResultCode INSUFFICIENT_ACCESS_RIGHTS =
      register(LDAPResultCode.INSUFFICIENT_ACCESS_RIGHTS,
          INFO_RESULT_INSUFFICIENT_ACCESS_RIGHTS.get());

  /**
   * The result code that indicates that the server is too busy to
   * process the requested operation.
   */
  public static final ResultCode BUSY =
      register(LDAPResultCode.BUSY, INFO_RESULT_BUSY.get());

  /**
   * The result code that indicates that either the entire server or one
   * or more required resources were not available for use in processing
   * the request.
   */
  public static final ResultCode UNAVAILABLE =
      register(LDAPResultCode.UNAVAILABLE, INFO_RESULT_UNAVAILABLE
          .get());

  /**
   * The result code that indicates that the server is unwilling to
   * perform the requested operation.
   */
  public static final ResultCode UNWILLING_TO_PERFORM =
      register(LDAPResultCode.UNWILLING_TO_PERFORM,
          INFO_RESULT_UNWILLING_TO_PERFORM.get());

  /**
   * The result code that indicates that a referral or chaining loop was
   * detected while processing the request.
   */
  public static final ResultCode LOOP_DETECT =
      register(LDAPResultCode.LOOP_DETECT, INFO_RESULT_LOOP_DETECT
          .get());

  /**
   * The result code that indicates that a search request included a VLV
   * request control without a server-side sort control.
   */
  public static final ResultCode SORT_CONTROL_MISSING =
      register(LDAPResultCode.SORT_CONTROL_MISSING,
          INFO_RESULT_SORT_CONTROL_MISSING.get());

  /**
   * The result code that indicates that a search request included a VLV
   * request control with an invalid offset.
   */
  public static final ResultCode OFFSET_RANGE_ERROR =
      register(LDAPResultCode.OFFSET_RANGE_ERROR,
          INFO_RESULT_OFFSET_RANGE_ERROR.get());

  /**
   * The result code that indicates that the requested operation failed
   * because it would have violated the server's naming configuration.
   */
  public static final ResultCode NAMING_VIOLATION =
      register(LDAPResultCode.NAMING_VIOLATION,
          INFO_RESULT_NAMING_VIOLATION.get());

  /**
   * The result code that indicates that the requested operation failed
   * because it would have resulted in an entry that violated the server
   * schema.
   */
  public static final ResultCode OBJECTCLASS_VIOLATION =
      register(LDAPResultCode.OBJECTCLASS_VIOLATION,
          INFO_RESULT_OBJECTCLASS_VIOLATION.get());

  /**
   * The result code that indicates that the requested operation is not
   * allowed for non-leaf entries.
   */
  public static final ResultCode NOT_ALLOWED_ON_NONLEAF =
      register(LDAPResultCode.NOT_ALLOWED_ON_NONLEAF,
          INFO_RESULT_NOT_ALLOWED_ON_NONLEAF.get());

  /**
   * The result code that indicates that the requested operation is not
   * allowed on an RDN attribute.
   */
  public static final ResultCode NOT_ALLOWED_ON_RDN =
      register(LDAPResultCode.NOT_ALLOWED_ON_RDN,
          INFO_RESULT_NOT_ALLOWED_ON_RDN.get());

  /**
   * The result code that indicates that the requested operation failed
   * because it would have resulted in an entry that conflicts with an
   * entry that already exists.
   */
  public static final ResultCode ENTRY_ALREADY_EXISTS =
      register(LDAPResultCode.ENTRY_ALREADY_EXISTS,
          INFO_RESULT_ENTRY_ALREADY_EXISTS.get());

  /**
   * The result code that indicates that the operation could not be
   * processed because it would have modified the objectclasses
   * associated with an entry in an illegal manner.
   */
  public static final ResultCode OBJECTCLASS_MODS_PROHIBITED =
      register(LDAPResultCode.OBJECTCLASS_MODS_PROHIBITED,
          INFO_RESULT_OBJECTCLASS_MODS_PROHIBITED.get());

  /**
   * The result code that indicates that the operation could not be
   * processed because it would impact multiple DSAs or other
   * repositories.
   */
  public static final ResultCode AFFECTS_MULTIPLE_DSAS =
      register(LDAPResultCode.AFFECTS_MULTIPLE_DSAS,
          INFO_RESULT_AFFECTS_MULTIPLE_DSAS.get());

  /**
   * The result code that indicates that the operation could not be
   * processed because there was an error while processing the virtual
   * list view control.
   */
  public static final ResultCode VIRTUAL_LIST_VIEW_ERROR =
      register(LDAPResultCode.VIRTUAL_LIST_VIEW_ERROR,
          INFO_RESULT_VIRTUAL_LIST_VIEW_ERROR.get());

  /**
   * The result code that should be used if no other result code is
   * appropriate.
   */
  public static final ResultCode OTHER =
      register(LDAPResultCode.OTHER, INFO_RESULT_OTHER.get());

  /**
   * The client-side result code that indicates that a
   * previously-established connection to the server was lost. This is
   * for client-side use only and should never be transferred over
   * protocol.
   */
  public static final ResultCode CLIENT_SIDE_SERVER_DOWN =
      register(LDAPResultCode.CLIENT_SIDE_SERVER_DOWN,
          INFO_RESULT_CLIENT_SIDE_SERVER_DOWN.get());

  /**
   * The client-side result code that indicates that a local error
   * occurred that had nothing to do with interaction with the server.
   * This is for client-side use only and should never be transferred
   * over protocol.
   */
  public static final ResultCode CLIENT_SIDE_LOCAL_ERROR =
      register(LDAPResultCode.CLIENT_SIDE_LOCAL_ERROR,
          INFO_RESULT_CLIENT_SIDE_LOCAL_ERROR.get());

  /**
   * The client-side result code that indicates that an error occurred
   * while encoding a request to send to the server. This is for
   * client-side use only and should never be transferred over protocol.
   */
  public static final ResultCode CLIENT_SIDE_ENCODING_ERROR =
      register(LDAPResultCode.CLIENT_SIDE_ENCODING_ERROR,
          INFO_RESULT_CLIENT_SIDE_ENCODING_ERROR.get());

  /**
   * The client-side result code that indicates that an error occurred
   * while decoding a response from the server. This is for client-side
   * use only and should never be transferred over protocol.
   */
  public static final ResultCode CLIENT_SIDE_DECODING_ERROR =
      register(LDAPResultCode.CLIENT_SIDE_DECODING_ERROR,
          INFO_RESULT_CLIENT_SIDE_DECODING_ERROR.get());

  /**
   * The client-side result code that indicates that the client did not
   * receive an expected response in a timely manner. This is for
   * client-side use only and should never be transferred over protocol.
   */
  public static final ResultCode CLIENT_SIDE_TIMEOUT =
      register(LDAPResultCode.CLIENT_SIDE_TIMEOUT,
          INFO_RESULT_CLIENT_SIDE_TIMEOUT.get());

  /**
   * The client-side result code that indicates that the user requested
   * an unknown or unsupported authentication mechanism. This is for
   * client-side use only and should never be transferred over protocol.
   */
  public static final ResultCode CLIENT_SIDE_AUTH_UNKNOWN =
      register(LDAPResultCode.CLIENT_SIDE_AUTH_UNKNOWN,
          INFO_RESULT_CLIENT_SIDE_AUTH_UNKNOWN.get());

  /**
   * The client-side result code that indicates that the filter provided
   * by the user was malformed and could not be parsed. This is for
   * client-side use only and should never be transferred over protocol.
   */
  public static final ResultCode CLIENT_SIDE_FILTER_ERROR =
      register(LDAPResultCode.CLIENT_SIDE_FILTER_ERROR,
          INFO_RESULT_CLIENT_SIDE_FILTER_ERROR.get());

  /**
   * The client-side result code that indicates that the user cancelled
   * an operation. This is for client-side use only and should never be
   * transferred over protocol.
   */
  public static final ResultCode CLIENT_SIDE_USER_CANCELLED =
      register(LDAPResultCode.CLIENT_SIDE_USER_CANCELLED,
          INFO_RESULT_CLIENT_SIDE_USER_CANCELLED.get());

  /**
   * The client-side result code that indicates that there was a problem
   * with one or more of the parameters provided by the user. This is
   * for client-side use only and should never be transferred over
   * protocol.
   */
  public static final ResultCode CLIENT_SIDE_PARAM_ERROR =
      register(LDAPResultCode.CLIENT_SIDE_PARAM_ERROR,
          INFO_RESULT_CLIENT_SIDE_PARAM_ERROR.get());

  /**
   * The client-side result code that indicates that the client
   * application was not able to allocate enough memory for the
   * requested operation. This is for client-side use only and should
   * never be transferred over protocol.
   */
  public static final ResultCode CLIENT_SIDE_NO_MEMORY =
      register(LDAPResultCode.OTHER, INFO_RESULT_CLIENT_SIDE_NO_MEMORY
          .get());

  /**
   * The client-side result code that indicates that the client was not
   * able to establish a connection to the server. This is for
   * client-side use only and should never be transferred over protocol.
   */
  public static final ResultCode CLIENT_SIDE_CONNECT_ERROR =
      register(LDAPResultCode.CLIENT_SIDE_CONNECT_ERROR,
          INFO_RESULT_CLIENT_SIDE_CONNECT_ERROR.get());

  /**
   * The client-side result code that indicates that the user requested
   * an operation that is not supported. This is for client-side use
   * only and should never be transferred over protocol.
   */
  public static final ResultCode CLIENT_SIDE_NOT_SUPPORTED =
      register(LDAPResultCode.CLIENT_SIDE_NOT_SUPPORTED,
          INFO_RESULT_CLIENT_SIDE_NOT_SUPPORTED.get());

  /**
   * The client-side result code that indicates that the client expected
   * a control to be present in the response from the server but it was
   * not included. This is for client-side use only and should never be
   * transferred over protocol.
   */
  public static final ResultCode CLIENT_SIDE_CONTROL_NOT_FOUND =
      register(LDAPResultCode.CLIENT_SIDE_CONTROL_NOT_FOUND,
          INFO_RESULT_CLIENT_SIDE_CONTROL_NOT_FOUND.get());

  /**
   * The client-side result code that indicates that the server did not
   * return any results for a search operation that was expected to
   * match at least one entry. This is for client-side use only and
   * should never be transferred over protocol.
   */
  public static final ResultCode CLIENT_SIDE_NO_RESULTS_RETURNED =
      register(LDAPResultCode.CLIENT_SIDE_NO_RESULTS_RETURNED,
          INFO_RESULT_CLIENT_SIDE_NO_RESULTS_RETURNED.get());

  /**
   * The client-side result code that indicates that the server has
   * returned more matching entries for a search operation than have
   * been processed so far. This is for client-side use only and should
   * never be transferred over protocol.
   */
  public static final ResultCode CLIENT_SIDE_MORE_RESULTS_TO_RETURN =
      register(LDAPResultCode.CLIENT_SIDE_MORE_RESULTS_TO_RETURN,
          INFO_RESULT_CLIENT_SIDE_MORE_RESULTS_TO_RETURN.get());

  /**
   * The client-side result code that indicates that the client detected
   * a referral loop caused by servers referencing each other in a
   * circular manner. This is for client-side use only and should never
   * be transferred over protocol.
   */
  public static final ResultCode CLIENT_SIDE_CLIENT_LOOP =
      register(LDAPResultCode.CLIENT_SIDE_CLIENT_LOOP,
          INFO_RESULT_CLIENT_SIDE_CLIENT_LOOP.get());

  /**
   * The client-side result code that indicates that the client reached
   * the maximum number of hops allowed when attempting to follow a
   * referral (i.e., following one referral resulted in another referral
   * which resulted in another referral and so on). This is for
   * client-side use only and should never be transferred over protocol.
   */
  public static final ResultCode CLIENT_SIDE_REFERRAL_LIMIT_EXCEEDED =
      register(LDAPResultCode.CLIENT_SIDE_REFERRAL_LIMIT_EXCEEDED,
          INFO_RESULT_CLIENT_SIDE_REFERRAL_LIMIT_EXCEEDED.get());

  /**
   * The result code that indicates that a cancel request was
   * successful, or that the specified operation was canceled.
   */
  public static final ResultCode CANCELED =
      register(LDAPResultCode.CANCELED, INFO_RESULT_CANCELED.get());

  /**
   * The result code that indicates that a cancel request was
   * unsuccessful because the targeted operation did not exist or had
   * already completed.
   */
  public static final ResultCode NO_SUCH_OPERATION =
      register(LDAPResultCode.NO_SUCH_OPERATION,
          INFO_RESULT_NO_SUCH_OPERATION.get());

  /**
   * The result code that indicates that a cancel request was
   * unsuccessful because processing on the targeted operation had
   * already reached a point at which it could not be canceled.
   */
  public static final ResultCode TOO_LATE =
      register(LDAPResultCode.TOO_LATE, INFO_RESULT_TOO_LATE.get());

  /**
   * The result code that indicates that a cancel request was
   * unsuccessful because the targeted operation was one that could not
   * be canceled.
   */
  public static final ResultCode CANNOT_CANCEL =
      register(LDAPResultCode.CANNOT_CANCEL, INFO_RESULT_CANNOT_CANCEL
          .get());

  /**
   * The result code that indicates that the filter contained in an
   * assertion control failed to match the target entry.
   */
  public static final ResultCode ASSERTION_FAILED =
      register(LDAPResultCode.ASSERTION_FAILED,
          INFO_RESULT_ASSERTION_FAILED.get());

  /**
   * The result code that should be used if the server will not allow
   * the client to use the requested authorization.
   */
  public static final ResultCode AUTHORIZATION_DENIED =
      register(LDAPResultCode.AUTHORIZATION_DENIED,
          INFO_RESULT_AUTHORIZATION_DENIED.get());

  /**
   * The result code that should be used if the server did not actually
   * complete processing on the associated operation because the request
   * included the LDAP No-Op control.
   */
  public static final ResultCode NO_OPERATION =
      register(LDAPResultCode.NO_OPERATION, INFO_RESULT_NO_OPERATION
          .get());



  public static ResultCode register(int intValue, Message name)
  {
    ResultCode t = new ResultCode(intValue, name, true);
    ELEMENTS[intValue] = t;
    return t;
  }



  public static ResultCode register(int intValue, Message name,
      boolean exceptional)
  {
    ResultCode t = new ResultCode(intValue, name, exceptional);
    ELEMENTS[intValue] = t;
    return t;
  }



  public static ResultCode valueOf(int intValue)
  {
    ResultCode e = ELEMENTS[intValue];
    if (e == null)
    {
      e =
          new ResultCode(intValue, INFO_UNDEFINED_TYPE.get(intValue),
              true);
    }
    return e;
  }



  public static List<ResultCode> values()
  {
    return Arrays.asList(ELEMENTS);
  }



  private final int intValue;

  private final Message name;

  private final boolean exceptional;



  private ResultCode(int intValue, Message name, boolean exceptional)
  {
    this.intValue = intValue;
    this.name = name;
    this.exceptional = exceptional;
  }



  @Override
  public boolean equals(Object o)
  {
    return (this == o)
        || ((o instanceof ResultCode) && (this.intValue == ((ResultCode) o).intValue));

  }



  @Override
  public int hashCode()
  {
    return intValue;
  }



  public int intValue()
  {
    return intValue;
  }



  public boolean isExceptional()
  {
    return exceptional;
  }



  public Message getName()
  {
    return name;
  }



  @Override
  public String toString()
  {
    return name.toString();
  }
}
