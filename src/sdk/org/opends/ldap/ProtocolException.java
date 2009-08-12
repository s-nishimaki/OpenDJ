/*
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License, Version 1.0 only
 * (the "License").  You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the license at
 * trunk/opends/resource/legal-notices/OpenDS.LICENSE
 * or https://OpenDS.dev.java.net/OpenDS.LICENSE.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each
 * file and include the License file at
 * trunk/opends/resource/legal-notices/OpenDS.LICENSE.  If applicable,
 * add the following below this CDDL HEADER, with the fields enclosed
 * by brackets "[]" replaced with your own identifying information:
 *      Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 *
 *
 *      Copyright 2009 Sun Microsystems, Inc.
 */

package org.opends.ldap;



import java.io.IOException;

import org.opends.messages.Message;
import org.opends.util.LocalizableException;



/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Jun 9, 2009 Time:
 * 7:19:56 PM To change this template use File | Settings | File
 * Templates.
 */
@SuppressWarnings("serial")
public final class ProtocolException extends IOException implements
    LocalizableException
{
  private final Message message;



  /**
   * Creates a new identified exception with the provided information.
   *
   * @param message
   *          The message that explains the problem that occurred.
   */
  public ProtocolException(Message message)
  {
    super(message.toString());
    this.message = message;
  }



  /**
   * Returns the message that explains the problem that occurred.
   *
   * @return Message of the problem
   */
  public Message getMessageObject()
  {
    return message;
  }

}
