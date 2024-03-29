/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package org.cresst.sb.irp.automation.adapter.proctor.data;

import AIR.Common.Utilities.TDSStringUtils;
import com.fasterxml.jackson.annotation.JsonProperty;

public class District
{

  private String  _name;
  private String  _id;
  private String    _key;
  private boolean _isSelected;

  public District (String name, String id, String key) {
    this._name = name;
    this._id = id;
    this._key = key;
  }

  /**
   * @return the _key
   */
  @JsonProperty("Key")
  public String getKey () {
    return _key;
  }

  /**
   * @param _key the _key to set
   */
  public void setKey (String _key) {
    this._key = _key;
  }

  @JsonProperty("ID")
  public String getId()
  {
    return _id;
  }
  public void setId(String id)
  {
    this._id = id;
  }
  
  @JsonProperty("Name")
  public String getName()
  {
    return _name;
  }
  
  public void setName(String name)
  {
    this._name = name;
  }
  
  @JsonProperty("IsSelected")
  public boolean getIsSelected()
  {
    return _isSelected;
  }
  
  @Override
  public String toString () {
    return TDSStringUtils.format ("[Name={0}] [ID={1}] [Key={2}]", _name, _id, _key);
  }
}
