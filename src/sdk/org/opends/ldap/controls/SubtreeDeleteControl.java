package org.opends.ldap.controls;



import static org.opends.messages.ProtocolMessages.ERR_SUBTREE_DELETE_INVALID_CONTROL_VALUE;
import static org.opends.server.util.ServerConstants.OID_SUBTREE_DELETE_CONTROL;

import org.opends.ldap.DecodeException;
import org.opends.messages.Message;
import org.opends.server.types.ByteString;
import org.opends.spi.ControlDecoder;



/**
 * This class implements the subtree delete control defined in
 * draft-armijo-ldap-treedelete. It makes it possible for clients to
 * delete subtrees of entries.
 */
public class SubtreeDeleteControl extends Control
{
  /**
   * ControlDecoder implementation to decode this control from a
   * ByteString.
   */
  private final static class Decoder implements
      ControlDecoder<SubtreeDeleteControl>
  {
    /**
     * {@inheritDoc}
     */
    public SubtreeDeleteControl decode(boolean isCritical,
        ByteString value) throws DecodeException
    {
      if (value != null)
      {
        Message message =
            ERR_SUBTREE_DELETE_INVALID_CONTROL_VALUE.get();
        throw new DecodeException(message);
      }

      return new SubtreeDeleteControl(isCritical);
    }



    public String getOID()
    {
      return OID_SUBTREE_DELETE_CONTROL;
    }

  }



  /**
   * The Control Decoder that can be used to decode this control.
   */
  public static final ControlDecoder<SubtreeDeleteControl> DECODER =
      new Decoder();



  /**
   * Creates a new subtree delete control.
   * 
   * @param isCritical
   *          Indicates whether the control should be considered
   *          critical for the operation processing.
   */
  public SubtreeDeleteControl(boolean isCritical)
  {
    super(OID_SUBTREE_DELETE_CONTROL, isCritical);
  }



  @Override
  public ByteString getValue()
  {
    return null;
  }



  @Override
  public boolean hasValue()
  {
    return false;
  }



  /**
   * {@inheritDoc}
   */
  @Override
  public void toString(StringBuilder buffer)
  {
    buffer.append("SubtreeDeleteControl(oid=");
    buffer.append(getOID());
    buffer.append(", criticality=");
    buffer.append(isCritical());
    buffer.append(")");
  }

}
