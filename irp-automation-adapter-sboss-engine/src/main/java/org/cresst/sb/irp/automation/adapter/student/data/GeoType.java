/*************************************************************************
 * Educational Online Test Delivery System Copyright (c) 2014 American
 * Institutes for Research
 * 
 * Distributed under the AIR Open Source License, Version 1.0 See accompanying
 * file AIR-License-1_0.txt or at
 * https://bitbucket.org/sbacoss/eotds/wiki/AIR_Open_Source_License
 *************************************************************************/

package org.cresst.sb.irp.automation.adapter.student.data;

/**
 * @author mskhan
 * 
 */
// / <summary>
// / The type of geo database server.
// / </summary>
public enum GeoType
{
  // / <summary>
  // / This is a single server responsible for all student interactions.
  // / </summary>
  Monolith,

  // / <summary>
  // / This is the first server that the student goes to login and find out what
  // the satellite is.
  // / </summary>
  Login,

  // / <summary>
  // / This is where the student takes the test.
  // / </summary>
  Satellite,

  // / <summary>
  // / This is used to figure out what the shard is.
  // / </summary>
  Hub,

  // / <summary>
  // / This is the final resting place for the students test information.
  // / </summary>
  Shard,

  // / <summary>
  // / This is used for logging errors.
  // / </summary>
  Logging
}
