/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the  "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * $Id$
 */

/* This class holds a parameter definition for a JDBC PreparedStatement or CallableStatement. */

package de.lyca.xalan.lib.sql;

import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

public class QueryParameter {
  private int m_type;
  private String m_name;
  private String m_value;
  private boolean m_output;
  private String m_typeName;
  private static Map<String, Integer> m_Typetable = null;

  public QueryParameter() {
    m_type = -1;
    m_name = null;
    m_value = null;
    m_output = false;
    m_typeName = null;
  }

  /**
   * @param v
   *          The parameter value.
   * @param t
   *          The type of the parameter.
   */
  public QueryParameter(String v, String t) {
    m_name = null;
    m_value = v;
    m_output = false;
    setTypeName(t);
  }

  public QueryParameter(String name, String value, String type, boolean out_flag) {
    m_name = name;
    m_value = value;
    m_output = out_flag;
    setTypeName(type);
  }

  /**
   *
   */
  public String getValue() {
    return m_value;
  }

  /**
   * @param newValue
   * 
   */
  public void setValue(String newValue) {
    m_value = newValue;
  }

  /**
   * Used to set the parameter type when the type information is provided in the
   * query.
   * 
   * @param newType
   *          The parameter type.
   * 
   */
  public void setTypeName(String newType) {
    m_type = map_type(newType);
    m_typeName = newType;
  }

  /**
   *
   */
  public String getTypeName() {
    return m_typeName;
  }

  /**
   *
   */
  public int getType() {
    return m_type;
  }

  /**
   *
   */
  public String getName() {
    return m_name;
  }

  /**
   * Set Name, this should really be covered in the constructor but the
   * QueryParser has a State issue where the name is discoverd after the
   * Parameter object needs to be created
   */
  public void setName(String n) {
    m_name = n;
  }

  /**
  *
  */
  public boolean isOutput() {
    return m_output;
  }

  /**
   * Set Name, this should really be covered in the constructor but the
   * QueryParser has a State issue where the name is discoverd after the
   * Parameter object needs to be created
   */
  public void setIsOutput(boolean flag) {
    m_output = flag;
  }

  private static int map_type(String typename) {
    if (m_Typetable == null) {
      // Load up the type mapping table.
      m_Typetable = new HashMap<>();
      m_Typetable.put("BIGINT", Types.BIGINT);
      m_Typetable.put("BINARY", Types.BINARY);
      m_Typetable.put("BIT", Types.BIT);
      m_Typetable.put("CHAR", Types.CHAR);
      m_Typetable.put("DATE", Types.DATE);
      m_Typetable.put("DECIMAL", Types.DECIMAL);
      m_Typetable.put("DOUBLE", Types.DOUBLE);
      m_Typetable.put("FLOAT", Types.FLOAT);
      m_Typetable.put("INTEGER", Types.INTEGER);
      m_Typetable.put("LONGVARBINARY", Types.LONGVARBINARY);
      m_Typetable.put("LONGVARCHAR", Types.LONGVARCHAR);
      m_Typetable.put("NULL", Types.NULL);
      m_Typetable.put("NUMERIC", Types.NUMERIC);
      m_Typetable.put("OTHER", Types.OTHER);
      m_Typetable.put("REAL", Types.REAL);
      m_Typetable.put("SMALLINT", Types.SMALLINT);
      m_Typetable.put("TIME", Types.TIME);
      m_Typetable.put("TIMESTAMP", Types.TIMESTAMP);
      m_Typetable.put("TINYINT", Types.TINYINT);
      m_Typetable.put("VARBINARY", Types.VARBINARY);
      m_Typetable.put("VARCHAR", Types.VARCHAR);

      // Aliases from Xalan SQL extension.
      m_Typetable.put("STRING", Types.VARCHAR);
      m_Typetable.put("BIGDECIMAL", Types.NUMERIC);
      m_Typetable.put("BOOLEAN", Types.BIT);
      m_Typetable.put("BYTES", Types.LONGVARBINARY);
      m_Typetable.put("LONG", Types.BIGINT);
      m_Typetable.put("SHORT", Types.SMALLINT);
    }

    final Integer type = m_Typetable.get(typename.toUpperCase());
    int rtype;
    if (type == null) {
      rtype = Types.OTHER;
    } else {
      rtype = type.intValue();
    }

    return rtype;
  }

  /**
   * This code was in the XConnection, it is included for reference but it
   * should not be used.
   * 
   * @TODO Remove this code as soon as it is determined that its Use Case is
   *       resolved elsewhere.
   */
  /**
   * Set the parameter for a Prepared Statement
   * 
   * @param pos
   * @param stmt
   * @param p
   * 
   * @throws SQLException
   */
  /*
   * private void setParameter( int pos, PreparedStatement stmt, QueryParameter
   * p )throws SQLException { String type = p.getType(); if
   * (type.equalsIgnoreCase("string")) { stmt.setString(pos, p.getValue()); }
   * 
   * if (type.equalsIgnoreCase("bigdecimal")) { stmt.setBigDecimal(pos, new
   * BigDecimal(p.getValue())); }
   * 
   * if (type.equalsIgnoreCase("boolean")) { Integer i = new Integer(
   * p.getValue() ); boolean b = ((i.intValue() != 0) ? false : true);
   * stmt.setBoolean(pos, b); }
   * 
   * if (type.equalsIgnoreCase("bytes")) { stmt.setBytes(pos,
   * p.getValue().getBytes()); }
   * 
   * if (type.equalsIgnoreCase("date")) { stmt.setDate(pos,
   * Date.valueOf(p.getValue())); }
   * 
   * if (type.equalsIgnoreCase("double")) { Double d = new Double(p.getValue());
   * stmt.setDouble(pos, d.doubleValue() ); }
   * 
   * if (type.equalsIgnoreCase("float")) { Float f = new Float(p.getValue());
   * stmt.setFloat(pos, f.floatValue()); }
   * 
   * if (type.equalsIgnoreCase("long")) { Long l = new Long(p.getValue());
   * stmt.setLong(pos, l.longValue()); }
   * 
   * if (type.equalsIgnoreCase("short")) { Short s = new Short(p.getValue());
   * stmt.setShort(pos, s.shortValue()); }
   * 
   * if (type.equalsIgnoreCase("time")) { stmt.setTime(pos,
   * Time.valueOf(p.getValue()) ); }
   * 
   * if (type.equalsIgnoreCase("timestamp")) {
   * 
   * stmt.setTimestamp(pos, Timestamp.valueOf(p.getValue()) ); }
   * 
   * }
   */

}
