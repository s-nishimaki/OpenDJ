/*
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License, Version 1.0 only
 * (the "License").  You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the license at legal-notices/CDDLv1_0.txt
 * or http://forgerock.org/license/CDDLv1.0.html.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each
 * file and include the License file at legal-notices/CDDLv1_0.txt.
 * If applicable, add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your own identifying
 * information:
 *      Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 *
 *
 *      Copyright 2006-2008 Sun Microsystems, Inc.
 *      Portions Copyright 2014-2015 ForgeRock AS
 */
package org.opends.server.config;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import javax.management.AttributeList;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanParameterInfo;

import org.forgerock.i18n.LocalizableMessage;
import org.forgerock.opendj.ldap.ByteString;
import org.forgerock.opendj.ldap.schema.Syntax;
import org.opends.server.core.DirectoryServer;
import org.opends.server.types.Attribute;

import static org.opends.messages.ConfigMessages.*;
import static org.opends.server.config.ConfigConstants.*;
import static org.opends.server.util.CollectionUtils.*;

/**
 * This class defines a configuration attribute that is only intended for use
 * in displaying information.  It will not allow its value to be altered.
 */
@org.opends.server.types.PublicAPI(
     stability=org.opends.server.types.StabilityLevel.VOLATILE,
     mayInstantiate=true,
     mayExtend=false,
     mayInvoke=true)
public final class ReadOnlyConfigAttribute
       extends ConfigAttribute
{
  /** The set of values for this attribute. */
  private List<String> values;



  /**
   * Creates a new read-only configuration attribute stub with the provided
   * information but no values.  The values will be set using the
   * <CODE>setInitialValue</CODE> method.
   *
   * @param  name           The name for this configuration attribute.
   * @param  description    The description for this configuration attribute.
   * @param  isMultiValued  Indicates whether this configuration attribute may
   *                        have multiple values.
   */
  public ReadOnlyConfigAttribute(String name, LocalizableMessage description,
                                 boolean isMultiValued)
  {
    super(name, description, false, isMultiValued, false);


    values = new ArrayList<>();
  }



  /**
   * Creates a new read-only configuration attribute with the provided
   * information.
   *
   * @param  name         The name for this configuration attribute.
   * @param  description  The description for this configuration attribute.
   * @param  value        The value for this configuration attribute.
   */
  public ReadOnlyConfigAttribute(String name, LocalizableMessage description, String value)
  {
    super(name, description, false, false, false, getValueSet(value));


    if (value == null)
    {
      values = new ArrayList<>(0);
    }
    else
    {
      values = newArrayList(value);
    }
  }



  /**
   * Creates a new read-only configuration attribute with the provided
   * information.
   *
   * @param  name         The name for this configuration attribute.
   * @param  description  The description for this configuration attribute.
   * @param  values       The set of values for this configuration attribute.
   */
  public ReadOnlyConfigAttribute(String name, LocalizableMessage description,
                                 List<String> values)
  {
    super(name, description, false, true, false, getValueSet(values));


    if (values == null)
    {
      this.values = new ArrayList<>();
    }
    else
    {
      this.values = values;
    }
  }



  /**
   * Retrieves the name of the data type for this configuration attribute.  This
   * is for informational purposes (e.g., inclusion in method signatures and
   * other kinds of descriptions) and does not necessarily need to map to an
   * actual Java type.
   *
   * @return  The name of the data type for this configuration attribute.
   */
  public String getDataType()
  {
    return "ReadOnly";
  }



  /**
   * Retrieves the attribute syntax for this configuration attribute.
   *
   * @return  The attribute syntax for this configuration attribute.
   */
  public Syntax getSyntax()
  {
    return DirectoryServer.getDefaultStringSyntax();
  }



  /**
   * Retrieves the active value for this configuration attribute as a string.
   * This is only valid for single-valued attributes that have a value.
   *
   * @return  The active value for this configuration attribute as a string.
   *
   * @throws  ConfigException  If this attribute does not have exactly one
   *                           active value.
   */
  public String activeValue()
         throws ConfigException
  {
    if (values == null || values.isEmpty())
    {
      throw new ConfigException(ERR_CONFIG_ATTR_NO_STRING_VALUE.get(getName()));
    }
    if (values.size() > 1)
    {
      throw new ConfigException(ERR_CONFIG_ATTR_MULTIPLE_STRING_VALUES.get(getName()));
    }

    return values.get(0);
  }



  /**
   * Retrieves the set of active values for this configuration attribute.
   *
   * @return  The set of active values for this configuration attribute.
   */
  public List<String> activeValues()
  {
    return values;
  }



  /**
   * Retrieves the pending value for this configuration attribute as a string.
   * This is only valid for single-valued attributes that have a value.  If this
   * attribute does not have any pending values, then the active value will be
   * returned.
   *
   * @return  The pending value for this configuration attribute as a string.
   *
   * @throws  ConfigException  If this attribute does not have exactly one
   *                           pending value.
   */
  public String pendingValue()
         throws ConfigException
  {
    return  activeValue();
  }



  /**
   * Retrieves the set of pending values for this configuration attribute.  If
   * there are no pending values, then the set of active values will be
   * returned.
   *
   * @return  The set of pending values for this configuration attribute.
   */
  public List<String> pendingValues()
  {
    return activeValues();
  }



  /**
   * Sets the value for this string configuration attribute.
   *
   * @param  value  The value for this string configuration attribute.
   *
   * @throws  ConfigException  If the provided value is not acceptable.
   */
  public void setValue(String value) throws ConfigException
  {
    throw new ConfigException(ERR_CONFIG_ATTR_READ_ONLY.get(getName()));
  }



  /**
   * Sets the values for this string configuration attribute.
   *
   * @param  values  The set of values for this string configuration attribute.
   *
   * @throws  ConfigException  If the provided value set or any of the
   *                           individual values are not acceptable.
   */
  public void setValues(List<String> values) throws ConfigException
  {
    throw new ConfigException(ERR_CONFIG_ATTR_READ_ONLY.get(getName()));
  }



  /**
   * Applies the set of pending values, making them the active values for this
   * configuration attribute.  This will not take any action if there are no
   * pending values.
   */
  public void applyPendingValues()
  {
  }



  /**
   * Indicates whether the provided value is acceptable for use in this
   * attribute.  If it is not acceptable, then the reason should be written into
   * the provided buffer.
   *
   * @param  value         The value for which to make the determination.
   * @param  rejectReason  A buffer into which a human-readable reason for the
   *                       reject may be written.
   *
   * @return  <CODE>true</CODE> if the provided value is acceptable for use in
   *          this attribute, or <CODE>false</CODE> if not.
   */
  public boolean valueIsAcceptable(ByteString value, StringBuilder rejectReason)
  {
    rejectReason.append(ERR_CONFIG_ATTR_READ_ONLY.get(getName()));
    return false;
  }



  /**
   * Converts the provided set of strings to a corresponding set of attribute
   * values.
   *
   * @param  valueStrings   The set of strings to be converted into attribute
   *                        values.
   * @param  allowFailures  Indicates whether the decoding process should allow
   *                        any failures in which one or more values could be
   *                        decoded but at least one could not.  If this is
   *                        <CODE>true</CODE> and such a condition is acceptable
   *                        for the underlying attribute type, then the returned
   *                        set of values should simply not include those
   *                        undecodable values.
   *
   * @return  The set of attribute values converted from the provided strings.
   *
   * @throws  ConfigException  If an unrecoverable problem occurs while
   *                           performing the conversion.
   */
  public LinkedHashSet<ByteString> stringsToValues(List<String> valueStrings, boolean allowFailures)
      throws ConfigException
  {
    if (valueStrings == null || valueStrings.isEmpty())
    {
      return new LinkedHashSet<>();
    }
    return getValueSet(valueStrings);
  }

  /**
   * Converts the set of active values for this configuration attribute into a
   * set of strings that may be stored in the configuration or represented over
   * protocol.  The string representation used by this method should be
   * compatible with the decoding used by the <CODE>stringsToValues</CODE>
   * method.
   *
   * @return  The string representations of the set of active values for this
   *          configuration attribute.
   */
  public List<String> activeValuesToStrings()
  {
    return values;
  }



  /**
   * Converts the set of pending values for this configuration attribute into a
   * set of strings that may be stored in the configuration or represented over
   * protocol.  The string representation used by this method should be
   * compatible with the decoding used by the <CODE>stringsToValues</CODE>
   * method.
   *
   * @return  The string representations of the set of pending values for this
   *          configuration attribute, or <CODE>null</CODE> if there are no
   *          pending values.
   */
  public List<String> pendingValuesToStrings()
  {
    return activeValuesToStrings();
  }



  /**
   * Retrieves a new configuration attribute of this type that will contain the
   * values from the provided attribute.
   *
   * @param  attributeList  The list of attributes to use to create the config
   *                        attribute.  The list must contain either one or two
   *                        elements, with both attributes having the same base
   *                        name and the only option allowed is ";pending" and
   *                        only if this attribute is one that requires admin
   *                        action before a change may take effect.
   *
   * @return  The generated configuration attribute.
   *
   * @throws  ConfigException  If the provided attribute cannot be treated as a
   *                           configuration attribute of this type (e.g., if
   *                           one or more of the values of the provided
   *                           attribute are not suitable for an attribute of
   *                           this type, or if this configuration attribute is
   *                           single-valued and the provided attribute has
   *                           multiple values).
   */
  public ConfigAttribute getConfigAttribute(List<Attribute> attributeList)
         throws ConfigException
  {
    // The attribute won't be present in the entry, so we'll just return a
    // reference to this attribute.
    return duplicate();
  }



  /**
   * Retrieves a JMX attribute containing the active value set for this
   * configuration attribute.
   *
   * @return  A JMX attribute containing the active value set for this
   *          configuration attribute, or <CODE>null</CODE> if it does not have
   *          any active values.
   */
  public javax.management.Attribute toJMXAttribute()
  {
    if (isMultiValued())
    {
      String[] valueArray = values.toArray(new String[values.size()]);
      return new javax.management.Attribute(getName(), valueArray);
    }
    else if (!values.isEmpty())
    {
      return new javax.management.Attribute(getName(), values.get(0));
    }
    else
    {
      return null;
    }
  }

  /**
     * Retrieves a JMX attribute containing the pending value set for this
     * configuration attribute. As this an read only attribute, this method
     * should never be called
     *
     * @return A JMX attribute containing the pending value set for this
     *         configuration attribute, or <CODE>null</CODE> if it does
     *         not have any active values.
     */
    @Override
    public javax.management.Attribute toJMXAttributePending()
    {
        // Should never occur !!!
        return toJMXAttribute();
    }



  /**
   * Adds information about this configuration attribute to the provided JMX
   * attribute list.  If this configuration attribute requires administrative
   * action before changes take effect and it has a set of pending values, then
   * two attributes should be added to the list -- one for the active value
   * and one for the pending value.  The pending value should be named with
   * the pending option.
   *
   * @param  attributeList  The attribute list to which the JMX attribute(s)
   *                        should be added.
   */
  public void toJMXAttribute(AttributeList attributeList)
  {
    attributeList.add(toJMXAttribute());
  }



  /**
   * Adds information about this configuration attribute to the provided list in
   * the form of a JMX <CODE>MBeanAttributeInfo</CODE> object.  If this
   * configuration attribute requires administrative action before changes take
   * effect and it has a set of pending values, then two attribute info objects
   * should be added to the list -- one for the active value (which should be
   * read-write) and one for the pending value (which should be read-only).  The
   * pending value should be named with the pending option.
   *
   * @param  attributeInfoList  The list to which the attribute information
   *                            should be added.
   */
  public void toJMXAttributeInfo(List<MBeanAttributeInfo> attributeInfoList)
  {
    attributeInfoList.add(new MBeanAttributeInfo(getName(), getType(),
        String.valueOf(getDescription()), true, false, false));
  }



  /**
   * Retrieves a JMX <CODE>MBeanParameterInfo</CODE> object that describes this
   * configuration attribute.
   *
   * @return  A JMX <CODE>MBeanParameterInfo</CODE> object that describes this
   *          configuration attribute.
   */
  public MBeanParameterInfo toJMXParameterInfo()
  {
    return new MBeanParameterInfo(getName(), getType(), String.valueOf(getDescription()));
  }

  private String getType()
  {
    return isMultiValued() ? JMX_TYPE_STRING_ARRAY : String.class.getName();
  }

  /**
   * Attempts to set the value of this configuration attribute based on the
   * information in the provided JMX attribute.
   *
   * @param  jmxAttribute  The JMX attribute to use to attempt to set the value
   *                       of this configuration attribute.
   *
   * @throws  ConfigException  If the provided JMX attribute does not have an
   *                           acceptable value for this configuration
   *                           attribute.
   */
  public void setValue(javax.management.Attribute jmxAttribute)
         throws ConfigException
  {
    throw new ConfigException(ERR_CONFIG_ATTR_READ_ONLY.get(getName()));
  }



  /**
   * Creates a duplicate of this configuration attribute.
   *
   * @return  A duplicate of this configuration attribute.
   */
  public ConfigAttribute duplicate()
  {
    return new ReadOnlyConfigAttribute(getName(), getDescription(), activeValues());
  }
}
