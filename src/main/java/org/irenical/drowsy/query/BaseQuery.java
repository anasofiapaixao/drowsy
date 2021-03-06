package org.irenical.drowsy.query;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.List;

public class BaseQuery implements Query {

  private TYPE type;

  private List<Object> parameters;

  private String query;

  public void setParameters(List<Object> parameters) {
    this.parameters = parameters;
  }

  @Override
  public List<Object> getParameters() {
    return parameters;
  }

  public void setQuery(String query) {
    this.query = query;
  }

  @Override
  public String getQuery() {
    return query;
  }

  public void setType(TYPE type) {
    this.type = type;
  }

  @Override
  public TYPE getType() {
    return type;
  }

  @Override
  public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
    PreparedStatement ps;
    if (TYPE.CALL.equals(type)) {
      ps = connection.prepareCall(query);
    } else if (TYPE.INSERT.equals(type)) {
      ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
    } else {
      ps = connection.prepareStatement(query);
    }
    setParameters(ps, parameters, 1);
    return ps;
  }

  @SuppressWarnings("unchecked")
  private int setParameters(PreparedStatement ps, Collection<Object> parameters, int current)
      throws SQLException {
    if (parameters != null) {
      for (Object param : parameters) {
        if (param instanceof Collection) {
          current = setParameters(ps, (Collection<Object>) param, current);
        } else {
          setInputParameter(ps, current++, param);
        }
      }
    }
    return current;
  }

  private void setInputParameter(PreparedStatement ps, int idx, Object value) throws SQLException {
    if (value instanceof Timestamp) {
      ps.setTimestamp(idx, (Timestamp) value);
    } else if (value instanceof ZonedDateTime) {
      ZonedDateTime zdt = (ZonedDateTime) value;
      Calendar cal = GregorianCalendar.from(zdt);
      Timestamp t = Timestamp.from(zdt.toInstant());
      ps.setTimestamp(idx, t, cal);
    } else if (value instanceof Time) {
      ps.setTime(idx, (Time) value);
    } else if (value instanceof Date) {
      ps.setDate(idx, (Date) value);
    } else if (value instanceof Enum<?>) {
      ps.setString(idx, value.toString());
    } else if (value instanceof Class<?>) {
      if (ps instanceof CallableStatement) {
        setOutputParameter((CallableStatement) ps, idx, (Class<?>) value);
      } else {
        throw new IllegalArgumentException(
            "Invalid parameter type for non-CallableStatement: " + value.getClass());
      }
      ps.setString(idx, value.toString());
    } else {
      ps.setObject(idx, value);
    }
  }

  private void setOutputParameter(CallableStatement statement, int idx, Class<?> value)
      throws SQLException {
    if (String.class.getName().equals(value.getName())) {
      statement.registerOutParameter(idx, java.sql.Types.VARCHAR);
    } else if (Float.class.getName().equals(value.getName())) {
      statement.registerOutParameter(idx, java.sql.Types.FLOAT);
    } else if (Integer.class.getName().equals(value.getName())) {
      statement.registerOutParameter(idx, java.sql.Types.INTEGER);
    } else if (Timestamp.class.getName().equals(value.getName())) {
      statement.registerOutParameter(idx, java.sql.Types.TIMESTAMP);
    } else if (Boolean.class.getName().equals(value.getName())) {
      statement.registerOutParameter(idx, java.sql.Types.BOOLEAN);
    } else {
      statement.registerOutParameter(idx, java.sql.Types.JAVA_OBJECT);
    }
  }

}
