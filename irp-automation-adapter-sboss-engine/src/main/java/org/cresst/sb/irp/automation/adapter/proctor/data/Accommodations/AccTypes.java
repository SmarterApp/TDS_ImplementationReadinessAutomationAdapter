/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package org.cresst.sb.irp.automation.adapter.proctor.data.Accommodations;

import AIR.Common.collections.HashMapDataSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.apache.commons.collections.IteratorUtils;
import org.cresst.sb.irp.automation.adapter.proctor.data.deserializer.AccTypesDeserializer;

import java.util.*;


@JsonSerialize(using = HashMapDataSerializer.class)
@JsonDeserialize(using = AccTypesDeserializer.class)
public class AccTypes extends HashMap<String, AccType>
{
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private List<String> _sortKeysBySortOrder = new ArrayList<String> ();

  public AccTypesDTO getAccTypesDTO () {
    if (_sortKeysBySortOrder == null)
      _sortKeysBySortOrder = new ArrayList<String> (IteratorUtils.toList (this.keySet ().iterator ()));//

    AccTypesDTO accTypesDTO = new AccTypesDTO ();
    for (String key : _sortKeysBySortOrder) {
      if (this.containsKey (key)) {
        AccType value = this.get (key);
        accTypesDTO.add (new AccTypeDTO (key, value));// kvp.Key,
        // kvp.Value
      }
    }
    return accTypesDTO;
  }

  public void sortValuesBySortOrder () {
    List<Map.Entry<String, AccType>> list = new ArrayList<Map.Entry<String, AccType>> (this.entrySet ());
    Collections.sort (list, new AccTyeComparator ());
    // save the sorting keys by SortOrder
    for (Map.Entry<String, AccType> data : list) {
      _sortKeysBySortOrder.add (data.getKey ());
    }

    for (Map.Entry<String, AccType> kvp : this.entrySet ()) {
      // kvp.Key, kvp.Value
      kvp.getValue ().sortValuesBySortOrder ();
    }
  }

  private static class AccTyeComparator implements Comparator<Map.Entry<String, AccType>>
  {
    @Override
    public int compare (Map.Entry<String, AccType> lhs, Map.Entry<String, AccType> rhs) {
      return Integer.compare (lhs.getValue ().getSortOrder (), rhs.getValue ().getSortOrder ());
    }

  }
}
