package org.irenical.drowsy.query.builder.sql;

import java.util.Arrays;

import org.irenical.drowsy.query.Query.TYPE;

public class InsertBuilder extends BaseQueryBuilder<InsertBuilder> {

  protected InsertBuilder() {
    super(TYPE.INSERT);
  }

  public static InsertBuilder create(String sql) {
    InsertBuilder result = new InsertBuilder();
    result.literal(sql);
    return result;
  }

  public static InsertBuilder into(String tableName) {
    return create("insert into " + tableName);
  }

  public InsertBuilder columns(Object... values) {
    if (values != null && values.length > 0) {
      literals(Arrays.asList(values), "(", ")", ",");
    }
    return this;
  }

  public InsertBuilder values(Object... values) {
    if (values != null && values.length > 0) {
      params(Arrays.asList(values), " values(", ")", ",");
    }
    return this;
  }

}
